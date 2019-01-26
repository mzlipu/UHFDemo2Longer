package com.uhfdemo2longer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.MyData.Runner;
import com.MyData.RunnerDBManager;
import com.MyData.Tools;
import com.handheld.UHFLonger.UHFLongerManager;
import com.handheld.UHFLongerDemo.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class AssetMove extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView tvTagCount;// tag count text view
    private TextView tvTagSum;// tag sum text view
    private ListView lvEpc;// epc list view
    private Button btnStart;// inventory button
    private Button btnClear;// clear button
    private Button btnMove;// Save button
    private Button btnSettings;
    private Button btnMainActivity;// Main Activity button
    private Button btnAssetAudit;// change to asset audit button
    private Spinner spinnerLocation;// Location Spinner
    private static List<Asset> listAsset = null;
    private static List<Asset> listAssetByLocation = null;
    private List<Location> listLocation = null;
    PgConnection pgConnection;
    private Dialog myDialog;
    private int currentFreq;
    private int currentFreqPosition = 1;
    private Button setFreq;
    private int currentLocationSpinnerPosition;
    private Location currentLocationSpinner;

    private TextView textview_epc;
    private Toast toast;
    private String root = "";
    private UHFLongerManager manager = null;
    private AssetMove.InventoryThread thread = null;
    private AssetMove.KeyReceiver keyReceiver = null;
    private RunnerDBManager dbHelper;
    private boolean connectFlag = false;
    private boolean runFlag = true;
    private boolean startFlag = false;
    private static final String TAG = "AssetMove";

    private int allCount = 0;// inventory count
    private Set<String> epcSet = null; // store different EPC
    private List<EpcDataModel> listEpc = null;// EPC list
    private Map<String, Integer> mapEpc = null; // store EPC position
    private EPCadapter adapter;// epc list adapter
    private static final String FOUND = "Found";
    private long lastTime = 0L;// record play sound time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //核心代码.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            //给状态栏设置颜色。我设置透明。
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        pgConnection = new PgConnection();
        pgConnection.execute();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_asset_move);
        initView();

        Util.initSoundPool(this);

        thread = new AssetMove.InventoryThread();
