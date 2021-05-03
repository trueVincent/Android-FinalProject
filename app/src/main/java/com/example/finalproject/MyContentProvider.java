package com.example.finalproject;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
    //資料庫參數
    static final String DB_NAME = "location";
    static final int DB_VERSION = 1;
    static final String TABLE_NAME = "locations";
    static final String COL_id = "id";
    static final String COL_longitude = "longitude";
    static final String COL_latitude = "latitude";
    static final String COL_name = "name";
    static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    COL_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_longitude + " REAL NOT NULL, " +
                    COL_latitude + " REAL NOT NULL, " +
                    COL_name + " TEXT ) ; " ;
    private SQLiteDatabase db;

    //content provider參數
    static final String AUTHORITY = "com.example.finalproject";
    static final String URL = "content://" + AUTHORITY + "/" + TABLE_NAME;
    static final Uri CONTENT_URI = Uri.parse(URL);
    static final int LOCATIONS = 1;
    static final int LOCATION_ID = 2;
    static final int NEARBY = 3;
    static final UriMatcher mUriMatcher;
    static{
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME, LOCATIONS);
        mUriMatcher.addURI(AUTHORITY, TABLE_NAME+"/#", LOCATION_ID);
        mUriMatcher.addURI(AUTHORITY, "nearBy/#" , NEARBY);
    }

    //資料庫設定
    public class MySQLiteOpenHelper extends SQLiteOpenHelper {

        public MySQLiteOpenHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    //content provider方法override
    @Override
    public boolean onCreate(){
        Context context = getContext();
        MySQLiteOpenHelper dbHelper = new MySQLiteOpenHelper(context);

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values){
        long rowID = db.insert(TABLE_NAME, "", values);
        if(rowID > 0){
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into" + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,String[] selectionArgs, String sortOrder) {
        Cursor c;
        String id, where;

        switch (mUriMatcher.match(uri)) {
            case LOCATIONS:
                c = db.query(TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                break;

            case LOCATION_ID:
                id = uri.getPathSegments().get(1);
                where = COL_id + " = " + id;
                c = db.query(TABLE_NAME, projection, where, selectionArgs,null, null, sortOrder);
                break;

            case NEARBY:
                id = uri.getPathSegments().get(1);
                String minId = "";
                double longitude = 0;
                double latitude = 0;
                double distence;
                where = COL_id + " = " + id;

                //計算各點距離存進list
                Cursor c2 = db.query(TABLE_NAME, projection, where, selectionArgs,null, null, sortOrder);
                Cursor c3 = db.query(TABLE_NAME, projection, selection, selectionArgs,null, null, sortOrder);
                if(c2.moveToFirst()){
                    longitude = c2.getDouble(c2.getColumnIndex(COL_longitude));
                    latitude = c2.getDouble(c2.getColumnIndex(COL_latitude));
                }else{
                    c = null;
                    return c;
                }
                if(c3.moveToFirst()){
                    if(id.equals(c3.getString(c3.getColumnIndex(COL_id)))){
                        c3.moveToNext();
                    }
                    distence = Math.sqrt(Math.pow((longitude - c3.getDouble(c3.getColumnIndex(COL_longitude))),2)+Math.pow((latitude - c3.getDouble(c3.getColumnIndex(COL_latitude))),2));
                    minId = c3.getString(c3.getColumnIndex(COL_id));
                    do{
                        if(!c3.getString(c3.getColumnIndex(COL_id)).equals(id)){
                            double distence2 = Math.sqrt(Math.pow((longitude - c3.getDouble(c3.getColumnIndex(COL_longitude))),2)+Math.pow((latitude - c3.getDouble(c3.getColumnIndex(COL_latitude))),2));
                            if(distence2 < distence){
                                distence = distence2;
                                minId = c3.getString(c3.getColumnIndex(COL_id));
                            }
                        }
                    }while(c3.moveToNext());
                }

                //傳回最小距離的location
                where = COL_id + " = " + minId;
                c = db.query(TABLE_NAME, projection, where, selectionArgs,null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (mUriMatcher.match(uri)){
            case LOCATIONS:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case LOCATION_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( TABLE_NAME, COL_id +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (mUriMatcher.match(uri)){
            case LOCATIONS:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            case LOCATION_ID:
                count = db.update(TABLE_NAME, values, COL_id + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)){
            case LOCATIONS:
                return "vnd.android.cursor.dir/vnd.com.example.finalproject.locations";
            case LOCATION_ID:
                return "vnd.android.cursor.item/vnd.com.example.finalproject.locations";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
