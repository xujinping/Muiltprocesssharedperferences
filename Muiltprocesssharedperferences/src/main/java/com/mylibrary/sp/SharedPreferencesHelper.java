package com.mylibrary.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import java.util.Map;
import java.util.Set;

/**
 * Created by xujinping on 2018/2/1.
 */

public class SharedPreferencesHelper {
    public final static String TAG = "Shared";
    public final static boolean DEBUG = true;
    private final static String DEFAULT_FILE_NAME = "multi_process_shared";
    private SharedPreferences mSp;
    private Context mCtx;

    private volatile static SharedPreferencesHelper mInstance = null;

    public static SharedPreferencesHelper getIns() {
        if (mInstance == null) {
            synchronized (SharedPreferencesHelper.class) {
                if (mInstance == null) {
                    mInstance = new SharedPreferencesHelper();
                }
            }
        }
        return mInstance;
    }
    public void init(String sharedFileName, Context context) {
        if (TextUtils.isEmpty(sharedFileName)) {
            throw new NullPointerException("sharedFileName==null");
        }
        mCtx = context;
        if (mSp == null) {
            mSp = context.getSharedPreferences(sharedFileName, Context.MODE_PRIVATE);
        }
    }

    public void putValue(String key, String value, boolean apply) {
        if (key != null) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(key, value);
            if (apply) {
                editor.apply();
            } else {
                editor.commit();
            }
        }
    }


    public String getValue(String key) {
        return getSharedPreferences().getString(key, null);
    }

    public Map<String, ?> getAll(){
        return getSharedPreferences().getAll();
    }

    public Set<String> getStringSet(String key) {
        return getSharedPreferences().getStringSet(key, null);
    }

    public void putStringSet(String key, Set<String> stringSet, boolean apply) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putStringSet(key, stringSet);
        if (apply) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    private SharedPreferences getSharedPreferences() {
        if (mSp == null) {
            mSp = mCtx.getSharedPreferences(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
        }
        return mSp;
    }
}
