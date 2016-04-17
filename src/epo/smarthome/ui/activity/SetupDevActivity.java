package epo.smarthome.ui.activity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import epo.smarthome.app.R;
import epo.smarthome.data.dao.ConfigDao;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.ConfigService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * �����豸��
 * 
 * @author Administrator
 * 
 */
public class SetupDevActivity extends Activity {
	EditText txtName = null;
	EditText txtPassword = null;
	Button btnSetup;
	ListView listView;

	String name = "";
	String password = "";
	ConfigService db;
	private static final String TAG = "SetupDevActivity";

	static final int SETUP_SUCCEED = 0;
	static final int SETUP_ERROR = 1;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// btnSetup.setVisibility( View.VISIBLE );
			btnSetup.setEnabled(true);
			switch (msg.what) {
			case SETUP_SUCCEED:
				Toast.makeText(SetupDevActivity.this, "���óɹ���",
						Toast.LENGTH_SHORT).show();

				String str = txtName.getText().toString() + ":"
						+ txtPassword.getText().toString();
				db.SaveSysCfgByKey("wifipass", str);
				finish();
				break;
			case SETUP_ERROR:
				Toast.makeText(SetupDevActivity.this, "����ʧ�ܣ�",
						Toast.LENGTH_SHORT).show();

				break;

			default:
				break;

			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // ע��˳��
		setContentView(R.layout.activity_setup_dev);

		TextView title = (TextView) findViewById(R.id.titleSetup);
		title.setClickable(true);
		title.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}

		});

		txtName = (EditText) findViewById(R.id.setupDevTxtApName);
		txtPassword = (EditText) findViewById(R.id.setupDevTxtApPwd);
		btnSetup = (Button) findViewById(R.id.setupDevBtnSetup);
		btnSetup.setOnClickListener(new BtnSetupOnClickListener());
		db = new ConfigDao(this.getBaseContext());
		String str = db.getCfgByKey("wifipass");
		if (str.length() > 3) {
			String[] tmp = str.split(":");
			if (tmp.length == 2) {
				txtName.setText(tmp[0]);
				txtPassword.setText(tmp[1]);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setup_dev, menu);
		return true;
	}

	/**
	 * ���ü�����
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnSetupOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			// String
			// str=txtName.getText().toString()+":"+txtPassword.getText().toString();
			// db.SaveSysCfgByKey("wifipass", str);

			name = txtName.getText().toString();
			password = txtPassword.getText().toString();
			if (name.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "������·��������", 0).show();
				txtName.setFocusable(true);
				return;
			}
			if (password.trim().isEmpty()) {
				Toast.makeText(getApplicationContext(), "������·��������", 0).show();
				txtPassword.setFocusable(true);
				return;
			}
			Toast.makeText(getApplicationContext(), "��ʼ���ò�����", 0).show();
			btnSetup.setEnabled(false);
			// btnSetup.setVisibility( View.INVISIBLE );//INVISIBLE
			new SetupThread().start();
		}

	}

	/**
	 * �����߳�
	 * 
	 * @author Administrator
	 * 
	 */
	class SetupThread extends Thread {

		@Override
		public void run() {
			boolean isSetup = false;
			DatagramSocket dSocket = null;
			String msg = name + "," + password;
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);

			InetAddress local = null;
			try {
				local = InetAddress.getByName(Cfg.DEV_UDP_IPADDR); // ��������
				// local = InetAddress.getLocalHost(); // ��������
				System.out.println("local:" + local);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			try {
				dSocket = new DatagramSocket(Cfg.DEV_UDP_PORT); // ע��˴�Ҫ���������ļ�������Ȩ��,�������Ȩ�޲�����쳣
			} catch (SocketException e) {
				e.printStackTrace();
			}
			int msg_len = msg == null ? 0 : msg.getBytes().length;
			DatagramPacket dPacket = new DatagramPacket(msg.getBytes(),
					msg_len, local, Cfg.DEV_UDP_PORT);
			try {
				// ��������Ϊ�㲥
				dSocket.setBroadcast(true);
				dSocket.send(dPacket);
				dSocket.setSoTimeout(5000);
				// sleep(5000);
				dSocket.receive(dp);
				String strInfo = new String(dp.getData(), 0, dp.getLength());
				System.out.println(strInfo);
				if (strInfo.trim().toUpperCase().equals("OK")) {
					isSetup = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			dSocket.close();

			Message messageStart = new Message();
			if (isSetup) {
				messageStart.what = SETUP_SUCCEED;
			} else {
				messageStart.what = SETUP_ERROR;
			}
			handler.sendMessage(messageStart);

		}
	}

}
