package com.wiscomfort.fridgeapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;


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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FridgeActivity extends Activity {

	protected static final int CONFLICT_IGNORE = 4;
	protected static final int ADD_ITEM_DIALOG = 100;
	protected static final int UPDATE_ITEM_DIALOG = 101;
	protected static final int ADD_VIA_SCAN_DIALOG = 102;
	private static final String TAG = "FridgeActivity";
	protected static final int UPDATE_FRIDGE_REQUEST = 200;
	protected static final int SEARCH_FRIDGE_REQUEST = 201;
	protected static final int ADD_TO_FRIDGE_REQUEST = 202;
	protected static final int QUERY_REQUEST = 203;
	protected static final int ZXING_SCAN_FROM_ADD = 300;
	protected static final int ZXING_SCAN_DIRECT = 301;
	protected static final int WEB_SCAN_RESULT = 400;
	protected static final String FLAG_FOR_UPDATE_COUNT = "999999999";

	private String debug = new String();
	protected DataHelper dataHelper;
	protected Cursor data;
	protected SimpleCursorAdapter dataSource;
	protected SQLiteDatabase database;
	protected String selectedItem;
	private static String scannedUPC;
	private static ArrayList<FridgeItem> items;
	private static int fridgeID = 1;

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

		case R.id.change_fridge:
			if(!this.getClass().equals(com.wiscomfort.fridgeapp.WebDBActivity.class)){
				Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
						com.wiscomfort.fridgeapp.WebDBActivity.class);
				String json_item = "[{\"pk\": \"Apple Jills\", \"model\": \"fridge.item\", "+ 
						"\"fields\": {\"initial_amount\": 1, \"amount\": 1, \"fridge\": 1, \"upc\": \"042111111111\"}}]";
				i.putExtra("item_to_add", json_item);
				startActivityForResult(i, UPDATE_FRIDGE_REQUEST);
			}

		default:
			return super.onContextItemSelected(item);
		}
	}


	/*
	 *
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UPDATE_FRIDGE_REQUEST){
			Bundle extras = data.getExtras();
			//TODO update list of items using the json here
			String json_string = (String) extras.get("json_items");
			DjangoModel[] models = DjangoParser.parseJsonModels(json_string);

			// TODO This is where we should update the FridgeView
			return;
		}
		else if (requestCode == ZXING_SCAN_DIRECT){

			// This is where we want to try and add UPC to webserver
			String upc = data.getStringExtra("SCAN_RESULT");
			if(Pattern.matches("[0-9]{1,13}", upc)) {
				this.scannedUPC = upc;
				Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
				com.wiscomfort.fridgeapp.WebDBActivity.class);
				i.putExtra("upc", upc);
				startActivityForResult(i, WEB_SCAN_RESULT);
			}
		}
		else if(requestCode == WEB_SCAN_RESULT){
			String webResult = data.getStringExtra("json_items");
			DjangoModel[] models = DjangoParser.parseJsonModels(webResult);
			items = DjangoParser.makeItemsFromModels(models);
			this.showDialog(ADD_VIA_SCAN_DIALOG);
		}	
		else if(requestCode == ADD_TO_FRIDGE_REQUEST){
			//TODO use addItem method to add to local DB if json is handed back 
			queryServer(getFridgeID());
			
		}
		else if(requestCode == QUERY_REQUEST){
			//TODO clear local database and repropagate with return data.
			database.delete(DataHelper.SOURCE_TABLE_NAME, null, null);
			Bundle extras = data.getExtras();
			//TODO update list of items using the json here
			String json_string = (String) extras.get("json_items");
			DjangoModel[] models = DjangoParser.parseJsonModels(json_string);
			ArrayList<FridgeItem> items = DjangoParser.makeItemsFromModels(models);
			// requery to refresh listview to reflect db change
			for(FridgeItem item : items){
				addItem(item);
			}
			this.data.requery();
			//database.rawQuery(sql, selectionArgs)
			
		}
		else {
			return;
		}
	}


	/*
	 * 
	 */
	protected Dialog onCreateDialog(int id){
		Dialog dialog;
		switch(id){
		case ADD_ITEM_DIALOG:
			dialog = getInstanceAddDialog();
			break;

		case UPDATE_ITEM_DIALOG:
			dialog = getInstanceUpdateDialog();
			break;

		case ADD_VIA_SCAN_DIALOG:
			dialog = getInstanceAddViaScan();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private Dialog getInstanceAddViaScan(){
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_via_scan);
		dialog.setTitle("Add Item");
		// launch webactivity add upc to extras
		
		TextView text = (TextView) dialog.findViewById(R.id.add_text);
		


		ImageView image = (ImageView) dialog.findViewById(R.id.add_image);
		image.setImageResource(R.drawable.ic_launcher);
		String name = null;
		String count = null;
		final FridgeItem scannedItem;
		final EditText editItemName = (EditText) dialog.findViewById(R.id.add_item_name);
		final EditText editItemCount = (EditText) dialog.findViewById(R.id.add_item_count);
		Button submit = (Button) dialog.findViewById(R.id.submit_add);
		ArrayList<FridgeItem> scanResult = getItems();

		if(scanResult == null || scanResult.isEmpty()){
			text.setText("UPC not in database add it now!.");
			editItemName.setText("Name");
			editItemCount.setText("Count");
			
		}
		else if(scanResult != null){
			text.setText("Please ensure data from scan is correct!.");
			scannedItem = scanResult.get(0);
			name = scanResult.get(0).getName();
			count = scanResult.get(0).getAmountString();

			editItemName.setText(name);
			editItemCount.setText(count);
		}
			
		submit.setOnClickListener( new OnClickListener() {
				public void onClick(View v){
					String name = editItemName.getText().toString();
					int count = -1;
					try{
						count = Integer.parseInt(editItemCount.getText().toString());
					}catch(Exception e){
						dialog.dismiss();
					}
					if(editItemName.getText().toString().isEmpty() || count < 0){
						dialog.dismiss();
					}else{
						FridgeItem itemToAdd = new FridgeItem(name, count, FridgeActivity.getFridgeID() , FridgeActivity.getScannedUPC());
						addItemToWeb(itemToAdd);
					}
					dialog.dismiss();
				}
			});
		return dialog;
	}

	protected void addItemToWeb(FridgeItem itemToAdd) {
		Gson gson = new Gson();
		String item_to_add = gson.toJson(itemToAdd);
		Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
				com.wiscomfort.fridgeapp.WebDBActivity.class);
		i.putExtra("item_to_add", item_to_add);
		startActivityForResult(i, ADD_TO_FRIDGE_REQUEST);
	}

	protected void queryServer(int fridgeID){
		Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
				com.wiscomfort.fridgeapp.WebDBActivity.class);
		i.putExtra("fridge_id", 1);
		startActivityForResult(i, QUERY_REQUEST);
	}

	/*
	 * Here's where we actually build the AddDialog
	 */
	private Dialog getInstanceAddDialog() {

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_item_dialog);
		dialog.setTitle("Add Item");

		TextView text = (TextView) dialog.findViewById(R.id.add_text);
		text.setText("Scan in a new product or manually enter it below:");

		ImageView image = (ImageView) dialog.findViewById(R.id.add_image);
		image.setImageResource(R.drawable.ic_launcher);

		final EditText editItemName = (EditText) dialog.findViewById(R.id.add_item_name);
		editItemName.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				editItemName.setText("");
			}
		});

		final EditText editItemCount = (EditText) dialog.findViewById(R.id.add_item_count);
		editItemCount.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				editItemCount.setText("");
			}
		});

		debug = "text: " + text.toString();
		Log.d(TAG, debug);


		Button scan = (Button) dialog.findViewById(R.id.add_via_scan);
		scan.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				Intent i = new Intent("com.google.zxing.client.android.SCAN");
				i.putExtra("SCAN_MODE", "PRODUCT_MODE");
				startActivityForResult(i, ZXING_SCAN_DIRECT);
				dialog.dismiss();
			}

		});

		Button submit = (Button) dialog.findViewById(R.id.submit_add);
		submit.setOnClickListener( new OnClickListener() {
			public void onClick(View v){
				String itemToAdd = editItemName.getText().toString();
				int countToAdd = -1;
				try{
					countToAdd = Integer.parseInt(editItemCount.getText().toString());
				}catch(NumberFormatException e){
					e.printStackTrace();
					//TODO handle this failure better
				}
				if (!itemToAdd.isEmpty() && countToAdd > 0) { 
					FridgeItem item = new FridgeItem(itemToAdd, countToAdd, getFridgeID(), "000000000");
					addItemToWeb(item);
				}
				else if(itemToAdd.isEmpty()){
					Toast.makeText(getApplicationContext(), "Need a name to identify the item!", Toast.LENGTH_SHORT).show();
				}
				else if(countToAdd < 0){
					Toast.makeText(getApplicationContext(), "Count must be greater then zero!", Toast.LENGTH_SHORT).show();
				}
				// empty EditText and close dialog window

				editItemName.setText("Name");	
				editItemCount.setText("Count");
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
				if(count < 0){
					Toast.makeText(getApplicationContext(), "Can't have Negative Items!", Toast.LENGTH_SHORT).show();
				}
				else{
					FridgeItem item = new FridgeItem(selectedItem, count, getFridgeID(), FLAG_FOR_UPDATE_COUNT );
					addItemToWeb(item);
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
	protected void addItem(String name, int amount, String UPC) {

		ContentValues updateItem = new ContentValues();
		long itemId;

		database = dataHelper.getWritableDatabase();

		updateItem.put("name", name);
		updateItem.put("amount", amount);
		updateItem.put("start_amount", amount);
		updateItem.put("UPC", UPC);

		itemId = database.insertWithOnConflict(DataHelper.SOURCE_TABLE_NAME, null, updateItem, database.CONFLICT_IGNORE);

		// requery to refresh listview to reflect db changes
		// data.requery();
	}
	
	protected void addItem(FridgeItem item) {

		ContentValues updateItem = new ContentValues();
		long itemId;

		database = dataHelper.getWritableDatabase();

		updateItem.put("name", item.getName());
		updateItem.put("amount", item.getAmount());
		updateItem.put("start_amount", item.getInital_amount());
		updateItem.put("UPC", item.getUPC());

		itemId = database.insertWithOnConflict(DataHelper.SOURCE_TABLE_NAME, null, updateItem, database.CONFLICT_IGNORE);

		// requery to refresh listview to reflect db changes
		data.requery();
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

		ContentValues updateItem = new ContentValues();
		database = dataHelper.getWritableDatabase();

		updateItem.put("name", name);
		updateItem.put("amount", count);

		database.updateWithOnConflict(DataHelper.SOURCE_TABLE_NAME, updateItem, null, null, database.CONFLICT_IGNORE);

		// requery to refresh listview to reflect db changes
		data.requery();

	}

	protected static String getScannedUPC(){
		return scannedUPC;
	}
	protected static int getFridgeID(){
		return fridgeID;
	}

	protected static ArrayList<FridgeItem> getItems(){
		return items;
	}

}
