package epo.smarthome.device;

/**
 * �豸��
 * 
 * @author Administrator
 * 
 */
public class Dev {
	private int id = 0;
	private String macAdress = "";
	private String nickName = "";
	private String lastUpdate = "";
	private String torken = "";
	private String ipPort = "";
	private String pass = "";
	private int onLine = 0;
	private int errorCount =0;
	private final int ERROR_COUNT_MAX=180;
	private boolean dataChange;
	
	private int groupID = 0;
	private String groupName;
	
	
	public void runStep(){
		if(errorCount>=ERROR_COUNT_MAX){
			this.onLine= 0 ;
		}else{
			errorCount++;
		}
	}
	public boolean isDataChange() {
		return this.dataChange;
	}

	public void isDataChange(boolean ok) {
		this.dataChange = ok;
		if(ok){
			onLine=ok?1:0;
			errorCount=0;
		}
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public  String getMacAddress(){
		return macAdress;
	}

	public void setMacAddress(String macAdress){
		this.macAdress = macAdress;
	}

	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return  id;
	}

	public String getTorken() {
		return torken;
	}

	public void setTorken(String torken) {
		this.torken = torken;
	}

	public String getIpPort() {
		return ipPort;
	}

	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public String getMac() {
		return macAdress;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
	public String toString() {
		return "  nickName:"+ nickName  + " " + (onLine==1 ? "����" : "������") + 
				"  lastUpdate:" + lastUpdate + "  torken:"
				+ torken + "  ipPort:" + ipPort;
	}

	public int isOnLine() {
		return onLine;
	}

	public void setOnLine(int i) {
		this.onLine = i;
	}
	
	public void setGroupID(int groupId) {
		this.groupID = groupId;
	}
	public int getGroupID(){
		return groupID;
	}

	public void setGroupName(String groupName){
		this.groupName = groupName;
	}
	
	public String getGroupName(){
		return groupName;
	}

}
