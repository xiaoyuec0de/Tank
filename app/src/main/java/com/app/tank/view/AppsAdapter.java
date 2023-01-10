package com.app.tank.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.tank.R;

import java.util.ArrayList;

public class AppsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<AppData> datas = new ArrayList<AppData>();

    public AppsAdapter(Context context){
        this.context = context;
    }

    public void addApp(AppData app){
        this.datas.add(app);
    }

    class ViewHolder{
        ImageView icon;
        TextView name;

        public ViewHolder(ImageView icon, TextView name) {
            this.icon = icon;
            this.name = name;
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int i) {
        return datas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null){
           convertView = View.inflate(context, R.layout.main_app_item,null);
           ImageView icon = convertView.findViewById(R.id.app_icon);
           TextView name = convertView.findViewById(R.id.app_name);

           holder = new ViewHolder(icon,name);
           convertView.setTag(holder);

        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppData data = datas.get(i);
        holder.icon.setImageDrawable(data.icon);
        holder.name.setText(data.name);

        return convertView;
    }
}
