package com.app.tank;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.app.tank.common.PkgHelpers;
import com.app.tank.view.AppData;
import com.app.tank.view.AppsAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity extends Activity {

    private GridView main_apps;
    private AppsAdapter appsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initDatas();

        Field f;
    }

    private void initViews(){
        main_apps = findViewById(R.id.main_apps);
    }

    private void initDatas(){
        appsAdapter = new AppsAdapter(this);
        addAllPkgs();

        main_apps.setAdapter(appsAdapter);
    }

    private void addAllPkgs(){

        ArrayList<AppData> datas = PkgHelpers.getAllApps(this);

        for (AppData data : datas) {
            appsAdapter.addApp(data);
        }
    }


}