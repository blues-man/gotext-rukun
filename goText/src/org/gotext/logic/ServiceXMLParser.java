package org.gotext.logic;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class ServiceXMLParser {
	
	public static final String TAG_NAME = "name";
	public static final String TAG_MAXCHAR = "maxchar";
	public static final String TAG_ICON = "icon";
	public static final String TAG_DESCRIPTION = "description";
	
	public static final String TAG_ALLOWED_PREFIX = "allowed_ccc";
	public static final String TAG_LANG_CODE = "code";
	public static final String TAG_VALUE = "value";
	public static final String TAG_TYPE = "type";
	public static final String TAG_QUANTITY= "quantity";
	public static final String TAG_RESET= "reset";
	public static final String TAG_MAX= "max";
	public static final String TAG_ID = "id";
	public static final String TAG_URL = "url";
	public static final String TAG_SEARCH = "search";
	public static final String TAG_MATCH = "match";
	public static final String TAG_BEGIN = "begin";
	public static final String TAG_END = "end";
	public static final String TAG_NOT_EMPTY = "not_empty";
	public static final String TAG_EMPTY = "empty";
	public static final String TAG_ERROR_MESSAGE = "error_message";








	
	
	public ServiceXMLParser(){
	}
	
	public Service getService(){
		return null;
	}
	
	public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
        		DocumentBuilder db = dbf.newDocumentBuilder();
            	InputSource is = new InputSource();
            	is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
                // return DOM
            return doc;
    }

}
