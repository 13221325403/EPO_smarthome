package epo.smarthome.ui.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import epo.smarthome.app.R;
import epo.smarthome.data.dao.DevDao;
import epo.smarthome.device.Dev;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.SocketService;
import epo.smarthome.service.SocketService.SocketBinder;
import epo.smarthome.tools.IpTools;
import epo.smarthome.tools.StrTools;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册类
 * 
 * @author Administrator
 * 
 */
public class RegisterActivity extends Activity {
	EditText txtName = null;
	EditText txtPassword = null;
	EditText txtMobile = null;
	EditText txtEmail = null;
	EditText txtDeviceId = null;
	EditText txtDevicePwd = null;
	Button btnScan;

	ProgressBar registerProgBarScanInfo;
	String name = "";
	String password = "";
	String mobile = "";
	String email = "";
	String deviceId = "";
	String devicePwd = "";

	String errprInfo = "";
	Dev dev = null;
	String info = "";
	boolean findDev = false;
	boolean isShow = false;
	private static final String TAG = "RegisterActivity";

	public static final int SCAN_CODE = 1;

	static final int REGISTER_SUCCEED = 0;
	static final int REGISTER_ERROR = 1;
	static final int FIND_DEVID = 2;
	static final int FIND_DEVPWD = 3;
	static final int NOT_FIND_DEVID = 4;

	static final int CMD_SUCCEEDT = 5;
	static final int CMD_TIMEOUT = 6;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case REGISTER_SUCCEED:
				Toast.makeText(RegisterActivity.this, "注册用户成功！",
						Toast.LENGTH_SHORT).show();
				new DevDao(RegisterActivity.this.getBaseContext()).saveDev(dev);

				Cfg.register = true;
				Cfg.regUserName = txtName.getText().toString();
				Cfg.regUserPass = txtPassword.getText().toString();

