package com.wiscomfort.fridgeapp;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

public class LoginToFridgeActivity extends FridgeActivity {
	private static final String TAG = "LoginToFridge";
	private String response;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//httpclient.setRedirectHandler(new DefaultRedirectHandler() { });
		String base_url = "http://ec2-23-20-255-144.compute-1.amazonaws.com/";
		String login = base_url + "fridge/login/";
		String search_fridge = base_url + "search/";
		// TODO get this the query filter from user qr scan
		search_fridge += "?q=SecondFridge";
		
		String[] urls = new String[2]; 
		urls[0] = login;
		urls[1] = search_fridge;
		
		new DownloadBody().execute(urls);

	}

	private void onPostDownloadListener(String result){
		WebView webview = new WebView(this);
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				
			}
		});

		webview.loadData(result, "text/html", "utf-8");
		setContentView(webview);
	}

	private class DownloadBody extends AsyncTask<String, Void, String> {
		/** The system calls this to perform work in a worker thread and
		 * delivers it the parameters given to AsyncTask.execute() 
		 * @return */
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			String csrftoken = getCsrfToken(httpclient, urls[0]);
			String result = getItemsFromFridge(httpclient, urls[1], csrftoken);
			return result;
		}
		
		/*
		 * Need to load the cookies before posting to django
		 */
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
			return null;
		}
		
		/*
		 * TODO Post to django with fridge auth code
		 */
		protected String getItemsFromFridge(HttpClient httpclient, String url, String csrftoken){
			String body = "";
			String charset = null;

			try {			
				HttpGet httpget = new HttpGet(url);
				
				//httppost.addHeader("Cookie", "csrftoken="+csrftoken);
				/*
				HttpParams params = new BasicHttpParams();
				params.setParameter("owner_name","neil");
				params.setParameter("fridge_id","neil");
				params.setParameter("csrfmiddlewaretoken", csrftoken);

				httppost.setParams(params);
				*/
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				body = (EntityUtils.toString(entity));
				charset = (EntityUtils.getContentCharSet(entity));
				Header header = entity.getContentType();

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return body;

		}

		/** The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground() */
		protected void onPostExecute(String result) {
			onPostDownloadListener(result);
			return;
		}
	}
}
