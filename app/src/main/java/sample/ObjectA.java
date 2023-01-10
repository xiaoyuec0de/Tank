package sample;


import android.util.Log;

public class ObjectA {

    public int a;

    public ObjectA(int v){
        this.a= v;
    }

    public void show(){
        Log.i("ObjectA","v:"+a);
    }

    public static void show1(boolean v){
        Log.i("ObjectA","boolean value:"+v);
    }
}
