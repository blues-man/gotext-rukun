package org.gotext;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gotext.logic.Service;
import org.gotext.logic.goTextApplication;

public class AutoCompleteMessageActivity extends Activity {
	
	ArrayList<Map<String, String>> mPeopleList;
	TextView editEntryView;
	SimpleAdapter mAdapter;
	AutoCompleteTextView mTxtPhoneNo;
	ProgressDialog progress;
	goTextApplication ga = null;
	Service s = null;
	TextWatcher mTextEditorWatcher;
	int maxChar = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.message);
	    ga = (goTextApplication)getApplication();
	    Bundle extras = getIntent().getExtras(); 
	    if (extras!=null){
	    	String uid = extras.getString("Service");
	    	s = ga.getService(uid);
		    maxChar = s.getMaxChar();
		    if (maxChar > 0){
			    editEntryView = (TextView)findViewById(R.id.editText2);
			    InputFilter[] FilterArray = new InputFilter[1];
			    FilterArray[0] = new InputFilter.LengthFilter(maxChar);
			    editEntryView.setFilters(FilterArray);
			    setTitle("0/"+maxChar);
		    }
	    }
	    

	    mPeopleList = new ArrayList<Map<String, String>>();
	    PopulatePeopleList();
	    mTxtPhoneNo = (AutoCompleteTextView) findViewById(R.id.phoneList);
	    mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.custcontview,
	            new String[] { "Name", "Phone", "Type" }, new int[] {
	                    R.id.ccontName, R.id.ccontNo, R.id.ccontType });
	    mTxtPhoneNo.setAdapter(mAdapter);
	    mTxtPhoneNo.setOnItemClickListener(new OnItemClickListener() {

	        @Override
	        public void onItemClick(AdapterView<?> av, View arg1, int index,
	                long arg3) {
	            Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

	            String name  = map.get("Name");
	            String number = map.get("Phone");
	            mTxtPhoneNo.setText(""+name+"<"+number+">");

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

	}

	public void PopulatePeopleList() {
	    mPeopleList.clear();
	    Cursor people = getContentResolver().query(
	            ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    while (people.moveToNext()) {
	        String contactName = people.getString(people
	                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	        String contactId = people.getString(people
	                .getColumnIndex(ContactsContract.Contacts._ID));
	        String hasPhone = people
	                .getString(people
	                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

	        if ((Integer.parseInt(hasPhone) > 0)){
	            // You know have the number so now query it like this
	            Cursor phones = getContentResolver().query(
	            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	            null,
	            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
	            null, null);
	            while (phones.moveToNext()){
	                //store numbers and display a dialog letting the user select which.
	                String phoneNumber = phones.getString(
	                phones.getColumnIndex(
	                ContactsContract.CommonDataKinds.Phone.NUMBER));
	                String numberType = phones.getString(phones.getColumnIndex(
	                ContactsContract.CommonDataKinds.Phone.TYPE));
	                Map<String, String> NamePhoneType = new HashMap<String, String>();
	                NamePhoneType.put("Name", contactName);
	                NamePhoneType.put("Phone", phoneNumber);
	                if(numberType.equals("0"))
	                    NamePhoneType.put("Type", "Work");
	                    else
	                    if(numberType.equals("1"))
	                    NamePhoneType.put("Type", "Home");
	                    else if(numberType.equals("2"))
	                    NamePhoneType.put("Type",  "Mobile");
	                    else
	                    NamePhoneType.put("Type", "Other");
	                    //Then add this map to the list.
	                    mPeopleList.add(NamePhoneType);
	            }
	            phones.close();
	        }
	    }
	    people.close();
	    startManagingCursor(people);
	}

	public void onItemClick(AdapterView<?> av, View v, int index, long arg){
	    Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
	    Iterator<String> myVeryOwnIterator = map.keySet().iterator();
	    while(myVeryOwnIterator.hasNext()) {
	        String key=(String)myVeryOwnIterator.next();
	        String value=(String)map.get(key);
	        mTxtPhoneNo.setText(value);
	    }
	}
	
	 public void onClick(View view) {
	   	 switch (view.getId()) {
	   	 case R.id.sendSMS:
	   		progress = ProgressDialog.show(AutoCompleteMessageActivity.this,    
	                "Please wait...", "Sending SMS ...", true);
	   		new SendSMS().execute();
	            break;
	   	 }
	   }
	
	 class SendSMS extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		  protected void onPostExecute(String s) {
				progress.dismiss();
		  }
		 
	 }

}
