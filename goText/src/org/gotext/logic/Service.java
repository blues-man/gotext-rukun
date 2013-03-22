package org.gotext.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.w3c.dom.Document;

public class Service {
	
	private String uid;
	private String name;
	private byte[] icon;
	private String xml;
	private int maxchar;
	private Date installdate;
	private Date lastSent;
	private int sms;
	private int limit;
	
	private String username;
	private String password;
	private String nickname;
	private boolean cutPrefix;
	
	private int recipients;
	private ArrayList<String> int_prefixes;
	private HashMap<String,Boolean> credentials;
	
	private HashMap<String, HashMap<String, String>> languages;
	
	private Document doc;
	private Job job;
	
	public enum Type {
		CREDIT, NUMERIC, MONEY
	}
	
	public enum Recipients {
		PHONE, EMAIL, ID
		
	}
	
	public enum ScheduledReset {
		NONE, DAILY, MONTHLY, YEARLY
	}
	
	private Type type;
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public ScheduledReset getReset() {
		return reset;
	}

	public void setReset(ScheduledReset reset) {
		this.reset = reset;
	}
	
	
	public Job getJob(){
		return job;
	}
	
	public void setJob(Job job){
		this.job = job;
	}



	private ScheduledReset reset;
	
	public Service(){
		
	}
	
	public void init(){
		int_prefixes = new ArrayList<String>();
		credentials = new HashMap<String, Boolean>();
		
		languages = new HashMap<String, HashMap<String, String>>();
		cutPrefix = false;
		
		sms = -1;
	}
	
	
	public int getSmsLimit(){
		return limit;
	}
	
	public void setSmsLimit(int limit){
		this.limit = limit;
	}
	
	
	public boolean hasToCutPrefix(){
		return cutPrefix;
	}
	
	public void setCutPrefix(boolean mode){
		cutPrefix = mode;
	}
	
	public Date getLastSentDate(){
		return lastSent;
	}
	
	
	public void setLastSentDate(Date lastSent){
		this.lastSent = lastSent;
	}
	
	
    public void addLanguages(HashMap<String, HashMap<String, String>> languages){
    	this.languages = languages;
    	
    }
    
    public void addLanguage(String lang, HashMap<String, String> strings){
    	languages.put(lang, strings);
    }
    
    public HashMap<String, String> getLanguage(String lang){
    	return languages.get(lang);
    }
    
    public HashMap<String, HashMap<String, String>> getLanguages(){
    	return languages;
    }
    
    public String getLanguageString(String lang, String var){
    	
    	return languages.get(lang).get(var);
    	
    }
	
	public void addIntPrefixes(String prefix){
		int_prefixes.add(prefix);
		
	}
	public void addCredentials(String auth, boolean required){
		credentials.put(auth, required);
	}
	
	public HashMap<String, Boolean> getCredentials(){
		return credentials;
	}
	
	public void pushDoc(Document doc){
		this.doc = doc;
	}
	
	public Document getDoc(){
		return doc;
	}
	
	public boolean isDocPushed(){
		return doc==null?false:true;
	}
	
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public int getSms(){
		return sms;
	}
	
	public void setSms(int sms){
		this.sms = sms;
	}
	
	public int getMaxChar(){
		return maxchar;
	}
	public void setMaxChar(int maxchar){
		this.maxchar = maxchar;
	}
	
	public Date getInstallDate(){
		return installdate;
	}
	
	public void setInstallDate(Date installdate){
		this.installdate = installdate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getIcon() {
		return icon;
	}
	public void setIcon(byte[] icon) {
		this.icon = icon;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getRecipients() {
		return recipients;
	}
	public void setRecipients(int recipients) {
		this.recipients = recipients;
	}
	public ArrayList<String> getInt_prefixes() {
		return int_prefixes;
	}
	public void setInt_prefixes(ArrayList<String> int_prefixes) {
		this.int_prefixes = int_prefixes;
	}



	@Override
	public String toString() {
		return "Service [name=" + name + ", uid=" + uid + ", maxchar="
				+ maxchar + "]";
	}
	
	@Override
	public boolean equals(Object o){
		Service s = (Service)o;
		return getUid().equals(s.getUid());
	}
	
	

}
