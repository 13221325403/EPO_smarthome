package epo.smarthome.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import epo.smarthome.data.db.DatabaseHelper;
import epo.smarthome.device.Friend;
import epo.smarthome.service.FriendListService;

public class FriendListDao implements  FriendListService {

	private DatabaseHelper dbHelper = null;

	private static final String TAG = "FriendListDao";
	private static final String ACTIVITY_TAG = "FriendListDao";

	public FriendListDao(Context context) {
		dbHelper = new DatabaseHelper(context);
	}
	
	@Override
	public boolean saveFriend(Friend friend){
		// TODO Auto-generated method stub
		boolean flag = false;
		if (friend == null) {
			return flag;
		}
		if (friend.getMacAddress().isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
//			if (findFriendByMac(db, friend.getMacAddress())) { // �м�¼ update
			if (findFriendByMac(db, friend.getMacAddress(),friend.getLocalDevMac())) {
				flag = updateFriend(db, friend);
			} else { // �޼�¼ insert
				flag = insertFriend(db, friend);
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
	public boolean removeFriend(String friendMac) {
		// TODO Auto-generated method stub
		boolean flag = false;
		if (friendMac.isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("delete from friendlist where frdmac = ?", new Object[] { friendMac });
			flag = true;
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return flag;
	}

	public Friend getFriendByMac(String friendMac) {
		// TODO Auto-generated method stub
		Log.i(ACTIVITY_TAG, "public Friend getFriendByMac(String friendMac)" + friendMac);
		Friend friend = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT frdmac,frdname,userid,username,groupid, groupname,devmac from friendlist where frdmac=? ";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, new String[] { friendMac });
			// if(cursor.moveToNext()){
			if (cursor.moveToFirst()) {
				index = 0;
				friend = new Friend();
				friend.setMacAddress(cursor.getString(index++));
				friend.setNickName(cursor.getString(index++));
				friend.setUserID(cursor.getString(index++));
				friend.setUserName(cursor.getString(index++));
				friend.setGroupID(cursor.getInt(index++));
				friend.setGroupName(cursor.getString(index++));
				friend.setLocalDevMac(cursor.getString(index++));
				Log.i(ACTIVITY_TAG, "end index:" + index);
			}
		} catch (Exception e) {
			friend = null;
			Log.i(ACTIVITY_TAG,
					"public Friend getFriendByMac(String friendMac) catch  Exception:"
							+ e.toString());
		} finally {
			if (db != null) {
				db.close();
			}
		}
		if (friend != null) {
			Log.i(ACTIVITY_TAG,
					"   Friend getFriendByMac(String friendMac)" + friend.toString());
		} else {
			Log.i(ACTIVITY_TAG, "   public Friend getFriendByMac(String friendMac) friend == null");
		}
		return friend;
	}

	@Override
	public List<Friend> getFriendList() {
		// TODO Auto-generated method stub
		List<Friend> listFriend = new ArrayList<Friend>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT frdmac,frdname,userid,username,groupid, groupname,devmac from friendlist order by _id";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, null);
			while (cursor.moveToNext()) {
				index = 0;
				Friend friend = new Friend();
				friend.setMacAddress(cursor.getString(index++));
				friend.setNickName(cursor.getString(index++));
				friend.setUserID(cursor.getString(index++));
				friend.setUserName(cursor.getString(index++));
				friend.setGroupID(cursor.getInt(index++));
				friend.setGroupName(cursor.getString(index++));
				friend.setLocalDevMac(cursor.getString(index++));

				listFriend.add(friend);
			}
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return listFriend;
	}
	
	@Override
	public List<Friend> getFriendListByLocalDevMac(String localDevMac) {
		// TODO Auto-generated method stub
		List<Friend> listFriend = new ArrayList<Friend>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT frdmac,frdname,userid,username,groupid, groupname,devmac from friendlist where devmac=?";
																	
			Cursor cursor = db.rawQuery(sqlStr, new String[] { localDevMac });
			while (cursor.moveToNext()) {
				index = 0;
				Friend friend = new Friend();
				friend.setMacAddress(cursor.getString(index++));
				friend.setNickName(cursor.getString(index++));
				friend.setUserID(cursor.getString(index++));
				friend.setUserName(cursor.getString(index++));
				friend.setGroupID(cursor.getInt(index++));
				friend.setGroupName(cursor.getString(index++));
				friend.setLocalDevMac(cursor.getString(index++));

				listFriend.add(friend);
			}
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return listFriend;
	}

	@Override
	public int findFriendMaxId() {
		// TODO Auto-generated method stub

		Log.i(TAG, "=====================findFriendMaxId");
		int id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = db.rawQuery("SELECT _id from friendlist order by _id desc",
					null);
			if (cursor.moveToFirst()) {
				id = cursor.getInt(0);
				Log.i(TAG, "=====================findFriendMaxId _id:" + id);
			}
		} catch (Exception e) {

			Log.i(TAG, "=====================findFriendMaxId Exception:" + e);

		} finally {
			if (db != null) {
				db.close();
			}
		}
		Log.i(TAG, "=====================findFriendMaxId return _id:" + id);
		return id;
	}

	@Override
	public boolean findFriendById(int id) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findFriendById(db, id);
		} catch (Exception e) {
			findOk = true;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return findOk;
	}	

	public boolean findFriendByMac(String friendMac) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findFriendByMac(db, friendMac);
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
	private boolean findFriendById(SQLiteDatabase db, int id) {

		boolean flag = false;
		if ((db == null) | (id <= 0)) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from friendlist where _id = ?",
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
	private boolean findFriendByMac(SQLiteDatabase db, String friendMac) {
		 int id =-1;
		boolean flag = false;
		if ((db == null) | (friendMac.isEmpty())) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from friendlist where frdmac = ?",
				new String[] { friendMac });
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		return flag;
	}
	
	private boolean findFriendByMac(SQLiteDatabase db, String friendMac,String localMac) {
		 int id =-1;
		boolean flag = false;
		if ((db == null) | (friendMac.isEmpty()|localMac.isEmpty())) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from friendlist where frdmac = ?,devmac=?",
				new String[] { friendMac,localMac});
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		return flag;
	}
	
	private boolean updateFriend(SQLiteDatabase db, Friend friend) {
		boolean flag = false;
		String friendMac = friend.getMacAddress();

		if ((db == null) | (friendMac.isEmpty())) {
			return flag;
		}
		try {
			db.execSQL("update  friendlist set " + "frdname=?,userid=?,username=?,groupid=?, groupname=?,devmac=? where frdmac=? ",
					new Object[] {friend.getNickName(),friend.getUserID(),friend.getUserName(),friend.getGroupID(),friend.getGroupName(),friend.getLocalDevMac(),friend.getMacAddress()});			
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
	
	private boolean insertFriend(SQLiteDatabase db, Friend friend) {
		Log.i(ACTIVITY_TAG,
				"   private boolean insertFriend(SQLiteDatabase db, Friend friend)"
						+ friend.toString());
		boolean flag = false;
		String friendMac = friend.getMacAddress();
		if ((db == null) | (friendMac.isEmpty())) {
			return flag;
		}
		try {
			db.execSQL("insert into gouplist (" + "frdmac,frdname,userid,username,groupid, groupname,devmac) values (?,?,?,?,?,?,?)",
					new Object[] {friend.getMacAddress(),friend.getNickName(),friend.getUserID(),friend.getUserName(),friend.getGroupID(),friend.getGroupName(),friend.getLocalDevMac()});
			Log.i(TAG, "database insert succeed:"+friend.getNickName());
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;

	}


}
