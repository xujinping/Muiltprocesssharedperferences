package com.mylibrary.sp;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.mylibrary.sp.PreferencesProvider.ALL_KEY_URI;
import static com.mylibrary.sp.PreferencesProvider.BASE_URI;
import static com.mylibrary.sp.PreferencesProvider.CONFIG_APPLY;
import static com.mylibrary.sp.PreferencesProvider.CONFIG_KEY;
import static com.mylibrary.sp.PreferencesProvider.CONFIG_VALUE;
import static com.mylibrary.sp.PreferencesProvider.CONTAINS_URI;
import static com.mylibrary.sp.PreferencesProvider.SET_STRING_URI;


/**
 * Created by xujinping on 2018/2/2.
 * 支持夸进程，多进程操作的SharedPreferences
 */

public class MPSharedPreferences implements ISharedPreferences {
    private final Object mLock = new Object();
    private volatile static MPSharedPreferences mInstance = null;
    private ContentResolver mResolver;

    private MPSharedPreferences(String sharedFileName, Context context) {
        if (!(context instanceof Application)) {
            context = context.getApplicationContext();
        }
        mResolver = context.getContentResolver();
        SharedPreferencesHelper.getIns().init(sharedFileName, context);
    }

    public static MPSharedPreferences getIns(String sharedFileName, Context context) {
        if (mInstance == null) {
            synchronized (MPSharedPreferences.class) {
                if (mInstance == null) {
                    mInstance = new MPSharedPreferences(sharedFileName, context);
                }
            }
        }
        return mInstance;
    }


    @Override
    public Map<String, String> getAll() {
        Cursor c = mResolver.query(ALL_KEY_URI, null, null, null, null);
        if (c != null) {
            Map<String, String> maps = new HashMap<>();
            while (c.moveToNext()) {
                int keyIndex = c.getColumnIndex(CONFIG_KEY);
                String key = c.getString(keyIndex);
                int valueIndex = c.getColumnIndex(CONFIG_VALUE);
                String value = c.getString(valueIndex);
                maps.put(key, value);
            }
            c.close();
            return maps;
        }
        return null;
    }

    @Nullable
    @Override
    public String getString(String key, String defValue) {
        Uri uri = Uri.withAppendedPath(BASE_URI, key);
        String value = mResolver.getType(uri);
        if (TextUtils.isEmpty(value)) {
            value = defValue;
        }
        return value;
    }


    @Nullable
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Cursor c = mResolver.query(SET_STRING_URI, null, key, null, null);
        if (c != null) {
            Set<String> stringSet = new HashSet<>();
            while (c.moveToNext()) {
                int valueIndex = c.getColumnIndex(CONFIG_VALUE);
                String value = c.getString(valueIndex);
                stringSet.add(value);
            }
            c.close();
            return stringSet;
        }
        return defValues;
    }

    @Override
    public int getInt(String key, int defValue) {
        Uri uri = Uri.withAppendedPath(BASE_URI, key);
        int value = defValue;
        String type = mResolver.getType(uri);
        if (!TextUtils.isEmpty(type)) {
            try {
                value = Integer.parseInt(type);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    @Override
    public long getLong(String key, long defValue) {
        Uri uri = Uri.withAppendedPath(BASE_URI, key);
        long value = defValue;
        String type = mResolver.getType(uri);
        if (!TextUtils.isEmpty(type)) {
            try {
                value = Long.parseLong(type);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    @Override
    public float getFloat(String key, float defValue) {
        Uri uri = Uri.withAppendedPath(BASE_URI, key);
        float value = defValue;
        String type = mResolver.getType(uri);
        if (!TextUtils.isEmpty(type)) {
            try {
                value = Float.parseFloat(type);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Uri uri = Uri.withAppendedPath(BASE_URI, key);
        boolean value = defValue;
        String type = mResolver.getType(uri);
        if (!TextUtils.isEmpty(type)) {
            try {
                value = Boolean.parseBoolean(type);
            } catch (NumberFormatException e) {
            }
        }
        return value;
    }

    @Override
    public boolean contains(String key) {
        Cursor c = mResolver.query(CONTAINS_URI, null, key, null, null);
        if (c != null) {
            c.moveToFirst();
            int index = c.getColumnIndex(CONFIG_VALUE);
            boolean contains = c.getInt(index) != 0;
            c.close();
            return contains;
        }
        return false;
    }


    public boolean putString(String key, String value) {
        ContentValues v = new ContentValues(2);
        v.put(CONFIG_KEY, key);
        v.put(CONFIG_VALUE, value);
        Uri uri = mResolver.insert(BASE_URI, v);
        return uri != null && uri.getLastPathSegment().equals(key);
    }

    public void putStringApply(String key, String value) {
        ContentValues v = new ContentValues(3);
        v.put(CONFIG_KEY, key);
        v.put(CONFIG_VALUE, value);
        v.put(CONFIG_APPLY, true);
        mResolver.insert(BASE_URI, v);
    }


    public boolean putLong(String key, long value) {
        return putString(key, String.valueOf(value));
    }

    public void putLongApply(String key, long value) {
        putStringApply(key, String.valueOf(value));
    }

    public boolean putInt(String key, int value) {
        return putString(key, String.valueOf(value));
    }

    public void putIntApply(String key, int value) {
        putStringApply(key, String.valueOf(value));
    }

    public boolean putFloat(String key, float value) {
        return putString(key, String.valueOf(value));
    }

    public void putFloatApply(String key, float value) {
        putStringApply(key, String.valueOf(value));
    }

    public boolean putBoolean(String key, boolean value) {
        return putString(key, String.valueOf(value));
    }

    public void putBooleanApply(String key, boolean value) {
        putStringApply(key, String.valueOf(value));
    }

    public boolean putStringSet(String key, Set<String> values) {
        synchronized (mLock) {
            return builderStringSet(key, values, false);
        }
    }

    public void putStringSetApply(String key, Set<String> values) {
        synchronized (mLock) {
            builderStringSet(key, values, true);
        }
    }

    private boolean builderStringSet(String key, Set<String> values, boolean apply) {
        ContentValues v = new ContentValues();
        v.put(CONFIG_KEY, key);
        v.put(CONFIG_APPLY, apply);
        int i = 0;
        for (String s : values) {
            v.put(CONFIG_VALUE + i, s);
            i++;
        }
        Uri uri = mResolver.insert(SET_STRING_URI, v);
        return uri != null && uri.getLastPathSegment().equals(key);
    }

}
