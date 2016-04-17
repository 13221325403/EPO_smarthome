package epo.smarthome.ui.fragment;

import epo.smarthome.app.R;
import epo.smarthome.ui.BaseFragment;
import epo.smarthome.ui.activity.setting.AboutActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class SettingFragment extends BaseFragment implements OnClickListener {


	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_about:
			startActivity(new Intent(getActivity(), AboutActivity.class));
			break;

		default:
			break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_setting, container, false);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.layout_about).setOnClickListener(this);
	}

}