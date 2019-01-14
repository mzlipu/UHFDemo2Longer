package com.MyData;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shaojie on 2017/9/16.
 */


public class RunnerDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_EXTRA_RUNNER_INFO = "RunnerInfo";
    public static String DATA_BASE_NAME = "runner.db";
    private static final int VERSION = 1;
    private static RunnerDBHelper instance;

    private static Context context;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    public static RunnerDBHelper getInstance(Context context) {

        RunnerDBHelper.context = context;
        sp = context.getSharedPreferences("card", Context.MODE_PRIVATE);

        if (instance == null) {
            instance = new RunnerDBHelper(context);
        }
        return instance;
    }

    public RunnerDBHelper(Context context) {
        super(context, DATA_BASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String init = sp.getString("init", "0");
        if (init == "0") {
            createTableUser(db);
        }
        editor = sp.edit();
        editor.putString("init", "1");
        editor.commit();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            db.execSQL("drop table IF EXISTS " + TABLE_EXTRA_RUNNER_INFO);
            createTableUser(db);
        }
    }

    public void createTableUser(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EXTRA_RUNNER_INFO + " (ID INTEGER PRIMARY KEY AUTOINCREMENT"
                + ",name TEXT, code TEXT, gender TEXT, groupname TEXT, photo TEXT,confirm datetime)");
    }
}
