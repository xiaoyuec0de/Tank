package com.android.emu.jenv;

import com.android.emu.jenv.mirror.JObject;

import java.util.HashMap;
import java.util.Iterator;

public class JReferencTable {

    private static final long MAX_ENTRIES = 10240;

    private long start;
    private long size;

    private HashMap<Long, JObject> tables;

    public JReferencTable(long start,long maxEntries){
        this.start = start;
        this.size = maxEntries;

        this.tables = new HashMap<>();
    }

    public void set(long idx, JObject object)throws Exception{
        if (!tables.containsKey(idx)){
            throw new Exception("Expected a index");
        }

        tables.put(idx,object);
    }

    public JObject get(long idx){
        return tables.get(idx);
    }

    public long add(JObject object){

        //寻找一个空的索引
        long idx = this.start;
        while (tables.containsKey(idx)){
            idx += 1;
        }

        tables.put(idx,object);

        return idx;
    }

    public boolean remove(JObject object){

        Iterator<Long> it =  tables.keySet().iterator();

        while (it.hasNext()){
            Long key = it.next();
            JObject value = tables.get(key);
            if (value == object){
                it.remove();
                return true;
            }
        }

        return false;
    }

    public boolean inRange(long idx){
        return (idx >= this.start) && (idx <= (this.start+this.size));
    }

    public void clear(){
        this.tables.clear();
    }


}
