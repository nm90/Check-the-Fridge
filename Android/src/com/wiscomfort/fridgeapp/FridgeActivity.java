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
	protected static final int ZXING_SCAN_FROM_ADD = 300;
	protected static final int ZXING_SCAN_DIRECT = 301;
	protected static final int WEB_SCAN_RESULT = 400;
	protected static final String FLAG_FOR_UPDATE_UPC = "999999999";

	private String debug = new String();
	protected DataHelper dataHelper;
	protected Cursor data;
	protected SimpleCursorAdapter dataSource;
	protected SQLiteDatabase database;
	protected String selectedItem;
	private static ArrayList<FridgeItem> items;

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
			try {
				JSONArray json_array = new JSONArray(json_string);
				int i = 0;
				String informationArray[];
				String jsonName;
				String jsonAmount;
				while(!json_array.get(i).equals(null)){
					informationArray = json_array.getString(i).split(",");
					jsonAmount = informationArray[2];
					informationArray = informationArray[1].split(":");
					jsonName = informationArray[1].replace("\"", "");
					informationArray = jsonAmount.split(":");
					jsonAmount = informationArray[2];
					addItem(jsonName, Integer.parseInt(jsonAmount), "000000000" );
					updateItem(jsonName, Integer.parseInt(jsonAmount));
					i++;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return;
		}
		else if (requestCode == ZXING_SCAN_FROM_ADD){
			if (data != null) {
				String result = data.getStringExtra("SCAN_RESULT");
				if(Pattern.matches("[0-9]{1,13}", result)) {
					//send update request
					updateUPC(result);         
				} 
				else{
					updateUPC("000000000");
				}
				return;
			}
			else{
				return;
			}
		}
		else if (requestCode == ZXING_SCAN_DIRECT){

			// This is where we want to try and add UPC to webserver
			String upc = data.getStringExtra("SCAN_RESULT");
			if(Pattern.matches("[0-9]{1,13}", upc)) {
				// launch webactivity add upc to extras
				Intent i = new Intent(com.wiscomfort.fridgeapp.FridgeActivity.this,
						com.wiscomfort.fridgeapp.WebDBActivity.class);
				i.putExtra("upc", upc);
				//i.putExtra("upc", "000000000");
				startActivityForResult(i, WEB_SCAN_RESULT);
			}
			// TODO searching local database doesn't work currently
			/*
			if (data != null) {
				String result = data.getStringExtra("SCAN_RESULT");

				if(Pattern.matches("[0-9]{1,13}", result)) {
					SQLiteDatabase database = dataHelper.getReadableDatabase();
					String mySQL="SELECT * FROM " + DataHelper.SOURCE_TABLE_NAME + " WHERE UPC LIKE '"+ result + "'";
					this.data = database.rawQuery( mySQL, null);
					if(this.data.getCount() != 0){
						this.data.moveToPosition(0);
						updateItem(this.data.getString(this.data.getColumnIndex("name")), Integer.parseInt(this.data.getString(this.data.getColumnIndex("start_amount"))));
					}
				}
			}
			else{
				return;
			}*/
		}
		else if(requestCode == WEB_SCAN_RESULT){
			String webResult = data.getStringExtra("json_items");
			DjangoModel[] models = DjangoParser.parseJsonModels(webResult);
			items = DjangoParser.makeItemsFromModels(models);
			this.showDialog(ADD_VIA_SCAN_DIALOG);
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
			dialog = getInstanceAddViaScan(this.items);
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

private Dialog getInstanceAddViaScan(ArrayList<FridgeItem> scanResult){
	final Dialog dialog = new Dialog(this);
	dialog.setContentView(R.layout.add_via_scan);
	dialog.setTitle("Add Item");
	TextView text = (TextView) dialog.findViewById(R.id.add_text);
	text.setText("Please ensure data from scan is correct!.");

	ImageView image = (ImageView) dialog.findViewById(R.id.add_image);
	image.setImageResource(R.drawable.ic_launcher);
	String name = null;
	String count = null;
	if(scanResult != null){
		name = scanResult.get(0).getName();
		count = scanResult.get(0).getAmountString();
	}
	else{
		dialog.dismiss();
		Toast.makeText(getApplicationContext(), "UPC not in database!", Toast.LENGTH_SHORT).show();
	}
	
	final EditText editItemName = (EditText) dialog.findViewById(R.id.add_item_name);
	final EditText editItemCount = (EditText) dialog.findViewById(R.id.add_item_count);
	
	editItemName.setText(name);
	editItemCount.setText(count);
	
	Button submit = (Button) dialog.findViewById(R.id.submit_add);
	submit.setOnClickListener( new OnClickListener() {
		public void onClick(View v){
			
		}
	});
	
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

		final CheckBox scanNewUPC =  (CheckBox) dialog.findViewById(R.id.scan_barcode_on_add);

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
					if(scanNewUPC.isChecked()){
						addItem(itemToAdd, countToAdd, FLAG_FOR_UPDATE_UPC);
						selectedItem = itemToAdd;
						Intent i = new Intent("com.google.zxing.client.android.SCAN");
						i.putExtra("SCAN_MODE", "PRODUCT_MODE");
						startActivityForResult(i, ZXING_SCAN_FROM_ADD);

					}
					else{
						addItem(itemToAdd, countToAdd, "000000000");
					}
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

		/*public void onScanResultListener() {
			if(!FridgeActivity.items.isEmpty()){
				editItemName.setText(FridgeActivity.items.get(0).getName());
				editItemCount.setText(FridgeActivity.items.get(0).getAmount());
				scanNewUPC.setChecked(false);
			}
			else{
				editItemName.setText("Name");
				editItemCount.setText("Count");
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), "That Beer is not in the DataBase Add it!", Toast.LENGTH_SHORT).show();
			}
			return;
		}*/
		
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
				if(count < 0){
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


	protected void updateUPC(String UPC){
		//TODO Have to update the qurey the server for item with FLAG UPC, then replace it with UPC passed
		ContentValues updateItem = new ContentValues();
		database = dataHelper.getWritableDatabase();
		String mySQL="SELECT * FROM " + DataHelper.SOURCE_TABLE_NAME + " WHERE UPC LIKE '"+ FLAG_FOR_UPDATE_UPC + "'";
		this.data = database.rawQuery(mySQL, null);
		this.data.moveToPosition(0);
		updateItem.put("UPC", UPC);
		database.updateWithOnConflict(DataHelper.SOURCE_TABLE_NAME, updateItem, null, null, database.CONFLICT_IGNORE);

		// requery to refresh listview to reflect db changes
		data.requery();
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

}
