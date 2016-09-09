package com.sunshine.smart.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by mac on 16/8/10.
 */
public class DbDao {

    private final DbHelper helper;
    private SQLiteDatabase db;

    public DbDao(Context c) {
        helper = new DbHelper(c,"sunshine",null,1);
    }

    /**
     * 添加设备
     * @param device
     * @return
     */
    public long addDevice(Device device){
        db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("address",device.getAddress());
        cv.put("name",device.getName());
        cv.put("password",device.getPassword());
        cv.put("type",device.getType());
        long rs = db.insert(DbHelper.devices_table, null, cv);
        db.close();
        return rs;
    }

    /**
     * 删除设备
     * @param device
     * @return
     */
    public int delDevice(Device device){
        db = helper.getWritableDatabase();
        int rs;
        if (device.getAddress()!=null){
            String[] args = {String.valueOf(device.getAddress())};//mac
            rs = db.delete(DbHelper.devices_table, "address=?", args);
            return rs;
        }else if (device.getName()!=null){
            String[] args = {String.valueOf(device.getName())};//mac
            rs = db.delete(DbHelper.devices_table, "name=?", args);
            return rs;
        }else if (device.getType()!=null){
            String[] args = {String.valueOf(device.getType())};//mac
            rs = db.delete(DbHelper.devices_table, "type=?", args);
            return rs;
        }else {
            return 0;
        }
    }

    public Device getDevice(int type){
        db = helper.getReadableDatabase();
        Cursor cursor = db.query(DbHelper.devices_table, new String[]{"address", "name","password", "type"}, "type=?",new String[]{String.valueOf(type)}, null, null,"id", "1");

        if (cursor.moveToNext()){
//            Log.e("aaaaa",cursor.getColumnNames().length+"---"+cursor.getString(0)+"---"+cursor.getString(1)+"---"+cursor.getString(2)+"---"+cursor.getString(3));
            return new Device(cursor.getString(cursor.getColumnIndex("address")),cursor.getString(cursor.getColumnIndex("name")),cursor.getString(cursor.getColumnIndex("password")),cursor.getInt(cursor.getColumnIndex("type")));
        }else{
            return null;
        }
    }


}
