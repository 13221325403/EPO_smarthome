package com.espressif.iot.esptouch.demo_activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.udp.UDPSocketClient;
import com.espressif.iot.esptouch.util.ByteUtil;

import epo.smarthome.app.R;
import epo.smarthome.data.dao.DevDao;
import epo.smarthome.device.Dev;
import epo.smarthome.service.DevService;
import epo.smarthome.ui.TitleActivity;
import epo.smarthome.ui.activity.SetupDevActivity;
import epo.smarthome.userconfig.IConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class EsptouchDemoActivity extends TitleActivity implements IConstants{

	private static final String TAG = "EsptouchDemoActivity";

	private ImageView mImageWifiRssiDisplay;
	
	private TextView mTvApSsid;

	private EditText mEdtApPassword;

	private Button mBtnConfirm;
	
	private Switch mSwitchIsSsidHidden;
	
	private ImageView mImageHiddenSsid;

	private EspWifiAdminSimple mWifiAdmin;
	
	Dev wifi_dev = null;
	
	DevService  devService;
	
	private Handler   mHandler; 
	
	String message = "";
	byte[] messageByte_send = null;
	
	static final int ESPtouch_SUCCEED = 0;
	static final int ESPtouch_ERROR = 1;

	Handler esptouch_handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
					
			switch (msg.what) {
			case ESPtouch_SUCCEED:
				Toast.makeText(EsptouchDemoActivity.this, "设置成功！",
						Toast.LENGTH_SHORT).show();
				break;
			case ESPtouch_ERROR:
				Toast.makeText(EsptouchDemoActivity.this, "设置失败！",
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
//		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.esptouch_demo_activity);
		setTitle(R.string.smartconfig);
        showBackwardView(R.string.button_backward, true);
        
        
		mImageWifiRssiDisplay = (ImageView) findViewById(R.id.imageWifiRssiDisplay);
		mWifiAdmin = new EspWifiAdminSimple(this);
		mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);
		mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
		
		mSwitchIsSsidHidden = (Switch) findViewById(R.id.switchIsSsidHidden);
		mSwitchIsSsidHidden.setOnClickListener(new switchbthOnClickListener());
		
		mImageHiddenSsid = (ImageView) findViewById(R.id.imaghiddenSsid);
		
		mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
		mBtnConfirm.setOnClickListener(new BtnConfirmOnClickListener());
		
		new UDPSocketClient();
		wifi_dev = new Dev();
		devService = new DevDao(EsptouchDemoActivity.this.getBaseContext());
		
		Button btnSetup = (Button) findViewById(R.id.devrouteset);
		btnSetup.setOnClickListener(new BtnSetupOnClickListener());
		
		mHandler = new Handler(); 
	    mHandler.post(new TimerProcess());

	}
    /* (non-Javadoc)
     * @see app.ui.TitleActivity#onBackward(android.view.View)
     */
    @Override
    protected void onBackward(View backwardView) {
        super.onBackward(backwardView);
    }

	private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {

		private ProgressDialog mProgressDialog;

		private IEsptouchTask mEsptouchTask;

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog
					.setMessage("设备正在配置，请稍后...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (__IEsptouchTask.DEBUG) {
						Log.i(TAG, "progress dialog is canceled");
					}
					if (mEsptouchTask != null) {
						mEsptouchTask.interrupt();
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
					"正在执行,请稍后...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}

		@Override
		protected IEsptouchResult doInBackground(String... params) {
			String apSsid = params[0];
			String apBssid = params[1];
			String apPassword = params[2];
			String isSsidHiddenStr = params[3];
			boolean isSsidHidden = false;
			if(isSsidHiddenStr.equals("YES"))
			{
				isSsidHidden = true;
			}
			mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, EsptouchDemoActivity.this);
			IEsptouchResult result = mEsptouchTask.executeForResult();
			return result;
		}

		@Override
		protected void onPostExecute(IEsptouchResult result) {
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"确认");
			Builder bd = new AlertDialog.Builder(EsptouchDemoActivity.this);
			bd.setCancelable(false);
			// it is unnecessary at the moment, add here just to show how to use isCancelled()
			if (!result.isCancelled()) {
				if (result.isSuc()) {
					
					/*mProgressDialog.setMessage("Esptouch success, bssid = "
							+ result.getBssid() + ",InetAddress = "
							+ result.getInetAddress().getHostAddress());
							*/
					mProgressDialog.dismiss();
					 
					final String wifi_Mac_address = result.getBssid();
					
					byte[] wifi_Mac_Byte_temp =wifi_Mac_address.getBytes();
					
					//Toast.makeText(EsptouchDemoActivity.this, wifi_Mac_address+
					//		"::"+wifi_Mac_address.length()+"::"+wifi_Mac_Byte_temp.length
					//		, Toast.LENGTH_SHORT).show();
					byte[] wifi_Mac_Byte =  new byte[17] ;
					for(int i = 0;i<wifi_Mac_Byte_temp.length;i++){
						wifi_Mac_Byte[i+(int)(i/2)] = wifi_Mac_Byte_temp[i];						
						if((i+1)%3==0){wifi_Mac_Byte[i] = ByteUtil.convertUint8toByte(':');	}						
					}
					wifi_Mac_Byte[14] = ByteUtil.convertUint8toByte(':');
					
					final String wifi_macAddress = new String(wifi_Mac_Byte);
//					try {
//						wifi_macAddress = new String(wifi_Mac_Byte, "GB2312");						
//						Log.i(TAG, "wifi_macAddress:"+wifi_macAddress);
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					wifi_macAddress = new String(wifi_Mac_Byte);
					String notice_name_head = "WIFI named:MAC:";
					message = notice_name_head.concat(wifi_macAddress);
					String catid_temp = ";ID:";
					message = message.concat(catid_temp);					
										
					bd.setTitle("配置成功，请命名");
					final EditText edittext_Msg = new EditText(EsptouchDemoActivity.this);
					edittext_Msg.setGravity(Gravity.TOP);
					edittext_Msg.setLines(1);
					edittext_Msg.setTextColor(getResources().getColor(R.color.alertex_dlg_edit_text_color));
					//edittext_Msg.setBackgroundDrawable(getResources().getDrawable(R.drawable.herily_alertex_dlg_textinput_drawable));
					bd.setView(edittext_Msg);
					bd.setPositiveButton("确认", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String devName = edittext_Msg.getText().toString();
							//byte[] devNameByte = devName.getBytes();
							wifi_dev.setMacAddress(wifi_macAddress);
							wifi_dev.setNickName(devName);
							wifi_dev.setOnLine(1); 
							Log.i(TAG, "添加设备MAC:"+wifi_macAddress);
							Log.i(TAG, "添加设备NICK:"+devName);
							
							new DevDao(EsptouchDemoActivity.this.getBaseContext()).saveDev(wifi_dev);
							Dev wifidev = new Dev();
							wifidev = devService.getDevByMac(wifi_macAddress);
							byte[] devId = new byte[1];
							devId[0] = (byte)wifidev.getId();
										
							byte[] messageByte_0 = new byte[message.getBytes().length+1];
							messageByte_0 = ByteUtil.byteMerger(message.getBytes(), devId);
							byte[] messageByte_1 = new byte[messageByte_0.length+";Name:".length()];
							messageByte_1 = ByteUtil.byteMerger(messageByte_0,";Name:".getBytes());
							byte[] messageByte = new byte[messageByte_1.length+wifidev.getNickName().getBytes().length];
							messageByte = ByteUtil.byteMerger(messageByte_1,wifidev.getNickName().getBytes());
							
							messageByte_send = messageByte;
							//下面一段代码是观察message	
							String messagetest =  null;
							try {
								 messagetest =  new String(messageByte, "GB2312");  
								Log.i(TAG,"message::"+ messagetest);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}					
							
							new EspTouchThread().start();							
							
						}
					});
					bd.setNeutralButton("已命名", null);
					bd.setNegativeButton("下次", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							edittext_Msg.setFocusable(false);
						}
					});
					bd.show();
					
				} else {
					mProgressDialog.setMessage("Esptouch fail");
				}
			}
		}
	}

	private class TimerProcess implements Runnable{ 
        public void run() { 
            showWIFIDetail(); 
            mHandler.postDelayed(this, 500); 
        } 
    }	
	public void showWIFIDetail() 
    { 
		// display the connected ap's Rssi		
		int wifiRssi = mWifiAdmin.getWifiConnectedRssi();
		if (Math.abs(wifiRssi) > 100) {  
			mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s0));  
        } else if (Math.abs(wifiRssi) > 80) {  
        	mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s1));  
        } else if (Math.abs(wifiRssi) > 70) {  
        	mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s2));  
        } else if (Math.abs(wifiRssi) > 60) {  
        	mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s3));  
        } else if (Math.abs(wifiRssi) > 50) {  
        	mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s5));  
        } else {  
        	mImageWifiRssiDisplay.setImageDrawable(getResources().getDrawable(R.drawable.wifi_s5));  
        } 
		// display the connected ap's ssid
		String apSsid = mWifiAdmin.getWifiConnectedSsid();
		if (apSsid != null) {
			mTvApSsid.setText(apSsid);
		} else {
			mTvApSsid.setText("");
		}		
		// check whether the wifi is connected
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		mBtnConfirm.setEnabled(!isApSsidEmpty);
    }
	
	class BtnConfirmOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			String apSsid = mTvApSsid.getText().toString();
			String apPassword = mEdtApPassword.getText().toString();
			String apBssid = mWifiAdmin.getWifiConnectedBssid();
			Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
			String isSsidHiddenStr = "NO";
			if (isSsidHidden) 
			{
				isSsidHiddenStr = "YES";
			}
			if (__IEsptouchTask.DEBUG) {
				Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
						+ ", " + " mEdtApPassword = " + apPassword);
			}
			/////   测试观察、、、、
			//messageByte_send = "hello world".getBytes();
			//new EspTouchThread().start();
			////////////////////////////
			new EsptouchAsyncTask2().execute(apSsid, apBssid, apPassword, isSsidHiddenStr);
		}
	}
	/**
	 * 监听WIFI信号隐藏选择按钮
	 * 
	 */
	
	class switchbthOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(mSwitchIsSsidHidden.isChecked()){
				mImageHiddenSsid.setImageResource(R.drawable.wirelesshidden_y);
			}
			else{
				mImageHiddenSsid.setImageResource(R.drawable.wirelesshidden_n);
			}
		}
	}
	
	
	/**
	 * 设置按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnSetupOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(EsptouchDemoActivity.this, SetupDevActivity.class);
			startActivity(intent);

		}

	}

	/**
	 * 设置UDP发信线程
	 * 
	 * @author Administrator
	 * 
	 */
	class EspTouchThread extends Thread {

		@Override
		public void run() {
			boolean isEspSet = false;
			DatagramSocket dSocket = null;
			
			byte[] buf = new byte[1024];
			DatagramPacket dp = new DatagramPacket(buf, 1024);
			
			InetAddress UDP_targetHostName = null;
			try {
				UDP_targetHostName = InetAddress.getByName("255.255.255.255"); // 本机测试
						System.out.println("local:" + UDP_targetHostName);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			try {
				dSocket = new DatagramSocket(UDP_WIFIClient_NameId_Port); // 注意此处要先在配置文件里设置权限,否则会抛权限不足的异常
			} catch (SocketException e) {
				e.printStackTrace();
			}
			DatagramPacket dPacket = new DatagramPacket(messageByte_send,
					messageByte_send.length, UDP_targetHostName, UDP_WIFIClient_NameId_Port);
			try {
				// 发送设置为广播
				dSocket.setBroadcast(true);
				dSocket.send(dPacket);
				dSocket.setSoTimeout(5000);
				// sleep(5000);
				dSocket.receive(dp);
				String strInfo = new String(dp.getData(), 0, dp.getLength());
				System.out.println(strInfo);
				isEspSet = true;
				if (strInfo.trim().toUpperCase().equals("OK")) {
					isEspSet = true;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			dSocket.close();

			Message messageStart = new Message();
			if (isEspSet) {
				messageStart.what = ESPtouch_SUCCEED;
			} else {
				messageStart.what = ESPtouch_ERROR;
			}
			esptouch_handler.sendMessage(messageStart);

		}
	}
	
	

}
