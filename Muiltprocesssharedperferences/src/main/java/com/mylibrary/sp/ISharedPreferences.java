package com.mylibrary.sp;

import java.util.Map;
import java.util.Set;

/**
 * Created by xujinping on 2018/2/2.
 * SharedPreferences interface
 */

public interface ISharedPreferences {
    String getString(String k, String dv);

    long getLong(String k, long dv);

    int getInt(String k, int dv);

    float getFloat(String k, float dv);

    boolean getBoolean(String k, boolean dv);

    Set<String> getStringSet(String k, Set<String> dvs);

    boolean contains(String k);

    Map<String, ?> getAll();

    boolean putString(String k, String v);

    boolean putLong(String k, long v);

    boolean putInt(String k, int v);

    boolean putFloat(String k, float v);

    boolean putBoolean(String k, boolean v);

    boolean putStringSet(String k, Set<String> vs);

    void putStringApply(String k, String v);

    void putLongApply(String k, long v);

    void putIntApply(String k, int v);

    void putFloatApply(String k, float v);

    void putBooleanApply(String k, boolean v);

    void putStringSetApply(String k, Set<String> vs);
}
