package org.gotext.logic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class RegexHelper {
	
	private String var;
	private String pattern;
	private String begin;
	private String end;
	private String error_message;
	
	
	private String ret = "";
	
	private Pattern p;
	private Matcher m;
	
	public enum Type {
	IN_WHOLE_TEXT, BETWEEN_TEXT
	}
	
	public enum ValueContext {
		CONFIRMATION, ERROR
	}
	
	private Type type;
	private ValueContext context;
	
	public RegexHelper(String var, String pattern, String error_message, ValueContext context){
		this.var = var;
		this.pattern = pattern;
		this.error_message = error_message;
		this.context = context;
		type = Type.IN_WHOLE_TEXT;
		
		pattern = StringEscapeUtils.unescapeXml(pattern);
		pattern = Utils.escapeForRegex(pattern);
		
	}
	
	public RegexHelper(String var, String begin, String end, String error_message, ValueContext context){
		this.var = var;
		this.begin = begin;
		this.end = end;
		this.error_message = error_message;
		this.context = context;
		type = Type.BETWEEN_TEXT;
		
		begin = StringEscapeUtils.unescapeXml(begin);
		end = StringEscapeUtils.unescapeXml(begin);
		
		begin = Utils.escapeForRegex(begin);
		end = Utils.escapeForRegex(begin);
		
	}
	
	public void setValueContext(ValueContext context, String message){
		if (context.equals(ValueContext.CONFIRMATION))
			ret = "SENT";
		else 
			ret = message;
	}
	
	
	
	
	public boolean eval(String source){
		boolean result = false;
		
		switch(type){
			case IN_WHOLE_TEXT:
				pattern = Utils.escapeForRegex(pattern);
				//pattern = ".*" + pattern + ".*";
				//result = source.matches(pattern);
				//p= Pattern.compile("(.*)" + pattern + "(.*)", Pattern.DOTALL);
				p = Pattern.compile(pattern, Pattern.DOTALL);
		        Matcher regexMatcher = p.matcher(source);
		        result = regexMatcher.find();
				break;
			case BETWEEN_TEXT:
				begin = Utils.escapeForRegex(begin);
				end = Utils.escapeForRegex(end);
				p = Pattern.compile(
		                begin + "(\\d+)" + end,
		                Pattern.MULTILINE
		            );
				m = p.matcher(source);
				result = m.find();
				if (result)
					ret = m.group(1);
				break;
		}
		
		return result;
		
	}
	
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}


	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public ValueContext getContext() {
		return context;
	}

	public void setValueContext(ValueContext context) {
		this.context = context;
	}

	@Override
	public String toString(){
		return "Matcher: "+var+" "+type.name();
	}
	
	public String getMessage(){

		String r = "";
		switch(context){
		case ERROR:
			r = error_message;
			break;
		case CONFIRMATION:
			if (var.equals(Job.VAR_IN_LOGIN_CHECK)){
				r = "LOGIN_OK";
			} else if(var.equals(Job.VAR_IN_CONFIRM)){
				r = "SMS_OK";
			} else if(var.equals(Job.VAR_SMS_REM)){
				r = ret;
			} 
			
		}
		return r;
	}

}
