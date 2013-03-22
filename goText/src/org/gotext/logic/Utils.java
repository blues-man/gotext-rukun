package org.gotext.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Matrix;


public class Utils {
	
	public static Date parseDate(String str){
		
		
	    SimpleDateFormat iso8601Format = new SimpleDateFormat(
	            "yyyy-MM-dd HH:mm:ss");

	    Date date = null;
	    if (str != null) {
	        try {
	            date = iso8601Format.parse(str);
	        } 
	        catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	return date;
	}
	
	
	
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;

		}
	
	
	public static String escapeForRegex(String str){
		
		if (str == null)
			return "";
		
		String escape = "";
		
		if (str.contains("."))
			escape = str.replace(".", "\\.");
		if (str.contains("*"))
			escape = str.replace("*", "\\*");
		//if (str.contains("/"))
			//escape = str.replace("/", "\\/*");
		
		return escape.equals("")?str:escape;
	}
	

}
