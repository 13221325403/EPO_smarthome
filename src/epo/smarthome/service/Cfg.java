package epo.smarthome.service;

import java.util.ArrayList;
import java.util.List;

import epo.smarthome.device.Dev;
import epo.smarthome.device.LedLight;
import android.app.Application;


/**
 * Ӧ�ó���ȫ�ֱ��� ��
 * @author Administrator
 *
 */
public class Cfg extends Application  {

	public final static String VERSION ="10001";
	

	public static String savePath="//sdcard//EPO/";

	
	//   �㲥��Ϣ����
	public final static String SendBoardCastName ="com.demo.smarthome.service.socketconnect";
	public final static String RegisterBoardCastName = "com.demo.smarthome.service.socketregister";
	public final static String LoginBoardCastName = "com.demo.smarthome.service.socketlogin";
	public final static String GetDevBoardCastName = "com.demo.smarthome.service.socketgetdev";
	public final static String DelDevBoardCastName = "com.demo.smarthome.service.socketdeletedev";
	
	public final static String DevJoinGroupBoardCastName = "com.demo.smarthome.service.socketdevjoingroup";
	public final static String ApplyGroupBoardCastName = "com.demo.smarthome.service.socketapplygroup";
	
	public final static String KEY_PASS_WORD ="password";
	public final static String KEY_USER_NAME ="username";
	
	//  ������ַ  cloud.ai-thinker.com
	//	cloud.ai-thinker.com
	//	admin
	//	admin_!@#*()
	//  ���Ե�ַ tangdengan.xicp.net
//	public final static String WEBSERVICE_SERVER_URL="http://182.139.160.79:8020/service/s.asmx";
//��	public final static String WEBSERVICE_SERVER_URL="http://cloud.ai-thinker.com/service/s.asmx";
//��	public final static String TCP_SERVER_URL="cloud.ai-thinker.com";
//	public final static String WEBSERVICE_SERVER_URL="http://tangdengan.xicp.net:8020/service/s.asmx";
//	public final static String TCP_SERVER_URL="tangdengan.xicp.net";
	
//	public final static String TCP_SERVER_URL="182.139.160.79";
//��	public final static int  TCP_SERVER_PORT=6009;
	
	public final static String WEBSERVICE_SERVER_URL="http://192.168.43.192:55151";	// ��ʱ����
	public final static String TCP_SERVER_URL="192.168.43.192";						// ��ʱ����
	public final static int  TCP_SERVER_PORT=2348;									// ��ʱ����
	
//	public final static String DEV_UDP_IPADDR="192.168.5.88"; //192.168.1.255
	public final static int  DEV_UDP_SEND_PORT=2468;
	public final static int  DEV_UDP_SEND_DELAY=100;
	public final static int  DEV_UDP_READ_DELAY=15; //udp ɨ��ȴ�15��

	public final static String DEV_UDP_IPADDR="192.168.4.1";
	public final static int  DEV_UDP_PORT=8001;
	
	public static byte[] userId= new byte[0];
	public static String userName="";
	public static long id = 0;
	public static byte[] passWd= new byte[0];
	public static String userPassword="";
	public static String torken = "";
	public static byte[] tcpTorken = new byte[0];
	

	public static boolean isLogin=false;
	public static boolean isSubmitDev=false;
	public static boolean isDeleteDev=false;

	public static boolean register=false;
	public  static String regUserName ="";
	public  static String regUserPass ="";
	
	public static List<Dev> listDev=new  ArrayList<Dev>();
	private static List<Dev> listDevScan=new  ArrayList<Dev>();//ɨ����豸

	public  static String deviceId ="";
	public  static String devicePwd ="";
	
	//  ***************   �豸��չ��������         *****************//
	public static List<LedLight> listLedLightDev=new  ArrayList<LedLight>();
	
	
//	public static final int timeDelayDef = 50; // ������ʱʱ�� ms
//	public static final int timeOutDef = 30 * (1000 / timeDelayDef); // ���ʱʱ��
//																		// 20��
//	public static final int timeReSendTimeDef = 5 * (1000 / timeDelayDef); // �����ط���ʱʱ��

	public static Dev getDevById(String id) {
		Dev dev = null;
		for (Dev d : listDev) {
			if (d.getMac().equals(id)) {
				dev = d;
				break;
			}
		}
		return dev;
	}
	
	public static Dev getDevScan() {
		Dev dev = null;
		for (Dev d : listDevScan) {
			dev = d;
			listDevScan.remove(d);
			break;
			
		}
		return dev;
	}
	public static void putDevScan(Dev dev) {
		if(dev == null){
			return;
		}
		for (Dev d : listDev) {
			if (d.getMac().equals(dev.getMac())) {
				return;
			}
		}
		for (Dev d : listDevScan) {
			if (d.getMac().equals(dev.getMac())) {
				return;
			}
		}
		listDevScan.add(dev);
		return ;
	}

	public static void devScanClean() {
		listDevScan.clear();
		return ;
	}

//***********    ����豸           ******************//
	public static LedLight getLedLightDevById(String id) {
		LedLight dev = null;
		for (LedLight d : listLedLightDev) {
			if (d.getMac().equals(id)) {
				dev = d;
				break;
			}
		}
		return dev;
	}

}
