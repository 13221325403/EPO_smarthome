package epo.smarthome.iprotocol;

import java.util.List;

import epo.smarthome.protocol.Buff;
import epo.smarthome.protocol.Msg;

public interface IProtocol {
	List<Msg> checkMessage(Buff buff);

	boolean MessageEnCode(Msg msg);

	boolean MessagePackData(Msg msg, String[] listStr);
}
