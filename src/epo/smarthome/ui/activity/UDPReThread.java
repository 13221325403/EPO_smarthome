package epo.smarthome.ui.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.net.wifi.WifiManager;
import android.util.Log;

public class UDPReThread extends Thread {

	public Boolean IsThreadDisable = false;// ָʾ�����߳��Ƿ���ֹ
	private WifiManager.MulticastLock lock;
	InetAddress mInetAddress;

	public UDPReThread(WifiManager manager) {
		this.lock = manager.createMulticastLock("UDPwifi");
	}

	public class UdpHelper implements Runnable {
		public Boolean IsThreadDisable = false;// ָʾ�����߳��Ƿ���ֹ
		private WifiManager.MulticastLock lock;
		InetAddress mInetAddress;

		public UdpHelper() {

		}

		public UdpHelper(WifiManager manager) {
			this.lock = manager.createMulticastLock("UDPwifi");
		}

		public void StartListen() {
			// UDP�����������Ķ˿�
			Log.d("UDP Demo", "StartListen");
			Integer port = 2468;
			// ���յ��ֽڴ�С���ͻ��˷��͵����ݲ��ܳ��������С
			byte[] message = new byte[100];
			try {
				// ����Socket����
				DatagramSocket datagramSocket = new DatagramSocket(port);
				datagramSocket.setBroadcast(true);
				DatagramPacket datagramPacket = new DatagramPacket(message,
						message.length);
				try {
					while (!IsThreadDisable) {
						// ׼����������
						Log.d("UDP Demo", "׼������");
						this.lock.acquire();

						datagramSocket.receive(datagramPacket);
						String strMsg = new String(datagramPacket.getData())
								.trim();
						Log.d("UDP Demo", datagramPacket.getAddress()
								.getHostAddress().toString()
								+ ":" + strMsg);
						this.lock.release();
					}
				} catch (IOException e) {// IOException
					e.printStackTrace();
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}

		}

		public void send(String message) {
			message = (message == null ? "Hello IdeasAndroid!" : message);
			int server_port = 8904;
			Log.d("UDP Demo", "UDP��������:" + message);
			DatagramSocket s = null;
			try {
				s = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
			InetAddress local = null;
			try {
				local = InetAddress.getByName("255.255.255.255");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			int msg_length = message.length();
			byte[] messageByte = message.getBytes();
			DatagramPacket p = new DatagramPacket(messageByte, msg_length,
					local, server_port);
			try {

				s.send(p);
				s.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Log.d("UDP Demo", "run");
			StartListen();
		}
	}

}
