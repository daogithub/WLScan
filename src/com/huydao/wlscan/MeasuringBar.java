package com.huydao.wlscan;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/* 	A horizontal bar on screen that has 
	AP name, signal level, and channel
	 
*/

public class MeasuringBar {
	
	public String mytext1; 	// text to display on bar
	public String mytext2; 	// text to display on bar
	public String mytext3; 	// text to display on bar
	public int bar_num;   	// which slot on screen to display bar
	int separator;          // to separate 2 bars
	int pad;                // pad bar so it looks nicer
	public int width;		// width of bar (usually the width of the screen)
	public int height;		// height of bar (110 dp for now)
	public int red_cutoff;  // how low to change color to red
	public int green_cutoff;// how high to change color to green
	public int db_filled;  	// db level (to be filled in the bar)
	public int db_range_max;// maximum db level to display
	public int db_range_min;// minimum db level to display
	int font_size;
	
	
	// constructor
	public MeasuringBar(int width, int height, int db_filled, int db_range_max, int db_range_min, int red_cutoff, int green_cutoff) {

		this.mytext1 = "";
		this.mytext2 = "";
		this.mytext3 = "";
		this.width = width;
		this.height = height;
		this.db_filled = db_filled;
		this.db_range_max = db_range_max;
		this.db_range_min = db_range_min;
		this.red_cutoff = red_cutoff;
		this.green_cutoff = green_cutoff;
		this.separator = 15;
		this.pad = 5;
		this.font_size = this.height/3;
	}

	
	public void draw(Canvas canvas, Paint paint)
	{
		int x = pad;
		int y = bar_num * height + pad;
		int x1 = x + width - 2 * pad;
		int y1 = y + height - separator;

		// make sure db level does not exceed the range
		if (db_filled > db_range_max) {
			db_filled = db_range_max;
		}
		
		if (db_filled < db_range_min) {
			db_filled = db_range_min;
		}

		// bar unit: how many <dp> is equivalent to 1 <db> 
		double bar_unit = (double) width / (double) (db_range_max - db_range_min); 
		
		// draw the empty bar
		paint.setTextSize(this.font_size);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(x, y, x1, y1, paint);

		// fill the bar based on db level
		// level coloring: red=bad, yellow=ok, green=good
		if (db_filled > db_range_min)
		{
			paint.setStyle(Paint.Style.FILL);
			if (db_filled <= red_cutoff)
			{
				paint.setColor(Color.rgb(200,  80,  80));
			}
			else if ((db_filled > red_cutoff ) && (db_filled < green_cutoff))
			{
				paint.setColor(Color.rgb(200, 200, 80));
			}
			else {
				paint.setColor(Color.rgb(80, 200, 80));
			}

			float x1_fill = (float) (x + width + (db_filled - db_range_max) * bar_unit  - 2 * pad);
			canvas.drawRect(x + 1, y + 1, x1_fill, (float) (y1 - height + height / 2), paint);		
			
			paint.setColor(Color.WHITE);
			canvas.drawText(mytext2 , x + pad, (y1 - pad - height + height / 2), paint);
			canvas.drawText(mytext1 + "   " + mytext3, x + pad, y1 - pad, paint);
			//canvas.drawText(mytext2, (float) (x + width * 1/2), y1 - pad, paint);
			//canvas.drawText(mytext3, width - this.font_size * 3 + font_size/2, y1 - pad, paint);
		}
	}
	
	
}
