package com.huydao.wlscan;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class DownloadFileTask extends AsyncTask<String, Void, String> {

	
	Context context;
	
	public DownloadFileTask(Context context){
		this.context =  context;
	}
	
	@Override
	protected String doInBackground(String... urls) {
		URL url;
		DatabaseStuff dbase;
		dbase = new DatabaseStuff(context);

		try {
			url = new URL(urls[0]);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			InputStream inputStream = urlConnection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			dbase.update_WAP_names(reader);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		Toast toast = Toast.makeText(context, "WAP names updated", Toast.LENGTH_LONG);
		toast.show();

	}

}