				// 跳转到设置界面
				Intent intent = new Intent();
				intent.setClass(RegisterActivity.this, LoginActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString("devId", txtName.getText().toString());
				bundle.putString("devPass", txtPassword.getText().toString());
				intent.putExtras(bundle);

//				Log.i(TAG, "ItemClickListener dev：" + dev.getMac());
				startActivity(intent);// 打开新界面
				finish();
				break;
			case FIND_DEVID:

				btnScan.setEnabled(true);
				if (findDev) {
					break;
				}
				registerProgBarScanInfo.setVisibility(View.INVISIBLE);
				findDev = true;
				Toast.makeText(RegisterActivity.this, "扫描设备成功！",
						Toast.LENGTH_SHORT).show();

				// txtDeviceId.setText(deviceId);
				txtDeviceId.setText(deviceId);
				txtDevicePwd.setText(devicePwd);
				dev = new Dev();
				dev.setMacAddress(deviceId);
				dev.setNickName(deviceId);
				dev.setPass(devicePwd);

				break;
			case FIND_DEVPWD:
				txtDevicePwd.setText(devicePwd);
				break;
			case NOT_FIND_DEVID:
				// txtDevicePwd.setText(devicePwd);
				break;

			case CMD_SUCCEEDT:
				btnScan.setEnabled(true);
				registerProgBarScanInfo.setVisibility(View.INVISIBLE);
				// txtDevicePwd.setText(devicePwd);
				break;
			case CMD_TIMEOUT:
				btnScan.setEnabled(true);
				registerProgBarScanInfo.setVisibility(View.INVISIBLE);
				// txtDevicePwd.setText(devicePwd);
				break;
			case REGISTER_ERROR:
				Toast.makeText(RegisterActivity.this, "注册用户失败！" + errprInfo,
						Toast.LENGTH_SHORT).show();
				break;

			default:
				break;

			}
		}

	};
	SocketBinder socketBinder;
	SocketService socketService;
	boolean isBinderConnected = false;

	IntentFilter intentFilter = null;
	SocketIsConnectReceiver socketConnectReceiver = new SocketIsConnectReceiver();
	RegisterBroadcast socketRegisterReceiver = new RegisterBroadcast();

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.i(TAG, "=============onServiceConnected");
			socketBinder = (SocketBinder) service;
			socketService = socketBinder.getService();
			socketService.myMethod();

			isBinderConnected = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxonServiceDisconnected");
			isBinderConnected = false;
			socketBinder = null;
			socketService = null;
		}

	};

	private void bindService() {
		Intent intent = new Intent(RegisterActivity.this, SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序

		setContentView(R.layout.activity_register);

		TextView title = (TextView) findViewById(R.id.titleRegister);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 跳转到设置界面
				Intent intent = new Intent();
				intent.setClass(RegisterActivity.this, LoginActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("type", 3);
				intent.putExtras(bundle);
				startActivity(intent);// 打开新界面
				finish();
			}

		});

		txtName = (EditText) findViewById(R.id.registerTxtName);
		txtPassword = (EditText) findViewById(R.id.registerTxtPassword);
		txtMobile = (EditText) findViewById(R.id.registerTxtMobile);
		txtEmail = (EditText) findViewById(R.id.registerTxtEemail);
		txtDeviceId = (EditText) findViewById(R.id.registerTxtDeviceId);
		txtDevicePwd = (EditText) findViewById(R.id.registerTxtDevicePwd);

		Button btnSetup = (Button) findViewById(R.id.registerBtnReg);
		btnSetup.setOnClickListener(new BtnRegOnClickListener());

		btnScan = (Button) findViewById(R.id.registerBtnScan);
		btnScan.setOnClickListener(new BtnScanDevOnClickListener());

		registerProgBarScanInfo = (ProgressBar) findViewById(R.id.registerProgBarScanInfo);
		registerProgBarScanInfo.setVisibility(View.INVISIBLE);

		// bind....
		bindService();
		/*
			 *  */
		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.SendBoardCastName);
		this.registerReceiver(socketConnectReceiver, intentFilter);
		
		//  开启广播，监听服务器发送的注册响应消息
		
		intentFilter.addAction(Cfg.RegisterBoardCastName);
		this.registerReceiver(socketRegisterReceiver, intentFilter);
		
		// registerProgBarScanInfo.setVisibility(View.INVISIBLE);
		// byte[] b = info.getBytes();
		// StrTools.bytesToHexString(b);
		// Log.v("RegisterActivity onCreate",
		// "二维码信息:" + StrTools.bytesToHexString(b));
		// if (info != null) {
		//
		// if (info.length() >= 5) {
		// String[] text = info.split(",");
		// int index = 0;
		// Log.v("RegisterActivity onCreate", "二维码信息:" + info);
		// if (text.length >= 2) {
		// deviceId = text[index++].trim();
		// devicePwd = text[index++].trim();
		// txtDeviceId.setText(deviceId);
		// txtDevicePwd.setText(devicePwd);
		//
		// }
		// }
		// }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	/**
	 * 注册 按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
			mobile = txtMobile.getText().toString();
			email = txtEmail.getText().toString();
			deviceId = txtDeviceId.getText().toString();
			devicePwd = txtDevicePwd.getText().toString();
			if (name.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入用户名", 0).show();
				txtName.setFocusable(true);
				return;
			}
			if (password.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入密码", 0).show();
				txtPassword.setFocusable(true);
				return;
			}
			if (mobile.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入手机号码", 0).show();
				txtMobile.setFocusable(true);
				return;
			}
			if (email.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入邮箱地址", 0).show();
				txtEmail.setFocusable(true);
				return;
			}
			if (deviceId.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入设备ID", 0).show();
				txtDeviceId.setFocusable(true);
				return;
			}
			if (devicePwd.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "请输入设备密码", 0).show();
				txtDevicePwd.setFocusable(true);
				return;
			}
			// new RegBySocketConnectThread().start();
			new RegisterUserThread().start();

		}

	}

	/**
	 * 扫描 按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnScanDevOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// Cfg.listDevScan.clear();

			findDev = false;
			registerProgBarScanInfo.setVisibility(View.VISIBLE);
			Cfg.devScanClean();
			String ip = IpTools
					.getIp((WifiManager) getSystemService(Context.WIFI_SERVICE));
			if (ip.length() < 4) {
				ip = "192.168.1.255";
			}
			btnScan.setEnabled(false);
			Toast.makeText(getApplicationContext(), "开始扫描...", 0).show();

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

	/**
	 * 注册用户 线程
	 * 
	 * @author Administrator
	 * 
	 */
	class RegisterUserThread extends Thread {
		@Override
		public void run() {
			
			String[] text;
			errprInfo = "";

			Log.v("RegisterUserThread", "name：" + name + "  password:"
					+ password + "  mobile:" + mobile + "  email:" + email
					+ "  deviceId:" + deviceId + "  devicePwd:" + devicePwd);

//**************************    使用HTTP 协议注册   *****************************//			
//			String result = HttpConnectService.registUser(name, password,
//					mobile, email, deviceId, devicePwd);
//			if (result.length() > 10) {
//			Log.v("RegisterUserThread", "info.length()：" + result.length());
//			text = result.split(":");
//			int index = 0;
//			if (text.length == 3) {
//				index = 1;
//				// Cfg.torken = text[index++];
//				Cfg.userId = StrTools.hexStringToBytes(StrTools
//						.strNumToBig(text[index++]));// id 要倒序
//				Cfg.passWd = StrTools.hexStringToBytes(StrTools
//						.strNumToHex(text[index++]));
//
//				Cfg.userName = name;
//				message.what = REGISTER_SUCCEED;
//
//			} else if (text.length == 2) {
//				errprInfo = text[1];
//			}
//		}
			

//**************************    使用TCP json 协议注册   *****************************//			
			//创建注册数据包：JSONObject对象regist_body
			JSONObject regist_body = new JSONObject();
			 try {
				 regist_body.put("name", name);
				 regist_body.put("password", password);
				 regist_body.put("mobile", mobile);
				 regist_body.put("email", email);
				 regist_body.put("deviceId", deviceId);
				 regist_body.put("devicePwd", devicePwd);	
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			//创建注册信息发送数据：JSONObject对象regist_json
			 JSONObject regist_json = new JSONObject();
			 
			 Random regist_nonce_rand =new Random();
			 int regist_nonce = regist_nonce_rand.nextInt();
			 
			 try {
				regist_json.put("nonce", regist_nonce);
				regist_json.put("type", "PHONE_REGIST");
				regist_json.put("regist_body", regist_body);
				//###  添加鉴权数据包
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			socketService.socketSendMessage(regist_json.toString());
			

//			handler.sendMessage(message);
		}

	}

	/**
	 * 查找设备线程
	 * 
	 * @author Administrator
	 * 
	 */
	class FindDevValThread extends Thread {

		@Override
		public void run() {

			// int delay = 50;
			// int reSendTime = 1;
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!Cfg.deviceId.isEmpty()) {
					deviceId = Cfg.deviceId;
					Cfg.deviceId = "";
					Message message = new Message();
					message.what = FIND_DEVID;
					handler.sendMessage(message);
				}
				if (!Cfg.devicePwd.isEmpty()) {
					devicePwd = Cfg.devicePwd;
					Cfg.devicePwd = "";
					Message message = new Message();
					message.what = FIND_DEVPWD;
					handler.sendMessage(message);
				}
			}
		}

	}

	/**
	 * 拍照结果回调
	 * 
	 * @author Administrator
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case SCAN_CODE:

			if (info != null) {
				if (info.length() >= 5) {
					String[] text = info.split(",");
					int index = 0;
					Log.v("onCreate", "二维码信息:" + info);
					if (text.length >= 3) {
						deviceId = text[index++].trim();
						devicePwd = text[index++].trim();
						txtDeviceId.setText(deviceId);
						txtDevicePwd.setText(devicePwd);
					}
				}
			}
			break;
		default:
			break;
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
			String msg = name + "," + password;
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);

			InetAddress local = null;
			try {
				local = InetAddress.getByName(ip); // 本机测试
				// local = InetAddress.getLocalHost(); // 本机测试
				System.out.println("local:" + local);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			try {
				dSocket = new DatagramSocket(); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
			} catch (SocketException e) {
				e.printStackTrace();
			}

			String localPort = dSocket.getLocalPort() + "";

			System.out.println("Hostip:" + Hostip + "  ip:" + ip
					+ "   localPort:" + localPort);

			msg = "RPL:\"" + Hostip + "\",\"" + localPort + "\"";

			int msg_len = msg == null ? 0 : msg.getBytes().length;
			DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
					msg_len, local, port);
			// String addr="";

			// addr = dPacket.getAddress().toString();

			// dPacket.getPort();
			// System.out.println("local:"+local);
			// dPacket.setData();
			try {

				// 发送设置为广播
				dSocket.setBroadcast(true);
				dSocket.setSoTimeout(10000);
				dSocket.send(dPacket);
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
						if (findDev) {
							return;
						}
						deviceId = StrTools.StrHexLowToLong(idStr) + "";
						;
						devicePwd = StrTools.StrHexHighToLong(passStr) + "";
						;
						// txtDeviceId.setText(idStr );
						// txtDevicePwd.setText(passStr );
						Message message = new Message();
						message.what = FIND_DEVID;
						handler.sendMessage(message);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			dSocket.close();

		}
	}

	private class RegisterBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Cfg.RegisterBoardCastName)){
				Message message = new Message();
				message.what = REGISTER_ERROR;
				if(intent.getStringExtra("regist_flag").equals("success")){
					message.what = REGISTER_SUCCEED;
				}				
				handler.sendMessage(message);
			}
		}
		}
	
	private class SocketIsConnectReceiver extends BroadcastReceiver {// 继承自BroadcastReceiver的子类
		@Override
		public void onReceive(Context context, Intent intent) {// 重写onReceive方法
			String action = intent.getAction();
			if(action.equals(Cfg.SendBoardCastName)){
			if (intent.getBooleanExtra("conn", false)) {
				Log.i(TAG, "socket连接成功。");
			} else {
				Log.i(TAG, "socket连接失败。");
			}
			}
		}
	}

}

