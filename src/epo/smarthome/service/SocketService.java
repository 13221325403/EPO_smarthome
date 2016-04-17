package epo.smarthome.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import org.json.*;

import epo.smarthome.data.dao.DevDao;
import epo.smarthome.device.Dev;
import epo.smarthome.device.Group;
import epo.smarthome.iprotocol.IProtocol;
import epo.smarthome.protocol.Buff;
import epo.smarthome.protocol.MSGCMD;
import epo.smarthome.protocol.MSGCMDTYPE;
import epo.smarthome.protocol.Msg;
import epo.smarthome.protocol.PlProtocol;
import epo.smarthome.tools.DateTools;
import epo.smarthome.tools.StrTools;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * Socket服务类
 * 
 * @author Administrator
 * 
 */
public class SocketService extends Service {

	// Dev dev = null;
	String ipAddr = "";
	int port = 0;
	int count;
	// boolean socketIsConnected = false;
	boolean isLogin = false;
	private Msg msg = new Msg();
	private byte[] sendStr;

	private static final String TAG = "SocketService";
	private SocketBinder socketBinder = new SocketBinder();

	public class SocketBinder extends Binder {
		public SocketService getService() {
			Log.i(TAG, "getService");
			return SocketService.this;
		}
	}

	Socket socket = null;
	boolean socketThreadIsRun = true;
	boolean socketReceiveThreadIsRun = true;
	byte[] buffer = new byte[1024];
	byte[] data = new byte[2048];
	int dataLength = 0;
	OutputStream socketOut = null;
	InputStream socketIn = null;

	IProtocol protocol = new PlProtocol();

	void putSocketData() {
		synchronized (this) {/* 区块 */

		}
	}

	void gutSocketData() {
		synchronized (this) {/* 区块 */

		}
	}

