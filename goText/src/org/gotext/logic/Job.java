package org.gotext.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.gotext.Message;

import android.util.Log;

public class Job {
	
	private String LOG_TAG ="JOB";
	private List<Step> steps;
	private HttpClient httpClient;	
    private CookieStore cookieStore;    
    private HttpContext localContext;
    private Message msg;
    
    private String[] agents = {
    		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11", // CHROME WIN7
    		"Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 6.0)"														   // IE 7 VISTA
    		
    };
    
    
    public final static String VAR_SMS_REM = "$sms_rem_0";
    public final static String VAR_USERNAME = "$user";
    public final static String VAR_PASSWORD = "$pass";
    public final static String VAR_SENDER = "$sender";
    public final static String VAR_MSG = "$msg";
    public final static String VAR_RCPT_NOCCC = "$rcpt_noccc";
    public final static String VAR_RCPT = "$rcpt";
    
    public final static String VAR_IN_LOGIN_CHECK = "$login_check";
    public final static String VAR_IN_CONFIRM = "$sent_confirm";
    
    
    


    
    
    private HashMap<String,String> reserved;
    
    private Service service;
    private String message;
	
	
	public Job(Service service, List<Step> steps){
		this.service = service;
		this.steps = steps;
		message = "";
		
		
		reserved = new HashMap<String, String>();
		resetReserved();

		
		
	}
	
	
	private void resetReserved(){
		reserved.put(VAR_SMS_REM,"0");
		reserved.put(VAR_USERNAME,"");
		reserved.put(VAR_PASSWORD,"");
		reserved.put(VAR_SENDER,"");
		reserved.put(VAR_MSG,"");
		reserved.put(VAR_RCPT_NOCCC,"");
		reserved.put(VAR_RCPT,"");
		
	}
	
	public List<Step> getSteps(){
		return steps;
	}
	
	
	public String getVar(String key){
		return reserved.get(key);
	}
	
	public void updateVar(String key, String value){
		reserved.put(key, value);
	}
	
	
	public void setMessage(Message msg){
		this.msg = msg;
	}
	
	public Message getMessage(){
		return msg;
	}
	
	public void pre(){
		httpClient = createHttpClient();
		cookieStore  = new BasicCookieStore();
		localContext = new BasicHttpContext();	
	    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	    
	    if (service.getUsername()!=null)
	    	reserved.put(VAR_USERNAME, service.getUsername());
	    if (service.getPassword()!=null)
	    	reserved.put(VAR_PASSWORD, service.getPassword());
	    if (service.getNickname()!=null)
	    	reserved.put(VAR_SENDER, service.getNickname());
	    if (msg.getDest()!=null){
	    	reserved.put(VAR_RCPT_NOCCC, msg.getDest());
	    	reserved.put(VAR_RCPT, msg.getDest());
	    }
	    if (msg.getMsg()!=null)
	    	reserved.put(VAR_MSG, msg.getMsg());
	    	
	    	
	    
	}
	
	
	public void execute(){
		boolean result = true;
		for (int i =0; i< steps.size() && result; i++){	
			Step step = steps.get(i);
			try {
				step.init();
				result = step.consume(this);
				message = step.getMessage();
				Log.i("JOB", "Step "+step.getId()+" "+result+" message"+message);
				
			} catch(StepExecutionError see){
				see.printStackTrace();
				message = see.getMessage();
			}
			step.finish();
		}
	}
	
	public void post(){
		releaseHttpClient();
		cookieStore.clear();
		resetReserved();
	}
	
	
	private final HttpClient createHttpClient(){
	
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, HTTP.DEFAULT_CONTENT_CHARSET);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		Scheme httpScheme = new Scheme("http",PlainSocketFactory.getSocketFactory(),80);
		schemeRegistry.register(httpScheme);
		Scheme httpsScheme = new Scheme("https",SSLSocketFactory.getSocketFactory(),443);
		schemeRegistry.register(httpsScheme);		
		ClientConnectionManager tsConnManager = new ThreadSafeClientConnManager(httpParams,schemeRegistry);
		HttpClient tmpClient = new DefaultHttpClient(tsConnManager,httpParams);
		
		int rnd = new Random().nextInt(agents.length);
		Log.e("JOB", "Session User agent: "+agents[rnd]);
		tmpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, agents[rnd]);

		Log.i(LOG_TAG,"HttpClient Created!");
		return tmpClient;
	}
	
	private final void releaseHttpClient(){
		if(httpClient!=null && httpClient.getConnectionManager()!=null){
			httpClient.getConnectionManager().shutdown();
			Log.i(LOG_TAG,"Releasing Connections");
		}
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public HttpContext getLocalContext() {
		return localContext;
	}
	
	public HttpClient getThreadSafeHttpClient(){
		return httpClient;
	}

}
