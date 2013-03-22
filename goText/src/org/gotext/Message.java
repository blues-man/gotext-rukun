package org.gotext;

public class Message {
	
	private String dest;
	private String msg;
	
	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Message(String dest, String msg){
		this.dest = dest;
		this.msg = msg;
	}
	
	@Override
	public String toString(){
		return "Message "+dest+" "+msg;
	}

}
