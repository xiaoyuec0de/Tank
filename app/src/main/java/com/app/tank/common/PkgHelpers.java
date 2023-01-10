package com.app.tank.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.app.tank.view.AppData;

import java.util.ArrayList;
import java.util.List;

public class PkgHelpers {

    public static ArrayList<AppData> getAllApps(Context context){
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> pkgs = pm.getInstalledPackages(0);

        ArrayList<AppData> datas = new ArrayList<>();

        for (PackageInfo info : pkgs){
            AppData data = new AppData();
            data.name = info.packageName;
            data.icon = info.applicationInfo.loadIcon(pm);

            datas.add(data);

        }

        return datas;
    }
}
