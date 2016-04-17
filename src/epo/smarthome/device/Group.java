package epo.smarthome.device;

public class Group {
	
	private int id = 0;	
	private String Name = "";
	private String lastUpdate = "";
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return  id;
	}
	
	public String getNickName() {
		return Name;
	}

	public void setNickName(String nickName) {
		this.Name = nickName;
	}
	
	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	@Override
	public String toString() {
		return "  GROUPID:"+ id  + "GROUPNAME:" + Name;
	}

}
