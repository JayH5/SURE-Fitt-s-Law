package com.jamie.surefittslaw.models;

import java.util.LinkedList;
import java.util.List;

public class MeasurementGrid {
	
	private final GridCell[][] mGrid;
	
	private final int mHeight;
	private final int mWidth;
	
	private final int mCellHeight;
	private final int mCellWidth;
	
	public MeasurementGrid(int width, int height, int cellWidth, 
			int cellHeight) {
		mGrid = new GridCell[height][width];
		
		mHeight = height;
		mWidth = width;
		
		mCellHeight = cellHeight;
		mCellWidth = cellWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
	
	public int getWidth(){
		return mWidth;
	}
	
	public void put(int x, int y, int value) {
		if (mGrid[y][x] == null) {
			mGrid[y][x] = new GridCell();
		}
		mGrid[y][x].add(value);
	}
	
	public List<Integer> get(int x, int y) {
		return mGrid[y][x];
	}
	
	public int getCellX(int x) {
		return (int) (mCellWidth * (x + 0.5));
	}
	
	public int getCellY(int y) {
		return (int) (mCellHeight * (y + 0.5));
	}

	private static final class GridCell extends LinkedList<Integer> {

		private static final long serialVersionUID = -6259466909011126736L;
	}
}
