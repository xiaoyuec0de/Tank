package com.android.emu.jenv;

import com.android.emu.Emulator;
import com.android.emu.helper.MemoryHelper;
import com.android.emu.jenv.mirror.JArray;
import com.android.emu.jenv.mirror.JClass;
import com.android.emu.jenv.mirror.JConstructor;
import com.android.emu.jenv.mirror.JField;
import com.android.emu.jenv.mirror.JMethod;
import com.android.emu.jenv.mirror.JObject;
import com.android.emu.jenv.mirror.JObjectArray;
import com.android.emu.jenv.mirror.JPrimitiveArray;
import com.android.emu.jenv.mirror.JString;
import com.android.emu.memory.MemoryManager;
import com.emu.log.Logger;

import java.util.HashMap;

import unicorn.Unicorn;

public class JniEnv {

    private Emulator emulator;
    private Unicorn uc;
    private JClassLoader classLoader;
    private JReferencTable locals;
    private JReferencTable globals;

    private long ptr;
    private long table;

    private Jvm jvm;

    private MemoryManager memoryManager;

    //JniEnv 结构体的函数
    private HashMap<Long, IJFunc> tables;

    public JniEnv(Emulator emulator, JClassLoader classLoader, JHandler handler){
        this.emulator = emulator;
        this.uc = emulator.getUc();
        this.memoryManager = emulator.getMemoryManager();
        this.classLoader = classLoader;
        this.locals = new JReferencTable(1,2048);
        this.globals = new JReferencTable(4096L,512000L);

        this.tables = new HashMap<>();
        initJniTables();

        try {
            TableRet ret = handler.writeFunctionTable(tables);
            this.ptr = ret.ptr;
            this.table = ret.table;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setJvm(Jvm jvm){
        this.jvm = jvm;
    }

    public long getPtr(){
        return this.ptr;
    }

    public JObject getReference(long idx) throws Exception{
        if (idx == 0)return null;

        if (locals.inRange(idx)){
            return locals.get(idx);
        }

        if (globals.inRange(idx)){
            return globals.get(idx);
        }
        String error = String.format("Invalid reference(%d)", idx);
        Logger.error(error);

        throw new Exception(error);
    }

    public JObject getLocalReference(long idx){
        if (idx == 0) return null;

        if (locals.inRange(idx)) {
            return locals.get(idx);
        }

        return null;
    }

    public JObject getGlobalReference(long idx){
        if (idx == 0) return null;

        if (globals.inRange(idx)) {
            return globals.get(idx);
        }

        return null;
    }

    public long addLocalReference(JObject ref){
        return this.locals.add(ref);
    }

    public void setLocalReference(long idx, JObject ref) throws Exception {
        this.locals.set(idx,ref);
    }

    public void deleteLocalReference(JObject ref) throws Exception {
        this.locals.remove(ref);
    }

    public long addGlobalReference(JObject ref){
        return globals.add(ref);
    }


    public void setGlobalReference(long idx, JObject ref) throws Exception {
        this.globals.set(idx,ref);
    }

    public void deleteGlobalReference(JObject ref) throws Exception {
        this.globals.remove(ref);
    }


    private void addJniFunc(long idx, String name,int acount,IJCall callback) {
        tables.put(idx, new JFunc(name,acount,callback));
    }

    private void initJniTables() {
        addJniFunc(4,"get_version",0,this::notImplemented);
        addJniFunc(5,"define_class",0,this::notImplemented);
        addJniFunc(6,"FindClass",2,this::FindClass);
        addJniFunc(7,"from_reflected_method",0,this::notImplemented);
        addJniFunc(8,"from_reflected_field",0,this::notImplemented);
        addJniFunc(9,"toReflectedMethod",4,this::toReflectedMethod);
        addJniFunc(10,"get_superclass",0,this::notImplemented);

        addJniFunc(11,"is_assignable_from",0,this::notImplemented);
        addJniFunc(12,"to_reflected_field",0,this::notImplemented);
        addJniFunc(13,"throw",0,this::notImplemented);
        addJniFunc(14,"throw_new",0,this::notImplemented);
        addJniFunc(15,"exception_occurred",0,this::notImplemented);
        addJniFunc(16,"exception_describe",0,this::notImplemented);
        addJniFunc(17,"exceptionClear",0,this::ExceptionClear);
        addJniFunc(18,"fatal_error",0,this::notImplemented);
        addJniFunc(19,"push_local_frame",0,this::notImplemented);
        addJniFunc(20,"pop_local_frame",0,this::notImplemented);

        addJniFunc(21,"newGlobalRef",1,this::NewGlobalRef);
        addJniFunc(22,"deleteGlobalRef",2,this::deleteGlobalRef);
        addJniFunc(23,"deleteLocalRef",2,this::deleteLocalRef);
        addJniFunc(24,"IsSameObject",2,this::IsSameObject);
        addJniFunc(25,"NewLocalRef",2,this::NewLocalRef);
        addJniFunc(26,"ensure_local_capacity",0,this::notImplemented);
        addJniFunc(27,"alloc_object",0,this::notImplemented);
        addJniFunc(28,"NewObject",4,this::NewObjectV);
        addJniFunc(29,"NewObjectV",4,this::NewObjectV);
        addJniFunc(30,"NewObjectA",4,this::NewObjectV);

        addJniFunc(31,"GetObjectClass",2,this::GetObjectClass);
        addJniFunc(32,"IsInstanceOf",3,this::IsInstanceOf);
        addJniFunc(33,"GetMethodId",4,this::GetMethodId);
        addJniFunc(34,"CallObjectMethod",0,this::notImplemented);
        addJniFunc(35,"CallObjectMethodV",4,this::CallObjectMethodV);
        addJniFunc(36,"call_object_method_a",0,this::notImplemented);
        addJniFunc(37,"call_boolean_method",0,this::notImplemented);
        addJniFunc(38,"CallBooleanMethodV",4,this::CallBooleanMethodV);
        addJniFunc(39,"call_boolean_method_a",0,this::notImplemented);
        addJniFunc(40,"call_byte_method",0,this::notImplemented);

        addJniFunc(41,"CallByteMethodV",4,this::CallByteMethodV);
        addJniFunc(42,"call_byte_method_a",0,this::notImplemented);
        addJniFunc(43,"call_char_method",0,this::notImplemented);
        addJniFunc(44,"CallCharMethodV",4,this::CallObjectMethodV);
        addJniFunc(45,"call_char_method_a",0,this::notImplemented);
        addJniFunc(46,"call_short_method",0,this::notImplemented);
        addJniFunc(47,"CallShortMethodV",4,this::CallObjectMethodV);
        addJniFunc(48,"call_short_method_a",0,this::notImplemented);
        addJniFunc(49,"call_int_method",0,this::notImplemented);
        addJniFunc(50,"CallIntMethodV",4,this::CallIntMethodV);

        addJniFunc(51,"call_int_method_a",0,this::notImplemented);
        addJniFunc(52,"call_long_method",0,this::notImplemented);
        addJniFunc(53,"CallLongMethodV",4,this::CallObjectMethodV);
        addJniFunc(54,"call_long_method_a",0,this::notImplemented);
        addJniFunc(55,"call_float_method",0,this::notImplemented);
        addJniFunc(56,"CallFloatMethodV",4,this::CallObjectMethodV);
        addJniFunc(57,"call_float_method_a",0,this::notImplemented);
        addJniFunc(58,"call_double_method",0,this::notImplemented);
        addJniFunc(59,"CallDoubleMethodV",4,this::CallObjectMethodV);
        addJniFunc(60,"call_double_method_a",0,this::notImplemented);

        addJniFunc(61,"CallVoidMethod",4,this::CallObjectMethodV);
        addJniFunc(62,"CallVoidMethodV",4,this::CallObjectMethodV);
        addJniFunc(63,"call_void_method_a",0,this::notImplemented);

        addJniFunc(64,"call_nonvirtual_object_method",0,this::notImplemented);
        addJniFunc(65,"call_nonvirtual_object_method_v",0,this::notImplemented);
        addJniFunc(66,"call_nonvirtual_object_method_a",0,this::notImplemented);
        addJniFunc(67,"call_nonvirtual_boolean_method",0,this::notImplemented);
        addJniFunc(68,"call_nonvirtual_boolean_method_v",0,this::notImplemented);
        addJniFunc(69,"call_nonvirtual_boolean_method_a",0,this::notImplemented);

        addJniFunc(70,"call_nonvirtual_byte_method",0,this::notImplemented);
        addJniFunc(71,"call_nonvirtual_byte_method_v",0,this::notImplemented);
        addJniFunc(72,"call_nonvirtual_byte_method_a",0,this::notImplemented);
        addJniFunc(73,"call_nonvirtual_char_method",0,this::notImplemented);
        addJniFunc(74,"call_nonvirtual_char_method_v",0,this::notImplemented);
        addJniFunc(75,"call_nonvirtual_char_method_a",0,this::notImplemented);
        addJniFunc(76,"call_nonvirtual_short_method",0,this::notImplemented);
        addJniFunc(77,"call_nonvirtual_short_method_v",0,this::notImplemented);
        addJniFunc(78,"call_nonvirtual_short_method_a",0,this::notImplemented);
        addJniFunc(79,"call_nonvirtual_int_method",0,this::notImplemented);

        addJniFunc(80,"call_nonvirtual_int_method_v",0,this::notImplemented);
        addJniFunc(81,"call_nonvirtual_int_method_a",0,this::notImplemented);
        addJniFunc(82,"call_nonvirtual_long_method",0,this::notImplemented);
        addJniFunc(83,"call_nonvirtual_long_method_v",0,this::notImplemented);
        addJniFunc(84,"call_nonvirtual_long_method_a",0,this::notImplemented);
        addJniFunc(85,"call_nonvirtual_float_method",0,this::notImplemented);
        addJniFunc(86,"call_nonvirtual_float_method_v",0,this::notImplemented);
        addJniFunc(87,"call_nonvirtual_float_method_a",0,this::notImplemented);
        addJniFunc(88,"call_nonvirtual_double_method",0,this::notImplemented);
        addJniFunc(89,"call_nonvirtual_double_method_v",0,this::notImplemented);

        addJniFunc(90,"call_nonvirtual_double_method_a",0,this::notImplemented);
        addJniFunc(91,"call_nonvirtual_void_method",0,this::notImplemented);
        addJniFunc(92,"call_nonvirtual_void_method_v",0,this::notImplemented);
        addJniFunc(93,"call_nonvirtual_void_method_a",0,this::notImplemented);


        addJniFunc(94,"GetFieldId",4,this::GetFieldId);

        addJniFunc(95,"GetObjectField",3,this::GetObjectField);
        addJniFunc(96,"GetBooleanField",3,this::GetObjectField);
        addJniFunc(97,"GetByteField",3,this::GetObjectField);
        addJniFunc(98,"GetCharField",3,this::GetObjectField);
        addJniFunc(99,"GetShortField",3,this::GetObjectField);
        addJniFunc(100,"GetIntField",3,this::GetObjectField);
        addJniFunc(101,"GetLongField",3,this::GetObjectField);
        addJniFunc(102,"GetFloatField",3,this::GetObjectField);
        addJniFunc(103,"GetDoubleField",3,this::GetObjectField);

        addJniFunc(104,"SetObjectField",4,this::SetObjectField);
        addJniFunc(105,"SetBooleanField",4,this::SetObjectField);
        addJniFunc(106,"SetByteField",4,this::SetObjectField);
        addJniFunc(107,"SetCharField",4,this::SetObjectField);
        addJniFunc(108,"SetShortField",4,this::SetObjectField);
        addJniFunc(109,"SetIntField",4,this::SetObjectField);
        addJniFunc(110,"SetLongField",4,this::SetObjectField);
        addJniFunc(111,"SetFloatField",4,this::SetObjectField);
        addJniFunc(112,"SetDoubleField",4,this::SetObjectField);


        addJniFunc(113,"GetStaticMethodID",4,this::GetStaticMethodID);
        addJniFunc(114,"CallStaticObjectMethod",4,this::CallStaticObjectMethodV);
        addJniFunc(115,"CallStaticObjectMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(116,"call_static_object_method_a",0,this::notImplemented);
        addJniFunc(117,"CallStaticBooleanMethod",4,this::CallStaticObjectMethodV);
        addJniFunc(118,"CallStaticBooleanMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(119,"call_static_boolean_method_a",0,this::notImplemented);
        addJniFunc(120,"call_static_byte_method",0,this::notImplemented);
        addJniFunc(121,"CallStaticByteMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(122,"call_static_byte_method_a",0,this::notImplemented);
        addJniFunc(123,"call_static_char_method",0,this::notImplemented);
        addJniFunc(124,"CallStaticCharMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(125,"call_static_char_method_a",0,this::notImplemented);
        addJniFunc(126,"call_static_short_method",0,this::notImplemented);
        addJniFunc(127,"CallStaticShortMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(128,"call_static_short_method_a",0,this::notImplemented);
        addJniFunc(129,"call_static_int_method",0,this::notImplemented);
        addJniFunc(130,"CallStaticIntMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(131,"call_static_int_method_a",0,this::notImplemented);
        addJniFunc(132,"call_static_long_method",0,this::notImplemented);
        addJniFunc(133,"CallStaticLongMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(134,"call_static_long_method_a",0,this::notImplemented);
        addJniFunc(135,"call_static_float_method",0,this::notImplemented);
        addJniFunc(136,"CallStaticFloatMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(137,"call_static_float_method_a",0,this::notImplemented);
        addJniFunc(138,"call_static_double_method",0,this::notImplemented);
        addJniFunc(139,"CallStaticDoubleMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(140,"call_static_double_method_a",0,this::notImplemented);
        addJniFunc(141,"CallStaticVoidMethod",4,this::CallStaticObjectMethodV);
        addJniFunc(142,"CallStaticVoidMethodV",4,this::CallStaticObjectMethodV);
        addJniFunc(143,"call_static_void_method_a",0,this::notImplemented);

        addJniFunc(144,"GetStaticFieldID",4,this::GetStaticFieldID);
        addJniFunc(145,"GetStaticObjectField",3,this::GetStaticObjectField);
        addJniFunc(146,"GetStaticBooleanField",3,this::GetStaticObjectField);
        addJniFunc(147,"GetStaticByteField",3,this::GetStaticObjectField);
        addJniFunc(148,"GetStaticCharField",3,this::GetStaticObjectField);
        addJniFunc(149,"GetStaticShortField",3,this::GetStaticObjectField);
        addJniFunc(150,"GetStaticIntField",3,this::GetStaticObjectField);
        addJniFunc(151,"GetStaticLongField",3,this::GetStaticObjectField);
        addJniFunc(152,"GetStaticFloatField",3,this::GetStaticObjectField);
        addJniFunc(153,"GetStaticDoubleField",3,this::GetStaticObjectField);

        addJniFunc(154,"SetStaticObjectField",4,this::SetStaticObjectField);
        addJniFunc(155,"SetStaticBooleanField",4,this::SetStaticObjectField);
        addJniFunc(156,"SetStaticByteField",4,this::SetStaticObjectField);
        addJniFunc(157,"SetStaticCharField",4,this::SetStaticObjectField);
        addJniFunc(158,"SetStaticShortField",4,this::SetStaticObjectField);
        addJniFunc(159,"SetStaticIntField",4,this::SetStaticObjectField);
        addJniFunc(160,"SetStaticLongField",4,this::SetStaticObjectField);
        addJniFunc(161,"SetStaticFloatField",4,this::SetStaticObjectField);
        addJniFunc(162,"SetStaticDoubleField",4,this::SetStaticObjectField);

        addJniFunc(163,"NewString",3,this::NewString);
        addJniFunc(164,"GetStringLength",2,this::GetStringLength);
        addJniFunc(165,"GetStringChars",3,this::GetStringChars);
        addJniFunc(166,"ReleaseStringChars",3,this::ReleaseStringChars);
        addJniFunc(167,"NewStringUTF",2,this::notImplemented);
        addJniFunc(168,"get_string_utf_length",0,this::notImplemented);
        addJniFunc(169,"get_string_utf_chars",0,this::notImplemented);
        addJniFunc(170,"release_string_utf_chars",0,this::notImplemented);


        addJniFunc(171,"GetArrayLength",2,this::GetArrayLength);
        addJniFunc(172,"NewObjectArray",4,this::NewObjectArray);
        addJniFunc(173,"GetObjectArrayElement",3,this::GetObjectArrayElement);
        addJniFunc(174,"SetObjectArrayElement",4,this::SetObjectArrayElement);

        addJniFunc(175,"NewBooleanArray",2,this::NewBooleanArray);
        addJniFunc(176,"new_byte_array",0,this::notImplemented);
        addJniFunc(177,"new_char_array",0,this::notImplemented);
        addJniFunc(178,"new_short_array",0,this::notImplemented);
        addJniFunc(179,"new_int_array",0,this::notImplemented);
        addJniFunc(180,"new_long_array",0,this::notImplemented);
        addJniFunc(181,"new_float_array",0,this::notImplemented);
        addJniFunc(182,"new_double_array",0,this::notImplemented);

        addJniFunc(183,"GetBooleanArrayElements",3,this::GetBooleanArrayElements);
        addJniFunc(184,"get_byte_array_elements",0,this::notImplemented);
        addJniFunc(185,"get_char_array_elements",0,this::notImplemented);
        addJniFunc(186,"get_short_array_elements",0,this::notImplemented);
        addJniFunc(187,"get_int_array_elements",0,this::notImplemented);
        addJniFunc(188,"get_long_array_elements",0,this::notImplemented);
        addJniFunc(189,"get_float_array_elements",0,this::notImplemented);
        addJniFunc(190,"get_double_array_elements",0,this::notImplemented);

        addJniFunc(191,"ReleaseBooleanArrayElements",4,this::ReleaseBooleanArrayElements);
        addJniFunc(192,"release_byte_array_elements",0,this::notImplemented);
        addJniFunc(193,"release_char_array_elements",0,this::notImplemented);
        addJniFunc(194,"release_short_array_elements",0,this::notImplemented);
        addJniFunc(195,"release_int_array_elements",0,this::notImplemented);
        addJniFunc(196,"release_long_array_elements",0,this::notImplemented);
        addJniFunc(197,"release_float_array_elements",0,this::notImplemented);
        addJniFunc(198,"release_double_array_elements",0,this::notImplemented);

        addJniFunc(199,"GetBooleanArrayRegion",5,this::GetBooleanArrayRegion);
        addJniFunc(200,"get_byte_array_region",0,this::notImplemented);
        addJniFunc(201,"get_char_array_region",0,this::notImplemented);
        addJniFunc(202,"get_short_array_region",0,this::notImplemented);
        addJniFunc(203,"get_int_array_region",0,this::notImplemented);
        addJniFunc(204,"get_long_array_region",0,this::notImplemented);
        addJniFunc(205,"get_float_array_region",0,this::notImplemented);
        addJniFunc(206,"get_double_array_region",0,this::notImplemented);

        addJniFunc(207,"SetBooleanArrayRegion",5,this::SetBooleanArrayRegion);
        addJniFunc(208,"set_byte_array_region",0,this::notImplemented);
        addJniFunc(209,"set_char_array_region",0,this::notImplemented);
        addJniFunc(210,"set_short_array_region",0,this::notImplemented);
        addJniFunc(211,"set_int_array_region",0,this::notImplemented);
        addJniFunc(212,"set_long_array_region",0,this::notImplemented);
        addJniFunc(213,"set_float_array_region",0,this::notImplemented);
        addJniFunc(214,"set_double_array_region",0,this::notImplemented);

        addJniFunc(215,"RegisterNatives",4,this::RegisterNatives);
        addJniFunc(216,"unregister_natives",0,this::notImplemented);

        addJniFunc(217,"monitor_enter",0,this::notImplemented);
        addJniFunc(218,"monitor_exit",0,this::notImplemented);

        addJniFunc(219,"GetJavaVM",2,this::GetJavaVM);

        addJniFunc(220,"get_string_region",0,this::notImplemented);
        addJniFunc(221,"get_string_utf_region",0,this::notImplemented);
        addJniFunc(222,"get_primitive_array_critical",0,this::notImplemented);
        addJniFunc(223,"release_primitive_array_critical",0,this::notImplemented);
        addJniFunc(224,"get_string_critical",0,this::notImplemented);
        addJniFunc(225,"release_string_critical",0,this::notImplemented);
        addJniFunc(226,"new_weak_global_ref",0,this::notImplemented);
        addJniFunc(227,"delete_weak_global_ref",0,this::notImplemented);
        addJniFunc(228,"exception_check",0,this::notImplemented);
        addJniFunc(229,"new_direct_byte_buffer",0,this::notImplemented);
        addJniFunc(230,"get_direct_buffer_address",0,this::notImplemented);
        addJniFunc(231,"get_direct_buffer_capacity",0,this::notImplemented);
        addJniFunc(232,"get_object_ref_type",0,this::notImplemented);

    }

    private JRet SetBooleanArrayRegion(Unicorn uc, long[] args) throws Exception{
        JPrimitiveArray array = (JPrimitiveArray) getReference(args[1]);
        int start = (int) args[2];
        int length = (int) args[3];
        long ptr = args[4];


//        long ptr = memoryManager.allocate(values.length);
        boolean [] values = (boolean[]) array.getValue();

        for (int i=start;i<length;i++) {

            long v = MemoryHelper.readLong(uc,ptr,8);

            values[i]= v == JConst.JNI_TRUE;

            ptr++;
        }

        return new JRet();

    }

    private JRet GetBooleanArrayRegion(Unicorn uc, long[] args) throws Exception {
        JPrimitiveArray array = (JPrimitiveArray) getReference(args[1]);
        int start = (int) args[2];
        int length = (int) args[3];
        long ptr = args[4];


        boolean [] values = (boolean[]) array.getValue();

//        long ptr = memoryManager.allocate(values.length);

        for (int i=start;i<length;i++) {
            boolean v = values[i];
            if (v) {
                MemoryHelper.writeValue(uc, ptr, JConst.JNI_TRUE, 8);
            } else {
                MemoryHelper.writeValue(uc, ptr, JConst.JNI_FALSE, 8);
            }

            ptr++;
        }

        return new JRet(ptr);

    }

    private JRet ReleaseBooleanArrayElements(Unicorn uc, long[] args) {
        //未实现
        return new JRet();
    }

    /*
    * Primitive类型的，返回首地址
    *
    * */
    private JRet GetBooleanArrayElements(Unicorn uc, long[] args) throws Exception {

        JPrimitiveArray array = (JPrimitiveArray) getReference(args[1]);

        boolean [] values = (boolean[]) array.getValue();

        long ptr = memoryManager.allocate(values.length);

        for (boolean v : values) {
            if (v) {
                MemoryHelper.writeValue(uc, ptr, JConst.JNI_TRUE, 1);
            } else {
                MemoryHelper.writeValue(uc, ptr, JConst.JNI_FALSE, 1);
            }

            ptr++;
        }

        return new JRet(ptr);
    }

    private JRet NewBooleanArray(Unicorn uc, long[] args) {
        int size = (int) args[1];

        JClass clzz = new JClass(boolean.class);
        JPrimitiveArray array = new JPrimitiveArray(boolean.class);
        array.setJClass(clzz);

        boolean [] value = new boolean[size];
        array.setValue(value);

        long ref = addLocalReference(array);

        return new JRet(ref);

    }

    private JRet SetObjectArrayElement(Unicorn uc, long[] args) throws Exception {
        long ref = args[1];
        long size = args[2];
        long lref = args[3];

        JArray array = (JArray) getReference(ref);
        Object [] values = (Object[]) array.getValue();

        JObject object = getReference(lref);

        int offset = (int) size;
        values[offset] = object.getValue();


        return new JRet();
    }

    private JRet GetObjectArrayElement(Unicorn uc, long[] args) throws Exception {
        long ref = args[1];
        long size = args[2];

        JArray array = (JArray) getReference(ref);

        Object [] values = (Object[]) array.getValue();

        int offset = (int) size;
        Object obj =values[offset];

        JObject object = new JObject(array.getKlass());
        object.setJClass(array.getJClass());
        object.setValue(obj);

        long lref = addLocalReference(object);

        return new JRet(lref);

    }

    private JRet GetArrayLength(Unicorn uc, long[] args) throws Exception{
        long ref = args[1];

        JArray array = (JArray) getReference(ref);

        Object [] values = (Object[]) array.getValue();

        return new JRet(values.length);
    }

    private JRet NewObjectArray(Unicorn uc, long[] args) throws Exception{

        long size = args[1];
        long class_idx = args[2];

        JObject object = getReference(class_idx);
        if (!(object instanceof JClass)){
            throw new Exception("Except a class");
        }

        JClass clzz = (JClass) object;

        Class<?> klass = clzz.getKlass();

        int len = (int) size;
        Object [] value = new Object[len];

        JObjectArray array = new JObjectArray(klass);
        array.setJClass(clzz);

        array.setValue(value);

        long ref = addLocalReference(array);

        return new JRet(ref);
    }



    private JRet ReleaseStringChars(Unicorn uc, long[] args) throws Exception{
        long ref = args[1];
        long addr = args[2];

        if (addr != 0){
            memoryManager.free(addr);
        }

        return new JRet();
    }

    private JRet GetStringChars(Unicorn uc, long[] args) throws Exception {
        long ref = args[1];
        long copy = args[2];

        JString jstr = (JString) getReference(ref);
        String str = (String) jstr.getValue();

        // 因为jvm使用java模拟的，所以没有直接返回的一说，都是copy的
        byte [] bb = str.getBytes();

        long addr = memoryManager.allocate(bb.length+1);

        MemoryHelper.writeBytes(uc,addr,bb);


        return new JRet(addr);
    }

    private JRet GetStringLength(Unicorn uc, long[] args) throws Exception{
        long ref = args[1];

        JString jstr = (JString) getReference(ref);

        String str = (String) jstr.getValue();
        int len = str.length();

        return new JRet(len);
    }

    private JRet NewString(Unicorn uc, long[] args) throws Exception{
        long ptr = args[1];
        long size = args[2];

        byte [] bb = MemoryHelper.readByteArray(uc,ptr,size);

        String str = new String(bb);
        JString jstr = new JString(String.class);
        jstr.setValue(str);

        long ref = addLocalReference(jstr);

        return new JRet(ref);

    }

    private JRet GetStaticObjectField(Unicorn uc, long[] args) throws Exception{
        long class_idx = args[1];
        long field_idx = args[2];

        JObject object = getReference(class_idx);
        if (!(object instanceof JClass)){
            throw new Exception("Except a class");
        }

        JClass clzz = (JClass) object;

        JField field = clzz.findField(field_idx);

        if (field == null){
            throw new Exception(String.format("Could not find field %d in object %s by id.", field_idx,object.getKlass().getName()));
        }

        Logger.info(String.format("JNIEnv->GetObjectField(%s, %s) was called",object.getJClass().getName(),field.getName()));

        Object value = field.getValue(null);
        // Parse arguments
        Class<?> type = field.getType();
        Logger.info(String.format("Field type:%s", type.getName()));
        return genJRet(type,value);
    }

    private JRet SetStaticObjectField(Unicorn uc, long[] args) throws Exception {
        long class_idx = args[1];
        long field_idx = args[2];

        //普通类型是值，对象是引用号码
        long value = args[3];

        JObject object = getReference(class_idx);
        if (!(object instanceof JClass)){
            throw new Exception("Except a class");
        }
        JClass clzz = (JClass) object;

        JField field = clzz .findField(field_idx);
        if (field == null){
            throw new Exception(String.format("Could not find field %d in object %s by id.", field_idx,object.getKlass().getName()));
        }

        Class<?> type = field.getType();
        if (type.isPrimitive()) {
            if (type.getName().equals("java.lang.Boolean")){
                if(value == 0){
                    field.setValue(null, null);
                }else {
                    field.setValue(null, value);
                }
            }else {
                field.setValue(null, value);
            }
        }else {
            //获取实际的对象
            JObject obj = getReference(value);
            field.setValue(object.getValue(),obj.getValue());
        }

        return new JRet();

    }

    private JRet GetStaticFieldID(Unicorn uc, long[] args) throws Exception{
        return GetFieldIdInternal(uc,args,"GetStaticFieldID");
    }

    private JRet GetStaticMethodID(Unicorn uc, long[] args) throws Exception{
        return GetMethodIDInternal(uc, args,"GetStaticMethodID");
    }

    private JRet CallStaticObjectMethodV(Unicorn uc, long[] args) throws Exception {
        long clzz_idx = args[1];
        long method_ix = args[2];

        JObject object = getReference(clzz_idx);

        if (!(object instanceof JClass)){
            throw new Exception("Except a class");
        }

        JClass clzz = (JClass) object;

        JMethod method = clzz.findMethod(method_ix);

        if (method == null){
            throw new Exception(String.format("Could not find method %d in clzz %s by id.", method_ix,clzz.getName()));
        }

        Logger.info(String.format("JNIEnv->CallStaticObjectMethodV(%s, %s) was called",object.getJClass().getName(),method.getName()));

        // Parse arguments
        Object[] params = readArgsV(uc,3,args,method.getMethod().getParameterTypes());

        Object value = method.invoke(null,params);

        JRet ret = new JRet();
        if (value == null){
            return ret;
        }
        /*
         * Primitive 类型，读取值
         * 引用类型，添加引用
         * */
        Class<?> type = method.getMethod().getReturnType();
        Logger.info(String.format("Return type:%s", type.getName()));

        return genJRet(type,value);


    }

    private JRet GetMethodIDInternal(Unicorn uc, long[] args, String methodName) throws Exception {
        JClass clzz = (JClass) getReference(args[1]);
        String name = MemoryHelper.readUTF8(uc, args[2]);
        String signature = MemoryHelper.readUTF8(uc, args[3]);

        if (clzz != null) {
            Logger.info(String.format("JNIEnv->%s(%d:%s, %s, %s) was called",methodName, args[1], clzz.getName(), name, signature));
        } else {
            Logger.error(String.format("JNIEnv->%s,No Reference:%x class",methodName, args[1]));
        }
        JMethod method = clzz.findMethod(name, signature);

        if (method != null) {
            return new JRet(method.getIndex());
        }

        JConstructor constructor = clzz.findConstructor(name, signature);
        if (constructor != null) {
            return new JRet(constructor.getIndex());
        }

        return new JRet(0);
    }


    private JRet SetObjectField(Unicorn uc, long[] args) throws Exception {
        long obj_idx = args[1];
        long field_idx = args[2];

        //普通类型是值，对象是引用号码
        long value = args[3];

        JObject object = getReference(obj_idx);
        JField field = object.getJClass().findField(field_idx);
        if (field == null){
            throw new Exception(String.format("Could not find field %d in object %s by id.", field_idx,object.getKlass().getName()));
        }

        Class<?> type = field.getType();
        if (type.isPrimitive()) {
            if (type.getName().equals("java.lang.Boolean")){
                if(value == 0){
                    field.setValue(object.getValue(), null);
                }else {
                    field.setValue(object.getValue(), value);
                }
            }else {
                field.setValue(object.getValue(), value);
            }
        }else {
            //获取实际的对象
            JObject obj = getReference(value);
            field.setValue(object.getValue(),obj.getValue());
        }

        return new JRet();

    }

    private JRet GetObjectField(Unicorn uc, long[] args) throws Exception {
        long obj_idx = args[1];
        long field_idx = args[2];

        JObject object = getReference(obj_idx);
        JField field = object.getJClass().findField(field_idx);

        if (field == null){
            throw new Exception(String.format("Could not find field %d in object %s by id.", field_idx,object.getKlass().getName()));
        }

        Logger.info(String.format("JNIEnv->GetObjectField(%s, %s) was called",object.getJClass().getName(),field.getName()));

        Object value = field.getValue(object.getValue());
        // Parse arguments
        Class<?> type = field.getType();
        Logger.info(String.format("Field type:%s", type.getName()));
        return genJRet(type,value);
    }

    private JRet genJRet(Class<?> type,Object value){
        JRet ret = new JRet();
        if (type.isPrimitive()){
            Number n = (Number) value;
            ret.setValue(n.longValue());
        }else {
            JObject jObject = new JObject(value.getClass());
            jObject.setValue(value);
            long ref = addLocalReference(jObject);
            ret.setValue(ref);
        }

        return ret;
    }

    private JRet GetFieldIdInternal(Unicorn uc, long[] args,String fieldName) throws Exception {
        JClass clzz = (JClass) getReference(args[1]);
        String name = MemoryHelper.readUTF8(uc,args[2]);
        String signature = MemoryHelper.readUTF8(uc,args[3]);

        if (clzz != null) {
            Logger.info(String.format("JNIEnv->%s(%d:%s, %s, %s) was called", fieldName,args[1], clzz.getName(), name, signature));
        }else {
            Logger.error(String.format("JNIEnv->%s,No Reference:%x class",fieldName, args[1]));
        }
        JField field = clzz.findField(name,signature);

        if (field != null){
            return new JRet(field.getIndex());
        }


        return new JRet(0);

    }


    private JRet GetFieldId(Unicorn uc, long[] args) throws Exception {
        return GetFieldIdInternal(uc,args,"GetIntField");
    }

    private JRet CallByteMethodV(Unicorn uc, long[] args) throws Exception {
        return CallObjectMethodV(uc,args);
    }

    private JRet CallBooleanMethodV(Unicorn uc, long[] args) throws Exception {
        return CallObjectMethodV(uc,args);
    }

    private JRet CallIntMethodV(Unicorn uc, long[] args) throws Exception {
        return CallObjectMethodV(uc,args);
    }

    private JRet CallObjectMethodV(Unicorn uc, long[] args) throws Exception {
        long obj_idx = args[1];
        long method_ix = args[2];

        JObject object = getReference(obj_idx);
        JMethod method = object.getJClass().findMethod(method_ix);

        if (method == null){
            throw new Exception(String.format("Could not find method %d in object %s by id.", method_ix,object.getKlass().getName()));
        }

        Logger.info(String.format("JNIEnv->CallObjectMethodV(%s, %s) was called",object.getJClass().getName(),method.getName()));

        // Parse arguments
        Object[] params = readArgsV(uc,3,args,method.getMethod().getParameterTypes());

        Object value =method.invoke(object.getValue(),params);

        JRet ret = new JRet();
        if (value == null){
            return ret;
        }
        /*
        * Primitive 类型，读取值
        * 引用类型，添加引用
        * */
        Class<?> type = method.getMethod().getReturnType();
        Logger.info(String.format("Return type:%s", type.getName()));

        return genJRet(type,value);
    }



    private JRet GetMethodId(Unicorn uc, long[] args) throws Exception {
        return GetMethodIDInternal(uc, args, "GetMethodId");
    }

    private JRet GetJavaVM(Unicorn uc, long[] args) {
        long vm = args[1];
        MemoryHelper.writePtr32(uc,vm,this.jvm.getPtr());

        return new JRet(JConst.JNI_OK);
    }

    private JRet IsInstanceOf(Unicorn uc, long[] args) throws Exception {
        long object_idx = args[1];
        long clzz_idx = args[2];

        JObject o1 = getReference(object_idx);
        JObject o2 = getReference(clzz_idx);


        boolean isInstance = o1.getJClass().isInstanceOf(o2.getJClass());
        // 对父类的判断

        JRet ret = new JRet();
        if (isInstance) {
            ret.setValue(JConst.JNI_TRUE);
        }else {
            ret.setValue(JConst.JNI_FALSE);
        }

        return ret;

    }

    private JRet GetObjectClass(Unicorn uc, long[] args) throws Exception {
        long ref_idx = args[1];

        JObject ref = getReference(ref_idx);

        return new JRet(ref.getIndex());
    }

    private Object[] readArgsV(Unicorn uc,int offset,long [] argv,Class<?>[] types) throws Exception {
        if (types == null){
            return null;
        }

        Object [] args = new Object[types.length];
        //从UC内存中获取参数
        for (int i=0;i<types.length;i++){
            Class<?> type = types[i];
            long value = argv[offset+i];
//            Logger.info("type:"+type.getName()+",value:"+value);

            //基本类型读取
            String name = type.getName();
            if (type.isPrimitive()){
                if (name.equals("int")){
                    args[i] = Long.valueOf(value).intValue();
//                    Logger.info("arg value:"+args[i]);
                }else if(name.equals("boolean")){
                    if (value == JConst.JNI_TRUE){
                        args[i] = true;
                    }else {
                        args[i]=false;
                    }
                }else {
                    args[i] = value;
                }
            }else {
                JObject obj = getReference(value);
                args[i] = obj.getValue();

            }
        }

        return args;

    }

    private JRet NewObjectV(Unicorn uc, long[] args) throws Exception {
        long class_idx = args[1];
        long constructor_idx = args[2];

        Logger.info(String.format("JNIEnv->NewObjectV(%x, %x)", class_idx,constructor_idx));

        JObject jobj = getReference(class_idx);

        if (!(jobj instanceof JClass)){
            throw new Exception("Expected a jclass.");
        }

        JClass clzz = (JClass) jobj;

        // Get constructor method.
        JConstructor constructor = clzz.findConstructor(constructor_idx);

        Logger.info(String.format("JNIEnv->NewObjectV(%s, %s) was called", clzz.getName(),constructor.getName()));


        // 读取方法的参数
        Class<?>[] types = constructor.getParameterTypes();
        Object[] cargs = readArgsV(uc,3,args, types);

        // 调用
        Object newInstance = constructor.newInstance(cargs);
        JObject obj = new JObject(clzz.getKlass());
        obj.setJClass(clzz);
        obj.setValue(newInstance);

        long local_ref_idx = addLocalReference(obj);

        return new JRet(local_ref_idx);
    }

    private JRet NewLocalRef(Unicorn uc, long[] args) throws Exception {
        long ref_idx = args[1];

        Logger.info(String.format("JNIEnv->NewLocalRef(%d) was called", ref_idx));


        JObject def = getReference(ref_idx);
        if (def == null){
            return new JRet();
        }

        long ref = addLocalReference(def);

        return new JRet(ref);
    }

    private JRet IsSameObject(Unicorn uc, long[] args) throws Exception {
        long ref1 = args[1];
        long ref2 = args[2];
        Logger.info(String.format("JNIEnv->IsSameObject(%d, %d) was called", ref1,ref2));

        JRet ret = new JRet();

        if (ref1 == 0 && ref2 == 0){
            return ret;
        }

        JObject def1 = getReference(ref1);
        JObject def2 = getReference(ref2);

        if (def1 == def2){
            ret.setValue(JConst.JNI_TRUE);
        }else {
            ret.setValue(JConst.JNI_FALSE);
        }

        return ret;
    }

    private JRet deleteLocalRef(Unicorn uc, long[] args) throws Exception {
        long ref_idx = args[1];

        Logger.info(String.format("JNIEnv->DeleteLocalRef(%d) was called", ref_idx));

        if (ref_idx == 0){
            return new JRet();
        }

        JObject ref = getLocalReference(ref_idx);

        deleteLocalReference(ref);

        return new JRet();
    }

    //Deletes the global reference pointed to by globalRef.
    private JRet deleteGlobalRef(Unicorn uc, long[] args) throws Exception {
        long ref_idx = args[1];

        Logger.info(String.format("JNIEnv->DeleteGlobalRef(%d) was called", ref_idx));

        if (ref_idx == 0){
            return new JRet();
        }

        JObject ref = getGlobalReference(ref_idx);

        deleteGlobalReference(ref);

        return new JRet();
    }

    /*
    * Creates a new global reference to the object referred to by the obj argument. The obj argument may be a
    * global or local reference. Global references must be explicitly disposed of by calling DeleteGlobalRef().
    * */
    private JRet NewGlobalRef(Unicorn uc, long[] args) throws Exception{
        long ref_idx = args[1];

        Logger.info(String.format("JNIEnv->NewGlobalRef(%d) was called", ref_idx));

        if (ref_idx == 0){

            return new JRet();
        }

        JObject def = getLocalReference(ref_idx);
        if (def == null){
            throw new Exception("Invalid local reference obj.");
        }

        long g_ref_idx = addGlobalReference(def);

        return new JRet(g_ref_idx);
    }

    /*
    * Clears any exception that is currently being thrown.
      If no exception is currently being thrown, this routine has no effect.
    * */
    private JRet ExceptionClear(Unicorn uc, long[] args) {
        Logger.info("JNIEnv->ExceptionClear() was called");

        return new JRet();
    }

    /*
    * Converts a method ID derived from cls to a java.lang.reflect.Method or java.lang.reflect.Constructor object.
      isStatic must be set to JNI_TRUE if the method ID refers to a static field, and JNI_FALSE otherwise.
      Throws OutOfMemoryError and returns 0 if fails.
    *
    * */

    private JRet toReflectedMethod(Unicorn uc, long[] args) throws Exception {
        long clzz_idx = args[1];
        long method_idx = args[2];
        long is_static = args[3];

        JObject obj = getReference(clzz_idx);

        if (!(obj instanceof JClass)){
            throw new Exception("Expected a jclass.");
        }

        JClass klass = (JClass) obj;

        JMethod method = klass.findMethod(method_idx);
        if (method != null){
            if (method.isStatic()){
                MemoryHelper.writePtr32(uc,is_static,JConst.JNI_TRUE);
            }else {
                MemoryHelper.writePtr32(uc,is_static,JConst.JNI_FALSE);
            }
            Logger.info(String.format("JNIEnv->ToReflectedMethod(%s, %s, %x) was called", klass.getName(), method.getName(), is_static));

            return new JRet(method.getIndex());
        }

        JConstructor constructor = klass.findConstructor(method_idx);
        if (constructor != null){
            MemoryHelper.writePtr32(uc,is_static,JConst.JNI_FALSE);
            Logger.info(String.format("JNIEnv->ToReflectedMethod(%s, %s, %x) was called", klass.getName(), constructor.getName(), is_static));

            return new JRet(constructor.getIndex());
        }

        throw new Exception(String.format("Could not find method ('%x') in class %s.", method_idx,klass.getName()));

    }

    //Returns a class object from a fully-qualified name, or NULL if the class cannot be found.
    private JRet FindClass(Unicorn uc, long[] args) throws Exception{
        long name_ptr = args[1];

        String name = MemoryHelper.readUTF8(uc,name_ptr);

        Logger.info(String.format("JNIEnv->FindClass(%s) was called", name));

        if (name.startsWith("[")){
            throw new Exception("Array type not implemented.");
        }

        Logger.info(String.format("FindClass:%s", name));

        JClass klass = classLoader.findClass(name);
        if (klass != null) {
            long ref = addLocalReference(klass);
            return new JRet(ref);
        }

        return new JRet(0);
    }

    private JRet notImplemented(Unicorn uc, long[] args) throws Exception{
        throw new Exception("notImplemented");
    }

    /*
    * 从UC内存中，调用注册Native函数
    *
    * */
    private JRet RegisterNatives(Unicorn uc, long[] args) throws Exception{
        long class_idx = args[1];
        long method_addr = args[2];
        long method_count = args[3];

        Logger.info(String.format("JNIEnv->RegisterNatives(%d, 0x%08x, %d) was called", class_idx,method_addr,method_count));

        JObject obj =  getLocalReference(class_idx);
        if (!(obj instanceof JClass)){
            throw new Exception("Expected a jclass.");
        }

        JClass clzz = (JClass) obj;

        for (long i=0;i<method_count;i++){
            // 与注册的native函数的内存结构体相同
            long name_addr = MemoryHelper.readPtr32(uc,(i*12)+method_addr);
            long signature_addr = MemoryHelper.readPtr32(uc,(i*12)+method_addr+4);
            long ptr = MemoryHelper.readPtr32(uc,(i*12)+method_addr+8);

            String name = MemoryHelper.readUTF8(uc,name_addr);
            String signature = MemoryHelper.readUTF8(uc,signature_addr);

            clzz.registerNative(name,signature,ptr);
        }

        return new JRet(JConst.JNI_OK);

    }


}
