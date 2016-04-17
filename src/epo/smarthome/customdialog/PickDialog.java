package epo.smarthome.customdialog;

import java.util.ArrayList;
import java.util.List;

import epo.smarthome.app.R;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class PickDialog extends Dialog{
	private Context context;
	private String title;
	private LinearLayout blend_dialog_preview;
	private ListView blend_dialog_nextview;
	private ArrayList<String> items=new ArrayList<String>();	
	private PickDialogListener pickDialogListener;
	DialogListViewAdapter adapter;
	
	private ExpandableListView mExpandableListView = null;
	private List<List<Item>> list_items=new ArrayList<List<Item>>();
	private ExpandAdapter mExpandAdapter = null;

	public PickDialog(Context context,String title,PickDialogListener pickDialogListener) {
		super(context, R.style.blend_theme_dialog);
		this.context=context;
		this.title=title;
		this.pickDialogListener=pickDialogListener;
	}
	
	public PickDialog(Context context,String title) {
		super(context, R.style.blend_theme_dialog);
		this.context=context;
		this.title=title;		
	}

	int tempPosition=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		LayoutInflater inflater =LayoutInflater.from(context);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.blend_dialog_preview_layout, null);

		TextView titleTextview = (TextView) layout.findViewById(R.id.blend_dialog_title);
		titleTextview.setText(title);
		TextView cancleTextView = (TextView) layout.findViewById(R.id.blend_dialog_cancle_btn);
		cancleTextView.setText(context.getResources().getString(R.string.no));
		blend_dialog_preview = (LinearLayout) layout.findViewById(R.id.blend_dialog_preview);
		blend_dialog_nextview = (ListView) layout.findViewById(R.id.blend_dialog_listview_nextview);
		
		mExpandableListView = (ExpandableListView) layout.findViewById(R.id.blend_dialog_nextview);
		mExpandableListView.setGroupIndicator(null);	
		

		this.setCanceledOnTouchOutside(true);
		this.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		cancleTextView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dismiss();
				
			}

			
		});
		
		
		this.setContentView(layout);
	}
	
	public void initListViewData(ArrayList<String> list){
		items=list;
		blend_dialog_preview.setVisibility(View.GONE);
		blend_dialog_nextview.setVisibility(View.VISIBLE);
		adapter = new DialogListViewAdapter(context, list);
		blend_dialog_nextview.setAdapter(adapter);
		mExpandableListView.setMinimumHeight(20);	
		blend_dialog_nextview.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dismiss();
				if(pickDialogListener!=null){
					pickDialogListener.onListItemClick(position, items.get(position));
				}
			}
		});
	}

	public void initListViewData(List<List<Item>> list){
		blend_dialog_preview.setVisibility(View.GONE);
		mExpandableListView.setVisibility(View.VISIBLE);
		list_items=list;	
		mExpandAdapter = new ExpandAdapter(context, list_items);
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
//				Toast.makeText(context, groupPosition, 1).show();
//			
//				v.setBackgroundResource(R.drawable.category_expandlv_itemone_bg_select);
//				v.invalidate();
//				v.postInvalidate();
				return false;
			}
		});
		
		mExpandableListView.setDescendantFocusability(ExpandableListView.FOCUS_AFTER_DESCENDANTS);
		mExpandableListView.setOnChildClickListener(new BtnRefreshOnClickListener());
	
	}

	
class BtnRefreshOnClickListener implements OnChildClickListener {

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		dismiss();
		Item item = mExpandAdapter.getChild(groupPosition, childPosition);
		if(pickDialogListener!=null){
			pickDialogListener.OnExpandChildClick(groupPosition, childPosition,item);
		}

		return false;
	}

}

	
}