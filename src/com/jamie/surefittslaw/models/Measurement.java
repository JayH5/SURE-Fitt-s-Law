package com.jamie.surefittslaw.models;

import android.content.ContentValues;

public class Measurement {
	private int mX;
	private int mY;
	private long mTime;
	private boolean mMistouch;
	
	public Measurement(int x, int y, int time, boolean mistouch) {
		mX = x;
		mY = y;
		mTime = time;
		mMistouch = mistouch;
	}
	
	public ContentValues toContentValues() {
		final ContentValues values = new ContentValues();
		values.put(MeasurementDatabase.COLUMN_NAME_X, mX);
		values.put(MeasurementDatabase.COLUMN_NAME_Y, mY);
		values.put(MeasurementDatabase.COLUMN_NAME_TIME, mTime);
		values.put(MeasurementDatabase.COLUMN_NAME_MISTOUCH, mMistouch);
		
		return values;
	}
	
	public String toString() {
		return ("x: " + mX + "px, y: " + mY + "px, time: " 
				+ mTime + "ms.");
	}
}
