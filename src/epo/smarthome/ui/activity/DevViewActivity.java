package epo.smarthome.ui.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import epo.smarthome.app.R;
import epo.smarthome.customdialog.CustomDialog;
import epo.smarthome.customdialog.Item;
import epo.smarthome.customdialog.PickDialog;
import epo.smarthome.customdialog.PickDialogListener;
import epo.smarthome.data.dao.FriendListDao;
import epo.smarthome.data.dao.GroupListDao;
import epo.smarthome.device.Dev;
import epo.smarthome.device.Friend;
import epo.smarthome.device.Group;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.FriendListService;
import epo.smarthome.service.GroupListService;
import epo.smarthome.service.SocketService;
import epo.smarthome.service.SocketService.SocketBinder;
import epo.smarthome.ui.TitleActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 设备显示类
 * 
 * @author Administrator
 * 
 */
public class DevViewActivity extends TitleActivity {
	
	//############################
    private List<List<Item>> mData = new ArrayList<List<Item>>();

    private int[] mGroupArrays = new int[] { 
            R.array.tianlongbabu,
            R.array.shediaoyingxiongzhuan, 
            R.array.shendiaoxialv };

    private int[] mDetailIds = new int[] { 
            R.array.tianlongbabu_detail,
            R.array.shediaoyingxiongzhuan_detail, 
            R.array.shendiaoxialv_detail };

    private int[][] mImageIds = new int[][] {
            { R.drawable.avatar1, 
              R.drawable.avatar2, 
              R.drawable.avatar3 },
            { R.drawable.avatar4, 
              R.drawable.avatar5, 
              R.drawable.avatar6,
              R.drawable.avatar7, 
              R.drawable.avatar8, 
              R.drawable.avatar9,
              R.drawable.avatar10 },
            { R.drawable.avatar11,
              R.drawable.avatar12 } };
    //#################################
	
	Dev dev = null;
	TextView title = null;
	
	Button mapplyGroupBtn;
	Button mdevJoinGroupBtn;
	Button maddFriendBtn;

	private String TAG = "DevViewActivity";
	
	static final int APPLYROUP_SUCCEED = 0;
	static final int APPLYROUP_ERROR = 1;
	
	static final int JOINGROUP_SUCCEED = 2;
	static final int JOINGROUP_ERROR = 3;
	
	static final int REFRESH_START = 4;
	static final int TIME_OUT = 5;

	static final int CMD_SUCCEEDT = 6;
	static final int CMD_TIMEOUT = 7;
	
	String mlocalDevMac;
	GroupListService  groupService;	
	FriendListService friendService;
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			switch (msg.what) {
			case APPLYROUP_SUCCEED:
				Toast.makeText(DevViewActivity.this, "成功申请群组",
						Toast.LENGTH_SHORT).show();
				Group group = new Group();
				group.setId(msg.arg1);
				group.setNickName((String) msg.obj);
				
				Log.i(TAG, "群组ID:"+group.getId());
				Log.i(TAG, "群组name:"+group.getNickName());				
				
				groupService.saveGroup(group);
				
				break;
			case APPLYROUP_ERROR:
				Toast.makeText(DevViewActivity.this, "申请群组失败",
						Toast.LENGTH_SHORT).show();

				break;
			case JOINGROUP_SUCCEED:
				Toast.makeText(DevViewActivity.this, "设备成功添加至群组",
						Toast.LENGTH_SHORT).show();

				break;
			case JOINGROUP_ERROR:
				Toast.makeText(DevViewActivity.this, "设备添加群组失败！",
						Toast.LENGTH_SHORT).show();

				break;
			case REFRESH_START:

				break;
			case CMD_SUCCEEDT:
				Toast.makeText(DevViewActivity.this, "命令执行成功！",
						Toast.LENGTH_SHORT).show();


				break;
			case CMD_TIMEOUT:
				Toast.makeText(DevViewActivity.this, "命令执行失败！",
						Toast.LENGTH_SHORT).show();


				break;
			case TIME_OUT:

				break;

			default:
				break;

			}
		}

	};

	IntentFilter intentFilter = null;
	ApplyGroupBroadcast socketApplyGroupReceiver = new ApplyGroupBroadcast();
	DevJoinGroupBroadcast socketGetDevReceiver = new DevJoinGroupBroadcast();	
	
	private PickDialog pickDialog;
	private CustomDialog friend_apply_dialog;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE); // 注意顺序
		setContentView(R.layout.activity_dev_view);
		setTitle(R.string.text_about);
        showBackwardView(R.string.button_backward, true);
	
		Intent intent = this.getIntent();
		String devId = intent.getStringExtra("devId");
		mlocalDevMac = devId;
		dev = Cfg.getDevById(devId);
		Log.i(TAG, "=============devId:" + devId);
		Log.i(TAG, "=============dev:" + dev);
		if (dev == null) {
			return;
		}

		mapplyGroupBtn = (Button) findViewById(R.id.applyGroupBtn);
		mdevJoinGroupBtn = (Button) findViewById(R.id.devJoinGroupBtn);
		maddFriendBtn  = (Button) findViewById(R.id.addFriendBtn);
		
		mapplyGroupBtn.setOnClickListener(new BtnApplyGroupListener());
		mdevJoinGroupBtn.setOnClickListener(new BtnDevJoinGroupListener());
		maddFriendBtn.setOnClickListener(new BtnAddFriendListener());
	
		
		//  初始化群组数据库
		groupService = new GroupListDao(DevViewActivity.this.getBaseContext());
		friendService = new FriendListDao(DevViewActivity.this.getBaseContext());
		//  注册广播服务
		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.DevJoinGroupBoardCastName);
		this.registerReceiver(socketGetDevReceiver, intentFilter);
		intentFilter.addAction(Cfg.ApplyGroupBoardCastName);
		this.registerReceiver(socketApplyGroupReceiver, intentFilter);
		
		//########   测试数据          ######
		initData();
		//########

	}

    @Override
    protected void onBackward(View backwardView) {
        super.onBackward(backwardView);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dev_view, menu);
		return true;
	}
	
