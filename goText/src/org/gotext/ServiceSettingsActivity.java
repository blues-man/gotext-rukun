package org.gotext;

import java.util.ArrayList;
import java.util.Map;

import org.gotext.logic.Service;
import org.gotext.logic.goTextApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceSettingsActivity extends Activity {
	
	EditText editUser, editPass, editNick;
	TextView textUser, textPass, textNick;
	Button but;
	goTextApplication ga = null;
	Service s = null;
	ArrayList<String> required = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.service_settings);

		editUser = (EditText)findViewById(R.id.editUser);
		editPass = (EditText)findViewById(R.id.editPass);
		editNick = (EditText)findViewById(R.id.editNick);
		
		textUser = (TextView)findViewById(R.id.textview_user);
		textPass = (TextView)findViewById(R.id.textview_pass);
		textNick = (TextView)findViewById(R.id.textview_nick);
		
		required = new ArrayList<String>();
		
		Bundle extras = getIntent().getExtras();
		ga = (goTextApplication)getApplication();
		 if (extras!=null){
		    	String uid = extras.getString("Service");
		    	s = ga.getService(uid);
		    	setTitle(s.getName());
		    	if (s.getCredentials().size() > 0){
		    		for (Map.Entry<String, Boolean> entry : s.getCredentials().entrySet()) {
		    		    String key = entry.getKey();
		    		    Boolean value = entry.getValue();
		    		    if (value)
		    		    	required.add(key);
		    		    
		    		    if (key.equals("user")){
		    		    	textUser.setVisibility(View.VISIBLE);
		    		    	editUser.setVisibility(View.VISIBLE);
		    		    	
		    		    	
		    		    	if (!value){
		    		    		textUser.setText(textUser.getText().toString()+" ("+getString(R.string.optional)+")");
		    		    	} 
		    		    	
		    		    	if (s.getUsername()!=null){
		    		    		editUser.setText(s.getUsername());
		    		    	}
		    		    } else if (key.equals("pass")){
		    		    	textPass.setVisibility(View.VISIBLE);
		    		    	editPass.setVisibility(View.VISIBLE);
		    		    	
		    		    	if (!value){
		    		    		textPass.setText(textPass.getText().toString()+" ("+getString(R.string.optional)+")");
		    		    	}
		    		    	
		    		    	if (s.getPassword()!=null){
		    		    		editPass.setText(s.getPassword());
		    		    	}
		    		    } else if (key.equals("sender")){
		    		    	textNick.setVisibility(View.VISIBLE);
		    		    	editNick.setVisibility(View.VISIBLE);
		    		    	
		    		    	if (!value){
		    		    		textNick.setText(textNick.getText().toString()+" ("+getString(R.string.optional)+")");
		    		    	} 
		    		    	
		    		    	if (s.getNickname()!=null){
		    		    		editNick.setText(s.getNickname());
		    		    	}
		    		    }
		    		    
		    		}
		    	}
		    	
		    	
		 }
		
		
	
	}
	
	public void onClick(View view){
		
		boolean check = true;
		for(String str: required){
			if (str.equals("user")){
				check &= !editUser.getText().toString().equals("");
			} else if (str.equals("pass")){
				check &= !editPass.getText().toString().equals("");
			} else if (str.equals("sender")){
				check &= !editNick.getText().toString().equals("");
			} 
		}
		if(!check){
			Toast.makeText(this, "Missing Fields!", Toast.LENGTH_LONG).show();
		} else {
			//for(String str: required){
				//if (str.equals("user")){
					s.setUsername(editUser.getText().toString());
				//} else if (str.equals("pass")){
					s.setPassword(editPass.getText().toString());
				//} else if (str.equals("sender")){
					s.setNickname(editNick.getText().toString());
				//} 
			//}
			boolean persisted = ga.persistCredentials(s);
			
			setResult(0);
			finish();
			
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater Menu = getMenuInflater();
		Menu.inflate(R.menu.menu_service_settings, menu);
		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {			
		case R.id.menu_service_delete:
			

			boolean deleted = ga.deleteService(s);
			setResult(0);
			finish();
			
			
			break;
		}
		return super.onOptionsItemSelected(item);

	
	}
	

}
