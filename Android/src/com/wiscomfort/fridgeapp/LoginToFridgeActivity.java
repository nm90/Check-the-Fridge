package com.wiscomfort.fridgeapp;

import java.io.IOException;

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
import org.apache.http.util.EntityUtils;

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
		new DownloadBody().execute("http://ec2-23-20-255-144.compute-1.amazonaws.com/fridge/admin/");

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
			Header header = loadCookiesFromNetwork(urls[0]);
			loadCookiesFromNetwork(urls[0]);
			String result = loadFromNetwork(urls[0], header);
			return result;
		}
		
		/*
		 * Need to load the cookies before posting to django
		 */
		protected Header loadCookiesFromNetwork(String url){

			try {
				HttpClient httpclient = new DefaultHttpClient();			
				HttpGet httpget = new HttpGet(url);
			
			
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (response.containsHeader("Vary")){
					return response.getFirstHeader("Vary");
				}else{
					return response.getFirstHeader("Vary");
				}
				
				/*
				Header[] header = response.getAllHeaders();
				for(int i = 0; i < header.length; i++){
					String header_name = header[i].getName();
					if(header_name.equals("Vary")){
						return header[i];
					}
				}
				return header[0];
				*/
				
				
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		/*
		 * TODO Post to django with fridge auth code
		 */
		protected String loadFromNetwork(String url, Header getheaders){
			String body = "";
			String charset = null;

			try {
				HttpClient httpclient = new DefaultHttpClient();			
				HttpGet httpget = new HttpGet(url);
				
				Header[] headers = {
						new BasicHeader("owner_name", "neil"),
						new BasicHeader("fridge_id", "neil")
				};
				
				/*
				Header[] headers = new Header[getheaders.length + extraheaders.length];
				for(int i = 0; i < getheaders.length; i++){
					headers[i] = getheaders[i];
				}
				*/
				
				/*
				for(int i = getheaders.length, j=0; i < headers.length; i++,j++){
					headers[i] = extraheaders[j];
				}
				*/
				
				httpget.setHeaders(headers);
				httpget.addHeader(getheaders);
				

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
