package com.MyData;

/**
 * Created by ysj on 2017/9/22.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ysj on 2017/9/18.
 */

public class RunnerDBManager {

    private static final String TAG = "ExtraVoiceDBManager";
    private static final String EXCEPTION = "exception";
    private RunnerDBHelper mDBHelper = null;
    private static RunnerDBManager instance = null;

    public static RunnerDBManager getInstance(Context context) {
        if (instance == null) {
            instance = new RunnerDBManager(context.getApplicationContext());
        }
        return instance;
    }

    private RunnerDBManager(Context context) {
        mDBHelper = RunnerDBHelper.getInstance(context);
//        if (PreferencesUtils.getBoolean(context, ConstData.IS_READED_EXTRA_SOUND_DATA, true)) {
//            readExcelToDB(context);
//        }
    }

    public void insert(Runner runner) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", runner.getName());
            values.put("code", runner.getCode());
            values.put("gender", runner.getGender());
            values.put("groupname", runner.getGroup());
            values.put("photo", runner.getPhoto());
            values.put("confirm", runner.getConfirm());
            db.insert(RunnerDBHelper.TABLE_EXTRA_RUNNER_INFO, null, values);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public int count() {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        try {
            int count = 0;
            String sql = "select count(*) from " + RunnerDBHelper.TABLE_EXTRA_RUNNER_INFO;
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            count = cursor.getInt(0);
            return count;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void clear() {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        try {
            String sql = "delete from " + RunnerDBHelper.TABLE_EXTRA_RUNNER_INFO;
            db.execSQL(sql);
        } finally {
            db.close();
        }
    }

    public Runner getRunner(String key) {

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String sql = "select * from " + RunnerDBHelper.TABLE_EXTRA_RUNNER_INFO + " where code='" + key + "'";
        Runner runner = null;
        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {

                String name = cursor.getString(cursor.getColumnIndex("name"));
                String code = cursor.getString(cursor.getColumnIndex("code"));
                String gender = cursor.getString(cursor.getColumnIndex("gender"));
                String group = cursor.getString(cursor.getColumnIndex("groupname"));
                String photo = cursor.getString(cursor.getColumnIndex("photo"));
                String confirm = cursor.getString(cursor.getColumnIndex("confirm"));
                runner = new Runner();
                runner.setName(name);
                runner.setCode(code);
                runner.setGender(gender);
                runner.setGroup(group);
                runner.setPhoto(photo);
                runner.setConfirm(confirm);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return runner;
    }
}
