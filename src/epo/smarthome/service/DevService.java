package epo.smarthome.service;

import java.util.List;

import epo.smarthome.device.Dev;

public interface DevService {
	boolean saveDev(Dev dev);

	boolean removeDev(String mac);

	Dev getDevByMac(String mac);

	List<Dev> getDevList();

	int findDevMaxId();

	boolean findDevById(int id);
	
	boolean findDevByMac(String mac);
}
