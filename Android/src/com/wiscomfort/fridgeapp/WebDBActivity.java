package com.wiscomfort.fridgeapp;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class WebDBActivity extends Activity {
	private static final String TAG = "LoginToFridge";
	protected static final int UPDATE_FRIDGE_REQUEST = FridgeActivity.UPDATE_FRIDGE_REQUEST;
	protected static final int SEARCH_FRIDGE_REQUEST = FridgeActivity.SEARCH_FRIDGE_REQUEST;
	private static final int nITEMS_FROM_FRIDGE = 0;
	private String response;
	private String upc_from_intent;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//httpclient.setRedirectHandler(new DefaultRedirectHandler() { });
		String[] urls = new String[2];
		
		String base_url = "http://ec2-23-20-255-144.compute-1.amazonaws.com/";
		String login = base_url + "fridge/login/";
		String search_fridge = base_url + "search/";
		
		// TODO get this the query filter from user qr scan
		search_fridge += "?q=Bassett";
		
		urls[nITEMS_FROM_FRIDGE] = search_fridge;
		
		Intent intent = this.getIntent();
		intent.getAction();
		
		Bundle extras = null;
		try{
			extras = intent.getExtras();
		} catch (NullPointerException e) {
			
		}
		if(extras != null){
			if(extras.containsKey("upc")){
				upc_from_intent = (String)extras.get("upc");
			}else{
				//TODO return failure	
			}
		}else{
			//TODO return failure
		}
		
		int flags = intent.getFlags();
		
		 
		
		new DownloadJsonItems().execute(urls);

	}

	
	/*
	 * 	pass json back to FridgeViewActivity
	 */
	private void onPostDownloadListener(String result){
	
		Intent intent = new Intent();
		intent.putExtra("json_items", result);
		this.setResult(RESULT_OK, intent);
		finish();
		//("json_items", result);
	}

	
	/*
	 * 
	 */
	private class DownloadJsonItems extends AsyncTask<String, Void, String> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() 
		 * @return */
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();

			JSONArray result = getItemsFromFridge(httpclient, urls[nITEMS_FROM_FRIDGE]);
			try {
				return result.toString(0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		
		/*
		 * Need to load the cookies before posting to django
		 */
		/**
		protected String getCsrfToken(HttpClient httpclient, String url){

			try {			
				HttpGet httpget = new HttpGet(url);
			
			
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();				
				
				entity.isChunked();
				String body = (EntityUtils.toString(entity));
				String charset = (EntityUtils.getContentCharSet(entity));
				Header header = entity.getContentType();
				
				JSONTokener jsontokener = new JSONTokener(body);
				try {
					JSONObject jsonobject = new JSONObject(jsontokener);
					String retval = jsonobject.get("csrf_token").toString();
					return retval;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return "#CHANGE_ME";
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO UnknownHost: launch dialog warning not logged in to wifi
				e.printStackTrace();
			}
			return "#CHANGE_ME";
		}
		*/
		
		/*
		 * TODO Post to django with fridge auth code
		 */
		protected JSONArray getItemsFromFridge(HttpClient httpclient, String url){
			String body = "";
			String charset = null;

			try {			
				HttpGet httpget = new HttpGet(url);
				
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				body = (EntityUtils.toString(entity));
				charset = (EntityUtils.getContentCharSet(entity));
				Header header = entity.getContentType();

				JSONTokener jsontokener = new JSONTokener(body);
				try {
					JSONArray jsonarray = new JSONArray(jsontokener);
					return jsonarray;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null; //"#CHANGE_ME";
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null; //"#CHANGE_ME";

		}

		/** The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground() */
		protected void onPostExecute(String result) {
			onPostDownloadListener(result);
			
			return;
		}
	}
}
