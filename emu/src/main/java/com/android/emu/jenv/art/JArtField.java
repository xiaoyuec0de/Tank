package com.android.emu.jenv.art;

import java.lang.reflect.Field;

public class JArtField {

    private Field field;

    public JArtField(Field field){
        this.field =field;
    }

    public boolean match(String name, String signature) {
        return field.getName().equals(name) && AsmClass.getSignature(field.getType()).equals(signature);
    }
}
