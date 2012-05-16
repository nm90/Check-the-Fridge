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
	protected static final int WEB_SCAN_RESULT = FridgeActivity.WEB_SCAN_RESULT;
	private static final int nITEMS_FROM_FRIDGE = 0;
	private static final int nSEARCH_FOR_UPC = 1;
	private String response;
	private String upc_from_intent;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//httpclient.setRedirectHandler(new DefaultRedirectHandler() { });
		String[] urls = new String[2];
		
		String base_url = "http://ec2-23-20-255-144.compute-1.amazonaws.com/fridge/";
		String login = base_url + "login/";
		String search_fridge = base_url + "search/";
		String search_upc = base_url + "search-upc/?q=";
		
		// TODO get this the query filter from user qr scan
		//urls[nITEMS_FROM_FRIDGE] = search_fridge;
		
		
		
		Intent intent = this.getIntent();
		int flags = intent.getFlags();
		intent.getAction();
		
		Bundle extras = null;
		try{
			extras = intent.getExtras();
		} catch (NullPointerException e) {
			
		}
		if(extras != null){
			if(extras.containsKey("upc")){
				upc_from_intent = (String)extras.get("upc");
				if(extras.containsKey("name") && extras.containsKey("initial_amount")){
					// TODO need to set a flag here
				}
				
				search_upc += upc_from_intent;
				urls[nSEARCH_FOR_UPC] = search_upc;
			}else{
				//TODO return failure	
			}
		}else{
			//TODO return failure
		}
		
		
		
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
			JSONArray result = null;
			
			if( urls[nITEMS_FROM_FRIDGE] != null ){
				result = searchFridge(httpclient, urls[nITEMS_FROM_FRIDGE]);	
			}
			if( urls[nSEARCH_FOR_UPC] != null ){
				result = searchFridge(httpclient, urls[nSEARCH_FOR_UPC]);	
			}
			
			
			try {
				return result.toString(0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		

		/*
		 * TODO Post to django with fridge auth code
		 */
		protected JSONArray searchFridge(HttpClient httpclient, String url){
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
