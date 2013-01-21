package com.jamie.surefittslaw.models;

import android.util.Log;

/**
 * You just gotta love Java :) Note to self, write a script to do 
 * this kind of stuff for me...
 * @author jamie
 *
 */

public class MeasurementAnalysis {
	public static final int SLOPE = 100;
	public static final int INTERCEPT = 500;
	
	private int mWidth;
	private int mHeight;
	
	private int[][] mSampleSizes;
	private int[][] mMeans;
	private int[][] mMaxs;
	private int[][] mMins;
	private int[][] mStanDevs;
	private int[][] mDistances;
	private int[][] mFittsLaw;
	private int[][] mFittsLawError;
	
	private int mMeanMin = 0;
	private int mMeanMax = 0;
	
	public MeasurementAnalysis(int width, int height) {
		mWidth = width;
		mHeight = height;
		
		mSampleSizes = new int[height][width];
		mMeans = new int[height][width];
		mMaxs = new int[height][width];
		mMins = new int[height][width];
		mStanDevs = new int[height][width];
		mDistances = new int[height][width];
		mFittsLaw = new int[height][width];
		mFittsLawError = new int[height][width];
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public void setSampleSize(int x, int y, int size) {
		mSampleSizes[y][x] = size;
	}
	
	public void setMean(int x, int y, int mean) {
		mMeans[y][x] = mean;
	}
	
	public void setMaximum(int x,int y, int max) {
		mMaxs[y][x] = max;
	}
	
	public void setMinimum(int x, int y, int min) {
		mMins[y][x] = min;
	}
	
	public void setStandardDeviation(int x, int y, int stanDev) {
		mStanDevs[y][x] = stanDev;
	}
	
	public void setDistance(int x, int y, int distance) {
		mDistances[y][x] = distance;
	}
	
	public void setFittsLaw(int x, int y, int fittsLaw) {
		mFittsLaw[y][x] = fittsLaw;
	}
	
	public void setFittsLawError(int x, int y, int error) {
		mFittsLawError[y][x] = error;
	}
	
	public int getSampleSize(int x, int y) {
		return mSampleSizes[y][x];
	}
	
	public int[][] getSampleSizes() {
		return mSampleSizes;
	}
	
	public int getMean(int x, int y) {
		return mMeans[y][x];
	}
	
	public int[][] getMeans() {
		return mMeans;
	}
	
	public int getMaximum(int x, int y) {
		return mMaxs[y][x];
	}
	
	public int[][] getMaximums() {
		return mMaxs;
	}
	
	public int getMinimum(int x, int y) {
		return mMins[y][x];
	}
	
	public int[][] getMinimums() {
		return mMins;
	}
	
	public int getStandardDeviation(int x, int y) {
		return mStanDevs[y][x];
	}
	
	public int[][] getStandardDeviations() {
		return mStanDevs;
	}
	
	public int getDistance(int x, int y) {
		return mDistances[y][x];
	}
	
	public int[][] getDistances() {
		return mDistances;
	}
	
	public int getFittsLaw(int x, int y) {
		return mFittsLaw[y][x];
	}
	
	public int[][] getFittsLaws() {
		return mFittsLaw;
	}
	
	public int getFittsLawError(int x, int y) {
		return mFittsLawError[y][x];
	}
	
	public int[][] getFittsLawErrors() {
		return mFittsLawError;
	}
	
	public void calculateMeanDelta() {
		int min = Integer.MAX_VALUE;
		int max = 0;
		
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				int value = mMeans[y][x];
				if (value > 0) {
					min = Math.min(min, value);
					max = Math.max(max, value);
				}
			}
		}
		
		if (min == Integer.MAX_VALUE) {
			min = 0;
		}
		
		Log.d("Analysis", "Max mean: " + max + ", min mean: " + min);
		
		mMeanMin = min;
		mMeanMax = max;
	}
	
	public int getMeanMin() {
		return mMeanMin;
	}
	
	public int getMeanMax() {
		return mMeanMax;
	}
	
}
