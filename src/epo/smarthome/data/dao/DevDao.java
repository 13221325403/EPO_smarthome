package epo.smarthome.data.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import epo.smarthome.data.db.DatabaseHelper;
import epo.smarthome.device.Dev;
import epo.smarthome.service.DevService;

public class DevDao implements DevService {
	private DatabaseHelper dbHelper = null;

	private static final String TAG = "DevDao";
	private static final String ACTIVITY_TAG = "LogDevDao";

	public DevDao(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	@Override
	public boolean saveDev(Dev dev) {
		// TODO Auto-generated method stub
		boolean flag = false;
		if (dev == null) {
			return flag;
		}
		if (dev.getMacAddress().isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			if (findDevByMac(db, dev.getMacAddress())) { // �м�¼ update
				flag = updateDev(db, dev);
			} else { // �޼�¼ insert
				flag = insertDev(db, dev);
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
	public boolean removeDev(String mac) {
		// TODO Auto-generated method stub
		boolean flag = false;
		if (mac.isEmpty()) {
			return flag;
		}
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("delete from dev where devmac= ?", new Object[] { mac });
			flag = true;
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return flag;
	}

	public Dev getDevByMac(String mac) {
		// TODO Auto-generated method stub
		// List<Dev> listDev = new ArrayList<Dev>();
		Log.i(ACTIVITY_TAG, "public Dev getDevById(String Mac)" + mac);
		Dev dev = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT _id,devmac,name  from dev  where devmac=? ";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, new String[] { mac  });
			// if(cursor.moveToNext()){
			if (cursor.moveToFirst()) {
				index = 0;
				dev = new Dev();
				// Log.i(ACTIVITY_TAG,"index:"+index);
				dev.setId(cursor.getInt(index++));
				dev.setMacAddress(cursor.getString(index++));
				dev.setNickName(cursor.getString(index++));
				Log.i(ACTIVITY_TAG, "end index:" + index);
				// listDev.add(dev);
			}
		} catch (Exception e) {
			dev = null;
			Log.i(ACTIVITY_TAG,
					"public Dev getDevById(int id) catch  Exception:"
							+ e.toString());

		} finally {
			if (db != null) {
				db.close();
			}
		}

		//

		if (dev != null) {
			Log.i(ACTIVITY_TAG,
					"   public Dev getDevById(int id)" + dev.toString());
		} else {
			Log.i(ACTIVITY_TAG, "   public Dev getDevById(int id) dev== null");
		}
		return dev;
	}

	@Override
	public List<Dev> getDevList() {
		// TODO Auto-generated method stub
		List<Dev> listDev = new ArrayList<Dev>();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			int index = 0;
			String sqlStr = "SELECT _id, devmac,name, OnLine from dev order by _id";// ��id����
																	// ��desc������
			Cursor cursor = db.rawQuery(sqlStr, null);
			while (cursor.moveToNext()) {
				index = 0;
				Dev dev = new Dev();
				dev.setId(cursor.getInt(index++));
				dev.setMacAddress(cursor.getString(index++));				
				dev.setNickName(cursor.getString(index++));
				dev.setOnLine(cursor.getInt(index++));				
				listDev.add(dev);
			}
		} catch (Exception e) {

		} finally {
			if (db != null) {
				db.close();
			}
		}

		return listDev;
	}

	@Override
	public int findDevMaxId() {
		// TODO Auto-generated method stub

		Log.i(TAG, "=====================findDevMaxId");
		int id = -1;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			Cursor cursor = db.rawQuery("SELECT _id from dev order by _id desc",
					null);
			if (cursor.moveToFirst()) {
				id = cursor.getInt(0);
				Log.i(TAG, "=====================findDevMaxId _id:" + id);
			}
		} catch (Exception e) {

			Log.i(TAG, "=====================findDevMaxId Exception:" + e);

		} finally {
			if (db != null) {
				db.close();
			}
		}
		Log.i(TAG, "=====================findDevMaxId return _id:" + id);
		return id;
	}

	@Override
	public boolean findDevById(int id) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findDevById(db, id);
		} catch (Exception e) {
			findOk = true;
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return findOk;
	}

	public boolean findDevByMac(String mac) {
		// TODO Auto-generated method stub
		boolean findOk = true;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			findOk = findDevByMac(db, mac);
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
	private boolean findDevById(SQLiteDatabase db, int id) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		// Cursor cursor = db.rawQuery("select * from person where id = ?", new
		// String[]{name});
		// int id =-1;
		boolean flag = false;
		if ((db == null) | (id <= 0)) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from dev where _id = ?",
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
	private boolean findDevByMac(SQLiteDatabase db, String mac) {
		// SQLiteDatabase db = helper.getReadableDatabase();
		// Cursor cursor = db.rawQuery("select * from person where id = ?", new
		// String[]{name});
		 int id =-1;
		boolean flag = false;
		if ((db == null) | (mac.isEmpty())) {
			return flag;
		}
		Cursor cursor = db.rawQuery("select _id from dev where devmac = ?",
				new String[] { mac });
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
			if (id > 0) {
				flag = true;
			}
		}
		return flag;
	}


	private boolean updateDev(SQLiteDatabase db, Dev dev) {
		boolean flag = false;
		String mac = dev.getMac();

//		Log.i(ACTIVITY_TAG,
//				"   private boolean updateDev(SQLiteDatabase db, Dev dev)"
//						+ dev.toString());
		if ((db == null) | (mac.isEmpty())) {
			return flag;
		}
		try {
			// db.execSQL("update person set name=? ,phone=? where id=?",new
			// Object[]{name,phone,id});  
//			db.execSQL("update  dev set " + "name=?  where devmac=? ",
//					new Object[] { dev.getNickName(),dev.getMac()});
			db.execSQL("update  dev set " + "name=? ,OnLine=?  where devmac=? ",
					new Object[] { dev.getNickName(),dev.isOnLine(),dev.getMac()});			
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	private boolean insertDev(SQLiteDatabase db, Dev dev) {
		Log.i(ACTIVITY_TAG,
				"   private boolean insertDev(SQLiteDatabase db, Dev dev)"
						+ dev.toString());
		boolean flag = false;
		String mac = dev.getMac();
		if ((db == null) | (mac.isEmpty())) {
			return flag;
		}
		try {
//			db.execSQL("insert into dev (" + "devmac,name) values (?,?)",
//					new Object[] { dev.getMac(), dev.getNickName()});
			db.execSQL("insert into dev (" + "devmac,name,OnLine) values (?,?,?)",
					new Object[] { dev.getMac(), dev.getNickName(),dev.isOnLine()});
			Log.i(TAG, "database insert succeed:"+dev.getNickName());
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;

	}
}