//        thread.start();

        myDialog = new Dialog(this);

        root = Environment.getExternalStorageDirectory().getPath();

        keyReceiver = new AssetMove.KeyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, intentFilter);

        prepareLocationSpinner();
    }

    protected void setTranslucentStatus() {
        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onStart() {
        dbHelper = RunnerDBManager.getInstance(this);
        connect();
        super.onStart();
    }

    @Override
    protected void onPause() {

        startFlag = false;
        super.onPause();
    }

    @Override
    protected void onRestart() {
        startFlag = true;
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        startFlag = false;
        runFlag = false;
        if (manager != null) {
            manager.close();
            manager = null;
        }
        unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    private void initView() {
        tvTagCount = (TextView) findViewById(R.id.textView_tag_count);
        lvEpc = (ListView) findViewById(R.id.listView_epc);
        btnStart = (Button) findViewById(R.id.button_start);
        tvTagSum = (TextView) findViewById(R.id.textView_tag);
        btnClear = (Button) findViewById(R.id.button_clear_epc);
        btnMove = (Button) findViewById(R.id.button_save_asset_move);
        btnSettings = (Button) findViewById(R.id.button_settings);
        btnMainActivity = (Button) findViewById(R.id.button_main_activity);
        btnAssetAudit = (Button) findViewById(R.id.button_asset_audit);
        spinnerLocation = (Spinner) findViewById(R.id.spinnerLocation);

        lvEpc.setFocusable(false);
        lvEpc.setClickable(false);
        lvEpc.setItemsCanFocus(false);
        lvEpc.setScrollingCacheEnabled(false);
        lvEpc.setOnItemClickListener(null);
        btnStart.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnMove.setOnClickListener(this);
        btnMainActivity.setOnClickListener(this);
        btnAssetAudit.setOnClickListener(this);
        spinnerLocation.setOnItemSelectedListener(this);

        btnStart.setEnabled(false);
    }

    private void prepareLocationSpinner(){
        listLocation = pgConnection.selectAllLocations();
        List<Location> categories = new ArrayList<>();
        categories.add(new Location("", "Select Location"));
        for (Location location : listLocation) {
            categories.add(location);
        }
        ArrayAdapter<Location> dataAdapter = new ArrayAdapter<Location>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerLocation.setAdapter(dataAdapter);
    }

    private class KeyReceiver extends BroadcastReceiver {
        private String TAG = "KeyReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (keyDown) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F1:
                        showtoast("f1");
                        break;
                    case KeyEvent.KEYCODE_F2:
                        showtoast("f2");
                        break;
                    case KeyEvent.KEYCODE_F3:
                        //Handheld Button
                        if(currentLocationSpinnerPosition != 0 ){
                            startFlag = true;
                            Log.i(TAG, "Star Scan");
                            runInventory();
                        }else{
                            showtoast("----- First Select Location -----");
                        }
                        break;
                    case KeyEvent.KEYCODE_F5:
                        showtoast("f5");
                        break;
                    case KeyEvent.KEYCODE_F4:
                        showtoast("f4");
                        break;
                }
            } else {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F4:
                        startFlag = false;
                        preepc = "";
                        Log.i(TAG, "Stop Scan");
                        break;
                }
            }
        }
    }

    private void showtoast(String info) {

        toast = Toast.makeText(AssetMove.this, info, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                startFlag = true;
                runInventory();
                break;
            case R.id.button_clear_epc:
                clearEpc();
                break;
            case R.id.button_save_asset_move:
                saveAssetMove();
                break;
            case R.id.button_main_activity:
                startFlag = false;
                runFlag = false;
                if (manager != null) {
                    manager.close();
                    manager = null;
                }
                unregisterReceiver(keyReceiver);
                super.onDestroy();
                Intent intent = new Intent(AssetMove.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.button_asset_audit:
                startFlag = false;
                runFlag = false;
                if (manager != null) {
                    manager.close();
                    manager = null;
                }
                unregisterReceiver(keyReceiver);
                super.onDestroy();
                Intent intentAudit = new Intent(AssetMove.this, AssetAudit.class);
                startActivity(intentAudit);
                break;
            case R.id.set_freq:
                SharedPreferences shared = getSharedPreferences("settings", 0);
                final int reg = shared.getInt("freband", currentFreq);
                if (manager.setFreBand((short) reg)) {
                    runOnUiThread(new Runnable() {
                        public void run() {

                            String a = getString(R.string._freBand);
                            String b = getResources().getStringArray(R.array.freBandArray)[reg];
                            showtoast(a + b);
                        }
                    });
                }
                break;
        }
    }

    private void connect() {
        try {
            manager = UHFLongerManager.getInstance();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    // TODO Auto-generated method stub
                    SharedPreferences shared = getSharedPreferences("settings", 0);
                    final int value = shared.getInt("power", 30);
                    if (manager.setOutPower((short) value)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                String temp = getString(R.string._power_now) + value;
//                                showtoast(temp);
                            }
                        });
                    }
                    final int reg = shared.getInt("freband", 1);
                    if (manager.setFreBand((short) reg)) {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                String a = getString(R.string._freBand);
                                String b = getResources().getStringArray(R.array.freBandArray)[reg];
//                                showtoast(a + b);
                            }
                        });
                    }
                }
            }, 1000);


        } catch (Exception ex) {

            ex.printStackTrace();
        }

        if (manager == null) {
            showtoast(getString(R.string.serialport_init_fail));
            return;
        } else {
            connectFlag = true;
        }
        Util.play(1, 0);
    }

    private void setButtonClickable(Button button, boolean flag) {
        button.setClickable(flag);
        if (flag) {
            button.setTextColor(Color.BLACK);
        } else {
            button.setTextColor(Color.GRAY);
        }
    }

    String preepc = "";

    private class InventoryThread extends Thread {
        private List<String> epcList;

        @Override
        public void run() {

            super.run();
            while (runFlag) {
                if (startFlag && manager != null) {
                    epcList = manager.inventoryRealTime();
                    if (epcList != null && !epcList.isEmpty()) {
                        for (String epc : epcList) {
                            if (preepc.equals(epc)) {
                                Log.e(TAG, "重复号码->" + epc);
                                break;
                            }
//                            Message message = handler.obtainMessage();
//                            message.what = 9876;
//                            Util.play(1, 0);
                            preepc = epc;
//                            Bundle bundle = new Bundle();
//                            bundle.putString("code", epc);
//                            message.setData(bundle);
//                            handler.sendMessage(message);

                            Message msg = new Message();
                            msg.what = 1;
                            Bundle b = new Bundle();
                            listAsset= pgConnection.selectAllAssets();
                            for (Asset asset1 : listAsset) {
                                System.out.println("Aaaaaaaasssssseeeeeetttttttttttttttttt-------------"+asset1.getRfidTags());
                                if(asset1.getRfidTags() == null || asset1.getLocation() == null){
                                    continue;
                                }else if(epc.equalsIgnoreCase(asset1.getRfidTags())){
                                    b.putString("epc", epc);
                                    b.putString("btAssetId", String.valueOf(asset1.getAssetID()));
                                    b.putString("currentLocation", currentLocationSpinner.getLocationName());
                                    b.putString("assetName", asset1.getAssetName());
                                    b.putString("previousLocationId", asset1.getLocationId());
                                    b.putString("previousLocation", asset1.getLocation());
                                    b.putString("status", "Not Matched");
                                    Util.play(1, 0);
                                    System.out.println("EEEEEEEEEEEPPPPPPPPPPPPPCCCCCCCCCCCCCC------------"+epc);
                                    for (Asset asset : listAssetByLocation) {
                                        if(asset.getRfidTags() == null){
                                            continue;
                                        }else {
                                            if(epc.equalsIgnoreCase(asset.getRfidTags())){
                                                b.putString("assetName", asset.getAssetName());
                                                b.putString("previousLocationId", asset.getLocationId());
                                                b.putString("previousLocation", asset.getLocation());
                                                b.putString("status", "Matched");
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            msg.setData(b);
                            dataHandler.sendMessage(msg);

                        }
                    }else{
                        System.out.println("Empty EPCList--------------------------------------");
                    }

                    epcList = null;
                    try {
                        Thread.sleep(20);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 9876:
                    Bundle bundle = msg.getData();
                    String epc = bundle.getString("code");
                    showRunner("25");
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import:
                doImport();
                break;
            case R.id.menu_clear:
                File file = new File(root + "//aa");
                Tools.deleteFile(file);
                dbHelper.clear();
                showtoast("执行成功");
                break;
            case R.id.menu_about:
                AboutDialog();
                break;
            case R.id.menu_test:
                showRunner("25");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showRunner(String key) {

        Runner runner = dbHelper.getRunner(key);
        if (runner != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("shit");
//            builder.setView(R.layout.dialog_runner);

            View temp = getLayoutInflater().inflate(R.layout.dialog_runner, null);
            TextView name = (TextView) temp.findViewById(R.id.textView_name);
            TextView code = (TextView) temp.findViewById(R.id.textView_code);
            TextView gender = (TextView) temp.findViewById(R.id.textView_gender);
            TextView group = (TextView) temp.findViewById(R.id.textView_group);
            ImageView photo = (ImageView) temp.findViewById(R.id.imageView_photo);
            builder.setView(temp);

            name.setText(runner.getName());
            code.setText(runner.getCode());
            gender.setText(runner.getGender());
            group.setText(runner.getGroup());

            photo.setImageBitmap(getBitmap(runner.getPhoto()));

            builder.show();

        } else {
            showtoast("查询失败");
        }
    }

    private Bitmap getBitmap(String path) {

        Bitmap bitmap = null;
        path = Environment.getExternalStorageDirectory().getPath() + path;
        bitmap = BitmapFactory.decodeFile(path);
        return bitmap;
    }

    int requestCode = 1;

    private void doImport() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.requestCode) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                String scheme = uri.getScheme();
                String path = GetPathFromUri4kitkat.getPath(this, uri);
                if (path == null) {
                    path = GetPathFromUri4kitkat.getDataColumn(this, uri, null, null);
                    if (path == null) {
                        showtoast("获取文件路径错误");
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            imporExcel(path);
                        }
                    }
                } else {
                    File file = new File(path);
                    if (file.exists()) {
                        imporExcel(path);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    ProgressDialog dialog;
    private Thread threadImport;

    private void imporExcel(String path) {

        //仅支持读取excel 97-2003
        try {

            String newFolder = root + "//aa";
            File temp = new File(newFolder);
            if (temp.exists() == false) {
                temp.mkdir();
            }

            Workbook book = Workbook.getWorkbook(new File(path));
            final Sheet sheet = book.getSheet(0);
            final int Rows = sheet.getRows();
            int Cols = sheet.getColumns();
            Log.d(TAG, "当前工作表的名字:" + sheet.getName());
            Log.d(TAG, "总行数:" + Rows + ", 总列数:" + Cols);

            final int copymax = Rows - 1;

            dialog = new ProgressDialog(this);
            dialog.setTitle("数据导入");
            dialog.setIcon(R.mipmap.ic_launcher_round);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax(copymax);
            dialog.show();

            final String source = "/download/ysj.jpg";
            threadImport = new Thread() {
                int step = 1;

                @Override
                public void run() {
                    super.run();
                    try {
                        for (int i = 1; i < Rows; i++) {

                            String name = ReadData(sheet, i, 0);
                            String code = ReadData(sheet, i, 1);
                            String gender = ReadData(sheet, i, 2);
                            String group = ReadData(sheet, i, 3);
                            String photo = ReadData(sheet, i, 4);
                            Log.d(TAG, name + " " + code + " " + photo);

                            String newPath = "//aa//" + Integer.toString(i) + ".jpg";
                            fileCopy(source, newPath);

                            dialog.setProgress(i);
                            Runner runner = new Runner();
                            runner.setName(name);
                            runner.setCode(code);
                            runner.setGender(gender);
                            runner.setGroup(group);
                            runner.setPhoto(newPath);
                            runner.setConfirm(getCurrentDateTime());
                            dbHelper.insert(runner);

                            //Thread.sleep(100);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    dialog.cancel();
                }
            };
            threadImport.start();


        } catch (java.io.IOException ex) {

            ex.printStackTrace();

        } catch (jxl.read.biff.BiffException ex) {
            ex.printStackTrace();
        }
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String temp = df.format(new Date());
        return temp;
    }

    private void fileCopy(String source, String density) {

        source = root + source;
        density = root + density;
        Log.d(TAG, density);
        Tools.copyFile(source, density);
    }

    public static String ReadData(Sheet excelSheet, int row, int col) {
        try {
            String CellData = "";
            Cell cell = excelSheet.getRow(row)[col];
            CellData = cell.getContents().toString();
            return CellData;
        } catch (Exception e) {
            return "";
        }
    }

    private void AboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.about);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("人像赋值比对系统 \n V1.0");
        builder.setNegativeButton("确定", null);
        builder.show();
    }

    private static class MyHandler extends Handler {
        private WeakReference<AssetMove> mWeakReference;

        public MyHandler(AssetMove assetMove) {
            mWeakReference = new WeakReference<AssetMove>(assetMove);
        }

        @Override
        public void handleMessage(Message msg) {
            AssetMove assetMove = mWeakReference.get();
            switch (msg.what) {
                case 1:
                    String epc = msg.getData().getString("epc");
                    String rssi = msg.getData().getString("rssi");
                    String status = msg.getData().getString("status");
                    String assetName = msg.getData().getString("assetName");
                    String previousLocationId = msg.getData().getString("previousLocationId");
                    String previousLocation = msg.getData().getString("previousLocation");
                    String currentLocation = msg.getData().getString("currentLocation");
                    String btAssetId = msg.getData().getString("btAssetId");
                    if (epc == null || epc.length() == 0) {
                        break;
                    }
                    int position;
                    assetMove.allCount++;

                    if (assetMove.epcSet == null) {// first add
                        assetMove.epcSet = new HashSet<String>();
                        assetMove.listEpc = new ArrayList<EpcDataModel>();
                        assetMove.mapEpc = new HashMap<String, Integer>();
                        assetMove.epcSet.add(epc);
                        assetMove.mapEpc.put(epc, 0);
                        EpcDataModel epcTag = new EpcDataModel();
                        epcTag.setepc(epc);
                        epcTag.setrssi(rssi);
                        epcTag.setStatus(status);
                        epcTag.setAssetName(assetName);
                        epcTag.setPreviousLocationId(previousLocationId);
                        epcTag.setPreviousLocation(previousLocation);
                        epcTag.setCurrentLocation(currentLocation);
                        epcTag.setBtAssetId(Integer.parseInt(btAssetId));
                        assetMove.listEpc.add(epcTag);
                        assetMove.adapter = new EPCadapter(assetMove,
                                assetMove.listEpc);
                        assetMove.lvEpc.setAdapter(assetMove.adapter);

                    } else {
                        if (assetMove.epcSet.contains(epc)) {// set already exit
                            position = assetMove.mapEpc.get(epc);
                            EpcDataModel epcOld = assetMove.listEpc.get(position);
//						    epcOld.setCount(epcOld.getCount() + 1);
                            epcOld.setStatus(status);
                            epcOld.setAssetName(assetName);
                            epcOld.setPreviousLocationId(previousLocationId);
                            epcOld.setPreviousLocation(previousLocation);
                            epcOld.setCurrentLocation(currentLocation);
                            epcOld.setBtAssetId(Integer.parseInt(btAssetId));
                            epcOld.setrssi(rssi);
                            assetMove.listEpc.set(position, epcOld);
                        } else {
                            assetMove.epcSet.add(epc);
                            assetMove.mapEpc.put(epc, assetMove.listEpc.size());
                            EpcDataModel epcTag = new EpcDataModel();
                            epcTag.setepc(epc);
                            epcTag.setrssi(rssi);
                            epcTag.setStatus(status);
                            epcTag.setAssetName(assetName);
                            epcTag.setPreviousLocationId(previousLocationId);
                            epcTag.setPreviousLocation(previousLocation);
                            epcTag.setCurrentLocation(currentLocation);
                            epcTag.setBtAssetId(Integer.parseInt(btAssetId));
                            assetMove.listEpc.add(epcTag);
                        }

                        /*if (System.currentTimeMillis() - assetMove.lastTime > 100) {
                            assetMove.lastTime = System.currentTimeMillis();
                            Util.play(1, 0);
                        }*/
                        assetMove.tvTagCount.setText("" + assetMove.allCount);
                        assetMove.tvTagSum.setText("" + assetMove.listEpc.size());
                        assetMove.adapter.notifyDataSetChanged();

                    }

                    break;
            }

            super.handleMessage(msg);
        }
    }

    private Handler dataHandler = new AssetMove.MyHandler(AssetMove.this);

    private boolean isRunning = false;
    private boolean isStart = false;
    private boolean keyControl = true;

    private void runInventory() {
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                if(!isRunning){
                    isRunning = true;
                    thread.start();
                }else{
                    startFlag = true;
                    resume();
                }
                btnStart.setText(getResources().getString(R.string.stop_inventory_epc));
                isStart = true;
                btnSettings.setVisibility(View.INVISIBLE);
            } else {
                try {
                    thread.wait();
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startFlag = false;
                preepc = "";
                btnStart.setText(getResources().getString(R.string.start_inventory_epc));
                isStart = false;
                btnSettings.setVisibility(View.VISIBLE);
            }
            keyControl = true;
        }
    }

    private void clearEpc() {
        if (epcSet == null) {
            return;
        }
        if (epcSet != null) {
            epcSet.removeAll(epcSet); // store different EPC
        }
        if (listEpc != null)
            listEpc.removeAll(listEpc);// EPC list
        if (mapEpc != null)
            mapEpc.clear(); // store EPC position
        if (adapter != null)
            adapter.notifyDataSetChanged();
        allCount = 0;
        tvTagSum.setText("0");
        tvTagCount.setText("0");
        spinnerLocation.setSelection(0);

        pgConnection = new PgConnection();
        pgConnection.execute();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        synchronized (thread) {
            thread.notifyAll();
        }
    }

    public void ShowPopup(View v) {
        TextView txtclose;
        final TextView seekBarView;
        Button  setPower;
        SeekBar seekBar;

        myDialog.setContentView(R.layout.custompopup);

        txtclose =(TextView) myDialog.findViewById(R.id.txt_close);
        seekBarView =(TextView) myDialog.findViewById(R.id.txt_seekbarview);

        seekBar =(SeekBar) myDialog.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress<5){
                    seekBarView.setText(""+5);
                }else{
                    seekBarView.setText(""+progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setPower =(Button) myDialog.findViewById(R.id.set_power);
        setPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences shared = getSharedPreferences("settings", 0);
                final int currentSeekBarValue = Integer.parseInt(seekBarView.getText().toString());
                final int value = shared.getInt("power", currentSeekBarValue);
                if (manager.setOutPower((short) value)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            String temp = getString(R.string._power_now) + value;
                            showtoast(temp);
                        }
                    });
                }
            }
        });

        txtclose.setText("X");
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        Spinner spinner = (Spinner) myDialog.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<String>();
        categories.add("RG_FRC");
        categories.add("RG_NA");
        categories.add("RG_EU3");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        spinner.setSelection(currentFreqPosition);

        setFreq =(Button) myDialog.findViewById(R.id.set_freq);
        setFreq.setOnClickListener(this);

        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner:
                if(position == 0){
                    currentFreqPosition = 0;
                    currentFreq = 0;
                }else if(position == 1){
                    currentFreqPosition = 1;
                    currentFreq = 1;
                }else if(position == 2){
                    currentFreqPosition = 2;
                    currentFreq = 6;
                }
                break;
            case R.id.spinnerLocation:
                currentLocationSpinnerPosition = position;
                if(position == 0){
                    showtoast("----- First Select Location -----");
                    btnStart.setEnabled(false);
                }else{
                    currentLocationSpinner = (Location) parent.getItemAtPosition(position);
                    listAssetByLocation= pgConnection.selectAllAssetsByLocationId(currentLocationSpinner.getLocationID());
                    btnStart.setEnabled(true);
                    Util.play(1, 0);
                    break;
                }
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void saveAssetMove(){
        if(listEpc != null){
            Boolean aBoolean = pgConnection.SaveAsssetMove(listEpc, currentLocationSpinner);
            if(aBoolean){
                showtoast("All Assets are Waiting For Approve Now.");
            }else{
                showtoast("Data Didn`t Move Properly.");
            }
        }else{
            showtoast("Please First Start Scanning Epc.");
        }
    }
}
