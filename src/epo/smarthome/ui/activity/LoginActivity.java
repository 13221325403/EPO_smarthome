package epo.smarthome.ui.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import epo.smarthome.app.R;
import epo.smarthome.data.dao.ConfigDao;
import epo.smarthome.device.Dev;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.ConfigService;
import epo.smarthome.service.HttpConnectService;
import epo.smarthome.service.SocketService;
import epo.smarthome.service.SocketService.SocketBinder;
import epo.smarthome.tools.MD5Tools;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登录类
 * 
 * @author Administrator
 * 
 */
public class LoginActivity extends Activity {
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		 if(Cfg.register){
		 try {
		 Thread.sleep(500);
		 } catch (InterruptedException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }
		 txtName.setText(Cfg.regUserName);
		 txtPassword.setText(Cfg.regUserPass);
		 name = txtName.getText().toString();
		 password = txtPassword.getText().toString();
		 
		 if((name != null) && (!name.isEmpty())){
			if((password != null) && (!password.isEmpty())){
				new LoginThread().start();
				}
			}
		 }
	}

	TextView title = null;

	EditText txtName = null;
	EditText txtPassword = null;

	String name = "";
	String password = "";
	boolean isLogin = false;
	private static final String TAG = "LoginActivity";
	ConfigService dbService;
	static final int LOGIN_SUCCEED = 0;
	static final int LOGIN_ERROR = 1;
	static final int GET_DEV_SUCCEED = 2;
	static final int GET_DEV_ERROR = 3;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();
			switch (msg.what) {
			case LOGIN_SUCCEED:
				isLogin = true;
				dbService.SaveSysCfgByKey(Cfg.KEY_USER_NAME, txtName.getText()
						.toString());
				dbService.SaveSysCfgByKey(Cfg.KEY_PASS_WORD, txtPassword
						.getText().toString());
				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT)
						.show();
				Cfg.userName = name;
				Cfg.userPassword = password;
				Cfg.passWd = password.getBytes();
				// 跳转到设置界面
