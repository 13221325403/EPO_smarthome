
package epo.smarthome.ui.activity;

import epo.smarthome.app.R;
import epo.smarthome.service.Cfg;
import epo.smarthome.service.SocketService;
import epo.smarthome.service.SocketService.SocketBinder;
import epo.smarthome.ui.FragmentCallback;
import epo.smarthome.ui.fragment.ServiceFragment;
import epo.smarthome.ui.fragment.ContactFragment;
import epo.smarthome.ui.fragment.MydeviceFragment;
import epo.smarthome.ui.fragment.SettingFragment;
import epo.smarthome.util.DialogUtils;
import epo.smarthome.util.FragmentUtils;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import app.ui.widget.TabView;
import app.ui.widget.TabView.OnTabChangeListener;

public class StartActivity extends FragmentActivity implements OnTabChangeListener, FragmentCallback {

	private final String TAG = "StartActivity";
	
    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;
    private TabView mTabView;
    private TextView mTitleTextView;

    /** 上一次的状态 */
    private int mPreviousTabIndex = 1;
    /** 当前状态 */
    private int mCurrentTabIndex = 1;

    /** 再按一次退出程序*/
    private long exitTime = 0;
    
    IntentFilter intentFilter = null;
	SocketIsConnectReceiver socketConnectReceiver = new SocketIsConnectReceiver();
    
	SocketBinder socketBinder;
	public static SocketService socketService;
	boolean isBinderConnected = false;
    
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
		Intent intent = new Intent(StartActivity.this, SocketService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     // bind....
     	bindService();
     	intentFilter = new IntentFilter();
		intentFilter.addAction(Cfg.SendBoardCastName);		
		this.registerReceiver(socketConnectReceiver, intentFilter);
        
        mFragmentManager = getSupportFragmentManager();        
        setupViews();
    }
    private void setupViews()
    {
        setContentView(R.layout.activity_start);
        mTitleTextView = (TextView) findViewById(R.id.text_title);
        mTabView = (TabView) findViewById(R.id.view_tab);
        mTabView.setOnTabChangeListener(this);
        initViews();
    }
    
    private void initViews(){
//    	mCurrentTabIndex = 1;
//        mPreviousTabIndex = 1;
//        mTabView.setCurrentTab(mCurrentTabIndex);
//      mCurrentFragment = new ServiceFragment();
//      FragmentUtils.replaceFragment(mFragmentManager, R.id.layout_content,ServiceFragment.class, null, false);
    	mCurrentTabIndex = 0;
        mPreviousTabIndex = 1;
        mTabView.setCurrentTab(mCurrentTabIndex);
//        mTabView.setBackground(getResources().getDrawable(R.drawable.ic_message_selected));
//        mTabView.
        mCurrentFragment = new MydeviceFragment();
        mTitleTextView.setText(R.string.text_tab_mydevice);
        replaceFragment(MydeviceFragment.class);
    }
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {/*
            case BaseActivity.REQUEST_OK_LOGIN:
                if (resultCode == RESULT_OK) {
                    ApplicationUtils.showToast(this, R.string.text_loginsuccess);
                    mTitleTextView.setText(R.string.text_tab_profile);
                    final ProfileFragment profileFragment =
                            (ProfileFragment) mFragmentManager.findFragmentByTag(ProfileFragment.class.getSimpleName());
                    if (profileFragment != null) {
                        Log.d(TAG, "ProfileFragment is refreshing.");
                        profileFragment.refreshViews();
                    } else {
                        Log.e(TAG, "ProfileFragment is null.");
                    }
                } else {
                    // 返回原来的页面
                    mTabView.setCurrentTab(mPreviousTabIndex);
                    ApplicationUtils.showToast(this, R.string.toast_login_failed);
                }
                break;

            default:
                break;
        */}
    }


    /* (non-Javadoc)
     * @see app.ui.FragmentCallback#onFragmentCallback(android.support.v4.app.Fragment, int, android.os.Bundle)
     */
    @Override
    public void onFragmentCallback(Fragment fragment, int id, Bundle args) {
        mTabView.setCurrentTab(mCurrentTabIndex);
    }
    /* (non-Javadoc)
     * @see app.ui.widget.TabView.OnTabChangeListener#onTabChange(java.lang.String)
     */
    @Override
    public void onTabChange(String tag) {

        if (tag != null) {
            if (tag.equals("mydevice")) {
                mPreviousTabIndex = mCurrentTabIndex;
                mCurrentTabIndex = 0;
                mTitleTextView.setText(R.string.text_tab_mydevice);
                replaceFragment(MydeviceFragment.class);
                // 检查，如果没有登录则跳转到登录界面
              /*  final UserConfigManager manager = UserConfigManager.getInstance();
                if (manager.getId() <= 0) {
                    startActivityForResult(new Intent(this, LoginActivity.class),
                            BaseActivity.REQUEST_OK_LOGIN);
                }*/
            }else if ("friendlist".equals(tag)) {
                mPreviousTabIndex = mCurrentTabIndex;
                mCurrentTabIndex = 1;
                mTitleTextView.setText(R.string.text_tab_friendlist);
                replaceFragment(ContactFragment.class);
            } else if (tag.equals("market")) {
                mPreviousTabIndex = mCurrentTabIndex;
                mCurrentTabIndex = 2;
                mTitleTextView.setText(R.string.text_tab_market);
                replaceFragment(ServiceFragment.class);
                // 检查，如果没有登录则跳转到登录界面
              /*  final UserConfigManager manager = UserConfigManager.getInstance();
                if (manager.getId() <= 0) {
                    startActivityForResult(new Intent(this, LoginActivity.class),
                            BaseActivity.REQUEST_OK_LOGIN);
                }*/
            } else if (tag.equals("settings")) {
                mPreviousTabIndex = mCurrentTabIndex;
                mCurrentTabIndex = 3;
                mTitleTextView.setText(R.string.text_tab_setting);
                replaceFragment(SettingFragment.class);
                // 检查，如果没有登录则跳转到登录界面
               /* final UserConfigManager manager = UserConfigManager.getInstance();
                if (manager.getId() <= 0) {
                    startActivityForResult(new Intent(this, LoginActivity.class),
                            BaseActivity.REQUEST_OK_LOGIN);
                }*/
            }
        }
    }

    private void replaceFragment(Class<? extends Fragment> newFragment) {

        mCurrentFragment = FragmentUtils.switchFragment(mFragmentManager,
                R.id.layout_content, mCurrentFragment,
                newFragment, null, false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                DialogUtils.showToast(this, "再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
