package epo.smarthome.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import epo.smarthome.data.db.DatabaseHelper;
import epo.smarthome.device.Group;
import epo.smarthome.service.GroupListService;

public class GroupListDao implements GroupListService {
	
	private DatabaseHelper dbHelper = null;

	private static final String TAG = "GroupListDao";
	private static final String ACTIVITY_TAG = "GroupListDao";

	public GroupListDao(Context context) {
		dbHelper = new DatabaseHelper(context);
	}
	@Override
	public boolean saveGroup(Group group){
		// TODO Auto-generated method stub
		boolean flag = false;
		if (group == null) {
			return flag;
		}
		if (group.getNickName().isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (findGroupByName(db, group.getNickName())) { // �м�¼ update
				flag = updateGroup(db, group);
			} else { // �޼�¼ insert
				flag = insertGroup(db, group);
			}
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return flag;
	}
	
	@Override
	public boolean removeGroup(String groupName) {
		// TODO Auto-generated method stub
		boolean flag = false;
		if (groupName.isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("delete from gouplist where goupname= ?", new Object[] { groupName });
			flag = true;
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return flag;
	}

	public Group getGroupByName(String groupName) {
		// TODO Auto-generated method stub
		Log.i(ACTIVITY_TAG, "public Group getGroupByName(String groupName)" + groupName);
		Group group = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT _id, goupid, goupname from gouplist where goupname=? ";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, new String[] { groupName  });
			// if(cursor.moveToNext()){
			if (cursor.moveToFirst()) {
				index = 0;
				group = new Group();
				// Log.i(ACTIVITY_TAG,"index:"+index);
				group.setId(cursor.getInt(index++));				
				group.setNickName(cursor.getString(index++));
				Log.i(ACTIVITY_TAG, "end index:" + index);
			}
		} catch (Exception e) {
			group = null;
			Log.i(ACTIVITY_TAG,
					"public Group getGroupByName(String groupName) catch  Exception:"
							+ e.toString());

		} finally {
			if (db != null) {
				db.close();
			}
		}

		//

		if (group != null) {
			Log.i(ACTIVITY_TAG,
					"   public Group getGroupById(int id)" + group.toString());
		} else {
			Log.i(ACTIVITY_TAG, "   public Group getGroupById(int id) group== null");
		}
		return group;
	}
	
	@Override
	public List<Group> getGroupList() {
		// TODO Auto-generated method stub
		List<Group> listGroup = new ArrayList<Group>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT goupid, goupname from gouplist order by _id";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, null);
			while (cursor.moveToNext()) {
				index = 0;
				Group group = new Group();
				group.setId(cursor.getInt(index++));		
				group.setNickName(cursor.getString(index++));		
				listGroup.add(group);
			}
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return listGroup;
	}
	
	@Override
	public int findGroupMaxId() {
		// TODO Auto-generated method stub

		Log.i(TAG, "=====================findGroupMaxId");
		int id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = db.rawQuery("SELECT _id from gouplist order by _id desc",
					null);
			if (cursor.moveToFirst()) {
				id = cursor.getInt(0);
				Log.i(TAG, "=====================findGroupMaxId _id:" + id);
			}
		} catch (Exception e) {

			Log.i(TAG, "=====================findGroupMaxId Exception:" + e);

		} finally {
			if (db != null) {
				db.close();
			}
		}
		Log.i(TAG, "=====================findGroupMaxId return _id:" + id);
		return id;
	}

	@Override
	public boolean findGroupById(int id) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findGroupById(db, id);
		} catch (Exception e) {
			findOk = true;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return findOk;
	}	
	
	public boolean findGroupByName(String groupName) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findGroupByName(db, groupName);
		} catch (Exception e) {
			findOk = true;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return findOk;
	}

	/**
	 * ����ID,����һ����¼��id,û�еĻ�������false
	 * 
	 * @param true
	 * @return
	 * */
	private boolean findGroupById(SQLiteDatabase db, int id) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		// Cursor cursor = db.rawQuery("select * from person where id = ?", new
		// String[]{name});
		// int id =-1;
		boolean flag = false;
		if ((db == null) | (id <= 0)) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from gouplist where _id = ?",
				new String[] { String.valueOf(id) });
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * ����MAC,����һ����¼��id,û�еĻ�������false
	 * 
	 * @param true
	 * @return
	 * */
	private boolean findGroupByName(SQLiteDatabase db, String groupName) {
		 int id =-1;
		boolean flag = false;
		if ((db == null) | (groupName.isEmpty())) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from gouplist where goupname = ?",
				new String[] { groupName });
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		return flag;
	}

	private boolean updateGroup(SQLiteDatabase db, Group group) {
		boolean flag = false;
		String groupName = group.getNickName();

		if ((db == null) | (groupName.isEmpty())) {
			return flag;
		}
		try {
			db.execSQL("update  gouplist set " + "goupid=? where goupname=? ",
					new Object[] {group.getId(),group.getNickName()});			
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	private boolean insertGroup(SQLiteDatabase db, Group group) {
		Log.i(ACTIVITY_TAG,
				"   private boolean insertGroup(SQLiteDatabase db, Group group)"
						+ group.toString());
		boolean flag = false;
		String groupName = group.getNickName();
		if ((db == null) | (groupName.isEmpty())) {
			return flag;
		}
		try {
			db.execSQL("insert into gouplist (" + "goupid,goupname) values (?,?)",
					new Object[] { group.getId(), group.getNickName()});
			Log.i(TAG, "database insert succeed:"+group.getNickName());
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;

	}
	
}
