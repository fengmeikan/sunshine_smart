package com.sunshine.smart.utils;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mac on 16/8/10.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static String devices_table = "devices";
    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //设备表   ID  MAC  自定义名称  密码 类型
        String devices = "create table "+devices_table+"(id INTEGER PRIMARY KEY AUTOINCREMENT,address varchar(30),name varchar(30),password varchar(4),type integer default 0)";
        sqLiteDatabase.execSQL(devices);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
