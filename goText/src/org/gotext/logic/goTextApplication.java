package org.gotext.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.gotext.logic.Service.ScheduledReset;
import org.gotext.logic.Service.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class goTextApplication extends Application {
	

	private final static String LOG_TAG = "goTextApplication";
	

	private HttpClient httpClient;	
    private CookieStore cookieStore;    
    private HttpContext localContext;
    
    private ServiceXMLParser parser;
    
    private ArrayList<Service> services;
    
    
    
	
	private static final String PREFS_NAME = "settings";

	private SharedPreferences settings;
	private ServiceDBHandler dbh = null;
	//private ServiceDataSource ds;
	
	@Override
	public void onCreate() {
		super.onCreate();
		httpClient = createHttpClient();
		
		cookieStore  = new BasicCookieStore();
		localContext = new BasicHttpContext();
	    localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			    
		settings = getSharedPreferences(PREFS_NAME, 0);

		parser = new ServiceXMLParser();
		
		services = new ArrayList<Service>();
		
		//ds = new ServiceDataSource(this);
		//ds.open();
		dbh = new ServiceDBHandler(this);
	}
	

	
	

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.i(LOG_TAG,"Low Memory!!");
		releaseHttpClient();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(LOG_TAG,"Terminate CustomApplication");
		releaseHttpClient();		
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public HttpContext getLocalContext() {
		return localContext;
	}
	
	public SharedPreferences getSettingsPref() {
		//Log.i(LOG_TAG, "LANG " + settings.contains("lang") + settings.getBoolean("firststart", true));
		if(!settings.contains("lang") ) {
			Log.i(LOG_TAG, "Setup default settings preferences");
			settings.edit().putBoolean("firststart", true).commit();
		}		
	    
		return settings;
	}
	
	
	public ArrayList<Service> getServices(){
		
		
		return services;
		
	}
	
	public void addService(Service s){
		services.add(s);
		
	}
	
	
	public void buildAll(){
		
		if(services.isEmpty()){
			syncronizeWithDB();
			populateList();
		}
		
		
	}
	
	public void updateAll(){
		
		
		List<Service> serv = dbh.getAllServices();
		
		for(Service s: serv){
			
			if (s.getName()==null){
				extractServiceAttributes(s);
				extractServiceSettings(s);
				extractServiceRecipients(s);
				extractServiceLimits(s);
				extractServiceLanguages(s);
				
				extractServiceSteps(s);
				
				if (s.getSms() == -1){
					s.setSms(s.getSmsLimit());
				}
				
				if (s.getReset().equals(Service.ScheduledReset.DAILY)){
					Date now = new Date();
					if (s.getSmsLimit() != s.getSms()){
						if (s.getLastSentDate() != null &&
								now.getDay() != s.getLastSentDate().getDay()){
								s.setSms(s.getSmsLimit());
						}
							
					}
					
				}
				
				int res = dbh.updateService(s);
				Log.i("goTextApplication", "updated "+s.getUid()+" ="+res);
			
			}
			if (!services.contains(s))
				services.add(s);
		}
		
		
		
		
		
	}
	
	private void syncronizeWithDB(){
		
		
		List<Service> serv = dbh.getAllServices();
		
		for(Service s: serv){
			
			if (s.getName()==null){
				extractServiceAttributes(s);
				int res = dbh.updateService(s);
				Log.i("goTextApplication", "updated "+s.getUid()+" ="+res);
			
			}
			if (!services.contains(s))
				services.add(s);
		}
		
	}
	
	
	
	private void populateList(){
		for (Service s: services){
			
			extractServiceSettings(s);
			extractServiceRecipients(s);
			extractServiceLimits(s);
			extractServiceLanguages(s);
			
			extractServiceSteps(s);
			
			if (s.getSms() == -1){
				s.setSms(s.getSmsLimit());
			}
			
			if (s.getReset().equals(Service.ScheduledReset.DAILY)){
				Date now = new Date();
				if (s.getSmsLimit() != s.getSms()){
					if (s.getLastSentDate() != null &&
							now.getDay() != s.getLastSentDate().getDay()){
							s.setSms(s.getSmsLimit());
					}
						
				}
			}
			
			
			
			
			System.out.println("LANG "+ s.getLanguages()+"\n Recipients "+s.getRecipients());
		}
	}
	
	public boolean persistCredentials(Service s){
		return dbh.updateServiceCredentials(s)==-1?false:true;
		
	}
	
	public boolean updateSms(Service s){
		
		return dbh.updateServiceSms(s)==-1?false:true;
		
	}
	
	public Service getService(String uid){
		
		Service found = null;
		for (Service s: services){
			if (s.getUid().equals(uid))
				found = s;
		}
		
		return found;
		
	}
	
	private Document getDoc(Service s){
	    Document doc = null;
	    if (!s.isDocPushed()){
	    	doc = parser.getDomElement(s.getXml());
	    	s.pushDoc(doc);
	    }
	    else
	    	doc = s.getDoc();
	    
	    return doc;
	}
	
	
	private void extractServiceSteps(Service s){
		 Document doc = getDoc(s);
		 NodeList nodeList = doc.getElementsByTagName("send");
		 Node send = nodeList.item(0);
		 Job job = null;
		 List<Step> steps = new ArrayList<Step>();
		 int stepCounter = 0;
		 
		 if(send.hasChildNodes()){
			 
			 NodeList list = send.getChildNodes();
    			for( int i=0; i< list.getLength(); i++){
    				
    				
    				Node current = list.item(i);
    				if (current.getNodeType() == Node.ELEMENT_NODE) {
    					
    					String id = "";
    					String name = current.getNodeName();
    					
    					if (name.equals(Step.PAGE)){
    						
    						NetworkStep step = new NetworkStep(stepCounter);
    						String url = "";
    						NetworkStep.HTTP method = null;
    					
    						NamedNodeMap attributes = current.getAttributes();

    						   for (int j = 0; j < attributes.getLength(); j++) {
    							   Node val = attributes.item(j);
    					            if (val.getNodeName().equals(ServiceXMLParser.TAG_ID)){
    					            	id = val.getNodeValue();
    					            	step.setPageId(id);
    					            } else if (val.getNodeName().equals(ServiceXMLParser.TAG_URL)){
    					            	url = val.getNodeValue();
    					            	step.setUrl(url);
    					            } else if (val.getNodeName().equals(ServiceXMLParser.TAG_TYPE)){
    					            	if (val.getNodeValue().equals(NetworkStep.HTTP.POST.name().toLowerCase())){
    					            		method = NetworkStep.HTTP.POST;
    					            	} else if (val.getNodeValue().equals(NetworkStep.HTTP.GET.name().toLowerCase())){
    					            		method = NetworkStep.HTTP.GET;
    					            	}
    					            	step.setMethod(method);
    					            }
    						   }//for 
    						   
    						   //<var in=...>
    						   if (current.hasChildNodes()){
    							   
    							   
    							   
    							   
    							   ArrayList<RegexHelper> varin = new ArrayList<RegexHelper>();
    							   NodeList listVarIn = current.getChildNodes();
    				    		   for( int x=0; x < listVarIn.getLength(); x++){
    				    				
    				    				Node pageChild = listVarIn.item(x);
    				    				
    				    				
    				    				if (pageChild.getNodeType() == Node.ELEMENT_NODE) {
    				    					NamedNodeMap attrb = pageChild.getAttributes();
    				    					
    				    					if (pageChild.getNodeName().equals("post")){
    				    						String key = null, value = null;
    				    						for (int z = 0; z < attrb.getLength(); z++) {
        				    						Node v = attrb.item(z);
        				    						if (v.getNodeName().equals(ServiceXMLParser.TAG_NAME)){
     			    								   key = v.getNodeValue();
     			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_VALUE)){
     			    								   value = v.getNodeValue();
     			    								   if (value.equals(Job.VAR_RCPT_NOCCC))
     			    										   s.setCutPrefix(true);
     			    							   } 
    				    						}
    				    						
    				    						step.addPostValue(key, value);
    				    						Log.i("Step "+step.getId(),"aggiunte post "+key+" "+value);
    				    						
    				    					} else if (pageChild.getNodeName().equals("var")){
    				    					
    				    						RegexHelper r = null;
    	    				    				RegexHelper.Type rtype = null;
    	    				    				RegexHelper.ValueContext contx = null;
    	    				    				String var = null,source = null,pattern = "",begin = null,end =null, empty =null, not_empty=null, search = null, error_message= null;
    				    					
	    				    					for (int z = 0; z < attrb.getLength(); z++) {
	    				    						Node v = attrb.item(z);
	    			    							   if (v.getNodeName().equals(ServiceXMLParser.TAG_NAME)){
	    			    								   var = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_SEARCH)){
	    			    								   search = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_MATCH)){
	    			    								   pattern = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_BEGIN)){
	    			    								   begin = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_END)){
	    			    								   end = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_NOT_EMPTY)){
	    			    								   not_empty = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_EMPTY)){
	    			    								   empty = v.getNodeValue();
	    			    							   } else if (v.getNodeName().equals(ServiceXMLParser.TAG_ERROR_MESSAGE)){
	    			    								   error_message = v.getNodeValue();
	    			    							   }
	    			    						   }
    				    					
    				    					
    				    					if(search != null){
    				    					
	    				    					if(search.equals("match")){
	    				    						rtype = RegexHelper.Type.IN_WHOLE_TEXT;
	    				    						if (not_empty != null){
	    				    								if( not_empty.equals("confirm")){
	    				    									
	    				    									contx = RegexHelper.ValueContext.CONFIRMATION;
	    				    								} else if (not_empty.equals("error")){
	    	    				    							contx = RegexHelper.ValueContext.ERROR;
	    	    				    						}
	    				    						}
	    				    						if (empty!=null){
	    				    							if(empty.equals("error")){
	    				    								contx = RegexHelper.ValueContext.CONFIRMATION;
	    				    							} else if (empty.equals("0")){
	    				    								contx = RegexHelper.ValueContext.CONFIRMATION;		
	    				    							}
	    				    						} 
	    				    						
	    				    						
	    				    						r = new RegexHelper(var, pattern, error_message, contx);
	    				    						
	    				    					} else if (search.equals("between")) {
	    				    						rtype = RegexHelper.Type.BETWEEN_TEXT;
	    				    						contx = RegexHelper.ValueContext.CONFIRMATION;	
	    				    						r = new RegexHelper(var, begin, end, error_message, contx);
	
	    				    					}
	    				    						
	    	    				    			varin.add(r);	
    				    					}// if search not null
    				    				
    				    					}// if var
    				    					
    				    				}//if element node
    				    				
    				    		   } // for child node
    						   
    						   step.setVarIn(varin);
    						   } // if child node
    						
    						
    						   
    				    stepCounter++;
    				    steps.add(step);
    					}// if page
    					
    					
    					
    					
    					
    				}
    				
    				
    			}// if element
			 
			 
		 } // has child
		 
		 if(steps.size()>0){
			 job = new Job(s,steps);
			 s.setJob(job);
		 }

		 
	
	}
	
	private void extractServiceLimits(Service s){
		
	    Document doc = getDoc(s);
	    NodeList nodeList = doc.getElementsByTagName("limit");
	    Node limit = nodeList.item(0);
	    
	    NamedNodeMap attributes = limit.getAttributes();
	    String smsLimit = "";

	    for (int i = 0; i < attributes.getLength(); i++) {
	            Node val = attributes.item(i);
	            if (val.getNodeName().equals(ServiceXMLParser.TAG_TYPE)){
	            	String type = val.getNodeValue();
	            	Type t = null;
	            	if (type.equals(Type.CREDIT.name().toLowerCase())){
	            		t = Type.CREDIT;		
	            	} else if (type.equals(Type.NUMERIC.name().toLowerCase())){
	            		t = Type.NUMERIC;	
	            	} else if (type.equals(Type.MONEY.name().toLowerCase())){
	            		t = Type.MONEY;	
	            	}
	            	s.setType(t);
	            } // tag type
	            else if (val.getNodeName().equals(ServiceXMLParser.TAG_QUANTITY)){
	            	//if (s.getSms()==-1)
	            		//s.setSms(Integer.parseInt(val.getNodeValue()));
	            	s.setSmsLimit(Integer.parseInt(val.getNodeValue()));
	            } else if (val.getNodeName().equals(ServiceXMLParser.TAG_RESET)){
	            	int reset = 0;
	            	ScheduledReset res = null;
	            	try{
	            		reset = Integer.parseInt(val.getNodeValue());
	            	} catch (NumberFormatException nfe){nfe.printStackTrace();}
	            	
	            	switch(reset){
	            	case 0:
	            		res = ScheduledReset.NONE; 
	            		break;
	            	case 1:
	            		res = ScheduledReset.DAILY;
	            		break;
	            	case 30:
	            		res = ScheduledReset.MONTHLY;
	            		break;
	            	case 360:
	            		res = ScheduledReset.MONTHLY;
	            		break;
	            	default:
	            		res = ScheduledReset.NONE;
	            		
	            	}
	            	
	            	s.setReset(res);
	            }
	    }

	}
	
	private void extractServiceSettings(Service s){
		
	    Document doc = getDoc(s);
	    NodeList nodeList = doc.getElementsByTagName("field");
	    for( int i=0; i< nodeList.getLength(); i++){
		    boolean required = false;
		    String auth = "";
	    	Node n = nodeList.item(i);
	    	NamedNodeMap attributes = n.getAttributes();
	    	for (int j = 0; j < attributes.getLength(); j++) {
		    	Node val = attributes.item(j);
		    	if (val.getNodeName().equals("required")){
		    		required = Boolean.parseBoolean(val.getNodeValue());
		    		
		    	} else if (val.getNodeName().equals(ServiceXMLParser.TAG_NAME)){
		    		auth = val.getNodeValue();
		    		
		    	} 
		    	
	    	}//for j
	    	s.addCredentials(auth, required);  	
	    }//for i
		
		
	}
	
	private void extractServiceRecipients(Service s){
		
	    Document doc = getDoc(s);

	    
	    NodeList nodeList = doc.getElementsByTagName("recipients");
	    Node recipients = nodeList.item(0);
	    NamedNodeMap attributes = recipients.getAttributes();
	    for (int i = 0; i < attributes.getLength(); i++) {
            if (recipients.getNodeName().equals(ServiceXMLParser.TAG_MAX)){
            	int r = 1;
            	try{
            		r = Integer.parseInt(recipients.getNodeValue());
            	} catch( NumberFormatException nfe){ nfe.printStackTrace();}
            	s.setRecipients(r);
            }

	    	
	    }
	    if (recipients.hasChildNodes()){
	    	NodeList rr = recipients.getChildNodes();
	    	Node current = rr.item(1);
		    //NodeList nodeLists = doc.getElementsByTagName("recipient");
			if (current.getNodeType() == Node.ELEMENT_NODE) {

	    	NamedNodeMap _attributes = current.getAttributes();

	 	    for (int i = 0; i < _attributes.getLength(); i++) {
	 	    	Node val = _attributes.item(i);
	            if (val.getNodeName().equals(ServiceXMLParser.TAG_ALLOWED_PREFIX)){
	            	s.addIntPrefixes(val.getNodeValue());
	            	
	            }
	 	    }
	 	   }
	    	
	    }
	    
	    
		
	}
	
	private void extractServiceLanguages(Service s){
		
	    Document doc = getDoc(s);

	    
	    NodeList language = doc.getElementsByTagName("language");

	    String langCode = "";

	    	for( int i=0; i< language.getLength(); i++){
	    		// <language code="CODE">
	    	    HashMap<String, String> values = new HashMap<String, String>();

	    		Node l = language.item(i);
	    		Node langC = l.getAttributes().item(0);
	    		if (langC.getNodeName().equals(ServiceXMLParser.TAG_LANG_CODE)){
	    			langCode = langC.getNodeValue();
	    		}
	    		// <var name="$_VAR" value="VALUE" />
	    		if (l.hasChildNodes()){
	    			NodeList langs = l.getChildNodes();
	    			for( int j=0; j< langs.getLength(); j++){
	    				
	    				Node current = langs.item(j);
	    				if (current.getNodeType() == Node.ELEMENT_NODE) {
	    			        Element element = (Element) current;
		    				NamedNodeMap attributes = element.getAttributes();
		    				if (attributes != null){
			    			    String key = null;
			    			    String value = null;
		
			    				for (int x = 0; x < attributes.getLength(); x++) {
			    			    	Node val = attributes.item(x);
			    			    	if (val.getNodeName().equals(ServiceXMLParser.TAG_NAME)){
			    			    		key = val.getNodeValue();
			    			    	} else if (val.getNodeName().equals(ServiceXMLParser.TAG_VALUE)){
			    			    		value = val.getNodeValue();
			    			    	}
			    				}
			    				
		    			    	values.put(key, value);
		    				}// if attributes not null
		    				
		    				
		    			}// for attributes var
	    			}// if elementva
		    			
		    		}
		    		
	    		s.addLanguage(langCode, values);
	    		
	    	}// for language
	    	
	    	
	    	
	    
	    
		
	}
	
	
	
	
	

	

	
	public void extractServiceAttributes(Service s){
		
		Document doc = getDoc(s);
	    NodeList nodeList = doc.getElementsByTagName("service");
	    Node service = nodeList.item(0);
	    
	    NamedNodeMap attributes = service.getAttributes();

	    for (int i = 0; i < attributes.getLength(); i++) {
	            Node val = attributes.item(i);
	            if (val.getNodeName().equals(ServiceXMLParser.TAG_NAME)) {
	            	s.setName(val.getNodeValue());
	            } else if (val.getNodeName().equals(ServiceXMLParser.TAG_MAXCHAR)){
	            	s.setMaxChar(Integer.parseInt(val.getNodeValue()));
	            } else if (val.getNodeName().equals(ServiceXMLParser.TAG_ICON)){
	            	
	            	try {
	            		HttpGet mHttpGet = new HttpGet(val.getNodeValue());
	   
	            		HttpResponse response = httpClient.execute(mHttpGet);
	            		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            			  HttpEntity entity = response.getEntity();
	            			    if ( entity != null) {
	            			    	byte[] img = EntityUtils.toByteArray(entity);
	            			    	s.setIcon(img);
	            			    }
	            		}
	            	} catch(IOException ioe){ioe.printStackTrace();}
	            }
	            System.out.println(val.getNodeName() + "=" + val.getNodeValue());
	    }
	    Log.i("SERVICE", s.toString());
	    //return ds.insertService(s);
		
	}
	
	public boolean insertService(String xml_id, String xml){
		Service s = new Service();
		s.setUid(xml_id);
		s.setXml(xml);
		return dbh.addService(s);
		
		//return ds.insertService(s);
		//return false;
	}
	
	public int getServiceCount(){
		return dbh.getServiceCount();
	}
	
	public Service getServiceFromDb(String uid){
		return dbh.getService(uid);
	}
	
	public boolean deleteService(Service srv){
		
		if (srv != null && services.contains(srv)){
			services.remove(srv);
			return dbh.deleteService(srv)==0?false:true;
		}
		
		return false;
	}
	


	


	public HttpClient getThreadSafeHttpClient(){
		return httpClient;
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
		Log.i(LOG_TAG,"HttpClient Created!");
		return tmpClient;
	}
	

	private final void releaseHttpClient(){
		if(httpClient!=null && httpClient.getConnectionManager()!=null){
			// Se esiste il ConnectionManager del client lo chiudiamo
			httpClient.getConnectionManager().shutdown();
			Log.i(LOG_TAG,"Releasing Connections");
		}
	}

}
