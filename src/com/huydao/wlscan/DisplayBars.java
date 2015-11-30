package com.huydao.wlscan;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DisplayBars extends View {

	Paint paint;
	int num_bars;
	int bar_width;
	int bar_height;
	WifiInfo connected_info;			// Current connection info
	String current_SSID;
	String connected_SSID;
	List<ScanResult> scan_result;	// APs' result from scanning
	List<MeasuringBar> bars; 		// bars to draw on screen
									// used to display AP name instead of MAC address
	
	public DisplayBars(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DisplayBars(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		connected_SSID = "";
		bars =  new ArrayList<MeasuringBar>();
		bar_height = 100;
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.WHITE);
		
	}
	
	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
	     super.onSizeChanged(xNew, yNew, xOld, yOld);
	     
	     // how many bars to display?
	     // use the screen height to determine this
	     num_bars = yNew / bar_height;
	     bar_width = xNew;
	     
	     // initialize all the bars with default values
	     for (int i=0; i<num_bars; i++)
	     {
	    	 //MeasuringBar bar = new MeasuringBar(bar_width - 2 * bar_padding, bar_height - 2 * bar_padding, -35, -95, -80, -72); 
	    	 MeasuringBar bar = new MeasuringBar(bar_width, bar_height, -95, -35, -95, -80, -72);
	    	 bar.bar_num = i;
	    	 bars.add(bar);
	     }
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

		// draw all bars
		for (int i = 0; i < num_bars; i++)
		{
			bars.get(i).draw(canvas, paint);
		}
	}
	
	// fetch current connection info
	public void fetch_connected_info (WifiInfo connected_info) {
		this.connected_info = connected_info;
	}
	
	// update bars with info from connectedInfo, apNameList, scanResultList
	public void updateBars(String selected_SSID) {
		
		//	reset all bars to display nothing
		for (int i=0; i<num_bars; i++)
	    {
			bars.get(i).db_filled = -95;
			bars.get(i).mytext1 = "";
	    }

		// put scan result values into bars for display
		DatabaseStuff dbase = new DatabaseStuff(this.getContext());
		SQLiteDatabase db = dbase.getReadableDatabase();
		String SQLtext; 
		
		if (selected_SSID.equals("All SSIDs")) { 
			/*SQLtext = "SELECT * FROM SCAN_RESULT LEFT OUTER JOIN WAP_NAME ON "
					+ "substr(SCAN_RESULT.BSSID,1,16)=substr(WAP_NAME.MAC,1,16) "
					+ "ORDER BY SCAN_RESULT.level DESC";*/
			SQLtext = "SELECT SSID, BSSID, name, level, channel, radio FROM "
					+ "FREQUENCY INNER JOIN SCAN_RESULT ON "
					+ "SCAN_RESULT.frequency=FREQUENCY.frequency "
					+ "LEFT OUTER JOIN WAP_NAME ON "
					+ "substr(SCAN_RESULT.BSSID,1,16)=substr(WAP_NAME.MAC,1,16) "
					+ "ORDER BY SCAN_RESULT.level DESC";
		}

		else {
			SQLtext = "SELECT SSID, BSSID, name, level, channel, radio FROM "
					+ "FREQUENCY INNER JOIN SCAN_RESULT ON "
					+ "SCAN_RESULT.frequency=FREQUENCY.frequency "
					+ "LEFT OUTER JOIN WAP_NAME ON "
					+ "substr(SCAN_RESULT.BSSID,1,16)=substr(WAP_NAME.MAC,1,16) "
					+ "WHERE SCAN_RESULT.SSID='" + selected_SSID + "' "
					+ "ORDER BY SCAN_RESULT.level DESC";

			/*SQLtext = "SELECT * FROM SCAN_RESULT LEFT OUTER JOIN WAP_NAME ON "
					+ "substr(SCAN_RESULT.BSSID,1,16)=substr(WAP_NAME.MAC,1,16) "
					+ "WHERE SCAN_RESULT.SSID='" + selected_SSID + "'"
					+ "ORDER BY SCAN_RESULT.level DESC";*/
		}
		
		Cursor cursor = db.rawQuery(SQLtext, null);

		int i = 0;
		if (cursor.moveToFirst())
		{
			do {

				Log.w("Huy", cursor.getString(0) + "-->" 
			               + cursor.getString(1) + "-->" 
	        			   + cursor.getString(2) + "-->" 
			               + cursor.getString(3) + "-->" 
			               + cursor.getString(4) + "-->"
	        			   + cursor.getString(5));
				
				MeasuringBar mb = bars.get(i);
				mb.db_filled = cursor.getShort(3);
        		
				String MAC = cursor.getString(1);
				String WAP_name = cursor.getString(2);
				
				if (WAP_name == null) {
					WAP_name = MAC;
				}
			
				String level = cursor.getString(3);
				int chan = cursor.getShort(4); 
				String SSID = cursor.getString(0);

				mb.mytext1 = SSID;
				if (MAC.equals(connected_SSID)) {
					mb.mytext1 = SSID + "(connected)";
				}
					
				mb.mytext2 = level + "db  " + "Ch" + chan; 				
				mb.mytext3 = WAP_name;
				
				i++;
				if (i == num_bars) {
    				break;
    			}
				
			} while (cursor.moveToNext());
		}
		db.close();
		
		// redraw with new values
		invalidate();
	}
}