//				Intent intent = new Intent();
//				intent.setClass(LoginActivity.this, MainActivity.class);
//				startActivity(intent);// 打开新界面
//				finish();
				
				
				
				Log.i(TAG, "成功登录");
				// 获取设备列表
				// new GetDevListThread().start();
				break;
			case LOGIN_ERROR:
				if (Cfg.register) {
					Cfg.register = false;
					new LoginThread().start();
					return;
				}
				Toast.makeText(LoginActivity.this, "登录失败！", Toast.LENGTH_SHORT)
						.show();
				// finish();
				break;

			case GET_DEV_SUCCEED:
				Toast.makeText(LoginActivity.this, "获取设备列表成功！",
						Toast.LENGTH_SHORT).show();
				// 跳转到设置界面
				// Intent intent = new Intent();
				// intent.setClass(LoginActivity.this, MainActivity.class);
				// startActivity(intent);// 打开新界面
				// finish();
				break;
			case GET_DEV_ERROR:
				Toast.makeText(LoginActivity.this, "获取设备列表失败！",
						Toast.LENGTH_SHORT).show();
				//
				break;
			default:
				break;

			}
		}

	};
	private UDPThread udphelper = null;
	private Thread tReceived = null;;

	SocketBinder socketBinder;
	SocketService socketService;
	boolean isBinderConnected = false;
	
	IntentFilter intentFilter = null;
	SocketIsConnectReceiver socketConnectReceiver = new SocketIsConnectReceiver();
	LoginBroadcast socketLoginReceiver = new LoginBroadcast();
	
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
		Intent intent = new Intent(LoginActivity.this, SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // 注意顺序
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_login);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);
		title = (TextView) findViewById(R.id.titlelogin);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		// bind....开启socket TCP连接
		bindService();	
		
		//  开启广播，监听服务器发送的注册响应消息
		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.SendBoardCastName);
		this.registerReceiver(socketConnectReceiver, intentFilter);
		intentFilter.addAction(Cfg.LoginBoardCastName);
		this.registerReceiver(socketLoginReceiver, intentFilter);
		
		txtName = (EditText) findViewById(R.id.loginTxtName);
		txtPassword = (EditText) findViewById(R.id.loginTxtPassword);

		Button btnOk = (Button) findViewById(R.id.loginBtnOk);
		btnOk.setOnClickListener(new BtnOkOnClickListener());
		Button btnReg = (Button) findViewById(R.id.loginBtnReg);
		btnReg.setOnClickListener(new BtnRegOnClickListener());
		
		Button btnSmartLink = (Button) findViewById(R.id.loginBtnSmartLink);
		btnSmartLink.setOnClickListener(new BtnSmartLinkOnClickListener());

		dbService = new ConfigDao(LoginActivity.this.getBaseContext());

		Intent intent = this.getIntent();
		String strUserName = intent.getStringExtra("userName");
		String strUserPass = intent.getStringExtra("userPass");

		if ((strUserName != null) && (!strUserName.isEmpty())) {
			if ((strUserName != null) && (!strUserPass.isEmpty())) {
				txtName.setText(strUserName);
				txtPassword.setText(strUserName);
				name = txtName.getText().toString();
				password = txtPassword.getText().toString();
				Log.i(TAG, "根据设备ID设置用户名");
			}
		} else {
			txtName.setText(dbService.getCfgByKey(Cfg.KEY_USER_NAME));
			txtPassword.setText(dbService.getCfgByKey(Cfg.KEY_PASS_WORD));
			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
		}		
		if ((name == null) || (name.isEmpty())) {
		// Toast.makeText(getApplicationContext(), "请输入用户名", 0).show();
			txtName.setFocusable(true);
			Log.i(TAG, "请输入用户名");
			return;
		}
		if ((password == null) || (password.isEmpty())) {
			// Toast.makeText(getApplicationContext(), "请输入密码", 0).show();
			txtPassword.setFocusable(true);
			Log.i(TAG, "请输入密码");
			return;
		}
		
		if((name != null) && (!name.isEmpty())){
			if((password != null) && (!password.isEmpty())){
				new LoginThread().start();
			}
		}
		Log.i(TAG, "Login初始化完成");	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * 登录按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnOkOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
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

			new LoginThread().start();

		}

	}

	/**
	 * 注册按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRegOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, RegisterActivity.class);// CaptureActivity
			Bundle bundle = new Bundle();
			bundle.putInt("type", 2);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}

	}

	
	class BtnSmartLinkOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(
					LoginActivity.this,
					com.espressif.iot.esptouch.demo_activity.EsptouchDemoActivity.class);
			startActivity(intent);

		}

	}

	/**
	 * 登录
	 * 
	 * @author Administrator
	 * 
	 */
	class LoginThread extends Thread {

		@Override
		public void run() {

			Log.v("LoginThread", "LoginThread start..");
			String md5Pass;
			if (password.length() >= 20) {
				md5Pass = password;
			} else {
				md5Pass = MD5Tools.string2MD5(password).toUpperCase();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//**************************    使用HTTP 协议登录        ************************//	
//			Message message = new Message();
//			message.what = LOGIN_ERROR;
//			String info = HttpConnectService.userLogin(name, (md5Pass));
//
//			if (info.length() > 10) {
//				String[] text = info.split(":");
//				int index = 0;
//				if (text.length == 4) {
//					index = 1;
//					Cfg.torken = text[index++];
//					Cfg.userId = StrTools.hexStringToBytes(StrTools
//							.strNumToBig(text[index++]));// id 要倒序
//					Cfg.passWd = StrTools.hexStringToBytes(StrTools
//							.strNumToHex(text[index++]));
//
//					Cfg.userName = name;
//					message.what = LOGIN_SUCCEED;
//					// Log.v("LoginThread", "Cfg.torken:" + Cfg.torken);
//					// Log.v("LoginThread", "Cfg.userId:" + Cfg.userId);
//					// Log.v("LoginThread", "Cfg.userName:" + Cfg.userName);
//					// Log.v("LoginThread", "Cfg.userId:" + Cfg.userId);
//					// Log.v("LoginThread", "Cfg.passWd:" + Cfg.passWd);
//				}
//			}
//			handler.sendMessage(message);
//**************************    使用TCP json 协议注册   *****************************//			
			//创建登录数据包：JSONObject对象regist_body
			JSONObject login_body = new JSONObject();
			 try {
				 
				 login_body.put("password", md5Pass);
				 login_body.put("name", name);
				 
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			//创建注册信息发送数据：JSONObject对象regist_json
			 JSONObject login_json = new JSONObject();
			 
			 Random login_nonce_rand =new Random();
			 int login_nonce = login_nonce_rand.nextInt();
			 
			 try {
				 login_json.put("nonce", login_nonce);
				 login_json.put("type", "PHONE_LOGIN");
				 login_json.put("login_body", login_body);
				//###  添加鉴权数据包
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "登录消息："+login_json.toString()); 
			socketService.socketSendMessage(login_json.toString());			
			
			
		}
	}

	/**
	 * 获得设备列表线程
	 * 
	 * @author Administrator
	 * 
	 */
	class GetDevListThread extends Thread {

		@Override
		public void run() {
			Message message = new Message();
			message.what = GET_DEV_ERROR;

			Log.v("GetDevListThread", "GetDevListThread start..");

			List<Dev> listDev = HttpConnectService.getDeviceList(Cfg.userName,
					new String(Cfg.torken));

			Cfg.listDev = listDev;
			for (Dev dev : listDev) {
				Log.v("GetDevListThread", "dev:" + dev);

			}
			if (listDev.size() > 0) {
				message.what = GET_DEV_SUCCEED;
			}
			handler.sendMessage(message);
		}
	}

	class UDPThread extends Thread {

		public String echo(String msg) {
			return " adn echo:" + msg;
		}

		public void run() {
			int port = 2468;
			// DatagramChannel channel = null;
			final int MAX_SIZE = 1024;

			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);

			InetAddress local = null;
			DatagramSocket socket = null;
			try {
				local = InetAddress.getByName("192.168.1.88"); // 本机测试
																// Cfg.DEV_UDP_IPADDR
				// local = InetAddress.getLocalHost(); // 本机测试
				System.out.println("local:" + local);
				socket = new DatagramSocket(null);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SocketAddress localAddr = new InetSocketAddress(port);
			try {
				socket.setReuseAddress(true);
				socket.bind(new InetSocketAddress(port));
				// socket.bind(localAddr);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("UDP服务器启动");

			ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_SIZE);

			String msg = "android. ";
			DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
					msg.length(), local, Cfg.DEV_UDP_PORT);
			try {
				socket.send(dPacket);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (true) {
				try {
					// 发送设置为广播
					socket.setBroadcast(true);
					// socket.send(dPacket);

					socket.receive(dp);
					String strInfo = new String(dp.getData(), 0, dp.getLength());
					System.out.println(strInfo);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class UDPReceiveThread extends Thread {
		public String echo(String msg) {
			return "and echo:" + msg;
		}

		public void run() {
			Log.v("UDPReceiveThread", "run start..");
			// 接收的字节大小，客户端发送的数据不能超过MAX_UDP_DATAGRAM_LEN
			byte[] lMsg = new byte[1024];
			// 实例化一个DatagramPacket类
			DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
			// 新建一个DatagramSocket类
			DatagramSocket ds = null;
			try {
				// UDP服务器监听的端口
				// 发送设置为广播
				// ds.setBroadcast(true);
				InetAddress local = null;
				try {
					local = InetAddress.getByName(Cfg.DEV_UDP_IPADDR); // 本机测试
					ds = new DatagramSocket(); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
					// local = InetAddress.getLocalHost(); // 本机测试
					System.out.println("local:" + local);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				String msg = "android hello.";
				int msg_len = msg == null ? 0 : msg.getBytes().length;
				DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
						msg_len, local, Cfg.DEV_UDP_PORT);
				ds.setBroadcast(true);
				ds.send(dPacket);

				Log.v("UDPReceiveThread", "run 1..");
				ds = new DatagramSocket(2468);
				sleep(2000);
				// 准备接收数据
				Log.v("UDPReceiveThread", "run 2..");
				ds.receive(dp);
				Log.v("UDPReceiveThread", "run 3..");
				String strInfo = new String(dp.getData(), 0, dp.getLength());
				System.out.println(strInfo);
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				// 如果ds对象不为空，则关闭ds对象
				if (ds != null) {
					ds.close();
				}
			}

			Log.v("UDPReceiveThread", "run end..");
		}
	}
	
	private class LoginBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(action.equals(Cfg.LoginBoardCastName)){		
				Message message = new Message();
				message.what = LOGIN_ERROR;
				if(intent.getStringExtra("login_flag").equals("success")){
					message.what = LOGIN_SUCCEED;
				}				
				handler.sendMessage(message);
			}
		}
		}

	private class SocketIsConnectReceiver extends BroadcastReceiver {// 继承自BroadcastReceiver的子类
		@Override
		public void onReceive(Context context, Intent intent) {// 重写onReceive方法

			if (intent.getBooleanExtra("conn", false)) {
				Log.i(TAG, "socket连接成功。");
			} else {
				Log.i(TAG, "socket连接失败。");
			}
		}
	}
}
