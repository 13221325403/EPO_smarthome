/*******************************************************************************
 *
 * Copyright (c) Weaver Info Tech Co. Ltd
 *
 * BaseActivity
 *
 * app.ui.BaseActivity.java
 * TODO: File description or class description.
 *
 * @author: Administrator
 * @since:  2014-4-22
 * @version: 1.0.0
 *
 * @changeLogs:
 *     1.0.0: First created this class.
 *
 ******************************************************************************/
package epo.smarthome.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Administrator
 *
 */
public class BaseActivity extends Activity {

    /**
     * ProfileActivity 中的RequestCode
     */
    public static final int TO_SELECT_PHOTO = 1;            //选择图片文件
    public static final int TO_SELECT_LOCALPHOTO = 13;            //选择本地相册图片
    public static final int TO_SELECT_GETPHOTO = 14;            //拍照显示图片
    public static final int TO_SELECT_UPDNICKNAME = 2;      //昵称
    public static final int TO_SELECT_UPDGENDER = 3;        //性别
    public static final int TO_SELECT_UPDAGE = 4;           //age
    public static final int TO_SELECT_PLACE = 5;            //地区
    public static final int TO_SELECT_HEALTHSTATUS = 6;     //健康状况
    public static final int TO_SELECT_TAG = 7;              //咨询

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
