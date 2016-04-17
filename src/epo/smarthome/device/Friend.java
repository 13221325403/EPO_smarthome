package epo.smarthome.device;

public class Friend extends Dev{
	
	private String localDevMacAdress;
	private String userName;
	private String userID;
	
	public void setUserID(String userID){
		this.userID = userID;
	}
	
	public String getUserID(){
		return  userID;
	}
	
	public void setUserName(String userName){
		this.userName = userName;
	}
	
	public String getUserName(){
		return  userName;
	}
	
	public void setLocalDevMac(String localDevMacAdress){
		this.localDevMacAdress = localDevMacAdress;
	}
	
	public String getLocalDevMac(){
		return  localDevMacAdress;
	}	
	
	
	

}
