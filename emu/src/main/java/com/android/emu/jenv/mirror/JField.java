package com.android.emu.jenv.mirror;

import com.android.emu.jenv.art.AsmClass;
import com.emu.log.Logger;

import java.lang.reflect.Field;

public class JField extends JObject{

    private Field field;

    public JField(Class klass, Field field) {
        super(klass);
        this.field = field;
    }

    public String getName(){
        return this.field.getName();
    }

    public Object getValue(Object o)throws Exception{
        field.setAccessible(true);
        return field.get(o);
    }

    public void setValue(Object o,Object value) throws Exception{
        field.setAccessible(true);
        Class<?> type = field.getType();
        Logger.info("Field value type:"+type.getName());
        if (type.isPrimitive()) {
            Number n = (Number) value;
            String name = type.getName();
            switch (name) {
                case "java.lang.Boolean":
                case "boolean":
                    field.setBoolean(o, value != null);
                    break;
                case "java.lang.Byte":
                case "byte":
                    field.setByte(o, n.byteValue());
                    break;
                case "java.lang.Short":
                case "short":
                    field.setShort(o, n.shortValue());
                    break;
                case "java.lang.Int":
                case "int":
                    field.setInt(o, n.intValue());
                    break;
                case "java.lang.Long":
                case "long":
                    field.setLong(o, n.longValue());
                    break;
                case "java.lang.Char":
                case "char":
                    field.setChar(o, (char) n.intValue());
                    break;
                case "java.lang.Double":
                case "double":
                    field.setDouble(o, n.doubleValue());
                    break;
                case "java.lang.Float":
                case "float":
                    field.setFloat(o, n.floatValue());
                    break;
            }
        }
        else {
            field.set(o,value);
        }

    }

    public boolean match(String name, String signature) {
        String fsig = AsmClass.getSignature(field);
        String fname = field.getName();
        return fname.equals(name) && fsig.equals(signature);
    }

    public Class<?> getType(){
        return field.getType();
    }
}
