package epo.smarthome.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import epo.smarthome.app.R;
import epo.smarthome.data.dao.DevDao;
import epo.smarthome.device.Dev;
import epo.smarthome.protocol.Msg;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.DevService;
import epo.smarthome.ui.activity.*;
import epo.smarthome.ui.BaseFragment;
import epo.smarthome.ui.activity.DevViewActivity;
import epo.smarthome.ui.activity.LoginActivity;
import epo.smarthome.ui.activity.ScanActivity;
import epo.smarthome.ui.activity.setting.AboutActivity;
import epo.smarthome.ui.fragment.ContactFragment.BtnCreatGroupListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 主界面类
 * 
 * @author Administrator
 * 
 */
public class MydeviceFragment extends BaseFragment{

	Button btnRefresh = null;
	ListView listView;
	private final String TAG = "MydeviceFragment";
	Msg msg = new Msg();
	static final int GET_DEV_SUCCEED = 0;
	static final int GET_DEV_ERROR = 1;
	static final int BUTTON_DELETE = 2;
	static final int BUTTON_CONTROL = 3;
	static final int DELETE_SUCCEED = 4;
	static final int DELETE_ERROR = 5;
	
	DevService  devService;

	IntentFilter intentFilter = null;
	GetDevBroadcast socketGetDevReceiver = new GetDevBroadcast();
	DeleteDevBroadcast	socketDeleteDevReceiver = new DeleteDevBroadcast();
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mydevice, container, false);
    }
    /* (non-Javadoc)
     * @see app.ui.BaseFragment#onViewCreated(android.view.View, android.os.Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		//  初始化设备数据库
		devService = new DevDao(getActivity().getBaseContext());
        //  初始化界面视图	
    	view.findViewById(R.id.layout_smartconfig).setOnClickListener(new BtnSmartConfigListener());
    	view.findViewById(R.id.layout_add_device).setOnClickListener(new BtnAddDevOnClickListener());
    	
		btnRefresh = (Button) view.findViewById(R.id.setupBtnRefresh);
		btnRefresh.setOnClickListener(new BtnRefreshOnClickListener());

		listView = (ListView) view.findViewById(R.id.devListView);
		listView.setOnItemClickListener(new ItemClickListener());
	
		changeDevList();

		//  注册广播服务
		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.GetDevBoardCastName);
		getActivity().registerReceiver(socketGetDevReceiver, intentFilter);
		intentFilter.addAction(Cfg.DelDevBoardCastName);
		getActivity().registerReceiver(socketDeleteDevReceiver, intentFilter);
        
    }
  //*******************   视图创建类         ********************//	
    private void changeDevList() {
		//   Cfg.listDev   需要从数据库中读取
		List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		List<Dev> listDev= devService.getDevList();
		
		if(listDev.size() > 0){
			Log.i(TAG, "成功获取当前数据库设备信息");
			Cfg.listDev = listDev;
		}else{
			Log.i(TAG, "当前数据库不存在设备信息");	
			}		
		
		for (Dev dev : Cfg.listDev) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id", dev.getMac());
			item.put("name", dev.getNickName());
			item.put("state", dev.isOnLine()==1 ? "在线" : "不在线");
			data.add(item);
		}
		// 创建SimpleAdapter适配器将数据绑定到item显示控件上
		SimpleAdapter adapter = new MySimpleAdapter(getActivity(), data,
				R.layout.devitem, new String[] { "id", "name", "state" },
				new int[] { R.id.devId, R.id.devName, R.id.devStat });
		// 实现列表的显示
		listView.setAdapter(adapter);
		// 删除分割线
		listView.setDivider(null);
	}

    class MySimpleAdapter extends SimpleAdapter {

		// protected static final int BUTTON_DELETE = 0;
		// protected static final int BUTTON_ADD = 0;
		public MySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int mPosition = position;
			convertView = super.getView(position, convertView, parent);
			ImageView buttonAdd = (ImageView) convertView
					.findViewById(R.id.devControl);// id为你自定义布局中按钮的id
			buttonAdd.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// mHandler.obtainMessage(BUTTON_ADD, mPosition, 0)
					// .sendToTarget();

					Message message = new Message();
					message.what = BUTTON_CONTROL;
					message.arg1 = mPosition;
					handler.sendMessage(message);

				}
			});
			ImageView buttonDelete = (ImageView) convertView
					.findViewById(R.id.devDelete);
			buttonDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Message message = new Message();
					message.what = BUTTON_DELETE;
					message.arg1 = mPosition;
					handler.sendMessage(message);
				}
			});
			return convertView;
		}
	}

  //*******************   按键监听类         ********************//	
  
	/**
	 * 智能配置监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnSmartConfigListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),com.espressif.iot.esptouch.demo_activity.EsptouchDemoActivity.class);
			startActivity(intent);
			}
		}
	
	/**
	 * 刷新 按钮监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnRefreshOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			new GetDevListThread().start();
		}
	}
	
	/**
	 * 添加设备 按钮监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnAddDevOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),ScanActivity.class);
			startActivity(intent);// 打开新界面
//			startActivity(new Intent(getActivity(), ScanActivity.class));
		}
	}

	/**
	 * 本地设备 条目监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	private final class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView listView = (ListView) parent;
			HashMap<String, Object> data = (HashMap<String, Object>) listView
					.getItemAtPosition(position);
			String devId = (String) data.get("id");

			Log.i(TAG, "ItemClickListener devId：" + devId);
			Toast.makeText(getActivity(), "选择设备" + devId, 0).show();
			Dev dev = getDevById(devId);
			if (dev == null) {
				Toast.makeText(getActivity(), "请重新选择设备", 0).show();
				return;
			}
			// 跳转到设置界面
			Intent intent = new Intent(getActivity(), DevViewActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("devId", dev.getMac());
			intent.putExtras(bundle);
			Log.i(TAG, "ItemClickListener dev：" + dev.getMac());
			// MyLog.i(TAG, "跳转至设置界面");
			startActivity(intent);// 打开新界面

		}
	}

	//*******************   进程交互类        *********************//
	/**
	 * 获得设备列表线程
	 * 
	 * @author Administrator
	 * 
	 */
	class GetDevListThread extends Thread {

		@Override
		public void run() {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			//创建获取设备请求：JSONObject对象getdev_json
			 JSONObject getdev_json = new JSONObject();
			 
			 Random getdev_nonce_rand =new Random();
			 int getdev_nonce = getdev_nonce_rand.nextInt();
			 
			 try {
				 getdev_json.put("nonce", getdev_nonce);
				 getdev_json.put("type", "PHONE_GET_DEVLIST");
				 getdev_json.put("phoneuserName", Cfg.userName);
				 
				//###  添加鉴权数据包   Cfg.torken
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "获取设备请求："+getdev_json.toString()); 
			StartActivity.socketService.socketSendMessage(getdev_json.toString());
		}
	}

	class DeleteDevThread extends Thread {
		String id;
		int delay = 50;

		public DeleteDevThread(String strId) {
			id = strId;
		}
		@Override
		public void run() {

			if (id.isEmpty()) {
				return;
			}
			//创建获取设备请求：JSONObject对象getdev_json
			 JSONObject deldev_json = new JSONObject();
			 
			 Random deldev_nonce_rand =new Random();
			 int deldev_nonce = deldev_nonce_rand.nextInt();
			 
			 try {
				 deldev_json.put("nonce", deldev_nonce);
				 deldev_json.put("type", "PHONE_DELETE_DEV");
				 deldev_json.put("phoneuserName", Cfg.userName);
				 deldev_json.put("deviceMAC",id);
				 
				//###  添加鉴权数据包   Cfg.torken
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "删除设备请求："+deldev_json.toString()); 
			StartActivity.socketService.socketSendMessage(deldev_json.toString());
			

		}
	}
	
	//*******************    广播监听类       ***************************//
	private class DeleteDevBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(action.equals(Cfg.DelDevBoardCastName)){		
				Message message = new Message();
				message.what = DELETE_ERROR;
				if(intent.getStringExtra("deldev_flag").equals("success")){
					message.what = DELETE_SUCCEED;
				}				
				handler.sendMessage(message);
			}
		}
	}
	
	private class GetDevBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(action.equals(Cfg.GetDevBoardCastName)){		
				Message message = new Message();
				message.what = GET_DEV_ERROR;
				if(intent.getStringExtra("getdev_flag").equals("success")){
					message.what = GET_DEV_SUCCEED;
				}				
				handler.sendMessage(message);
			}
		}
	}
	
	//*******************    消息处理类       ***************************//
	Handler handler = new Handler() {


		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// dataToui();
			switch (msg.what) {

			case GET_DEV_SUCCEED:
				Toast.makeText(getActivity(), "获取设备列表成功！",
						Toast.LENGTH_SHORT).show();
				changeDevList();
				Log.i(TAG, "获取设备列表成功！");

				break;
			case GET_DEV_ERROR:
				// Toast.makeText(MainActivity.this, "获取设备列表成功！",
				// Toast.LENGTH_SHORT).show();
				Cfg.listDev.clear();
				changeDevList();

				break;

			case BUTTON_DELETE:
				HashMap<String, Object> data1 = (HashMap<String, Object>) listView
						.getItemAtPosition(msg.arg1);
				String devId1 = (String) data1.get("id");

				Log.i(TAG, "ItemClickListener devId：" + devId1);
				Toast.makeText(getActivity(), "选择设备" + devId1, 0)
						.show();
				Dev dev1 = getDevById(devId1);
				if (dev1 == null) {
					Toast.makeText(getActivity(), "请重新选择设备", 0)
							.show();
					return;
				}
				new DeleteDevThread(dev1.getMac()).start();
				break;
			case BUTTON_CONTROL:

				HashMap<String, Object> data = (HashMap<String, Object>) listView
						.getItemAtPosition(msg.arg1);
				String devId = (String) data.get("id");

				Log.i(TAG, "ItemClickListener devId：" + devId);
				Toast.makeText(getActivity(), "选择设备" + devId, 0)
						.show();
				Dev dev = getDevById(devId);
				if (dev == null) {
					Toast.makeText(getActivity(), "请重新选择设备", 0)
							.show();
					return;
				}
				// 跳转到设置界面
				Intent intent = new Intent(getActivity(),DevViewActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString("devId", dev.getMac());
				intent.putExtras(bundle);

				Log.i(TAG, "ItemClickListener dev：" + dev.getMac());
				// MyLog.i(TAG, "跳转至设置界面");DeleteDevThread
				startActivity(intent);// 打开新界面
				break;

			case DELETE_SUCCEED:
				Toast.makeText(getActivity(), "删除设备成功。", Toast.LENGTH_SHORT)
						.show();
				HashMap<String, Object> data2 = (HashMap<String, Object>) listView
						.getItemAtPosition(msg.arg1);
				String devId2 = (String) data2.get("id");
				devService.removeDev(devId2);
				changeDevList();
				Log.i(TAG, "要删除设备的ID："+devId2);
				
//				new GetDevListThread().start();
				break;
			case DELETE_ERROR:
				Toast.makeText(getActivity(), "删除设备失败！", Toast.LENGTH_SHORT)
						.show();

				break;
			default:
				break;

			}
		}

	};

	//*******************    数据处理类       ***************************// 
	
	/**
	 * 通过设备id获取设备对象
	 * 
	 * @param id
	 *            设备id
	 * @return 设备对象
	 */
	private Dev getDevById(String id) {
		if (id == null) {
			return null;
		}
		for (Dev dev : Cfg.listDev) {
			if (dev.getMac().equals(id)) {
				return dev;
			}
		}
		return null;
	}

}
