package com.example.xiaoliweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class XiaoliWeatherOpenHelper extends SQLiteOpenHelper {
    public static final String CREATE_PROVINCE="creat table Province("+"id integer primary key autoincrement,"+"province_name text,"+"province_code text)";
	public static final String CREATE_CITY="creat table City("+"id integer primary key autoincrement,"+"City_name text,"+"City_code text)";
	public static final String CREATE_COUNTY="creat table County("+"id integer primary key autoincrement,"+"County_name text,"+"County_code text)";
	public XiaoliWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
