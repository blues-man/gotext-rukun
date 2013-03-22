package org.gotext;



import org.gotext.logic.goTextApplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;


public class goTextActivity extends Activity {
	
	SharedPreferences settings;
	boolean firstlaunch = false;
	boolean misconfiguration = false;
	private final static String LOG_TAG = "goTextActivity";
	goTextApplication ga = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        ga = (goTextApplication)getApplication();
		settings = ga.getSettingsPref();
				
		if(settings.getBoolean("firststart", true)) {
			Log.i(LOG_TAG, "CheckConfiguration - First launch");
			firstlaunch = true;
	        setContentView(R.layout.wizard);
		} else {
			 Intent intention = new Intent(getApplicationContext(), SelectServiceActivity.class);
             goTextActivity.this.startActivityForResult(intention, 0);

		}
        
        
      
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(resultCode == 0) {
	        finish();
	    }
	}
    
    public void onClick(View view) {
    	 switch (view.getId()) {
    	 case R.id.button1:
    		 Intent intention = new Intent(getApplicationContext(), SettingsActivity.class);
             startActivityForResult(intention, 0);
             break;
    	 }
    }
    
	@Override
	public void onPause() {
		super.onPause();
		Log.i("goTextActivity","onPause");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.i("goTextActivity","onResume");
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Log.i("goTextActivity","onStart");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.i("goTextActivity","onStop");
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		Log.i("goTextActivity","onRestart");	
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("goTextActivity","onDestroy");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
private class CheckConfiguration extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(LOG_TAG,"CheckConfiguration - onPreExecute");
						
		
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			Log.i("goTextActivity","CheckConfiguration - doInBackround");
						
			goTextApplication ga = (goTextApplication)getApplication();
			settings = ga.getSettingsPref();
						

			if(settings.getBoolean("firstlaunch", true) == true) {
				Log.i(LOG_TAG, "CheckConfiguration - doInBackround - First launch");
				firstlaunch = true;
				return false;
			}
		    
		    if(settings.getString("lang", "").equals("") ) {
		    	Log.i("GASACTIVITY", "One of the required parameters is null");
		    	misconfiguration = true;
		    	return false;
		    }
		   
		    
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.i(LOG_TAG, "CheckConfiguration - onPostExecute - il risultato e' " + result);
			
		
		}
		
	}
    
    



    
}
