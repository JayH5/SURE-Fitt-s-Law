package com.jamie.surefittslaw.tasks;

import java.util.Queue;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.jamie.surefittslaw.models.Measurement;
import com.jamie.surefittslaw.models.MeasurementDatabase;

public class DatabaseSaveTask extends AsyncTask<Queue<Measurement>, Void, Void> {

	private static final String TAG = "DatabaseSaveTask";
	
	private MeasurementDatabase mDatabase;
	
	public DatabaseSaveTask(MeasurementDatabase db) {
		mDatabase = db;
	}
	
	@Override
	protected Void doInBackground(Queue<Measurement>... params) {
		final Queue<Measurement> measurements = params[0];
		
		if (measurements != null) {
			SQLiteDatabase db = mDatabase.getWritableDatabase();
			// This doesn't need to be synchronized so long as the tasks are
			// executed in a serial fashion since this should be the only place
			// where items are removed from the queue.
			while (!measurements.isEmpty()) {
				// Nom, nom, nom...
				db.insert(MeasurementDatabase.TABLE_NAME_LANDSCAPE, null, 
						measurements.remove().toContentValues());
			}
			
			Log.d(TAG, "Database entries saved. New database size: " + 
					DatabaseUtils.queryNumEntries(db, MeasurementDatabase.TABLE_NAME_LANDSCAPE));
			
			db.close();
		}
				
		return null;
	}

}
