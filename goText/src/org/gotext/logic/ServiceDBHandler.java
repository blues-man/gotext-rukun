package org.gotext.logic;
 
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class ServiceDBHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "gotext";
 
    // Contacts table name
    private static final String TABLE_SERVICES = "services";
 
    // Contacts Table Columns names
    private static final String KEY_UID = "uid";
    private static final String KEY_XML = "xml";
    private static final String KEY_NAME = "name";
    private static final String KEY_MAXCHAR = "maxchar";
    private static final String KEY_SMS = "sms";
    private static final String KEY_LAST_SENT = "last_sent";
    private static final String KEY_INSTALL_DATE = "install_date";
    private static final String KEY_ICON = "icon";
    private static final String KEY_USER = "user";
    private static final String KEY_PASS = "pass";
    private static final String KEY_NICK = "nick";

    

 
    public ServiceDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_SERVICES + "("
        		+ KEY_UID + " TEXT PRIMARY KEY, " 								// 0
                + KEY_XML + " TEXT, " 											// 1
        		+ KEY_NAME + " TEXT, "											// 2
                + KEY_SMS + " INTEGER, "										// 3
                + KEY_LAST_SENT + " DATETIME, "									// 4
                + KEY_INSTALL_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "	// 5
                + KEY_ICON + " BLOB, "											// 6
                + KEY_MAXCHAR + " INTEGER,"										// 7
        		+ KEY_USER + " TEXT,"											// 8
        		+ KEY_NICK + " TEXT,"											// 9
        		+ KEY_PASS + " TEXT" + ")";										// 10
        		
        db.execSQL(CREATE_CONTACTS_TABLE);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
 
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
 
    // Adding new contact
    public boolean addService(Service s) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_UID, s.getUid()); // Contact Name
        values.put(KEY_XML, s.getXml());
 
        // Inserting Row
        long exit = db.insertWithOnConflict(TABLE_SERVICES, null, values,SQLiteDatabase.CONFLICT_IGNORE);
        db.close(); // Closing database connection
        return exit==-1?false:true;
    }
 
    // Getting single contact
    public Service getService(String xml) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SERVICES, new String[] { KEY_UID,
                KEY_XML }, KEY_UID + "=?",
                new String[] { xml }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        Service contact = new Service();
        contact.setUid(cursor.getString(0));
        contact.setXml(cursor.getString(1));
        cursor.close();
        // return contact
        return contact;
    }
    
    // Getting All Services
    public List<Service> getAllServices() {
        List<Service> servList = new ArrayList<Service>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SERVICES;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Service s = new Service();
                s.init();
                s.setUid(cursor.getString(0));
                s.setXml(cursor.getString(1));
                if(!cursor.isNull(2)){
                	s.setName(cursor.getString(2));
                }
                if(!cursor.isNull(3)){
                	s.setSms(cursor.getInt(3));
                }
                if(!cursor.isNull(4)){
                	String date = cursor.getString(4);
                	Date d = Utils.parseDate(date);
                	if (d!=null)
                		s.setLastSentDate(d);
                }
                if(!cursor.isNull(5)){
                	String date = cursor.getString(5);
                	Date d = Utils.parseDate(date);
                	if (d!=null)
                		s.setInstallDate(d);
                }
                if (!cursor.isNull(6)){
                	s.setIcon(cursor.getBlob(6));
                	
                }
                if(!cursor.isNull(7)){
                	s.setMaxChar(cursor.getInt(7));
                }
                if(!cursor.isNull(8)){
                	s.setUsername(cursor.getString(8));
                }
                if(!cursor.isNull(9)){
                	s.setNickname(cursor.getString(9));
                }
                if(!cursor.isNull(10)){
                	s.setPassword(cursor.getString(10));
                }
                
                // Adding contact to list
                servList.add(s);
            } while (cursor.moveToNext());
        }
 
        cursor.close();
        // return contact list
        return servList;
    }
    
    

 
    // Updating single contact
    public int updateService(Service s) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_XML, s.getXml());
        values.put(KEY_NAME, s.getName());
        values.put(KEY_MAXCHAR, s.getMaxChar());
        values.put(KEY_SMS, s.getSms());
        values.put(KEY_ICON, s.getIcon());
        

        
        
 
        // updating row
        return db.update(TABLE_SERVICES, values, KEY_UID + " = ?",
                new String[] { s.getUid() });
    }
    
    public int updateServiceCredentials(Service s){
    	   SQLiteDatabase db = this.getWritableDatabase();
    	   
           ContentValues values = new ContentValues();
           if (s.getUsername()!=null)
        	   values.put(KEY_USER, s.getUsername());
           if (s.getPassword()!=null)
        	   values.put(KEY_PASS, s.getPassword());
           if (s.getNickname()!=null)
        	   values.put(KEY_NICK, s.getNickname());
           // updating row
           return db.update(TABLE_SERVICES, values, KEY_UID + " = ?",
                   new String[] { s.getUid() });

    	
    }
    
    public int updateServiceSms(Service s){
    	 SQLiteDatabase db = this.getWritableDatabase();
    	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    	 Date date = new Date();
         ContentValues values = new ContentValues();
         if (s.getSms()<0)
        	 return -1;
      	 values.put(KEY_SMS, s.getSms());
      	 values.put(KEY_LAST_SENT, dateFormat.format(date));
         return db.update(TABLE_SERVICES, values, KEY_UID + " = ?",
                 new String[] { s.getUid() });
    	
    }
 
    
    public int deleteService(Service s) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SERVICES, KEY_UID + " = ?",
                new String[] { s.getUid() });
        db.close();
        
        return result;
    }
 
    // Getting contacts Count
    public int getServiceCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SERVICES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();
 
        // return count
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
 
}