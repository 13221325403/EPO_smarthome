package epo.smarthome.service;

import java.util.List;

import epo.smarthome.device.Friend;

public interface FriendListService {

	boolean saveFriend(Friend friend);

	boolean removeFriend(String friendMac);
	
	Friend getFriendByMac(String friendMac);

	List<Friend> getFriendList();

	int findFriendMaxId();

	boolean findFriendById(int id);
	
	boolean findFriendByMac(String friendMac);

	List<Friend> getFriendListByLocalDevMac(String localDevMac);

}