	int heartbeatCount = 0;
	int HeartbeatMax = 30;// 30秒发送心跳
	int noDataCount = 0;
	Thread socketHeartbeatThread = new Thread() {

		@Override
		public void run() {
			int heartbeatCount = 0;
			int HeartbeatMax = 30;// 0秒发送心跳
			while (socketThreadIsRun) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (socket != null) {
					if (isLogin) {
						heartbeatCount++;
						// Log.i(TAG, DateTools.getNowTimeString()
						// + "==>heartbeatCount:" + heartbeatCount
						// + "    HeartbeatMax:" + HeartbeatMax);
						if (heartbeatCount >= HeartbeatMax) {
							Log.i(TAG, DateTools.getNowTimeString()
									+ "==>heartbeatCoun  time out! socketOut:"
									+ socketOut);
							heartbeatCount = 0; // 心跳

							msg.setCmdType(MSGCMDTYPE.valueOf(0xA0));
							msg.setCmd(MSGCMD.valueOf(0x01));
							msg.setId(Cfg.userId);
							msg.setTorken(Cfg.torken);
							msg.setDataLen(0);
							protocol.MessageEnCode(msg);
							if (!(socketOut == null)) {
								// Log.i(TAG, "==发送 心跳指令！");
								sendStr = msg.getSendData();
								try {
									Log.i(TAG,
											DateTools.getNowTimeString()
													+ "==>发送 心跳指令！"
													+ StrTools
															.bytesToHexString(sendStr));
									socketOut.write(sendStr);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							HttpConnectService.heartThrob(Cfg.userName,
									Cfg.torken);

						}
						noDataCount++; //
						// Log.i(TAG, DateTools.getNowTimeString()
						// + "==>noDataCount:" + noDataCount
						// + "    HeartbeatMax * 3:" + (HeartbeatMax * 3));
						if (noDataCount >= HeartbeatMax * 3) { // socket无有效数据
							noDataCount = 0; //
							socketClose();
						}

					}

				}

			}
		}
	};

	public Thread socketThread = new Thread() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int len;

			while (socketThreadIsRun) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (socket == null) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if ("".equals(ipAddr)) {
						continue;
					}
					if (port < 1000) {
						continue;
					}
					try {
						socket = new Socket(ipAddr, port);
						// socket.set
						dataLength = 0;
						socketOut = socket.getOutputStream();
						socketIn = socket.getInputStream();

					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socketClose();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socketClose();
						continue;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						socketClose();
						continue;
					}
					SendBoardCast(true);
					Log.i(TAG, "==socketThread==ip:" + ipAddr + "  port:"
							+ port + " "
							+ ((socket == null) ? "conn error" : "conn ok"));

					} else {// socket connect...

					if (!isLogin) {// 发送登录指令
						if (!(socketOut == null)) {
						}
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					

				}

			}
		}

	};

	Thread socketreceiverThread = new Thread(){
		
		@Override
		public void run() {
			int length_receivedata;
			while (socketReceiveThreadIsRun) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(socket == null){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				try {
					Log.i(TAG, DateTools.getNowTimeString()
							+ "==read Data start！");
					length_receivedata = socketIn.read(buffer);
					Log.i(TAG, DateTools.getNowTimeString()
							+ "==read Data end！");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					length_receivedata = 0;
					socketClose();
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					length_receivedata = 0;
					socketClose();
					e.printStackTrace();
				}
				if (length_receivedata > 0) {
					Log.i(TAG, DateTools.getNowTimeString()
							+ "==code Data start！");
					
					noDataCount = 0;
					System.out.println("接收数据 长度:" + length_receivedata);
					
					
					System.arraycopy(buffer, 0, data, 0, length_receivedata);
//					dataLength = length_receivedata;
					
					String receivedata = new String(data);
//					System.out.println("接收数据:" + receivedata);
					Log.i(TAG,"接收数据:" + receivedata.toString());
					
					messageTypeProcess(receivedata);

				}
				

			}
		}
	};
	
	private	void messageTypeProcess(String receivedata){
		
		JSONObject jsonObj = null;
		String messageType = null;
		//转换成为JSONObject对象
		try {
			jsonObj = new JSONObject(receivedata.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			messageType = jsonObj.getString("type");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		switch(messageType){
		
		case "ping":
			//   生成心率数据包
			 JSONObject beacon_json = new JSONObject();
			 
			 Random beacon_json_nonce_rand =new Random();
			 int beacon_nonce = beacon_json_nonce_rand.nextInt();
			 
			 try {
				 beacon_json.put("nonce", beacon_nonce);
				 beacon_json.put("type", "pong");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 socketSendMessage(beacon_json.toString()); 
			return;
		
		case "regist_response":
			Intent intent=new Intent(Cfg.RegisterBoardCastName);
			try {
				intent.putExtra("regist_flag",jsonObj.getString("regist_flag"));
				Log.i(TAG,"注册信息:" + jsonObj.getString("regist_flag"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendBroadcast(intent);
			return;
		case "login_response":
			Intent intent1 =new Intent(Cfg.LoginBoardCastName);
			try {
				intent1.putExtra("login_flag",jsonObj.getString("login_flag"));
				Log.i(TAG,"登录信息:" + jsonObj.getString("login_flag"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendBroadcast(intent1);
			Log.i(TAG, "发送广播");
			return;
		case "getdevlist_response":
			Intent intent2 =new Intent(Cfg.GetDevBoardCastName);			
			Dev wifi_dev = new Dev();
			JSONObject devjson = null;			
			try {
				devjson = jsonObj.getJSONObject("device_body");
				wifi_dev.setMacAddress(devjson.getString("MAC"));
				wifi_dev.setNickName(devjson.getString("Name"));
				wifi_dev.setLastUpdate(devjson.getString("update"));
				wifi_dev.setTorken(devjson.getString("token"));
				wifi_dev.setIpPort(devjson.getString("IpPort"));
				wifi_dev.setOnLine(devjson.getInt("OnLine"));
				new DevDao(SocketService.this.getBaseContext()).saveDev(wifi_dev);
				intent2.putExtra("getdev_flag",jsonObj.getString("getdev_flag"));
				Log.i(TAG, "添加设备信息成功");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendBroadcast(intent2);
			return;
		case "deletedev_response":	
			Intent intent3 =new Intent(Cfg.DelDevBoardCastName);
			try {
				intent3.putExtra("deldev_flag",jsonObj.getString("deldev_flag"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendBroadcast(intent3);
			return;
		case "addDevToGroup_response":
			Intent intent4 =new Intent(Cfg.DevJoinGroupBoardCastName);
			try {
				intent4.putExtra("set_flag",jsonObj.getString("set_flag"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendBroadcast(intent4);
			return;			
		case "applygroup_response":
			Intent intent5 =new Intent(Cfg.ApplyGroupBoardCastName);
			JSONObject groupjson = null;
			try {
				groupjson = jsonObj.getJSONObject("group_body");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				intent5.putExtra("applygroup_flag",jsonObj.getString("applygroup_flag"));
				intent5.putExtra("groupID",groupjson.getInt("GROUPID"));
				intent5.putExtra("groupName",groupjson.getString("GROUPNAME"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "消息已发送");
			sendBroadcast(intent5);
			return;
		default:
			Log.i(TAG, "接收到未知信息");
			return;	
			
		}
			
		
	}
				
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(TAG, "=======onBind");
		return socketBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		// WifiManager manager = (WifiManager) this
		// .getSystemService(Context.WIFI_SERVICE);
		// UdpHelper udphelper = new UdpHelper(manager);
		//
		// //传递WifiManager对象，以便在UDPHelper类里面使用MulticastLoserver(SocketService.this);
		// tReceived = new Thread(udphelper);
		// tReceived.start();

		super.onCreate();

		ipAddr = Cfg.TCP_SERVER_URL;
		port = Cfg.TCP_SERVER_PORT;
		socketThread.start();
//		socketHeartbeatThread.start();
		socketreceiverThread.start();

		// new UDPThread().start();
		Log.i(TAG, "====onCreate");

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "xxxxxxxxxxxonDestroy");
		socketThreadIsRun = false;
		socketReceiveThreadIsRun = false;
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socketClose();
		super.onDestroy();
	}

	public void myMethod() {// String strIpAddr, int port
		Log.i(TAG, " myMethod");
		Log.i(TAG, "ip：" + ipAddr + "   port:" + port + "    strIpAddr:"
				+ ipAddr + "    port:" + port + "      socketIsConnected:");

	}

	public void myMethod(int args) {// String strIpAddr, int port
		Log.i(TAG, " myMethod args:" + args);
		Toast.makeText(SocketService.this, "正在执行", Toast.LENGTH_SHORT).show();
	}

	public void socketReConnect() {
		// this.dev = dev;
		// ipAddr = dev.getIpAddr().trim();
		// port = dev.getPort();
		socketClose();
	}

	public boolean socketIsConnected() {
		return (socket != null);
	}

	void socketClose() {
		try {
			if (socketOut != null) {
				socketOut.close();
			}
			if (socketIn != null) {
				socketIn.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// socketIsConnected = false;
		isLogin = false;
		socket = null;
		socketOut = null;
		socketIn = null;
		SendBoardCast(false);

	}

	public synchronized void socketSendMessage(String sendstring) {

		if (socket == null) {
			Log.i(TAG, "==socketSendMessage==xxx socket == null");
			return;
		}

		if (socketOut == null) {
			Log.i(TAG, "==socketSendMessage==xxx socketOut == null");
			return;
		}
		if (sendstring.equals(null)) {
			Log.i(TAG, "==socketSendMessage==xxx sendstring == null");
			return;
		}
		byte[] sendData = sendstring.getBytes();
		if (sendData == null) {
			return;	}
		
		try {
			socketOut.write(sendData);
			Log.i(TAG,
					"=发送数据=socketSendMessage:"+new String(sendData).toString());
			// StrTools.printHexString(sendData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socketClose();
		}

	}
	
	public synchronized void socketSendMessage(Msg msg) {

		if (socket == null) {
			Log.i(TAG, "==socketSendMessage==xxx socket == null");
			return;
		}

		if (socketOut == null) {
			Log.i(TAG, "==socketSendMessage==xxx socketOut == null");
			return;
		}
		if (msg == null) {
			Log.i(TAG, "==socketSendMessage==xxx msg == null");
			return;
		}
		byte[] sendData = msg.getSendData();
		if (sendData == null) {
			return;
		}
		try {
			socketOut.write(sendData);
			Log.i(TAG,
					"=发送数据=socketSendMessage== send ok hex:"
							+ StrTools.bytesToHexString(sendData));
			// StrTools.printHexString(sendData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socketClose();
		}

	}

	public synchronized void socketSendMessage(List<Msg> listMsg) {

		Log.i(TAG, "==socketSendMessage==listMsg:" + listMsg.size());

		for (Msg msg : listMsg) {
			if (socket == null) {
				Log.i(TAG, "==socketSendMessage==xxx socket == null");
				break;
			}

			if (socketOut == null) {
				Log.i(TAG, "==socketSendMessage==xxx socketOut == null");
				break;
			}
			if (msg == null) {
				Log.i(TAG, "==socketSendMessage==xxx msg == null");
				continue;
			}
			byte[] sendData = msg.getSendData();
			if (sendData == null) {
				continue;
			}
			try {
				socketOut.write(sendData);
				// Log.i(TAG,
				// "==socketSendMessage== send ok hex:"+StrTools.bytesToHexString(sendData));
				// StrTools.printHexString(sendData);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				socketClose();
			}
		}

	}

	private void SendBoardCast(boolean isConnect) {
		String str = " ip:" + ipAddr + ":" + port;
		Intent intent = new Intent();
		boolean connOk = false;
		intent.setAction(Cfg.SendBoardCastName);
		if (isConnect) {
			str += "   连接成功。";
			connOk = true;
		} else {
			str += "   连接断开。";
		}
		intent.putExtra("result", str);
		intent.putExtra("conn", connOk);
		SocketService.this.sendBroadcast(intent);
	}

	public class UDPThread extends Thread {
		String Hostip = "127.0.0.1";
		// String ip="127.0.0.1";
		int port = Cfg.DEV_UDP_SEND_PORT;

		public UDPThread() {

		}

		public String echo(String msg) {
			return " adn echo:" + msg;
		}

		public void run() {

			boolean isSetup = false;
			DatagramSocket dSocket = null;
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);

			InetAddress local = null;
			DatagramPacket dPacket;
			while (socketThreadIsRun) {
				try {
					sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					local = InetAddress.getByName(Hostip); // 本机测试
					// local = InetAddress.getLocalHost(); // 本机测试
					System.out.println("local:" + local);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.out.println("init localAddr error Hostip:" + Hostip);
					continue;
				}
				try {
					dSocket = new DatagramSocket(port); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
				} catch (SocketException e) {
					e.printStackTrace();
					System.out
							.println("init DatagramSocket error port:" + port);
					continue;
				}

				String localPort = dSocket.getLocalPort() + "";

				System.out.println("Hostip:" + Hostip + "   localPort:"
						+ localPort);

				String msg = "RPL:\"" + Hostip + "\",\"" + localPort + "\"";

				int msg_len = msg == null ? 0 : msg.getBytes().length;
				// DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
				// msg_len, local, port);

				// try {
				dPacket = new DatagramPacket(msg.getBytes(), msg_len, local,
						port);
				// } catch (OException e) {
				// Log.i(TAG, "====UDP init ok");
				// }

				Log.i(TAG, "====UDP init ok");
				break;
			}

			while (socketThreadIsRun) {
				try {

					dSocket.receive(dp);
					String strInfo = "";// dp.getSocketAddress().toString()+
										// " recvData:";
					strInfo = new String(dp.getData(), 0, dp.getLength());
					System.out.println(strInfo);
					String str = strInfo;
					String[] tmp = str.split(":");
					for (String s : tmp) {
						Log.i(TAG, "item1:" + s);
					}
					if (tmp.length >= 2) {
						str = tmp[0];
						if (!str.equals("RPT")) {
							continue;
						}
						str = tmp[1];
						tmp = str.split(",");
						for (String s : tmp) {
							Log.i(TAG, "item2:" + s);
						}
						if (tmp.length >= 5) {
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
							Dev d = new Dev();
							d.setMacAddress(StrTools.StrHexLowToLong(idStr) + "");
							d.setPass(StrTools.StrHexHighToLong(passStr) + "");
							Cfg.putDevScan(d);
							// Cfg.listDevScan.add(d);
							// deviceId = StrTools.StrHexLowToLong(idStr)+"";;
							// devicePwd =
							// StrTools.StrHexHighToLong(passStr)+"";;
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
