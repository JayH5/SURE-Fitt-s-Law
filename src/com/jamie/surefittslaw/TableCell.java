package com.jamie.surefittslaw;

import android.content.Context;
import android.graphics.Color;
import android.widget.TableRow;
import android.widget.TextView;

public class TableCell extends TextView {

	// Default values for saturation and brightness
	private static final float SATURATION = 0.9f;
	private static final float VALUE = 0.9f;
	
	// Value between 0 and 1 - the fraction of background hue
	private float mHuePercent = 0.0f;
	
	// The maximum hue value (should be between 0 and 360)
	private float mHue = 196.0f;
	
	public final int x;
	public final int y;
	
	private static TableRow.LayoutParams sParams = 
			new TableRow.LayoutParams();
	static {		
		sParams.weight = 1;
		sParams.width = 0;
		sParams.height = TableRow.LayoutParams.MATCH_PARENT;
	}
	
	public TableCell(Context context, int xPos, int yPos) {
		super(context);
		x = xPos;
		y = yPos;
		setLayoutParams(sParams);
	}
	
	public void setHuePercent(float huePercent) {
		mHuePercent = huePercent;
		updateBackgroundColor();
	}
	
	public void setHue(float hue) {
		mHue = hue;
		updateBackgroundColor();
	}
	
	// Look what Android has done to my spelling of colour!
	public void updateBackgroundColor() {
		float[] hsv = new float[] {
				mHue * mHuePercent,
				SATURATION,
				VALUE
		};
		
		setBackgroundColor(Color.HSVToColor(hsv));
	}

}
