package org.gotext.logic;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class NetworkStep extends Step {
	
	
	private String LOG_TAG = "NetworkStep";
	
	HashMap<String, String> params;
	private HttpGet get;
	private HttpPost post;
	private String url;
	
	
	private String pageId;

	public enum HTTP {
		GET, POST
	}
	
	HTTP method;
	

	public NetworkStep(int id, HTTP method, String url, ArrayList<RegexHelper> varIn) {
		super(id, varIn);
		this.url = StringEscapeUtils.unescapeXml(url);
		this.method = method;
		if (method.equals(HTTP.POST))
				params = new HashMap<String, String>();
		
	}
	
	public void setVarIn(ArrayList<RegexHelper> varIn){
		this.varIn = varIn;
	}
	
	
	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	


	public HTTP getMethod() {
		return method;
	}

	public void setMethod(HTTP method) {
		this.method = method;
		if (params==null)
			params = new HashMap<String, String>();
	}

	public NetworkStep(int id){
		super(id);
	}
	
	

	@Override
	public void init() throws StepExecutionError {
		if(method.equals(HTTP.POST) && params == null){
			throw new StepExecutionError(this, "No Post values!");
		}

	}

	@Override
	public boolean consume(Job job) throws StepExecutionError {
		
		boolean done = true;
		
		try {
			
			
			if (method.equals(HTTP.POST)){
				post = new HttpPost(url);
			} else if (method.equals(HTTP.GET)){
				url = updateGetParams(job, url);
				URL url_ = new URL(url);
				URI uri = new URI(url_.getProtocol(), url_.getUserInfo(), url_.getHost(), url_.getPort(), url_.getPath(), url_.getQuery(), url_.getRef());
				get = new HttpGet(uri);
			}
			
	
												
			HttpClient httpClient = job.getThreadSafeHttpClient();
	        HttpContext localContext = job.getLocalContext();
	        localContext.setAttribute(ClientContext.COOKIE_STORE, job.getCookieStore());
			
	        List<Cookie> cookies = job.getCookieStore().getCookies();
			for (int i = 0; i < cookies.size(); i++) {
				Log.e(LOG_TAG,
						"Post Request "+getId()+" - Local cookie: ["+i+"] - " + cookies.get(i));
			}								
			
			httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
			HttpResponse response = null;
			
			if (method.equals(HTTP.POST)){
				
				
				
				ArrayList<BasicNameValuePair> paramsPair = updateParams(job);
				post.setEntity(new UrlEncodedFormEntity(paramsPair, "UTF-8"));
	
				 response = httpClient.execute(post, localContext);
			} else if (method.equals(HTTP.GET)){
				
				response = httpClient.execute(get, localContext);
			} 
			
			
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				Log.i(LOG_TAG, "entity - Response content length: " + entity.getContentLength());
		
				
				CookieStore cookieStore = job.getCookieStore();
	            
	            List<Cookie> cookie = cookieStore.getCookies();
	            for (int i = 0; i < cookie.size(); i++) {
	            	Log.i(LOG_TAG, "LoginResponseHandler - Local cookie: " + cookie.get(i));
	            }	
	
					StatusLine statusLine = response.getStatusLine();
					int code = statusLine.getStatusCode();
					Log.i(LOG_TAG, "code = " + code);
					
					
					
					if (code == HttpStatus.SC_OK || code == HttpStatus.SC_ACCEPTED){
						
						 
						 ByteArrayOutputStream out = new ByteArrayOutputStream();
				         response.getEntity().writeTo(out);
				         out.close();
				         String content = out.toString();
						
						/*InputStream is = entity.getContent();
						 String content = "";
						  
						  BufferedReader bufferReader = new BufferedReader(new InputStreamReader(is));
						   String StringBuffer;
						   while ((StringBuffer = bufferReader.readLine()) != null) {
							   	content += StringBuffer;
						   }
						   bufferReader.close();
						  */  
						    
						   if (varIn!=null){
						   
						   for(int i=0; i< varIn.size() && done; i++){
						    	RegexHelper r = varIn.get(i);
						    	done &= r.eval(content);
						    	String msg = r.getMessage();   	
						    	
						    	if (r.getContext().equals(RegexHelper.ValueContext.ERROR)){
						    		if (done){
						    			done = false;
						    			//break;
						    		}  else {
						    			done = true;
						    		}
						    		
						    		//break;
						    	} else {
						    		
						    		if(r.getVar().equals(Job.VAR_SMS_REM)){
							    		if(!msg.equals(""))
							    			job.updateVar(Job.VAR_SMS_REM, msg);
							    	}
						    	}
						    	
						    	setMessage(msg);
						    	
						    	
						    }
						   }
						    
						      
						
					} else {
						setMessage(statusLine.getReasonPhrase());
						done = false;
						response.getEntity().getContent().close();
					}
				
			}
			
			
		} 
		catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StepExecutionError(this, e.getClass().getName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StepExecutionError(this, "Network Error!");

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new StepExecutionError(this, "URL Error!");
		}
		
		
		return done;
	}

	@Override
	public void finish() {
		
		Log.i("NetworkStep","step "+getId()+" "+getMessage());

	}
	
	private String updateGetParams(Job job, String url){
		
		if (url.contains(Job.VAR_USERNAME)){
			url =url.replace(Job.VAR_USERNAME, job.getVar(Job.VAR_USERNAME));
		}
		if (url.contains(Job.VAR_PASSWORD)){
			url =url.replace(Job.VAR_PASSWORD, job.getVar(Job.VAR_PASSWORD));
		}
		if (url.contains(Job.VAR_SENDER)){
			url =url.replace(Job.VAR_SENDER, job.getVar(Job.VAR_SENDER));
		}
		if (url.contains(Job.VAR_RCPT)){
			url =url.replace(Job.VAR_RCPT, job.getVar(Job.VAR_RCPT));
		}
		if (url.contains(Job.VAR_RCPT_NOCCC)){
			url =url.replace(Job.VAR_RCPT_NOCCC, job.getVar(Job.VAR_RCPT_NOCCC));
		}
		if (url.contains(Job.VAR_MSG)){
			url =url.replace(Job.VAR_MSG, job.getVar(Job.VAR_MSG));
		}
		
		
		
		return url;
		
	}
	private ArrayList<BasicNameValuePair> updateParams(Job job){
		
		ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		
		for (Map.Entry<String, String> entry : params.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if (value.equals(Job.VAR_USERNAME) 
		    		|| value.equals(Job.VAR_PASSWORD) 
		    		|| value.equals(Job.VAR_SENDER)
		    		|| value.equals(Job.VAR_RCPT_NOCCC)
		    		|| value.equals(Job.VAR_RCPT)
		    		|| value.equals(Job.VAR_MSG)){
		    	value = job.getVar(value);
		    }
		    pairs.add(new BasicNameValuePair(key, value));
		}
		
		
		return pairs;
		
	
	}
	

	
	public void addPostValue(String key, String value){
		if (method.equals(HTTP.POST))
			params.put(key, value);
	}

}