//*****************   按键监听类         ********************//	
	/**
	 * 申请群组按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnApplyGroupListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}
			CustomDialog.Builder customBuilder = new
	                CustomDialog.Builder(DevViewActivity.this);
			customBuilder.setTitle("填写群组名称");
			
			final EditText edittext_Msg = new EditText(DevViewActivity.this);
			edittext_Msg.setGravity(Gravity.TOP);
			edittext_Msg.setLines(1);
//			edittext_Msg.setBackground(null);
			edittext_Msg.setLayoutParams(new TextSwitcher.LayoutParams( 
	                LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));			

			edittext_Msg.setTextColor(getResources().getColor(R.color.alertex_dlg_edit_text_color));
			edittext_Msg.setHint("填写群名称（10个字以内）");
			edittext_Msg.setHintTextColor(getResources().getColor(R.color.lightblack));
			
			edittext_Msg.setBackgroundColor(getResources().getColor(R.color.lightgray));
	
			customBuilder.setContentView(edittext_Msg);
            customBuilder.setPositiveButton("确认", 
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    
                    String groupName = edittext_Msg.getText().toString();
                    new ApplyGroupThread(groupName).start();
                    
                }
            });
			customBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                	
                }
            });
            friend_apply_dialog = customBuilder.create();
            friend_apply_dialog.show();
		}

	}

	/**
	 * 设备加入群组按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnDevJoinGroupListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}
			
			List<Group> mgrouplist= new ArrayList<Group>();
			final List<Group> mgrouplist_adapt= new ArrayList<Group>();			
			final ArrayList<String> list=new ArrayList<String>();	
			
			mgrouplist = groupService.getGroupList();
			for(Group s:mgrouplist){
				list.add(s.getNickName());	
				mgrouplist_adapt.add(s);
			}
		
			pickDialog = new PickDialog(DevViewActivity.this, getString(R.string.gouplist), new PickDialogListener() {

				@Override
				public void onRightBtnClick() {
					// TODO Auto-generated method stub

				}
				@Override
				public void onListItemLongClick(int position, String string) {
					// TODO Auto-generated method stub

				}
				@Override
				public void onListItemClick(int position, String string) {
					// TODO Auto-generated method stub
					Toast.makeText(DevViewActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
					new DevJoinGroupThread(mgrouplist_adapt.get(position)).start();
				}

				@Override
				public void onLeftBtnClick() {
					// TODO Auto-generated method stub

				}
				@Override
				public void OnExpandChildClick(int groupPosition, int childPosition,Item item){

				};				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
				}
			});
			pickDialog.show();
			
			//for test 延迟 1 秒加载
			new Handler().postDelayed(new Runnable(){   

			    public void run() {
			    //execute the task 
			    	pickDialog.initListViewData(list);
			    }   

			 }, 100); 
		}

	}

	/**
	 * 添加好友按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class BtnAddFriendListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (dev == null) {
				return;
			}
			
			List<Friend> mfriendlist= new ArrayList<Friend>();
			final List<Friend> mfriendlist_adapt= new ArrayList<Friend>();
			
			mfriendlist = friendService.getFriendList();
			
			if(mfriendlist == null){
				return;}
			
			for(Friend f:mfriendlist){
				if(!f.getLocalDevMac().equals(mlocalDevMac)){
					mfriendlist_adapt.add(f);
				}
			}
			

			pickDialog = new PickDialog(DevViewActivity.this, getString(R.string.friendlist), new PickDialogListener() {

				@Override
				public void onRightBtnClick() {
					// TODO Auto-generated method stub

				}
				@Override
				public void onListItemLongClick(int position, String string) {
					// TODO Auto-generated method stub

				}
				@Override
				public void onListItemClick(int position, String string) {
					// TODO Auto-generated method stub
//					Toast.makeText(DevViewActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
//					new DevJoinGroupThread(position).start();
				}
				@Override
				public void onLeftBtnClick() {
					// TODO Auto-generated method stub

				}
				@Override
				public void OnExpandChildClick(int groupPosition, int childPosition,Item item){
					
					new AlertDialog.Builder(DevViewActivity.this)
	                .setTitle(item.getName())
	                .setMessage(item.getDetail())
	                .setIcon(android.R.drawable.ic_menu_more).create().show();
					
				};				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
				}
			});
			pickDialog.show();
			
			//for test 延迟 1 秒加载
			new Handler().postDelayed(new Runnable(){   

			    public void run() {
			    //execute the task 
//			    	pickDialog.initListViewData(list);
			    	pickDialog.initListViewData(mData);
			    }   

			 }, 500);

		}

	}
	
//*******************   进程交互类        *********************//
	/**
	 * 申请群组线程
	 * 
	 * @author Administrator
	 * 
	 */
	class ApplyGroupThread extends Thread {
		String groupName;
		
		public ApplyGroupThread(String groupName) {
			this.groupName = groupName;
		}
		
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//  创建用户信息
			JSONObject user_json = new JSONObject();
			
			try {
//				user_json.put("user_ID", Cfg.userId);
				user_json.put("user_NAME", Cfg.userName);
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			//  创建群组信息
			JSONObject group_json = new JSONObject();
			
			try {
//				group_json.put("group_ID", groupid);
				group_json.put("group_NAME", groupName);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			//创建设备加入群组请求：JSONObject对象getdev_json
			 JSONObject joingroup_json = new JSONObject();
			 
			 Random joingroup_nonce_rand =new Random();
			 int joingroup_nonce = joingroup_nonce_rand.nextInt();
			 
			 try {
				 joingroup_json.put("nonce", joingroup_nonce);
				 joingroup_json.put("type", "PHONE_USER_APPLY_GROUP");
				 joingroup_json.put("user_body", user_json);
				 joingroup_json.put("group_body", group_json);
				 
				//###  添加鉴权数据包   Cfg.torken
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "用户申请群组请求："+joingroup_json.toString()); 
			StartActivity.socketService.socketSendMessage(joingroup_json.toString());

		}

	}	
	
	/**
	 * 设备加入群组线程
	 * 
	 * @author Administrator
	 * 
	 */
	class DevJoinGroupThread extends Thread {
		Group group;
		
		public DevJoinGroupThread(Group group) {
			this.group = group;
		}
		
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			//  创建设备信息
			JSONObject client_json = new JSONObject();
			
			try {
				client_json.put("client_MAC", dev.getMacAddress());
				client_json.put("client_NAME", dev.getNickName());
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			//  创建群组信息
			JSONObject group_json = new JSONObject();
			
			try {
				group_json.put("group_ID", group.getId());
				group_json.put("group_NAME", group.getNickName());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
			
			//创建设备加入群组请求：JSONObject对象getdev_json
			 JSONObject joingroup_json = new JSONObject();
			 
			 Random joingroup_nonce_rand =new Random();
			 int joingroup_nonce = joingroup_nonce_rand.nextInt();
			 
			 try {
				 joingroup_json.put("nonce", joingroup_nonce);
				 joingroup_json.put("type", "ADD_DEV_TO_GROUP");
				 joingroup_json.put("client_body", client_json);
				 joingroup_json.put("group_body", group_json);
				 
				//###  添加鉴权数据包   Cfg.torken
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "设备加入群组请求："+joingroup_json.toString()); 
			StartActivity.socketService.socketSendMessage(joingroup_json.toString());

		}

	}


//***********************   广播监听类       ***************************//
	private class ApplyGroupBroadcast extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(action.equals(Cfg.ApplyGroupBoardCastName)){	
				Log.i(TAG, "接收消息");
				Message message = new Message();
				message.what = APPLYROUP_ERROR;
				if(intent.getStringExtra("applygroup_flag").equals("success")){
					message.what = APPLYROUP_SUCCEED;
					message.arg1 = intent.getIntExtra("groupID", 0);
					message.obj = intent.getStringExtra("groupName");
				}				
				handler.sendMessage(message);
			}
		}
	}
	
	
	private class DevJoinGroupBroadcast extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();			
			if(action.equals(Cfg.DevJoinGroupBoardCastName)){		
				Message message = new Message();
				message.what = JOINGROUP_ERROR;
				if(intent.getStringExtra("set_flag").equals("success")){
					message.what = JOINGROUP_SUCCEED;
				}				
				handler.sendMessage(message);
			}
		}
	}
	
	// ####################   测试数据       ###################//
    private void initData() {
        for (int i = 0; i < mGroupArrays.length; i++) {
            List<Item> list = new ArrayList<Item>();
            String[] childs = getStringArray(mGroupArrays[i]);
            String[] details = getStringArray(mDetailIds[i]);
            for (int j = 0; j < childs.length; j++) {
                Item item = new Item(mImageIds[i][j], childs[j], details[j]);
                list.add(item);
            }
            mData.add(list);
        }
    }
    private String[] getStringArray(int resId) {
        return getResources().getStringArray(resId);
    }
	
	
}