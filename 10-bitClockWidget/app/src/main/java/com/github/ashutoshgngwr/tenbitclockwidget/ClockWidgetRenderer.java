package com.github.ashutoshgngwr.tenbitclockwidget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.DimenRes;

import java.util.Calendar;

class ClockWidgetRenderer {

	private static final String TAG = ClockWidgetRenderer.class.getSimpleName();

	private static final int SEPARATOR_LINE_ALPHA = 0x75;

	private static ClockWidgetRenderer mInstance;

	private final int width = getDimen(R.dimen.widget_width);
	private final int height = getDimen(R.dimen.widget_height);
	private final Paint mPaint;
	private final Bitmap clockBitmap;
	private final Canvas canvas;

	private ClockWidgetRenderer() {
		clockBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(clockBitmap);
		mPaint = new Paint();
	}

	static Bitmap renderBitmap() {
		if (mInstance == null) {
			Log.d(TAG, "Creating a new renderer instance...");
			mInstance = new ClockWidgetRenderer();
		}

		return mInstance.render();
	}

	private int getDimen(@DimenRes int resId) {
		return Math.round(ClockWidgetApplication.getContext().getResources().getDimension(resId));
	}

	private void clearClockBitmap() {
		clockBitmap.eraseColor(Color.TRANSPARENT);
	}

	private void resetPaint() {
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
	}

	private Bitmap render() {
		clearClockBitmap();
		resetPaint();
	
		Calendar calendar = Calendar.getInstance();
		final boolean is24Hour = ClockWidgetSettings.shouldUse24HourFormat();
		final int nHourBits = is24Hour ? 5 : 4; // Use 5 bits for 24-hour mode, 4 bits for 12-hour mode
		final int hour = calendar.get(is24Hour ? Calendar.HOUR_OF_DAY : Calendar.HOUR);
		final int minute = calendar.get(Calendar.MINUTE);
		final int second = calendar.get(Calendar.SECOND); // Get seconds
	
		final int onBitColor = is24Hour || calendar.get(Calendar.AM_PM) == Calendar.AM
			? ClockWidgetSettings.getClockAMOnColor() : ClockWidgetSettings.getClockPMOnColor();
	
		final int offBitColor = is24Hour || calendar.get(Calendar.AM_PM) == Calendar.AM
			? ClockWidgetSettings.getClockAMOffColor() : ClockWidgetSettings.getClockPMOffColor();
	
		// Background
		mPaint.setColor(ClockWidgetSettings.getClockBackgroundColor());
		canvas.drawRoundRect(new RectF(0, 0, width, height), px(5), px(5), mPaint);
	
		// Define three horizontal rows for hour, minute, and second
		final float rowHeight = height / 3;
		
		// Hour line
		RectF bounds = new RectF(0, 0, width, rowHeight);
		renderBits(onBitColor, offBitColor, bounds, 1, nHourBits, nHourBits, hour);
	
		// Minute line
		bounds.set(0, rowHeight, width, 2 * rowHeight);
		renderBits(onBitColor, offBitColor, bounds, 1, 6, 6, minute);
	
		// Second line
		bounds.set(0, 2 * rowHeight, width, height);
		renderBits(onBitColor, offBitColor, bounds, 1, 6, 6, second);
	
		return clockBitmap;
	}


	@SuppressWarnings("SameParameterValue")
	private void renderBits(int onColor, int offColor, RectF bounds, int rows, int cols, int nBits, int bits) {
		final float dr = px(ClockWidgetSettings.getDotSize()); // Dot size
		final float cw = bounds.width() / cols; // Cell width
		final float ch = bounds.height() / rows; // Cell height
		final float cpx = (cw - (dr * 2)) / 2;
		final float cpy = (ch - (dr * 2)) / 2;
		float x = bounds.left;
		float y = bounds.centerY();
	
		for (int i = 0; i < cols; i++) {
			if (--nBits < 0) break;
	
			// Set color based on whether the bit is ON or OFF
			mPaint.setColor((bits >> i & 1) == 1 ? onColor : offColor);
	
			// Draw circle for bit
			canvas.drawCircle(x + cpx + dr, y, dr, mPaint);
			x += cw;
		}
	}


	private int px(int dp) {
		return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
			ClockWidgetApplication.getContext().getResources().getDisplayMetrics()));
	}
}
