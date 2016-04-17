package epo.smarthome.ui.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import epo.smarthome.app.R;
import epo.smarthome.device.Dev;
import epo.smarthome.iprotocol.IProtocol;
import epo.smarthome.protocol.Msg;
import epo.smarthome.protocol.PlProtocol;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.SocketService;
import epo.smarthome.service.SocketService.SocketBinder;
import epo.smarthome.tools.IpTools;
import epo.smarthome.tools.StrTools;
import epo.smarthome.ui.TitleActivity;
import epo.smarthome.protocol.MSGCMDTYPE;
import epo.smarthome.protocol.MSGCMD;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 扫描类
 * 
 * @author Administrator
 * 
 */
public class ScanActivity extends TitleActivity {

	// ui
	// TextView txtViewInfo; // 二维码信息： 型号 编号 功率 斗容 生产日期
	TextView txtViewType; // 型号
	TextView txtViewId; // 编号
	TextView txtViewPasswd; // 功率

	// data
	// Record record = new Record();
	Dev dev;
	String deviceId = "567";
	String devicePwd = "";
	String info = "";
	String type = "";
	String devId = "1234";
	String passwd = "";

	Button btnScan;

	ProgressBar scanProgBarScanInfo;
	// Button btnSubmit;
	IProtocol protocol = new PlProtocol();
	Msg msg = new Msg();
	List<Dev> listDev = new ArrayList<Dev>();

	boolean findDev = false;
	static final int SUBMIT_START = 0;
	static final int SUBMIT_SUCCEED = 1;
	static final int SUBMIT_ERROR = 2;
	static final int FIND_DEVID = 3;
	static final int FIND_ERROR = 4;
	static final int START_SUBMIT = 5;
	static final int TIME_OUT = 6;
	static final int CMD_SUCCEEDT = 7;
	static final int CMD_TIMEOUT = 8;
	public static final String TAG = "ScanActivity";

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();

			uiEnable(true);

