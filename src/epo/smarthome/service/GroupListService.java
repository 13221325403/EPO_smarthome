package epo.smarthome.service;

import java.util.List;

import epo.smarthome.device.Group;

public interface GroupListService {
	
	boolean saveGroup(Group group);

	boolean removeGroup(String groupName);

	Group getGroupByName(String groupName);

	List<Group> getGroupList();

	int findGroupMaxId();

	boolean findGroupById(int id);
	
	boolean findGroupByName(String groupName);
	

}
