package com.jamie.surefittslaw;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.jamie.surefittslaw.models.Measurement;
import com.jamie.surefittslaw.models.MeasurementDatabase;
import com.jamie.surefittslaw.tasks.DatabaseSaveTask;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private static final int LOW_PROFILE_DELAY_MILLIS = 2000;
	private static final int SHOW_TARGET_DELAY_MILLIS = 1000;
	
	private static final int VIBRATION_LENGTH_MILLIS = 70;
	
	private static final int UNSAVED_MEASUREMENTS_THRESHOLD = 15;
	
	// The main content view, just used to hide the status bar
	private View mContentView;
	
	private ImageButton mTargetButton;
	
	private ImageButton mLeftButton;	
	private ImageButton mRightButton;
	// Both left and right button pressed -> 2 buttons pressed
	private int mNumButtonsPressed = 0;
	
	// This button takes the user to th
	private Button mViewHeatmapButton;
	
	// We don't want to be doing loads of db access so we will save periodically
	// a list of measurements
	private Queue<Measurement> mUnsavedMeasurements = 
			new ConcurrentLinkedQueue<Measurement>();
	
	// The async task that will commit changes to the database
	private MeasurementDatabase mDatabase;
	//private DatabaseSaveTask mDatabaseWorker;
	
	// Display dimensions in pixels
	private int mDisplayWidth;
	private int mDisplayHeight;
	
	// Timer for measuring response times
	private MilliTimer mTimer = new MilliTimer();
	
	// We don't want to place the button too close to the edge of the
	// screen or some of it will be outside the view bounds
	private int mTargetSize;
	
	private Vibrator mVibrator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Hide the action bar
		//getActionBar().hide();
		
		// Remove the status bar (only needed for tablet interfaces with status 
		// bars like in 4.2 and on some smaller tablets like the Nexus 7)
		final Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Go into low profile mode		 
		mContentView = findViewById(android.R.id.content);
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		mContentView.setOnSystemUiVisibilityChangeListener(new 
				View.OnSystemUiVisibilityChangeListener() {
			
			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				Log.d(TAG, "System ui visibility changed.");
				if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
					Log.d(TAG, "UI changed to visible");
					delayedLowProfile(LOW_PROFILE_DELAY_MILLIS);
				}
			}
		});

		// Set up the measurement database and the worker
		mDatabase = new MeasurementDatabase(this);
		
		// Take control of the vibrator
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Set up the button to take the user to the heat map
		mViewHeatmapButton = (Button) findViewById(R.id.btn_view_heatmap);		
		mViewHeatmapButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Try save any unsaved measurements before moving to new activity
				saveToDatabase();
				
				// Launch new activity
				final Intent intent = new Intent(MainActivity.this, HeatmapActivity.class);
				startActivity(intent);
			}
		});
		
		// Get the display height and width
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mDisplayHeight = displayMetrics.heightPixels;
		mDisplayWidth = displayMetrics.widthPixels;
		
		// Set up left and right thumb buttons
     	mLeftButton = (ImageButton) findViewById(R.id.btn_left);
		mLeftButton.setOnTouchListener(mReadyButtonsListener);
		mRightButton = (ImageButton) findViewById(R.id.btn_right);
		mRightButton.setOnTouchListener(mReadyButtonsListener);
			
		// Initialise and place the target button
		mTargetButton = (ImageButton) findViewById(R.id.btn_target);
		mTargetButton.setOnClickListener(mTargetListener);		
		mTargetSize = getResources().getDimensionPixelOffset(R.dimen.target_btn_size);		
		placeTarget();
	}
	
	private void saveMeasurement(Measurement measurement) {
		mUnsavedMeasurements.add(measurement);
		
		if (mUnsavedMeasurements.size() > 
			UNSAVED_MEASUREMENTS_THRESHOLD) {
			saveToDatabase();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveToDatabase() {
		DatabaseSaveTask dst = new DatabaseSaveTask(mDatabase);
		dst.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, mUnsavedMeasurements);
	}
	
	private void placeTarget() {
		// Hide the target and then place it somewhere new...
		mTargetButton.setVisibility(View.INVISIBLE);
		
		// Choose a random position on the screen
		int y = mDisplayHeight * 2;
		// These are hacky loops to account for Java's rubbish random number
		// generator. In a parallel universe they are never escaped...
		while (y > mDisplayHeight - mTargetSize) {
			y = (int)Math.round(Math.random() * y);
		}
		int x = mDisplayWidth * 2;
		while (x > mDisplayWidth - mTargetSize) {
			x = (int)Math.round(Math.random() * x);
		}
		
		//Log.d(TAG, "y = " + y + ", x = " + x);
		//int x = (int)Math.round(Math.random() * (mDisplayWidth - mTargetSize));
		
		RelativeLayout.LayoutParams layoutParams = 
				(RelativeLayout.LayoutParams) mTargetButton.getLayoutParams();
		layoutParams.setMargins(x, y, 0, 0);
		mTargetButton.setLayoutParams(layoutParams);
	}
	
	private void showUI() {
		mLeftButton.setVisibility(View.VISIBLE);
		mRightButton.setVisibility(View.VISIBLE);
		mViewHeatmapButton.setVisibility(View.VISIBLE);
	}
	
	private void hideUI() {
		mLeftButton.setVisibility(View.INVISIBLE);
		mRightButton.setVisibility(View.INVISIBLE);
		mViewHeatmapButton.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Switches back to low-profile mode if the user touches the navbar
	 * after a certain delay
	 * @param delayMillis - the delay
	 */
	private void delayedLowProfile(int delayMillis) {
		mLowProfileHandler.removeCallbacks(mLowProfileRunnable);
		mLowProfileHandler.postDelayed(mLowProfileRunnable, delayMillis);
	}
	
	/**
	 * Shows the target after a delay.
	 * @param delayMillis - the delay
	 */
	private void delayedShowTarget(int delayMillis) {
		mShowTargetHandler.removeCallbacks(mShowTargetRunnable);
		mShowTargetHandler.postDelayed(mShowTargetRunnable, delayMillis);
	}
	
	// Handler just for hiding/showing the nav buttons
	private Handler mLowProfileHandler = new Handler();
	private Runnable mLowProfileRunnable = new Runnable() {
		@Override
		public void run() {
			mContentView.setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	};
	
	// Handler for the target as it moves about
	private Handler mShowTargetHandler = new Handler();
	private Runnable mShowTargetRunnable = new Runnable() {

		@Override
		public void run() {
			// Provide feedback that the target is about to become visible
			mVibrator.vibrate(VIBRATION_LENGTH_MILLIS);
			
			// Hide the thumb buttons so we can use the full expanse of the screen
			hideUI();
			
			// Show the target and set the timer
			mTargetButton.setVisibility(View.VISIBLE);
			mTimer.restart();
		}
		
	};
	
	private View.OnTouchListener mReadyButtonsListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mNumButtonsPressed++;
				// If both buttons pressed
				if (mNumButtonsPressed == 2) {
					delayedShowTarget(SHOW_TARGET_DELAY_MILLIS);
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				mNumButtonsPressed--;
				mShowTargetHandler.removeCallbacks(mShowTargetRunnable);
			}
			return false;
		}
	};
	
	private View.OnClickListener mTargetListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// Stop the clocks!
			int time = (int) mTimer.getTime();
			int x = v.getLeft() + mTargetSize / 2;
			int y = v.getTop() + mTargetSize / 2;
			
			Log.d(TAG, "Target hit at x=" + x + ", y=" + y);
			
			saveMeasurement(new Measurement(x, y, time, false));
			placeTarget();
			showUI();
		}
	};
}
