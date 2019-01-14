package com.uhfdemo2longer;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TabHost;
import android.widget.Toast;

public class TabMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tab_main);

        initView();
    }

    private void showToast(String info) {
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
    }

    private void initView() {


        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        LayoutInflater.from(this).inflate(R.layout.tab1,tabHost.getTabContentView(),true);
        LayoutInflater.from(this).inflate(R.layout.tab2,tabHost.getTabContentView(),true);
        LayoutInflater.from(this).inflate(R.layout.tab3,tabHost.getTabContentView(),true);

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("标签一").setContent(R.id.shit01));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("标签二").setContent(R.id.shit02));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("标签三").setContent(R.id.shit03));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {

                if (s == "tab1") {

                    showToast("tab1");

                } else if (s == "tab2") {

                    showToast("tab2");

                } else if (s == "tab3") {

                    showToast("tab3");
                }
            }
        });

    }
}
