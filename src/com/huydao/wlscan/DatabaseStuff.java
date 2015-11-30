package com.huydao.wlscan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.util.Log;

public class DatabaseStuff extends SQLiteOpenHelper {

	Context context;
	String WAP_NAME;
	String SCAN_RESULT;
	String FREQUENCY;
	
	public DatabaseStuff(Context context) {
		super(context, "WAP_MANAGER", null, 1);
		WAP_NAME = "WAP_NAME";
		SCAN_RESULT ="SCAN_RESULT";
		FREQUENCY = "FREQUENCY";
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// WAP name table
		String SQLtext = "CREATE TABLE " +  WAP_NAME + " (MAC TEXT PRIMARY KEY, name TEXT);";
		db.execSQL(SQLtext);

		// Frequency table
		SQLtext = "CREATE TABLE " +  FREQUENCY + " (frequency INT PRIMARY KEY, channel INT, radio TEXT);";
		db.execSQL(SQLtext);
		
		// ScanResult table
		SQLtext = "CREATE TABLE " +  SCAN_RESULT + " ("
				+ "BSSID TEXT,"
				+ "SSID TEXT,"
				+ "frequency INTEGER,"
				+ "level INTEGER"
				+ ");";
		db.execSQL(SQLtext);
		
		new DownloadFileTask(context).execute("http://netman.worcester.edu/dl/WAP_names.csv");
		//update_WAP_names(db);
		insert_frequencies(db); 
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		String SQLtext = "DROP TABLE IF EXISTS " + WAP_NAME + ";";
		db.execSQL(SQLtext);

		SQLtext = "DROP TABLE IF EXISTS " + FREQUENCY + ";";
		db.execSQL(SQLtext);

		SQLtext = "DROP TABLE IF EXISTS " + SCAN_RESULT + ";";
		db.execSQL(SQLtext);

		onCreate(db);
	}

	public void update_WAP_names (SQLiteDatabase db) {
		try {

			URL url = new URL("http://netman.worcester.edu/dl/WAP_names.csv");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			InputStream inputStream = urlConnection.getInputStream();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			db.delete(WAP_NAME, null, null);
			
			String mLine = reader.readLine();
			while (mLine != null) {

				// Process text file with MAC address and names
				String[] stuff = mLine.split(",");
				String name = stuff[0].trim();
				String MAC = stuff[1].toLowerCase(Locale.US).trim();

				// Setting values for inserting
				ContentValues values = new ContentValues();
			    values.put("MAC", MAC); // Contact Name
			    values.put("name", name); // Contact Phone Number
			 
			    // Inserting...
			    db.insert(WAP_NAME, null, values);
				mLine = reader.readLine();
				
			}
			
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void update_WAP_names (BufferedReader reader) {
		try {

			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(WAP_NAME, null, null);
			
			String mLine = reader.readLine();
			while (mLine != null) {

				// Process text file with MAC address and names
				String[] stuff = mLine.split(",");
				String name = stuff[0].trim();
				String MAC = stuff[1].toLowerCase(Locale.US).trim();

				// Setting values for inserting
				ContentValues values = new ContentValues();
			    values.put("MAC", MAC); // Contact Name
			    values.put("name", name); // Contact Phone Number
			 
			    // Inserting...
			    db.insert(WAP_NAME, null, values);
				mLine = reader.readLine();
				
			}
			
			reader.close();
			db.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insert_frequencies (SQLiteDatabase db) {
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					context.getAssets().open("freq_chan.csv"), "UTF-8"));			
			
			db.delete(FREQUENCY, null, null);

			String mLine = reader.readLine();
			while (mLine != null) {

				String[] stuff = mLine.split(",");
				String frequency = stuff[0].trim();
				String channel = stuff[1].trim();
				String radio = stuff[2].trim();

				ContentValues values = new ContentValues();
			    values.put("frequency", frequency);
			    values.put("channel", channel);
			    values.put("radio", radio);
			 
			    db.insert(FREQUENCY, null, values);
				mLine = reader.readLine();
				
			}
			
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insert_scan_result (List<ScanResult> scan_result) {
		
		SQLiteDatabase db = this.getWritableDatabase();

		// Clear that SSID table first
		db.delete(SCAN_RESULT, null, null);

		// Insert scan result...
		for (int i = 0; i < scan_result.size(); i++) {


			ContentValues values = new ContentValues();

			String temp_SSID = scan_result.get(i).SSID;
			if (temp_SSID.isEmpty()) 
			{
				temp_SSID = "Hidden SSID";
			}
			values.put("SSID", temp_SSID);
			values.put("BSSID", scan_result.get(i).BSSID);
			values.put("frequency", scan_result.get(i).frequency);
			values.put("level", scan_result.get(i).level);
			
			db.insert(SCAN_RESULT, null, values);
		}

		db.close();
	}

	public List<String> get_SSIDs() {
		
		List<String> SSID_list = new ArrayList<String>();
		SQLiteDatabase db = this.getWritableDatabase();
		String SQLtext = "SELECT DISTINCT SSID FROM SCAN_RESULT ORDER BY SSID";
		SSID_list.add("All SSIDs");
		Cursor cursor = db.rawQuery(SQLtext, null);

		if (cursor.moveToFirst())
		{
			do {
			
				SSID_list.add(cursor.getString(0));

			} while (cursor.moveToNext());
		}
		
		db.close();
		return SSID_list;
	}
	
	public void test2() {
		
		SQLiteDatabase db = this.getWritableDatabase();
		String SQLtext = "SELECT * FROM SCAN_RESULT, WAP_NAME WHERE "
				+ "substr(SCAN_RESULT.BSSID,1,16)=substr(WAP_NAME.MAC,1,16) "
				+ "ORDER BY SCAN_RESULT.BSSID";

		Cursor cursor = db.rawQuery(SQLtext, null);
		
		if (cursor.moveToFirst())
		{
			do {
				Log.w("Huy", cursor.getString(0) + "-->" 
			               + cursor.getString(1) + "-->" 
	        			   + cursor.getString(2) + "-->" 
			               + cursor.getString(3) + "-->" 
			               + cursor.getString(4) + "-->"
	        			   + cursor.getString(5));
				
			} while (cursor.moveToNext());
		}

		db.close();
	}


}
