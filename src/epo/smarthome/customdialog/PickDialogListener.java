package epo.smarthome.customdialog;

public interface PickDialogListener {
	public void onLeftBtnClick();
	public void onRightBtnClick();
	public void onListItemClick(int position, String string);
	public void onListItemLongClick(int position, String string);
	public void OnExpandChildClick(int groupPosition, int childPosition,Item item);
	
	public void onCancel();
}
