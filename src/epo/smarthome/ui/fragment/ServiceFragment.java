package epo.smarthome.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import epo.smarthome.app.R;
import epo.smarthome.ui.BaseFragment;
import epo.smarthome.ui.adapter.ViewPaperAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ServiceFragment extends BaseFragment  {

	

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        return inflater.inflate(R.layout.fragment_service, container, false);
	    }
	    /* (non-Javadoc)
	     * @see app.ui.BaseFragment#onViewCreated(android.view.View, android.os.Bundle)
	     */
	    @Override
	    public void onViewCreated(View view, Bundle savedInstanceState) {
	        super.onViewCreated(view, savedInstanceState);
	    }
	}