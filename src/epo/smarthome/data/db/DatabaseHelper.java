package epo.smarthome.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper��Ϊһ������SQLite�������࣬�ṩ��������Ĺ��ܣ�
 * ��һ��getReadableDatabase(),getWritableDatabase
 * ()���Ի��SQLiteDatabse����ͨ���ö�����Զ����ݿ���в���
 * �ڶ����ṩ��onCreate()��onUpgrade()�����ص����������������ڴ������������ݿ�ʱ�������Լ��Ĳ���
 * 
 * @author Administrator
 * 
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;

	private final static String dbFileName = "smarthome.db";

	public DatabaseHelper(Context context) {
		this(context, dbFileName, null, VERSION);
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, null, VERSION);
	}

	// ��SQLiteOepnHelper�����൱�У������иù��캯��
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		// ����ͨ��super���ø��൱�еĹ��캯��
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(" CREATE TABLE syscfg(  key varchar(200)  ,   value varchar(200)  ,  def_value varchar(200)	);    ");
//		db.execSQL(" CREATE TABLE dev ( _id integer primary key autoincrement,devmac text not null, name text);   ");
		db.execSQL(" CREATE TABLE dev ( _id integer primary key autoincrement,devmac text not null, name text, OnLine integer);   ");
//		db.execSQL(" CREATE TABLE dev ( _id integer primary key autoincrement,devmac text not null, name text,"
//				+ "OnLine integer,	update text, token text, IpPort text); ");
		db.execSQL(" CREATE TABLE friendlist ( _id integer primary key autoincrement,frdmac text, frdname text, "
											+ "	userid text,username text,groupid integer,groupname text,devmac text);   ");
		db.execSQL(" CREATE TABLE gouplist ( _id integer primary key autoincrement,goupid integer,goupname text);   ");
	}

	// ���ݿ��ļ��İ汾���ڸ��µ�ʱ�����,����Ϊ������µ��ֶΣ�Ҫ�޸İ汾��
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
