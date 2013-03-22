package org.gotext.logic;

import java.util.ArrayList;



public abstract class Step {

	private int id;
	protected ArrayList<RegexHelper> varIn;
	private Job job;
	private String message;

	
	public final static String PAGE = "page";
	public final static String FORK = "fork"; 
	
	public Step(int id){
		this.id = id;
	}
	
	public Step(int id, ArrayList<RegexHelper> varIn){
		this.id = id;
		this.varIn = varIn;
		
	}
	
	public abstract void init() throws StepExecutionError;
	
	public abstract boolean consume(Job job) throws StepExecutionError;
	
	public abstract void finish();
	
	public int getId(){
		return id;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	

}
