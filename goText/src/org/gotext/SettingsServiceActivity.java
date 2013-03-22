package org.gotext;

import java.util.ArrayList;

import org.gotext.logic.Service;
import org.gotext.logic.Utils;
import org.gotext.logic.goTextApplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsServiceActivity extends ListActivity {
	
	 private ProgressDialog progress = null; 
	 private ArrayList<Service> services = null;
	 goTextApplication ga = null;
	 private ServiceAdapter adapter;
	 private Runnable viewServices;

	    
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.select_service);
	        ga = (goTextApplication)getApplication();
	        
	        
	        services = new ArrayList<Service>();
	        this.adapter = new ServiceAdapter(this, R.layout.row, services);
	        setListAdapter(this.adapter);
	        
	        viewServices = new Runnable(){
	            @Override
	            public void run() {
	            	ga.buildAll();
	            	services = ga.getServices();
	            	runOnUiThread(returnRes);
	            }
	        };
	        Thread thread =  new Thread(null, viewServices, "MagentoBackground");
	        thread.start();
	        
	        progress = ProgressDialog.show(SettingsServiceActivity.this,    
	                "Please wait...", "Retrieving data ...", true);
	        
	   
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
	    
	    
	  /*  @Override
	    public void onBackPressed() {
	    	// Impedisce il back
	    }*/
	    
	    
	    
	    private class ServiceAdapter extends ArrayAdapter<Service> {

	        private ArrayList<Service> items;

	        public ServiceAdapter(Context context, int textViewResourceId, ArrayList<Service> items) {
	                super(context, textViewResourceId, items);
	                this.items = items;
	        }

	        @Override
	        public View getView(int position, View convertView, ViewGroup parent) {
	                View v = convertView;
	                if (v == null) {
	                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                    v = vi.inflate(R.layout.row, null);
	                }
	                Service s = items.get(position);
	                if (s != null) {
	                        TextView tt = (TextView) v.findViewById(R.id.toptext);
	                        TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	                        if (tt != null) {
	                              tt.setText("Name: "+s.getName());                            }
	                        if(bt != null){
	                        	  boolean misconfigured = s.getCredentials().size() > 0 && s.getUsername()==null;
	                              String dText = "SMS: "+s.getSms();
	                              if (misconfigured){
	                            	  dText+= " CONFIGURATION MISSING!";
		                        	  bt.setTextColor(Color.RED);
	                              }
	                        	  bt.setText(dText);

	                        }
	                        if (s.getIcon().length > 0){
	                        	ImageView iv = (ImageView) v.findViewById(R.id.icon); 
		                        byte[] byteArray = s.getIcon();
								Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray.length);
								if (bitmap.getWidth() >= 32 || bitmap.getHeight() >= 32){
									Bitmap bitmapResize = Utils.getResizedBitmap(bitmap, 32, 32);
									iv.setImageBitmap(bitmapResize);
								}
	                        }
	                        
	                }
	                return v;
	        }
	}


	    @Override
	    protected void onListItemClick(ListView l, View v, int position, long id) {
	     // TODO Auto-generated method stub
	     //super.onListItemClick(l, v, position, id);
	     Service s = (Service)l.getItemAtPosition(position);
	     Log.i("onListItemClick", "id= "+s.getUid());
	     Intent intention = null;
		     intention = new Intent(getApplicationContext(), ServiceSettingsActivity.class);
		     intention.putExtra("Service", s.getUid());
		     startActivityForResult(intention, 0);
	    		   
	  
	    }
		
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			super.onCreateOptionsMenu(menu);
			MenuInflater Menu = getMenuInflater();
			Menu.inflate(R.menu.menu_services, menu);
			return true;
		}
		

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {			
			case R.id.menu_download_services:
				

	
				 Intent intention = null;
			     intention = new Intent(getApplicationContext(), DownloadServicesActivity.class);
			     startActivityForResult(intention, 0);
				
				break;
			}
			return super.onOptionsItemSelected(item);

		
		}
		
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if(resultCode == 0) {
		        finish();
		    }
		}
		
		
	

		    
		
		

		
	
}
