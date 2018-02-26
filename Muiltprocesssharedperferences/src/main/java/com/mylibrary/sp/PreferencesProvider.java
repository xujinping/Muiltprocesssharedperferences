package com.mylibrary.sp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.content.UriMatcher.NO_MATCH;

/**
 * Created by xujinping on 2018/2/1.
 * 由于Android 系统的SharedPreferences不支持多进程操作，Context.MODE_MULTI_PROCESS 并不能保证多进程下Sp是有效的
 * 所以Android 在2.3以后该标记被废弃了。官方建议：当需要夸进程做标记时，请使用ContentProvider。
 * 该类是基于ContentProvider 实现支持跨进程的SharedPreferences
 */

public class PreferencesProvider extends ContentProvider {
    public static final String TAG = SharedPreferencesHelper.TAG;
    public final static String AUTHORITY = "com.example.xjp.demo.storage";
    public final static Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    private final static String ALL_KEY_PATH = "all_key";
    public final static Uri ALL_KEY_URI = Uri.withAppendedPath(BASE_URI, ALL_KEY_PATH);
    private final static String STRING_SET_PATH = "string_set";
    public final static Uri SET_STRING_URI = Uri.withAppendedPath(BASE_URI, STRING_SET_PATH);
    private final static String CONTAINS_PATH = "contains";
    public final static Uri CONTAINS_URI = Uri.withAppendedPath(BASE_URI, CONTAINS_PATH);
    public final static String CONFIG_KEY = "config_key";
    public final static String CONFIG_VALUE = "config_value";
    public final static String CONFIG_APPLY = "config_apply";
    private static UriMatcher matcher = new UriMatcher(NO_MATCH);

    private final Object mGetAllLock = new Object();
    private final Object mGetStringSetLock = new Object();
    private final Object mPutStringSetLock = new Object();

    private final static int CODE_KEY = 0;
    private final static int CODE_ALL_KEY = 1;
    private final static int CODE_STRING_SET = 2;
    private final static int CODE_CONTAINS = 3;

    static {
        matcher.addURI(AUTHORITY, null, CODE_KEY);
        matcher.addURI(AUTHORITY, ALL_KEY_PATH, CODE_ALL_KEY);
        matcher.addURI(AUTHORITY, STRING_SET_PATH, CODE_STRING_SET);
        matcher.addURI(AUTHORITY, CONTAINS_PATH, CODE_CONTAINS);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (SharedPreferencesHelper.DEBUG) {
            Log.i(TAG, "query: uri=>" + uri.toString());
        }
        MatrixCursor c = null;
        int code = matcher.match(uri);
        switch (code) {
            case CODE_ALL_KEY:
                c = getAll();
                break;
            case CODE_STRING_SET:
                if (selection != null) {
                    c = getStringSet(selection);
                }
                break;
            case CODE_CONTAINS:
                if (selection != null) {
                    c = new MatrixCursor(new String[]{CONFIG_VALUE});
                    String key = selection;
                    boolean contains = SharedPreferencesHelper.getIns().contains(key);
                    int value = contains ? 1 : 0;
                    c.addRow(new Object[]{value});
                }
            default:
                break;
        }
        return c;
    }

    private MatrixCursor getAll() {
        MatrixCursor c = new MatrixCursor(new String[]{CONFIG_KEY, CONFIG_VALUE});
        synchronized (mGetAllLock) {
            Map<String, ?> maps = SharedPreferencesHelper.getIns().getAll();
            for (Map.Entry<String, ?> entry : maps.entrySet()) {
                c.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        return c;
    }

    private MatrixCursor getStringSet(String key) {
        MatrixCursor c = new MatrixCursor(new String[]{CONFIG_VALUE});
        synchronized (mGetStringSetLock) {
            Set<String> stringSet = SharedPreferencesHelper.getIns().getStringSet(key);
            for (String s : stringSet) {
                c.addRow(new Object[]{s});
            }
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if (SharedPreferencesHelper.DEBUG) {
            Log.i(TAG, "getType: uri=>" + uri.toString());
        }
        String key = uri.getLastPathSegment();
        return SharedPreferencesHelper.getIns().getValue(key);
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (SharedPreferencesHelper.DEBUG) {
            Log.i(TAG, "insert: uri=>" + uri.toString());
        }
        if (values == null) {
            return null;
        }
        int code = matcher.match(uri);
        String key = values.getAsString(CONFIG_KEY);
        Boolean apply = values.getAsBoolean(CONFIG_APPLY);
        if (apply == null) {
            apply = false;
        }
        switch (code) {
            case CODE_KEY:
                String value = values.getAsString(CONFIG_VALUE);
                SharedPreferencesHelper.getIns().putValue(key, value, apply);
                break;
            case CODE_STRING_SET:
                putStringSet(values, key, apply);
                break;
            default:
                break;
        }
        return Uri.withAppendedPath(uri, key);
    }

    private void putStringSet(ContentValues values, String key, boolean apply) {
        synchronized (mPutStringSetLock) {
            Set<String> stringSet = new HashSet<>();
            int size = values.size();
            for (int i = 0; i < size - 2; i++) {
                stringSet.add(values.getAsString(CONFIG_VALUE + i));
            }
            SharedPreferencesHelper.getIns().putStringSet(key, stringSet, apply);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
