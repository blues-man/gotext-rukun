package org.gotext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NetworkActivity extends ListActivity implements Runnable{
	
	ArrayList<Test> items = new ArrayList<Test>();
	Test http, https;
	String _http;
	String _https;
	ProgressDialog progress;
	
	
	class Test {
		String name;
		boolean result;
		
		public Test(String _name, boolean _result){
			name = _name;
			result = _result;
			items.add(this);
			runOnUiThread(new Runnable() {
			     public void run() {

			    	 ((TestAdapter)getListAdapter()).notifyDataSetChanged();
			    }
			});
		}
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
	       setContentView(R.layout.test_connection);

			setListAdapter(new TestAdapter(this,android.R.layout.simple_list_item_checked, items));  
			progress = ProgressDialog.show(NetworkActivity.this,    
		              getString(R.string.wait), getString(R.string.connect_test)+"...", true);
	       new Thread(this).start();
	
	       
	      
	}
	
	public String getServers(){
        String builder = "";
        
        Test test1, test2 = null;
    	HttpURLConnection http = null;
    	URL url = null;
        try {
        	
        	url = new URL("https://gotext.org/rukun/json.php");
       
    	    trustAllHosts();
    		HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
    		https.setHostnameVerifier(DO_NOT_VERIFY);
    		http = https;
    		
    		 
        			
        	if (https.getResponseCode() != 200){
        		
        		url = new URL("http://gotext.org/rukun/json.php");
        		http = (HttpURLConnection) url.openConnection();
        		if (http.getResponseCode() != 200){
           		 	test1 = new Test("HTTP...", false);
           		 	test2 = new Test("HTTPS...", false);

        		}
        	}
        	
        	
	       	 InputStream in = http.getInputStream();
	
	         InputStreamReader isw = new InputStreamReader(in);
	
	         int data = isw.read();
	         String s = "";
	         while (data != -1) {
	             char current = (char) data;
	             data = isw.read();
	             s+=current;
	         }
	         builder = s;
		 	 test1 = new Test("HTTP...", true);
		 	 test2 = new Test("HTTPS...", true);
        } catch (IOException e) {
          e.printStackTrace();
        }

             
        return builder;
      }
    
    
 // always verify the host - dont check for certificate
    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
    	public boolean verify(String hostname, SSLSession session) {
    		return true;
    	}
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
    	// Create a trust manager that does not validate certificate chains
    	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    			return new java.security.cert.X509Certificate[] {};
    		}

    		public void checkClientTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}

    		public void checkServerTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}
    	} };

    	// Install the all-trusting trust manager
    	try {
    		SSLContext sc = SSLContext.getInstance("TLS");
    		sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		HttpsURLConnection
    				.setDefaultSSLSocketFactory(sc.getSocketFactory());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    


	//@Override
	public void run() {
		 String serverJson = getServers();
	    
	        try {
	          JSONObject jsonArray = new JSONObject(serverJson);
	          Log.i(goTextActivity.class.getName(),
	              "Number of entries " + jsonArray.length());
	          JSONObject https = jsonArray.getJSONObject("https");
	          System.out.println(https.getString("host"));
	          System.out.println(https.getString("port"));
	          
	          runOnUiThread(new Runnable() {
				     public void run() {

				    	 findViewById(R.id.send).setEnabled(true);
				    	 progress.dismiss();
				     	}
	          });
				     
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
		
	}
	
    public void onClick(View view) {
   	 switch (view.getId()) {
   	 case R.id.send:
   		 Intent intention = new Intent(getApplicationContext(), DownloadServicesActivity.class);
            startActivityForResult(intention, 0);
            break;
   	 }
   }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(resultCode == 0) {
	        finish();
	    }
	}

	
	class TestAdapter extends ArrayAdapter<NetworkActivity.Test>
	{
		public TestAdapter(NetworkActivity context, int id, ArrayList<NetworkActivity.Test> tests) {
			super(context, id, tests);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView name = (TextView)convertView;
			if(name == null)
				name = new TextView(getContext());

			NetworkActivity.Test test = getItem(position);

			name.setText(test.name);
			name.setCompoundDrawablesWithIntrinsicBounds(0, 0, test.result ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background, 0);
		    //name.setChecked(test.result);
			return name;
		}
	}
	

}
