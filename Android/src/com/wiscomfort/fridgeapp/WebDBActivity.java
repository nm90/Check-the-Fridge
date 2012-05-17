package com.wiscomfort.fridgeapp;

import java.io.IOException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class WebDBActivity extends Activity {
	private static final String TAG = "LoginToFridge";
	protected static final int UPDATE_FRIDGE_REQUEST = FridgeActivity.UPDATE_FRIDGE_REQUEST;
	protected static final int SEARCH_FRIDGE_REQUEST = FridgeActivity.SEARCH_FRIDGE_REQUEST;
	protected static final int WEB_SCAN_RESULT = FridgeActivity.WEB_SCAN_RESULT;
	private static final int nITEMS_FROM_FRIDGE_NAME = 0;
	private static final int nITEMS_FROM_FRIDGE_ID = 1;	
	private static final int nADD_ITEM_URL = 2;
	private static final int nADD_ITEM_HACK = 3;
	private static final int nSEARCH_FOR_UPC = 4;
	private String response;
	private String upc_from_intent;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//httpclient.setRedirectHandler(new DefaultRedirectHandler() { });
		String[] urls = new String[10];

		String base_url = "http://ec2-23-20-255-144.compute-1.amazonaws.com/fridge/";

		String login 				=	base_url + "login/";
		String search_fridge_name 	=	base_url + "search/?q=";
		String search_fridge_id		=	base_url + "search-id/?q=";	//TODO add to django
		String search_upc 			=	base_url + "search-upc/?q=";
		String post_item 			=	base_url + "update-item/";

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
				search_upc += upc_from_intent;
				urls[nSEARCH_FOR_UPC] = search_upc;
			}else if(extras.containsKey("item_to_add")){
				String item_to_add = (String)extras.get("item_to_add");
				urls[nADD_ITEM_URL] 			=	post_item;
				urls[nADD_ITEM_HACK] 			= 	item_to_add;
			}else if(extras.containsKey("fridge_name")){
				search_fridge_name 	+=	extras.getString("fridge_name");
				urls[nITEMS_FROM_FRIDGE_NAME] 	= 	search_fridge_name;
			}else if(extras.containsKey("fridge_id")){
				search_fridge_id	+=	extras.getInt("fridge_id");
				urls[nITEMS_FROM_FRIDGE_ID]		=	search_fridge_id;
			}else{
				//TODO return unknown extra detected
			}
		}else{
			//TODO return extra required
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
		@Override
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			JSONArray result = new JSONArray();

			if( urls[nITEMS_FROM_FRIDGE_NAME] != null ){
				result = getJSON(httpclient, urls[nITEMS_FROM_FRIDGE_NAME]);	
			}
			if( urls[nSEARCH_FOR_UPC] != null ){
				result = getJSON(httpclient, urls[nSEARCH_FOR_UPC]);	
			}
			if( urls[nADD_ITEM_HACK] != null ){
				result = postFridge(httpclient, urls[nADD_ITEM_URL], urls[nADD_ITEM_HACK]);
			}
			if( urls[nITEMS_FROM_FRIDGE_ID] != null){
				result = getJSON(httpclient, urls[nITEMS_FROM_FRIDGE_ID]);
			}

			try {
				return result.toString(0);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return "[]";
		}


		/*
		 * 
		 */
		private JSONArray postFridge(HttpClient httpclient, String url,
				String json_item_to_add) {
			String body = "[]";
			JSONArray jsonarray = new JSONArray();
			Gson gson = new Gson();
			
			try {			
				HttpPost httppost = new HttpPost(url);
				httppost.setHeader("Accept", "text/html");

				// Convert json back to FridgeItem so django has to do less work
				FridgeItem item_to_add = gson.fromJson(json_item_to_add, FridgeItem.class);

				// call method using items that returns List<NameValuePair> for all item attributes
				List<NameValuePair> nameValuePairs = DjangoParser.getAttributesValuePairs(item_to_add);			
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();


				body = (EntityUtils.toString(entity));

				Header header = entity.getContentType();

				JSONTokener jsontokener = new JSONTokener(body);
				try {
					jsonarray = new JSONArray(jsontokener);
					return jsonarray;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return jsonarray;
		}


		/*
		 * Get request to django url. Expecting JSON back.
		 */
		protected JSONArray getJSON(HttpClient httpclient, String url){
			String body = "[]";
			String charset = null;
			JSONArray jsonarray = new JSONArray();

			try {			
				HttpGet httpget = new HttpGet(url);

				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				body = (EntityUtils.toString(entity));
				charset = (EntityUtils.getContentCharSet(entity));
				Header header = entity.getContentType();

				JSONTokener jsontokener = new JSONTokener(body);
				try {
					jsonarray = new JSONArray(jsontokener);
					return jsonarray;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return jsonarray; //jsonarray should be empty if we get here
		}

		/** The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground() */
		@Override
		protected void onPostExecute(String result) {
			onPostDownloadListener(result);

			return;
		}
	}
}
