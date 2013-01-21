package com.jamie.surefittslaw.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.jamie.surefittslaw.models.MeasurementDatabase;
import com.jamie.surefittslaw.models.MeasurementGrid;

public class GridCreationTask extends AsyncTask<MeasurementDatabase, Void, MeasurementGrid> {

	private static final String TAG = "GridCreationTask";
	
	private GridCreatedListener mListener;
	
	private int mColumns;
	private int mRows;
	
	private int mCellHeight;
	private int mCellWidth;
	
	public GridCreationTask(GridCreatedListener listener, int columns, int rows, 
			int cellHeight, int cellWidth) {
		
		mListener = listener;
		
		mColumns = columns;
		mRows = rows;
		
		mCellHeight = cellHeight;
		mCellWidth = cellWidth;
	}
	
	@Override
	protected MeasurementGrid doInBackground(MeasurementDatabase... params) {
		final MeasurementGrid grid = new MeasurementGrid(mColumns, mRows, 
				mCellWidth, mCellHeight);
		final MeasurementDatabase dbHelper = params[0];
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(MeasurementDatabase.TABLE_NAME_LANDSCAPE,
				null, null, null, null, null, null);
		
		if (cursor != null) {
			int xColIdx = cursor.getColumnIndexOrThrow(
					MeasurementDatabase.COLUMN_NAME_X);
			int yColIdx = cursor.getColumnIndexOrThrow(
					MeasurementDatabase.COLUMN_NAME_Y);
			int timeColIdx = cursor.getColumnIndexOrThrow(
					MeasurementDatabase.COLUMN_NAME_TIME);
			
			if (cursor.moveToFirst()) {
				do {
					int col = cursor.getInt(xColIdx) 
							/ mCellWidth;
					int row = cursor.getInt(yColIdx) 
							/ mCellHeight;
					int time = cursor.getInt(timeColIdx);
					
					// Some kind of off by one error or rounding error somewhere :(
					if (col >= 0 && row >= 0) {
						grid.put(col, row, time);
					} else {
						Log.w(TAG, "Invalid row or column. Coordinates: (" + 
								cursor.getInt(xColIdx) + "," + cursor.getInt(yColIdx));
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			cursor = null;
		}
		db.close();
		
		return grid;
	}
	
	@Override
	protected void onPostExecute(MeasurementGrid result) {
		mListener.onGridCreated(result);
	}
	
	public interface GridCreatedListener {
		public void onGridCreated(MeasurementGrid grid);
	}

}
