package org.gotext.logic;

public class StepExecutionError extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public StepExecutionError(Step step, String msg){
		super("Errore in step "+step.getId()+ " "+msg);
		
	}

}
