package org.gotext;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;

import org.gotext.logic.Service;
import org.gotext.logic.ServiceDBHandler;
import org.gotext.logic.goTextApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class DownloadServicesActivity extends Activity {
	
	 ListView list = null;
	 Button but = null;
	 ArrayList<String> services = null;
	 ProgressDialog progress = null; 
	 Runnable viewServices;
	 
	 AlertDialog alertDialog = null;
	 
	 //ServiceDataSource ds = null;
	ServiceDBHandler dbh = null;
	goTextApplication ga = null;

	boolean firstDownload = true;
	 
	 

	    ItemsAdapter adapter;
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        setContentView(R.layout.service_list);
	        
	        list = (ListView)findViewById(R.id.list);
	        but = (Button)findViewById(R.id.but);
	        
	        services = new ArrayList<String>();
	        //ds = new ServiceDataSource(this);
	        //ds.open();
	        dbh = new ServiceDBHandler(this);
	        ga = (goTextApplication)getApplication();
	        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        adapter = new ItemsAdapter(this, android.R.layout.simple_list_item_checked, services);
	        list.setAdapter(adapter);
	        
	        viewServices = new Runnable(){
	            @Override
	            public void run() {
	                getServices();
	            }

	        };
	        Thread thread =  new Thread(null, viewServices, "MagentoBackground");
	        thread.start();
	        progress = ProgressDialog.show(DownloadServicesActivity.this,    
	              getString(R.string.wait), getString(R.string.retrieving), true);
	        
	        list.setOnItemClickListener(new OnItemClickListener() {

			   @Override
			   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
			     long arg3) {
			    // TODO Auto-generated method stub
			    CheckedTextView tv = (CheckedTextView)arg1;
			    toggle(tv);
			    Log.i("TOGGLE", "Pos ="+arg2+" "+tv.isChecked());
			    
			    

			   }
			         
			  });
			        
			but.setOnClickListener(new OnClickListener() {
			   
			   @Override
			   public void onClick(View v) {
			    // TODO Auto-generated method stub
			   /* Log.i("listview", ""+list.getChildCount());
			    ArrayList<String> xml = new ArrayList<String>();
			    for(int i = 0;i<list.getChildCount();i++){
			    	View view = list.getChildAt(i);
			    	CheckedTextView cv =(CheckedTextView)view.findViewById(R.id.checkList);
			    	if(cv.isChecked()) {
			    		Log.i("listview", cv.getText().toString());
			    		xml.add(cv.getText().toString());
			    	}
			    }*/
			    
			    
			    int len = list.getCount();
			    ArrayList<String> xml = new ArrayList<String>();
			    SparseBooleanArray checked = list.getCheckedItemPositions();
			    for (int i = 0; i < len; i++)
			    	if (checked.get(i)) {
			    		//Log.i("listview", cv.getText().toString());
			    		xml.add(services.get(i));
			    	}
			    
			    
			    
			    if (xml.size()>0){
			    	 progress = ProgressDialog.show(DownloadServicesActivity.this,    
				              getString(R.string.wait), getString(R.string.preparing_download), true);
			    	 String[] xml_str = new String[xml.size()];
			    	 xml_str = xml.toArray(xml_str);
			    	 new DownloadService().execute(xml_str);
			    	 
			    }
			    
			    
			    
			   }
			   
			  });
			
			
			
			alertDialog = new AlertDialog.Builder(
                    DownloadServicesActivity.this).create();
		

	    }
	    
	    
	    private Runnable returnRes = new Runnable() {

	        @Override
	        public void run() {
	            if(services != null && services.size() > 0){
	                adapter.notifyDataSetChanged();
	                for(int i=0;i<services.size();i++)
	                adapter.add(services.get(i));
	            }
	            progress.dismiss();
	            adapter.notifyDataSetChanged();
	        }
	    };
	    
	    
	   // @Override
	   // public void onBackPressed() {
	    	// Impedisce il back
	    //}
	    
	    private void getServices(){
	    	
	    	HttpURLConnection http = null;
	    	URL url = null;
	    	
	        try{
	              services = new ArrayList<String>();
	              url = new URL("http://www.gotext.org/rukun/engine.php");
	              http = (HttpURLConnection) url.openConnection();
	              
	              if (http.getResponseCode() == 200){
	     	       	 InputStream in = http.getInputStream();
	     	    	
	    	         InputStreamReader isw = new InputStreamReader(in);
	    	
	    	         int data = isw.read();
	    	         String jsonString = "";
	    	         while (data != -1) {
	    	             char current = (char) data;
	    	             data = isw.read();
	    	             jsonString+=current;
	    	         }
	    	         
	    	         JSONArray jsonArray = new JSONArray(jsonString);
	    	         Log.i(DownloadServicesActivity.class.getName(),
	    	             "Number of entries " + jsonArray.length());
	    	         
	    	         for (int i = 0; i < jsonArray.length(); i++) {
	    	        	 //File file = getBaseContext().getFileStreamPath(FILE_NAME);

	    	           String uid = jsonArray.get(i).toString();
	    	           if( ga.getService(uid) == null)
	    	        	   services.add(uid);
	    	           else 
	    	        	   firstDownload = false;
	    	           Log.i(DownloadServicesActivity.class.getName(), jsonArray.get(i).toString());
	    	         }
	    	         
	    	         
	              }
	              
	            } catch (Exception e) { 
	              Log.e("BACKGROUND_PROC", e.getMessage());
	            }
	            runOnUiThread(returnRes);
	    	
	    }
	    
	    
	 private class ItemsAdapter extends  ArrayAdapter<String> {
	  ArrayList<String> items;

	  public ItemsAdapter(Context context, int id, ArrayList<String> items) {
		  super(context, id, items);
		  this.items = items;
	  }

	  // @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	   
	   View v = convertView;
	   if (v == null) {
	    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    v = vi.inflate(R.layout.services, null);
	   } 
	   CheckedTextView post = (CheckedTextView) v.findViewById(R.id.checkList);
	   post.setText(items.get(position));
	   
	   
	   Log.i("ADAPTER","Inserisce "+items.get(position)+ " pos=["+position+"] is checked?"+post.isChecked());
	   
	   return v;
	  }

	  public int getCount() {
	   return items.size();
	  }

	  public String getItem(int position) {
	   return items.get(position);
	  }

	  public long getItemId(int position) {
	   return position;
	  }


	 }

	 public void toggle(CheckedTextView v)
	 {
	         if (v.isChecked())
	         {
	             v.setChecked(false);
	         }
	         else
	         {
	             v.setChecked(true);
	         }
	 }
	 
	 
	 class DownloadService extends AsyncTask<String, String, String> {

		 @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	        }
		 
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String[] services = arg0;
			URLConnection http = null;
			URL url = null;
			
			try {
			
				for(int i=0;i<services.length;i++){
					String xml = services[i];
					url = new URL("http://www.gotext.org/rukun/xmlservices/"+xml+".xml");
	        		http =  url.openConnection();
	        		http.connect();
	        		
	        		/* // download the file
	                InputStream input = new BufferedInputStream(url.openStream(), 8192);
	 
	                // Output stream
	    			FileOutputStream output = getApplicationContext().openFileOutput(xml+".xml", MODE_PRIVATE);
	   
	 
	                byte data[] = new byte[1024];
	 
	                int count = 0;
	 
	                while ((count = input.read(data)) != -1) {
	                    // publishing the progress....
	                    // After this onProgressUpdate will be called	 
	                    // writing data to file
	                    output.write(data, 0, count);
	                }
	                // flushing output
	                output.flush();
	 
	                // closing streams
	                output.close();
	                input.close();
	                					
					 
*/
				   BufferedReader bufferReader = new BufferedReader(new InputStreamReader(url.openStream()));
				   String StringBuffer;
				   String xmlBody = "";
				   while ((StringBuffer = bufferReader.readLine()) != null) {
					   	xmlBody += StringBuffer;
				   }
				   bufferReader.close();
				   
		      	   //goTextApplication ga = (goTextApplication)getApplication();
		      	   //ga.saveService(xmlBody);
						        
				
	                
			      	
			      	 //boolean saved = ga.saveServiceFromXml(xml);
	                Service s = new Service();
	                s.setUid(xml);
	                s.setXml(xmlBody);
			      	boolean saved = dbh.addService(s);
			      	//if(saved)
			      		//ga.addService(s);
				    //boolean saved = ga.insertService(xml, xmlBody);  
			      	Log.i("SERVICE_IN_DB",xml+" saved? "+saved);
			        //Log.i("SERVICE_IN_DB", "Services :"+dbh.getServiceCount());

	                Service s1 = dbh.getService(xml);
	                Log.i("SERVICE_IN_DB", s1.getUid());
	                
                    publishProgress((i+1)+"/"+services.length);				
					
				}
			
			} catch (IOException e) {
		          e.printStackTrace();
	        }

			
			return null;
		}
		
		@SuppressWarnings("deprecation")
		@Override
        protected void onPostExecute(String file_url) {
			progress.dismiss();
			
			
			
			

			alertDialog.setTitle(getString(R.string.services_download));
			alertDialog.setMessage(getString(R.string.services_download));
			alertDialog.setIcon(R.drawable.apply);

			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	if (firstDownload){
	            	Intent intention = new Intent(getApplicationContext(), SelectServiceActivity.class);
	            	DownloadServicesActivity.this.startActivityForResult(intention, 0);
            	} else {
            		ga.updateAll();
            		setResult(0);
            		finish();
            		//Intent intention = new Intent(getApplicationContext(), SelectServiceActivity.class);
	            	//DownloadServicesActivity.this.startActivityForResult(intention, 0);
            	}
            	}
			});
			alertDialog.show();
          
        }
		
		  protected void onProgressUpdate(String... progressStr) {
	            progress.setMessage(getString(R.string.progress_retrieved)+" "+progressStr[0]+" " + getString(R.string.progress_services));
	       }
		
		
		 
	 }
	 
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if(resultCode == 0) {
		    	setResult(0);
		        finish();
		    }
		}

}
