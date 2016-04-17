package epo.smarthome.service;

/**
 * ���ݿⱣ���������Ϣ ����ӿ�
 * 
 * @author Administrator
 * 
 */
public interface ConfigService {
	public String getCfgByKey(String key);

	public boolean SaveSysCfgByKey(String key, String value);
}
