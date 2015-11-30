package com.huydao.wlscan;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {

	DisplayBars displayBars; // to display measuring bars on screen
	WifiManager wifi_manager; // to access to wifi services
	WifiReceiver wifi_receiver; // to receive wifi broadcast
	List<ScanResult> scan_result; // temporarily store wifi scan result
	WifiInfo wifi_info; // current wifi connection
	GestureDetector detector; // for GUI menu and stuff
	DatabaseStuff dbase; // To save wifi scan result
	Spinner spinner; // drop-down list of scanned SSIDs
	ArrayAdapter<String> adapter; // Used to popular the drop-down list
	String current_SSID; // What SSID to filter

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Turn off strict mode for Internet access (bad design, will try
		// different approach next version)
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// What SSID to filter, default is all SSIDs
		current_SSID = "All SSIDs";

		// Set up database to store scan information
		dbase = new DatabaseStuff(this);

		// initialize Displaybars
		displayBars = (DisplayBars) findViewById(R.id.DT);

		// Set up wifi service for scanning wifi network
		scan_result = new ArrayList<ScanResult>();
		wifi_manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifi_receiver = new WifiReceiver();

		// Initial scan...
		startScan_wifi();

		// Button to refresh (re-scan)
		final Button button_refresh = (Button) findViewById(R.id.button_refresh);
		button_refresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// actual scanning call
				startScan_wifi();
			}
		});

		// drop-down list of scanned SSIDs
		spinner = (Spinner) findViewById(R.id.spinner);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				current_SSID = parent.getSelectedItem().toString();
				setTitle("SSID: " + current_SSID);
				displayBars.updateBars(current_SSID);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		// startScan_wifi();
	}

	// use to receive scanning results
	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {

			scan_result = wifi_manager.getScanResults();

			// does scan return any info?
			if (!scan_result.isEmpty()) {

				dbase.insert_scan_result(scan_result);
				setTitle("SSID: " + current_SSID);
				WifiInfo wifiInfo = wifi_manager.getConnectionInfo();
				displayBars.connected_SSID = wifiInfo.getBSSID();
				displayBars.updateBars(current_SSID);
				adapter.clear();
				adapter.addAll(dbase.get_SSIDs());
			}

			unregisterReceiver(wifi_receiver);
		}
	}

	// Actual wifi scan
	void startScan_wifi() {
		setTitle("Please wait...");
		registerReceiver(wifi_receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifi_manager.startScan();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_update_WAPs:

			new DownloadFileTask(this).execute("http://netman.worcester.edu/dl/WAP_names.csv");
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