			switch (msg.what) {
			case SUBMIT_START:
				Toast.makeText(ScanActivity.this, "开始提交数据:"+msg.obj, Toast.LENGTH_SHORT)
						.show();

				Log.v("ScanActivity", "开始提交数据！");
				break;
			case SUBMIT_SUCCEED:
				Toast.makeText(ScanActivity.this, "提交数据成功:"+msg.obj, Toast.LENGTH_SHORT)
						.show();

				Log.v("ScanActivity", "提交数据成功！");
				// new DevDao(ScanActivity.this.getBaseContext()).saveDev(dev);
				// finish();
				break;
			case SUBMIT_ERROR:
				 Toast.makeText(ScanActivity.this, "提交数据失败:"+msg.obj,
				 Toast.LENGTH_SHORT)
				 .show();
				// finish();
				break;
			case START_SUBMIT:
				 Toast.makeText(ScanActivity.this, "开始提交数据："+msg.obj,
				 Toast.LENGTH_SHORT)
				 .show();
				// finish();
				break;
			case TIME_OUT:
				uiEnable(true);
				// Toast.makeText(ScanActivity.this, "提交数据失败！",
				// Toast.LENGTH_SHORT)
				// .show();
				// finish();
				break;
			case FIND_ERROR:
				Toast.makeText(ScanActivity.this, msg.obj+" 设备已经存在。", Toast.LENGTH_SHORT)
				.show();

				break;
			case FIND_DEVID:
				uiEnable(true);
				if (deviceId.isEmpty()) {
					return;
				}

				txtViewId.setText(deviceId); // 编号
				txtViewPasswd.setText(devicePwd); // 功率
				Toast.makeText(ScanActivity.this, "扫描设备成功！"+msg.obj, Toast.LENGTH_SHORT)
						.show();

				break;
			case CMD_SUCCEEDT:
				uiEnable(true);
				// btnScan.setEnabled(true);
				// registerProgBarScanInfo.setVisibility(View.INVISIBLE);
				// txtDevicePwd.setText(devicePwd);
				break;
			case CMD_TIMEOUT:
				uiEnable(true);
				// btnScan.setEnabled(true);
				// registerProgBarScanInfo.setVisibility(View.INVISIBLE);
				// txtDevicePwd.setText(devicePwd);
				break;
			default:
				break;

			}
		}

	};

	private void uiEnable(boolean b) {
		// TODO Auto-generated method stub

		btnScan.setEnabled(b);
		if (b) {
			scanProgBarScanInfo.setVisibility(View.INVISIBLE);
		} else {
			scanProgBarScanInfo.setVisibility(View.VISIBLE);
		}
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_scan);
		setTitle(R.string.text_add_device);
        showBackwardView(R.string.button_backward, true);
		
		uiInit();
		dataInit();

		new TimeThread().start();

	}

    @Override
    protected void onBackward(View backwardView) {
        super.onBackward(backwardView);
    }

	private void dataInit() {
	}

	private void dataToUi() {
		// TODO Auto-generated method stub
		// / txtViewInfo.setText(info); // 二维码信息
		txtViewType.setText(type); // 型号
		txtViewId.setText(devId); // 编号
		txtViewPasswd.setText(passwd); // 功率
	}

	private void uiInit() {
		// TODO Auto-generated method stub

		// txtViewInfo = (TextView) this.findViewById(R.id.scanLblInfo);// 二维码信息
		txtViewType = (TextView) this.findViewById(R.id.scanLblType);// 型号
		txtViewId = (TextView) this.findViewById(R.id.scanLblId);// 编号
		txtViewPasswd = (TextView) this.findViewById(R.id.scanLblPassWd);// 功率

		// btnSubmit = (Button) this.findViewById(R.id.scanBtnSave);// 功率
		// btnSubmit.setOnClickListener(new BtnSubmitOnClickListener());
		btnScan = (Button) this.findViewById(R.id.scanBtnScan);// 功率
		btnScan.setOnClickListener(new BtnScanOnClickListener());
		// btnSubmit.setEnabled(false);
		scanProgBarScanInfo = (ProgressBar) findViewById(R.id.scanProgBarScanInfo);
		scanProgBarScanInfo.setVisibility(View.INVISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}

	/**
	 * 提交按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnScanOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// new SubmitThread().start();
			String ip = IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE));
			if (ip.length() < 4) {
				ip = "192.168.1.255";
			}
			// Cfg.listDevScan.clear();
			Cfg.devScanClean();
			Toast.makeText(getApplicationContext(), "开始扫描...", 0).show();
			// new UDPThread(ip,88).start();
			// new UDPThread(ip,102).start();
			findDev = false;

			uiEnable(false);
			// scanProgBarScanInfo.setVisibility(View.VISIBLE);
			new StartUDPThread(ip).start();

		}

	}

	class StartUDPThread extends Thread {
		String ip = "";

		public StartUDPThread(String ip) {
			this.ip = ip;
		}

		public void run() {
			for (int i = 1; i < 255; i++) {

				if (findDev) {
					return;
				}
				new UDPThread(ip, i).start();
				try {
					Thread.sleep(Cfg.DEV_UDP_SEND_DELAY);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (findDev) {
				return;
			}
			Message message = new Message();
			message.what = CMD_TIMEOUT;
			handler.sendMessage(message);

		}

	}

	class BtnSubmitOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (txtViewId.getText().toString().isEmpty()) {
				return;
			}

			if (txtViewPasswd.getText().toString().isEmpty()) {
				return;
			}

			Log.v("ScanActivity", "有提交数据  准备提交");
			new SubmitThread().start();
		}

	}

	/**
	 * 提交线程
	 * 
	 * @author Administrator
	 * 
	 */
	class SubmitThread extends Thread {

		@Override
		public void run() {
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			if (listDev.size() <= 0) {
				return;
			}
			Dev d = listDev.get(0);
			listDev.remove(d);
			if (d == null) {
				return;
			}
			Message message = new Message();
			message.what = SUBMIT_ERROR;
			
			
			Log.v("SendLigheStateThread", "开始提交数据 start..");
			int delay = 50;
			int reSendTime = 1;
			Cfg.isSubmitDev = false;

			String strId = d.getMac();
			if (StrTools.stringToInt(strId.substring(0, 1)) == 0) {
				devId = strId.substring(1);
			} else {
				devId = strId;
			}

			message.obj=devId;
			passwd = d.getPass();
			byte[] data = new byte[16];
			int index = 0;
			long val = 0;

			// id 低位在前
			val = StrTools.stringToInt(devId);
			for (int i = 0; i < 8; i++) {
				data[index++] = (byte) (val % 256);
				val /= 256;
			}
			// pass 高位在前
			byte[] b = new byte[8];
			val = StrTools.stringToInt(passwd);
			for (int i = 0; i < 8; i++) {
				b[i] = (byte) (val % 256);
				val /= 256;
			}
			byte[] buff = StrTools.byteToSwapByte(b);
			for (int i = 0; i < 8; i++) {
				// if(buff[i] != 0){
				data[index++] = buff[i];
				// }
			}
			msg.setId(Cfg.userId);
			msg.setCmdType(MSGCMDTYPE.valueOf((byte) 0xEF));
			msg.setCmd(MSGCMD.valueOf((byte) 0x06));
			msg.setTorken(Cfg.tcpTorken);
			msg.setData(data);
			msg.setDataLen(data.length);
			protocol.MessageEnCode(msg);

			Log.i(TAG, "         data:" + StrTools.bytesToHexString(data));
			Log.i(TAG,
					"sendData:" + StrTools.bytesToHexString(msg.getSendData()));
			Message handlermsg = new Message();
			handlermsg.obj=devId;
			handlermsg.what = SUBMIT_START;
			handler.sendMessage(handlermsg);
			while (true) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reSendTime--;
				if (reSendTime <= 0) {// send
					reSendTime = 10;

					StartActivity.socketService.socketSendMessage(msg);//

				}
				// 查询
				if (Cfg.isSubmitDev) {
					message.what = SUBMIT_SUCCEED;
					handler.sendMessage(message);
					break;
				}

				delay--;
				if (delay <= 0) {
					message.what = SUBMIT_ERROR;
					handler.sendMessage(message);
					break;
				}
			}

		}
	}
	
	class TimeThread extends Thread {
		public void run() {
			while (true) {

				// Log.v("TimeThread", "run...");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (listDev.size() <= 0) {
					// Log.v("TimeThread", "not data." );
					continue;
				}
				Log.v("TimeThread", "有提交数据  准备提交");
				new SubmitThread().start();
			}

		}

	}
	
	class UDPThread extends Thread {
		String Hostip = "";
		String ip = "";
		int port = Cfg.DEV_UDP_SEND_PORT;

		public UDPThread(String ipStr, int i) {

			this.Hostip = ipStr;
			byte[] addr = IpTools.getIpV4Byte(ipStr);
			if (addr.length == 4) {
				addr[3] = (byte) (i);
				ip = IpTools.getIpV4StringByByte(addr, 0);
			}
		}

		public String echo(String msg) {
			return " adn echo:" + msg;
		}

		public void run() {

			boolean isSetup = false;
			DatagramSocket dSocket = null;
			String msg = "";
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);

			InetAddress local = null;
			try {
				local = InetAddress.getByName(ip); // 本机测试
				// local = InetAddress.getLocalHost(); // 本机测试
				// System.out.println("local:" + local);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			try {
				dSocket = new DatagramSocket(); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
			} catch (SocketException e) {
				e.printStackTrace();
			}

			String localPort = dSocket.getLocalPort() + "";

			// System.out.println("Hostip:" + Hostip + "  ip:" + ip
			// + "   localPort:" + localPort);

			msg = "RPL:\"" + Hostip + "\",\"" + localPort + "\"";

			int msg_len = msg == null ? 0 : msg.getBytes().length;
			DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
					msg_len, local, port);

			try {

				// 发送设置为广播
				dSocket.setBroadcast(true);
				dSocket.send(dPacket);
				dSocket.setSoTimeout(10000);
				dSocket.receive(dp);
				String strInfo = new String(dp.getData(), 0, dp.getLength());
				System.out.println(strInfo);
				String str = strInfo;
				String[] tmp = str.split(":");
				for (String s : tmp) {
					Log.i(TAG, "item1:" + s);
				}
				if (tmp.length >= 2) {
					str = tmp[1];
					tmp = str.split(",");
					for (String s : tmp) {
						Log.i(TAG, "item2:" + s);
					}
					if (tmp.length >= 2) {
						Log.i(TAG, "tmp[0]:" + tmp[0]);
						Log.i(TAG, "tmp[1]:" + tmp[1]);
						String idStr = tmp[0].replace('"', ' ');
						String passStr = tmp[1].replace('"', ' ');
						Log.i(TAG, "idStr:" + idStr);
						Log.i(TAG, "pasStrs:" + passStr);
						StrTools.StrHexLowToLong(idStr);
						StrTools.StrHexLowToLong(passStr);
						StrTools.StrHexHighToLong(idStr);
						StrTools.StrHexHighToLong(passStr);
						// int id = Int
						deviceId = StrTools.StrHexLowToLong(idStr) + "";
						;
						devicePwd = StrTools.StrHexHighToLong(passStr) + "";
						;
						Dev d = Cfg.getDevById(deviceId);
						Message message = new Message();
						if(d != null){
							message.what = FIND_ERROR;
							message.obj=deviceId;
							handler.sendMessage(message);	
						}else{
							// txtDeviceId.setText(idStr );
							// txtDevicePwd.setText(passStr );
							Dev dev = new Dev();
							dev.setMacAddress(deviceId);
							dev.setPass(devicePwd);
							listDev.add(dev);
							Log.v("ScanActivity", "准备提交数据  deviceId：" + deviceId
									+ "   devicePwd:" + devicePwd);
							message.obj=deviceId;
							message.what = FIND_DEVID;
							handler.sendMessage(message);
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			dSocket.close();

		}
	}
	
    /* (non-Javadoc)
     * @see app.ui.TitleActivity#onBackward(android.view.View)
     */


}