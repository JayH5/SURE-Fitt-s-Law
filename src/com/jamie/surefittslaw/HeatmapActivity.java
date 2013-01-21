package com.jamie.surefittslaw;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.jamie.surefittslaw.models.MeasurementAnalysis;
import com.jamie.surefittslaw.models.MeasurementDatabase;
import com.jamie.surefittslaw.models.MeasurementGrid;
import com.jamie.surefittslaw.tasks.GridAnalysisTask;
import com.jamie.surefittslaw.tasks.GridAnalysisTask.GridAnalysedListener;
import com.jamie.surefittslaw.tasks.GridCreationTask;
import com.jamie.surefittslaw.tasks.GridCreationTask.GridCreatedListener;

public class HeatmapActivity extends Activity implements GridCreatedListener, 
		GridAnalysedListener, OnNavigationListener {
	
	private static final String TAG = "HeatmapActivity";
	
	// To hide/show system UI
	//private static final boolean AUTO_HIDE = true;
	//private static final boolean TOGGLE_ON_CLICK = false;
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	//private static final int HIDER_FLAGS = SystemUiHider.FLAG_FULLSCREEN;
	//private SystemUiHider mSystemUiHider;
	
	// Diferent states selectable from ActionBar spinner
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final int STATE_MEAN = 0;
	private static final int STATE_MAX = 1;
	private static final int STATE_MIN = 2;
	private static final int STATE_STAN_DEV = 3;
	private static final int STATE_DISTANCE = 4;
	private static final int STATE_SAMPLE_SIZE = 5;
	private static final int STATE_FITTS_LAW = 6;
	private static final int STATE_FITTS_LAW_ERROR = 7;
	
	private MeasurementAnalysis mAnalysis;	
	private ProgressDialog mProgressDialog;
	
	private TableLayout mTable;
	private TableCell[][] mTableCells;
	private int mHeight;
	private int mWidth;
	private int mCellHeight;
	private int mCellWidth;
	private int mLeftBtnX;
	private int mRightBtnX;
	private int mBtnY;
	private int mTargetSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_heatmap);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		// Show the Up button in the action bar.
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// But we're gonna hide it anyway for now...
		//actionBar.hide();
		
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, 
						getResources().getStringArray(R.array.heatmap_states)),
						this);
		
		actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
			
			@Override
			public void onMenuVisibilityChanged(boolean isVisible) {
				if (isVisible) {
					mHideHandler.removeCallbacks(mHideRunnable);
				} else {
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
				
			}
		});
		
		// Hide the status bar
		final Window window = getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mTable = (TableLayout) findViewById(R.id.table);
		
		/*mSystemUiHider = new SystemUiHider(this, mTable, HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					
					@Override
					public void onVisibilityChange(boolean visible) {
						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});*/
		
		// Set up the user interaction to manually show or hide the system UI.
		mTable.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					/*if (TOGGLE_ON_CLICK) {
						mSystemUiHider.toggle();
					} else {
						mSystemUiHider.show();
					}*/
					getActionBar().show();
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			});
		
		initDimensions();
		initTable();
		initGrid();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			Log.d(TAG, "Restoring instance state...");
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		if (mAnalysis != null) {
			switch (position) {
			case STATE_MEAN:
				displayAnalysis(mAnalysis.getMeans());
				break;
			case STATE_MAX:
				displayAnalysis(mAnalysis.getMaximums());
				break;
			case STATE_MIN:
				displayAnalysis(mAnalysis.getMinimums());
				break;
			case STATE_STAN_DEV:
				displayAnalysis(mAnalysis.getStandardDeviations());
				break;
			case STATE_DISTANCE:
				displayAnalysis(mAnalysis.getDistances());
				break;
			case STATE_SAMPLE_SIZE:
				displayAnalysis(mAnalysis.getSampleSizes());
				break;
			case STATE_FITTS_LAW:
				displayAnalysis(mAnalysis.getFittsLaws());
				break;
			case STATE_FITTS_LAW_ERROR:
				displayAnalysis(mAnalysis.getFittsLawErrors());
				break;
			default:
				position = STATE_MEAN;
			}
			return true;
		}
		return false;
	}

	private void initDimensions() {
		// Get the cell width and height from dimens.xml
		mCellHeight = getResources()
				.getDimensionPixelOffset(R.dimen.grid_cell_height);
		mCellWidth = getResources()
				.getDimensionPixelOffset(R.dimen.grid_cell_width);
		
		// Get the effective display size in pixels
		final DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		final int displayHeight = dm.heightPixels;
		final int displayWidth = dm.widthPixels;
		
		// Calculate rows and columns of the grid
		mWidth = Math.round((float)displayWidth / mCellWidth);		
		mHeight = Math.round((float)displayHeight / mCellHeight);
		
		int btnSideMargin = getResources()
				.getDimensionPixelOffset(R.dimen.thumb_btn_side_margin);
		int btnTopMargin = getResources()
				.getDimensionPixelOffset(R.dimen.thumb_btn_top_margin);
		int btnSize = getResources()
				.getDimensionPixelOffset(R.dimen.thumb_btn_size);
		
		mLeftBtnX = btnSideMargin + btnSize / 2;
		mRightBtnX = displayWidth - mLeftBtnX;
		mBtnY = btnTopMargin + btnSize / 2;
		
		mTargetSize = getResources()
				.getDimensionPixelOffset(R.dimen.target_btn_size);
		
		Log.d(TAG, "Left button x: " + mLeftBtnX + ", right button x: " 
				+ mRightBtnX + ", button y: " + mBtnY);
	}
	
	private void initTable() {
		// Set up the references to each cell
		mTableCells = new TableCell[mHeight][mWidth];
		
		// Layout parameters for each row
		TableLayout.LayoutParams tableParams =
				new TableLayout.LayoutParams();
		tableParams.weight = 1;
		tableParams.height = 0;
		
		// Place all the rows and cells therein
		for (int y = 0; y < mHeight; y++) {			
			TableRow row = new TableRow(this);
			row.setLayoutParams(tableParams);
			
			for (int x = 0; x < mWidth; x++) {
				TableCell cell = new TableCell(this, x, y);
				mTableCells[y][x] = cell;
				row.addView(cell);
				cell.setOnClickListener(mHideListener);
				cell.setOnLongClickListener(mCalibrateListener);
			}
			
			mTable.addView(row);
		}
	}
	
	private void initGrid() {
		mProgressDialog = ProgressDialog.show(this, "Loading..", 
				"Loading measurements from database. Please wait.", true, false);
		
		MeasurementDatabase db = new MeasurementDatabase(this);
		GridCreationTask gct = new GridCreationTask(this, mWidth, mHeight, 
				mCellWidth, mCellHeight);
		
		gct.execute(db);
	}
	
	private void calibrateFittsLaw(int x, int y) {
		int mean = mAnalysis.getMean(x, y);
		int distance = mAnalysis.getDistance(x, y);
		
		// First calculate the new slope coefficient
		int intercept = MeasurementAnalysis.INTERCEPT;
		int slope = (mean - intercept);
		slope = (int) (slope / (Math.log(1 + distance / mTargetSize) 
				/ Math.log(2)));
		
		// Then update the Fitt's law values
		for (int row = 0; row < mHeight; row++) {
			for (int col = 0; col < mWidth; col++) {
				// Calculate new Fitt's Law value
				distance = mAnalysis.getDistance(col, row);
				int fittsLaw = calculateFittsLaw(distance, intercept, slope);
				
				// Calculate new error value
				mean = mAnalysis.getMean(col, row);
				int error = Math.abs(mean - fittsLaw);
				
				// Update the analysis
				mAnalysis.setFittsLaw(col, row, fittsLaw);
				mAnalysis.setFittsLawError(col, row, error);
			}
		}
		
		maybeUpdateDisplay();
	}
	
	private int calculateFittsLaw(int distance, int intercept, int slope) {
		return (int) (intercept + slope * 
				Math.log(1 + distance / mTargetSize) / Math.log(2));
	}
	
	private void maybeUpdateDisplay() {
		int navIndex = getActionBar().getSelectedNavigationIndex();
		if (navIndex == STATE_FITTS_LAW || 
				navIndex == STATE_FITTS_LAW_ERROR) {
			onNavigationItemSelected(navIndex, 0);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void displayAnalysis(int[][] data) {
		// Calculate the range of the data
		int min = Integer.MAX_VALUE;
		int max = 0;
		
		for (int[] row : data) {
			for (int cell : row) {
				if (cell > 0) {
					min = Math.min(min, cell);
					max = Math.max(max, cell);
				}
			}
		}
		
		if (min == Integer.MAX_VALUE) {
			min = 0;
		}
		
		// Take logs to accentuate differences
		final double logMin = Math.log10(min);
		final double logMax = Math.log10(max);
		final double logDelta = logMax - logMin;		
		
		for (int y = 0; y < mHeight; y++) {			
			for (int x = 0; x < mWidth; x++) {				
				int value = data[y][x];
				TableCell cell = mTableCells[y][x];
				if (value > 0) {
					double logValue = Math.log10(value);				
					float huePercent = (float)((logMax - logValue) / (logDelta));
					cell.setHuePercent(huePercent);				
				} else {
					cell.setBackgroundColor(getResources()
							.getColor(android.R.color.white));
				}
				cell.setText("" + value);
			}
		}
	}

	@Override
	public void onGridCreated(MeasurementGrid grid) {
		GridAnalysisTask gat = new GridAnalysisTask(this, mLeftBtnX, mRightBtnX, 
				mBtnY, mTargetSize);
		gat.execute(grid);
	}

	@Override
	public void onGridAnalysed(MeasurementAnalysis analysis) {
		mAnalysis = analysis;
		mProgressDialog.dismiss();
		
		onNavigationItemSelected(STATE_MEAN, 0);
	}
	
	// Handler for hiding system UI
	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			//mSystemUiHider.hide();
			//delayedHide(AUTO_HIDE_DELAY_MILLIS);
			getActionBar().hide();
		}
	};
	
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	private View.OnLongClickListener mCalibrateListener = new View.OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			final int x = ((TableCell) v).x;
			final int y = ((TableCell) v).y;
			Log.d(TAG, "Calibrating with cell: (" + x + "," + y + ")");
			
			// Open up an alert dialog to confirm the action
			AlertDialog.Builder builder = new AlertDialog.Builder(HeatmapActivity.this);
			AlertDialog dialog = builder.setTitle(R.string.dialog_title)
				.setMessage(R.string.dialog_message)
				.setNegativeButton(R.string.dialog_negative_button, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						
					}
				})
				.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						calibrateFittsLaw(x, y);
						
					}
				})
				.create();
			
			dialog.show();
			
			return true;
		}
	};
	
	private View.OnClickListener mHideListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			getActionBar().show();
			delayedHide(AUTO_HIDE_DELAY_MILLIS);			
		}
	};

}
