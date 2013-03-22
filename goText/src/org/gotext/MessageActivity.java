package org.gotext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gotext.AutoCompleteMessageActivity.SendSMS;
import org.gotext.logic.Job;
import org.gotext.logic.Service;
import org.gotext.logic.Step;
import org.gotext.logic.StepExecutionError;
import org.gotext.logic.goTextApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MessageActivity extends Activity {
	
	protected static final int PICK_CONTACT_REQUEST = 0;
	TextView editEntryView;
	SimpleAdapter mAdapter;
	EditText mTxtPhoneNo;
	ProgressDialog progress;
	goTextApplication ga = null;
	Service s = null;
	TextWatcher mTextEditorWatcher;
	int maxChar = 0;
	AlertDialog alertDialog = null;
	String lang = "";
	Message sms = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.message);
	    ga = (goTextApplication)getApplication();
	    lang = ga.getSettingsPref().getString("lang", lang);
	    Bundle extras = getIntent().getExtras(); 
	    if (extras!=null){
	    	String uid = extras.getString("Service");
	    	s = ga.getService(uid);
		    maxChar = s.getMaxChar();
		    Log.i("MessageActivity", "MAXCHAR "+maxChar);
		    if (maxChar > 0){
			    editEntryView = (TextView)findViewById(R.id.editText2);
			    InputFilter[] FilterArray = new InputFilter[1];
			    FilterArray[0] = new InputFilter.LengthFilter(maxChar);
			    editEntryView.setFilters(FilterArray);
			    setTitle("0/"+maxChar);
		    }
	    }
	    

	    mTxtPhoneNo = (EditText) findViewById(R.id.phoneList);
	    mTxtPhoneNo.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	       	 /*Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
	 		 pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
	 		 startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);*/
	 		 
	 		try {
	 	        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
	 	        //intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
	 	        startActivityForResult(intent, PICK_CONTACT_REQUEST);
	 	    } catch (Exception e) {
	 	            e.printStackTrace();
	 	      }
	        }
	    });
	    
	    mTextEditorWatcher = new TextWatcher() {

	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	        };

	        public void onTextChanged(CharSequence sc, int start, int before, int count) {
	           //This sets a textview to the current length
	        	setTitle(sc.length()+"/"+maxChar);
	           
	        }

	        //public void afterTextChanged1(Editable s) {
	        //}

	        public void afterTextChanged(Editable arg0) {
	            // TODO Auto-generated method stub

	        }
	      };
	      editEntryView.addTextChangedListener(mTextEditorWatcher);
	      
	      alertDialog = new AlertDialog.Builder(
                  MessageActivity.this).create();

	}
	
	
	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    if (resultCode == RESULT_OK) {  
	        switch (requestCode) {  
	        case PICK_CONTACT_REQUEST:  
	        	
	        	
	        	   Cursor cursor = null;  
	               String phoneNumber = "";
	               List<String> allNumbers = new ArrayList<String>();
	               int phoneIdx = 0;
	               try {  
	                   Uri result = data.getData();  
	                   String id = result.getLastPathSegment();  
	                   cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] { id }, null);  
	                   phoneIdx = cursor.getColumnIndex(Phone.DATA);
	                   if (cursor.moveToFirst()) {
	                       while (cursor.isAfterLast() == false) {
	                           phoneNumber = cursor.getString(phoneIdx);
	                           allNumbers.add(phoneNumber);
	                           cursor.moveToNext();
	                       }
	                   } else {
	                       //no results actions
	                   }  
	               } catch (Exception e) {  
	                  //error actions
	               } finally {  
	                   if (cursor != null) {  
	                       cursor.close();
	                   }
	                   
	                   final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
	                   AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
	                   builder.setTitle("Choose a number");
	                   builder.setItems(items, new DialogInterface.OnClickListener() {
	                       public void onClick(DialogInterface dialog, int item) {
	                           String selectedNumber = items[item].toString();
	                           selectedNumber = selectedNumber.replace("-", "");
	                           
	    	                   if(s.hasToCutPrefix()){
	    	                	   if (s.getInt_prefixes()!=null && s.getInt_prefixes().size()>0){
	    	                		   ArrayList<String> pr = s.getInt_prefixes();
	    	                		   for (String s: pr){
	    	                			   if (selectedNumber.startsWith(s)){
	    	                				   selectedNumber = selectedNumber.replace(s, "");
	    	                			   }
	    	                		   }
	    	                		   
	    	                	   } 
	    	                   } else {
    	                		   String prefix = ga.getSettingsPref().getString("int_prefix", null);
    	                		   boolean hasPrefix = true;
    	                		   if (s.getInt_prefixes()!=null && s.getInt_prefixes().size()>0){
	    	                		   ArrayList<String> pr = s.getInt_prefixes();
	    	                		   for (int i=0; i< pr.size() && hasPrefix; i++){
	    	                			   String str = pr.get(i);
	    	                			   if (!str.equals(""))
	    	                				   hasPrefix = selectedNumber.startsWith(str);
	    	                			   else
	    	                				   hasPrefix = selectedNumber.startsWith("+");
	    	                		   }
	    	                		   
	    	                		   if (!hasPrefix && prefix!=null){
	    	                			   selectedNumber = prefix + selectedNumber;   
	    	                		   }
	    	                	}
    	                		   
    	                		   
    	                	   }
	    	                   
	    	                   mTxtPhoneNo.setText(selectedNumber);

	                       }
	                   });
	                   AlertDialog alert = builder.create();
	                   if(allNumbers.size() > 1) {
	                       alert.show();
	                   } else {
	                       String selectedNumber = phoneNumber.toString();
	                       selectedNumber = selectedNumber.replace("-", "");
    	                   if(s.hasToCutPrefix()){
    	                	   if (s.getInt_prefixes()!=null && s.getInt_prefixes().size()>0){
    	                		   ArrayList<String> pr = s.getInt_prefixes();
    	                		   for (String s: pr){
    	                			   if (selectedNumber.startsWith(s)){
    	                				   selectedNumber = selectedNumber.replace(s, "");
    	                			   }
    	                		   }
    	                		   
    	                	   }
    	                   }
    	                   mTxtPhoneNo.setText(selectedNumber);

	                   }

	                   if (phoneNumber.length() == 0) {  
	                       //no numbers found actions  
	                   }  
	                   
	                   
	                   
	                   
	                   
	               
	            
                }
                  /*Cursor cursor = null;  
	            String phone = "";  
	            try {  
	                Uri result = data.getData();  
	                Log.i("MessageActivity", "Got a contact result: "  
	                        + result.toString());  
	                // get the contact id from the Uri  
	                String id = result.getLastPathSegment();  
	                // query for everything email  
	                cursor = getContentResolver().query(Phone.CONTENT_URI,  
	                        null, Phone.CONTACT_ID + "=?", new String[] { id },  
	                        null);  
	                int emailIdx = cursor.getColumnIndex(Phone.DATA);  
	                // let's just get the first email  
	                if (cursor.moveToFirst()) {  
	                    phone = cursor.getString(emailIdx);  
	                    Log.i("MessageActivity", "Got phone: " + phone);  
	                } else {  
	                    Log.i("MessageActivity", "No results");  
	                }  
	            } catch (Exception e) {  
	                Log.e("MessageActivity", "Failed to get email data", e);  
	            } finally {  
	                if (cursor != null) {  
	                    cursor.close();  
	                }  
	                mTxtPhoneNo.setText(phone);  
	                if (phone.length() == 0) {  
	                    Toast.makeText(this, "No email found for contact.",  
	                            Toast.LENGTH_LONG).show();  
	                }  
	            }  */
	            break;  
	        }  
	    } else {  
	        Log.i("Mammata", "Warning: activity result not ok");  
	    }  
	}
	
	
	 public void onClick(View view) {
	   	 switch (view.getId()) {
	   	 case R.id.sendSMS:
	   		 
	   		 String dest = mTxtPhoneNo.getText().toString().trim();
	   		 String msg = editEntryView.getText().toString();
	   		 if (dest.equals("") || msg.equals("")){
	   			Toast.makeText(this, getString(R.string.empty_fields), Toast.LENGTH_LONG).show();	   		
	   		} else {
	   			progress = new ProgressDialog(MessageActivity.this);
				progress.setCancelable(true);
				progress.setMessage(getString(R.string.send_sms));
				progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progress.setProgress(0);
				progress.setMax(100);
				progress.show();
	 
		   		sms = new Message(dest, msg);
		   		new SendSMS().execute();
	   		}
	            break;
	   	 }
	   }
	
	 class SendSMS extends AsyncTask<Void, Integer, Void> {

		int percentage = 0;
		int mult = 0;
		String message = "";
		boolean result = true;
		 
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Job job = s.getJob();
			job.setMessage(sms);
			job.pre();
			List<Step> steps = job.getSteps();
			
			if (steps.size() > 0)
				mult = 100 / steps.size();
			
			if (mult == 100){
				publishProgress(50);
			}
			
			for (int i =0; i< steps.size() && result; i++){	
				Step step = steps.get(i);
				try {
					step.init();
					result = step.consume(job);
					message = step.getMessage();
					Log.i("JOB", "Step "+step.getId()+" "+result+" message"+message);
					//if (message!= null && message.equals("LOGIN_OK")){
						//progress.setMessage("Login ok");
					//}
					
				} catch(StepExecutionError see){
					see.printStackTrace();
					message = see.getMessage();
				}
				step.finish();
				if (result){
					percentage += mult;
					publishProgress(percentage);
				} else {
					job.post();
					return null;
				}
			
				
			}
			
			job.post();
			return null;
		
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
	         progress.setProgress(values[0]);
	     }
		
		@SuppressWarnings("deprecation")
		@Override
		  protected void onPostExecute(Void results) {
				progress.dismiss();
				
				
				if (result){
					Log.e("MESSAGE", message);	
					alertDialog.setTitle(getString(R.string.sent_sms));
					alertDialog.setIcon(R.drawable.apply);	
					alertDialog.setMessage(getString(R.string.press_ok));
						
					
				} else {
					alertDialog.setTitle(getString(R.string.error_sms));
					alertDialog.setIcon(R.drawable.error);
					if (message.equals("$L_ERROR_NOT_SENT")){
						alertDialog.setMessage(getString(R.string.error_sms_sent));
					} else if (s.getLanguageString(lang, message)!=null){
						alertDialog.setMessage(s.getLanguageString(lang, message));
					} else {
						alertDialog.setMessage(getString(R.string.error_sms));
					}
				}
				
				

				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	
	            	if (result){
		            	if (s.getSms()>0){
		            		s.setSms(s.getSms()-1);
							ga.updateSms(s);
		            	}
						setResult(0);
						finish();
	            	} else {
	            		alertDialog.dismiss();
	            	}
	            	
	            	}
				});
				alertDialog.show();
		  }
		 
	 }
	 
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuInflater Menu = getMenuInflater();
			Menu.inflate(R.menu.menu_message, menu);
			return true;
		}
		
	 
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			
			switch (item.getItemId()) {			
			case R.id.menu_messages:
				String text = editEntryView.getText().toString();
				if (text.equals("%tirchio")){
					Calendar c = Calendar.getInstance(); 
					String month = (c.get(Calendar.MONTH)+1)+"";
					if (month.length()==1)
						month = "0"+month;
					String fuzzy = "My Wind. Ti ho cercato alle "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE) 
								+  " del "+c.get(Calendar.DAY_OF_MONTH)+"/"+month+".\nVai sull'Area Clienti di wind.it"
								+  " e scopri quanto e' semplice e veloce gestire il tuo numero di telefono.";
					
					editEntryView.setText(fuzzy);
				}
				
			}
			
			return super.onOptionsItemSelected(item);
		}

}
