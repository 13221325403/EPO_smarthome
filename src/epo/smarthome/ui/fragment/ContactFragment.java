package epo.smarthome.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import epo.smarthome.app.R;
import epo.smarthome.customdialog.CustomDialog;
import epo.smarthome.customdialog.ExpandAdapter;
import epo.smarthome.customdialog.Item;
import epo.smarthome.data.dao.FriendListDao;
import epo.smarthome.data.dao.GroupListDao;
import epo.smarthome.device.Group;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.FriendListService;
import epo.smarthome.service.GroupListService;
import epo.smarthome.ui.BaseFragment;
import epo.smarthome.ui.activity.DevViewActivity;
import epo.smarthome.ui.activity.StartActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextSwitcher;
import android.widget.Toast;
import android.widget.AbsListView.LayoutParams;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;



public class ContactFragment extends BaseFragment{

	private String TAG = "ContactFragment";
	
	static final int APPLYROUP_SUCCEED = 0;
	static final int APPLYROUP_ERROR = 1;
	
	private CustomDialog friend_apply_dialog;
	private ExpandableListView mExpandableListView = null;
	private List<List<Item>> list_items=new ArrayList<List<Item>>();
	private ExpandAdapter mExpandAdapter = null;
	int tempPosition=0;
	
	GroupListService  groupService;	
	FriendListService friendService;
	
	IntentFilter intentFilter = null;
	ApplyGroupBroadcast socketApplyGroupReceiver = new ApplyGroupBroadcast();
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	//  初始化界面视图	
		view.findViewById(R.id.layout_creat_group).setOnClickListener(new BtnCreatGroupListener());
		mExpandableListView = (ExpandableListView) view.findViewById(R.id.list_friend_group);
		mExpandableListView.setGroupIndicator(null);
		mExpandableListView.setMinimumHeight(20);
		changeDevList();
	//  初始化群组数据库和好友列表数据库
		groupService = new GroupListDao(getActivity().getBaseContext());
		friendService = new FriendListDao(getActivity().getBaseContext());
	//  注册广播服务
		intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.ApplyGroupBoardCastName);
		getActivity().registerReceiver(socketApplyGroupReceiver, intentFilter);	

	}

	//*******************   视图创建类         ********************//	
	private void initListViewData(List<List<Item>> list){
		
		mExpandableListView.setVisibility(View.VISIBLE);
		list_items=list;	
		mExpandAdapter = new ExpandAdapter(getActivity(), list_items);
		mExpandableListView.setAdapter(mExpandAdapter);	
		
		mExpandableListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			
			@Override
			public void onGroupExpand(int groupPosition) {
				// TODO Auto-generated method stub
				if(tempPosition!=groupPosition){
					mExpandableListView.collapseGroup(tempPosition);
					tempPosition=groupPosition;
				}
				
			}
		});
		
		mExpandableListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			
			@Override
			public void onGroupCollapse(int groupPosition) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub

				return false;
			}
		});
		
		mExpandableListView.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
		mExpandableListView.setOnChildClickListener(new BtnChildOnClickListener());
	
	}
	
	private void changeDevList() {
		
		//########   测试数据          ######
		initData();
		//########
		initListViewData(mData);
	}

	
    
	//*******************   按键监听类         ********************//	
		/**
		 * 申请群组按钮监听类
		 * 
		 * @author Administrator
		 * 
		 */
		class BtnCreatGroupListener implements OnClickListener {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CustomDialog.Builder customBuilder = new
		                CustomDialog.Builder(getActivity());
				customBuilder.setTitle("填写群组名称");
				
				final EditText edittext_Msg = new EditText(getActivity());
				edittext_Msg.setGravity(Gravity.TOP);
				edittext_Msg.setLines(1);
//				edittext_Msg.setBackground(null);
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
	                	dialog.dismiss();
	                	Toast.makeText(getActivity(), "取消申请群组",
								Toast.LENGTH_SHORT).show();
	                }
	            });
	            friend_apply_dialog = customBuilder.create();
	            friend_apply_dialog.show();
			}

		}
		/**
		 * 群组用户按钮监听类
		 * 
		 * @author Administrator
		 * 
		 */
		class BtnChildOnClickListener implements OnChildClickListener {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub

				Item item = mExpandAdapter.getChild(groupPosition, childPosition);


				return false;
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
//					user_json.put("user_ID", Cfg.userId);
					user_json.put("user_NAME", Cfg.userName);
				} catch (JSONException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				//  创建群组信息
				JSONObject group_json = new JSONObject();
				
				try {
//					group_json.put("group_ID", groupid);
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
		
	//*******************    广播监听类       ***************************//
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
	
		
	//*******************    消息处理类       ***************************//	
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				switch (msg.what) {
				case APPLYROUP_SUCCEED:
					Toast.makeText(getActivity(), "成功申请群组",
							Toast.LENGTH_SHORT).show();
					Group group = new Group();
					group.setId(msg.arg1);
					group.setNickName((String) msg.obj);
					
					Log.i(TAG, "群组ID:"+group.getId());
					Log.i(TAG, "群组name:"+group.getNickName());				
					
					groupService.saveGroup(group);
					
					break;
				case APPLYROUP_ERROR:
					Toast.makeText(getActivity(), "申请群组失败",
							Toast.LENGTH_SHORT).show();

					break;
				
				default:
					break;

				}
			}

		};	

		
		// ####################   测试数据       ###################//

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


