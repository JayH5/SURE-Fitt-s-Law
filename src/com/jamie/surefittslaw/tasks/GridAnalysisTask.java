package com.jamie.surefittslaw.tasks;

import java.util.List;

import android.os.AsyncTask;

import com.jamie.surefittslaw.models.MeasurementAnalysis;
import com.jamie.surefittslaw.models.MeasurementGrid;

public class GridAnalysisTask extends AsyncTask<MeasurementGrid, Void, 
		MeasurementAnalysis> {

	private final GridAnalysedListener mListener;
	
	private final int mLeftBtnX;
	private final int mRightBtnX;
	private final int mBtnY;
	
	private final int mTargetSize;
	
	public GridAnalysisTask(GridAnalysedListener listener, int leftBtnX,
			int rightBtnX, int btnY, int targetSize) {
		
		mListener = listener;
		
		mLeftBtnX = leftBtnX;
		mRightBtnX = rightBtnX;
		mBtnY = btnY;
		
		mTargetSize = targetSize;
	}
	
	@Override
	protected MeasurementAnalysis doInBackground(MeasurementGrid... params) {
		final MeasurementGrid grid = params[0];
		
		final int width = grid.getWidth();
		final int height = grid.getHeight();
		
		final int screenMidpoint = (mRightBtnX + mLeftBtnX) / 2;
		
		final MeasurementAnalysis analysis = 
				new MeasurementAnalysis(width, height);
		
		
		for (int y = 0; y < height; y++) {
			int cellY = grid.getCellY(y);
			
			for (int x = 0; x < width; x++) {
				int cellX = grid.getCellX(x);
				
				List<Integer> cell = grid.get(x, y);
				
				int sampleSize = 0;
				int mean = 0;
				int min = Integer.MAX_VALUE;
				int max = 0;
				int stanDev = 0;
				int distance = 0;
				int fittsLaw = 0;
				int error = 0;
				
				if (cell != null) {
					sampleSize = cell.size();
					if (sampleSize > 0) {
						int total = 0;
						for (int measurement : cell) {
							total += measurement;
							min = Math.min(min, measurement);
							max = Math.max(max, measurement);
						}
						mean = total / sampleSize;
						
						float sumDiffFromMean = 0;
						for (int measurement : cell) {
							sumDiffFromMean += Math.pow(measurement - mean, 2);
						}
						stanDev = (int) Math.sqrt(sumDiffFromMean / sampleSize);
					}
				}
				
				if (min == Integer.MAX_VALUE) {
					min = 0;
				}
				
				int x1;
				if (cellX < screenMidpoint) {
					x1 = mLeftBtnX;
				} else {
					x1 = mRightBtnX;
				}
				
				distance = calculateDistance (cellX, cellY, x1, mBtnY);
				fittsLaw = calculateFittsLaw(distance);
				error = Math.abs((mean - fittsLaw));
				
				analysis.setSampleSize(x, y, sampleSize);
				analysis.setMean(x, y, mean);
				analysis.setMaximum(x, y, max);
				analysis.setMinimum(x, y, min);
				analysis.setStandardDeviation(x, y, stanDev);
				analysis.setDistance(x, y, distance);				
				analysis.setFittsLaw(x, y, fittsLaw);
				analysis.setFittsLawError(x, y, error);
			}
		}
		
		analysis.calculateMeanDelta();
		
		return analysis;
	}
	
	@Override
	protected void onPostExecute(MeasurementAnalysis result) {
		mListener.onGridAnalysed(result);
	}
	
	private int calculateDistance(int x0, int y0, int x1, int y1) {
		int deltaX = (x1 - x0) * (x1 - x0);
		int deltaY = (y1 - y0) * (y1 - y0);
		
		return (int) Math.sqrt(deltaY + deltaX);
	}
	
	private int calculateFittsLaw(int distance) {
		return (int) (MeasurementAnalysis.INTERCEPT + MeasurementAnalysis.SLOPE * 
				Math.log(1 + distance / mTargetSize) / Math.log(2));
	}
	
	public interface GridAnalysedListener {
		public void onGridAnalysed(MeasurementAnalysis analysis);
	}

}
