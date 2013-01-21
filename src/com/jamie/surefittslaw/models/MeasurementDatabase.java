package com.jamie.surefittslaw.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MeasurementDatabase extends SQLiteOpenHelper {

	private static final String TAG = "MeasurementsDatabase";
	
	private static final String DATABASE_NAME = "measurements.db";
    private static final int DATABASE_VERSION = 1;
    
    public static final String TABLE_NAME_LANDSCAPE = "measurements";
    public static final String TABLE_NAME_PORTRAIT = "measurements2";
    
    public static final String COLUMN_NAME_X = "x";
    public static final String COLUMN_NAME_Y = "y";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_MISTOUCH = "mistouch";
    
    public MeasurementDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db, TABLE_NAME_LANDSCAPE);
		createTable(db, TABLE_NAME_PORTRAIT);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Logs that the database is being upgraded
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");

        // Kills the tables and existing data
        dropTable(db, TABLE_NAME_LANDSCAPE);
        dropTable(db, TABLE_NAME_PORTRAIT);

        // Recreates the database with a new version
        onCreate(db);
		
	}
	
	private void createTable(SQLiteDatabase db, String tableName) {
		db.execSQL("CREATE TABLE " + tableName + " ("
				+ COLUMN_NAME_X + " INTEGER, "
				+ COLUMN_NAME_Y + " INTEGER, "
				+ COLUMN_NAME_TIME + " INTEGER, "
				+ COLUMN_NAME_MISTOUCH + " INTEGER"
				+ ");");
	}
	
	private void dropTable(SQLiteDatabase db, String tableName) {
		db.execSQL("DROP TABLE IF EXISTS " + tableName);
	}
}
