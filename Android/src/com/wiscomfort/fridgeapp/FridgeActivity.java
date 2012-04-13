package com.wiscomfort.fridgeapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FridgeActivity extends Activity {

	protected static final int UPDATE_ITEM_DIALOG = 101;
	protected static final int CONFLICT_IGNORE = 4;
	protected static final int ADD_ITEM_DIALOG = 100;
	private static final String TAG = "FridgeActivity";
	private String debug = new String();
	protected DataHelper dataHelper;
	protected Cursor data;
	protected SimpleCursorAdapter dataSource;
	protected SQLiteDatabase database;
	protected String selectedItem;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHelper = new DataHelper(this);
				
	}

	/*
	 * Set xml for menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return true;
	}

	/*
	 * Set items in menu and resulting actions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.add_item:
			// Show the dialog for adding an item
			this.showDialog(ADD_ITEM_DIALOG);

			return true;

		case R.id.show_inventory:
			if(!this.getClass().equals(com.wiscomfort.fridgeapp.FridgeViewActivity.class)){
				Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
						com.wiscomfort.fridgeapp.FridgeViewActivity.class);

				startActivity(i);
			}
			
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	protected void setupHttpRequest(){
		//TODO FIX this is only an example to test Android/Django interaction	
		HttpParams myParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(myParams, 10000);
		HttpConnectionParams.setSoTimeout(myParams, 10000);

		HttpClient httpClient = new DefaultHttpClient(myParams);
		HttpContext localContext = new BasicHttpContext();
		HttpPut httpPut = new HttpPut("http://localhost:8000/admin/fridge/item/add/");
		httpPut.setHeader("Accept", "application/json");
		httpPut.setHeader("Content-Type", "application/json");

		String data = // put your JSON object here
				"[ " +
				"	{" +
				"		\"pk\": \"AndroidItem\", " +
				"		\"model\": \"fridge.item\"," +
				"		\"fields\": {" +
				"			\"amount\": 42," +
				"			\"fridge\": 1" +
				"		}" +
				"	}" +
				"]";
		StringEntity tmp = null;
		try {
			tmp = new StringEntity(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		httpPut.setEntity(tmp);
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPut, localContext);
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		try {
			String result = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		

	}

	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id){
		case ADD_ITEM_DIALOG:
			dialog = getInstanceAddDialog();
			break;

		case UPDATE_ITEM_DIALOG:
			dialog = getInstanceUpdateDialog();
			break;

		default:
			dialog = null;
		}
		return dialog;
	}

	/*
	 * Here's where we actually build the AddDialog
	 */
	private Dialog getInstanceAddDialog() {

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_item_dialog);
		dialog.setTitle("Add Item");

		TextView text = (TextView) dialog.findViewById(R.id.add_text);
		text.setText("Please enter an item to add to your fridge.");

		ImageView image = (ImageView) dialog.findViewById(R.id.add_image);
		image.setImageResource(R.drawable.ic_launcher);

		final EditText editItemName = (EditText) dialog.findViewById(R.id.add_item_name);

		debug = "text: " + text.toString();
		Log.d(TAG, debug);

		Button scan = (Button) dialog.findViewById(R.id.add_via_scan);
		scan.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
            	Intent i = new Intent("com.google.zxing.client.android.SCAN");

            	startActivityForResult(i, 1);
				
				dialog.dismiss();
			}
		});
		
		Button submit = (Button) dialog.findViewById(R.id.submit_add);
		submit.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				String itemToAdd = editItemName.getText().toString();
				if (!itemToAdd.isEmpty()) { 
					addItem(itemToAdd);
				}
				// empty EditText and close dialog window
				
				editItemName.setText("");			
				dialog.dismiss();

			}
			
			
		});

		debug = "addItemSubmit: " + submit.toString();
		Log.d(TAG, debug);

		return dialog;
	}

	/*
	 * Here's where we actually build the UpdateDialog
	 */
	private Dialog getInstanceUpdateDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.update_item_dialog);
		dialog.setTitle("Update Item");

		//NumberPicker updatedAmount = (NumberPicker) dialog.findViewById(R.id.update_number_picker);
		//updatedAmount.setOn

		Button cancel = (Button) dialog.findViewById(R.id.cancel_update);
		cancel.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				dialog.dismiss();		
			}
		});

		final EditText countBox = (EditText) dialog.findViewById(R.id.update_item_count);
		Button submit = (Button) dialog.findViewById(R.id.submit_update);
		submit.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				int count = 0;
				try{
					count = Integer.parseInt(countBox.getText().toString());
				}catch(NumberFormatException nfe){
					countBox.setText("");
					dialog.dismiss();
					Toast.makeText(getApplicationContext(), "Not a number!", Toast.LENGTH_SHORT).show();
					return;
				}
				System.out.print(count);
				if(count == 0){
					removeItem(selectedItem);
				}
				else if(count < 0){
					Toast.makeText(getApplicationContext(), "Can't have Negative Items!", Toast.LENGTH_SHORT).show();
				}
				else{
					updateItem(selectedItem, count);
				}
				countBox.setText("");
				dialog.dismiss();
			}
		});

		return dialog;
	}

	/*
	 * Add item with name to db 
	 */
	protected void addItem(String name) {

		ContentValues updateItem = new ContentValues();
		long itemId;

		database = dataHelper.getWritableDatabase();
				
		updateItem.put("name", name);
		
		itemId = database.insertWithOnConflict(DataHelper.SOURCE_TABLE_NAME, null, updateItem, CONFLICT_IGNORE);

		// requery to refresh listview to reflect db changes
		data.requery();
		selectedItem = name;
		showDialog(UPDATE_ITEM_DIALOG);
		}

	/*
	 * remove item with name from db
	 */
	protected void removeItem(String name) {
		ContentValues removeItem = new ContentValues();
		database = dataHelper.getWritableDatabase(); 

		database.delete(DataHelper.SOURCE_TABLE_NAME, "name=?", new String[] {String.valueOf(name)});

		data.requery();
		Log.v(TAG, "Item removed from db");
	}
	
	/*
	 * update item with count
	 */
	
	protected void updateItem(String name, int count) {
		//TODO FIX BUG. Updating local Android DB causes app to crash
		ContentValues updateItem = new ContentValues();
		database = dataHelper.getWritableDatabase();
		
		updateItem.put("name", name);
		updateItem.put("amount", count);
		
		database.updateWithOnConflict(DataHelper.SOURCE_TABLE_NAME, updateItem, null, null, database.CONFLICT_IGNORE);
		
		// requery to refresh listview to reflect db changes
		data.requery();
		
	}

}
