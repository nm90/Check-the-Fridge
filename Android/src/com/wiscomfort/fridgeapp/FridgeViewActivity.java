package com.wiscomfort.fridgeapp;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.util.Log;
import android.view.View;

/**Dialog
 * 
 * Activity creates a ListView to display stored rss feeds from the database
 *
 * @author neil
 *
 */
public class FridgeViewActivity extends FridgeActivity {
	private static final String TAG = "ListFeeds";
	
	String prefix;
	String[] rssList;
	String[] columns;
	Object[] column_objects;
	ListView listView;
	ArrayList<String> displayedColumns = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// Adds the columns to be displayed in the list
		setupList();
		// Only reading information here...
		dataHelper = new DataHelper(this);
		this.database = dataHelper.getReadableDatabase();

		// displayedColumns is an ArrayList here to simplify adding additional columns to be displayed later
		column_objects = displayedColumns.toArray();
		columns = new String[column_objects.length];
		for(int i=0; i<column_objects.length; i++) {
			columns[i] = column_objects[i].toString();
		}

		// order the list by the first displayed column
		data = database.rawQuery("select * from " + DataHelper.SOURCE_TABLE_NAME + " where amount > 0", null);
		
		dataSource = new SimpleCursorAdapter(this,
				R.layout.listview, data, columns,
				new int[] { R.id.item_names, R.id.item_count });

		listView = new ListView(this);
		listView.setAdapter(dataSource);
		
		setContentView(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id){

				// Setup db cursor to retrieve strings
				SQLiteDatabase database = dataHelper.getReadableDatabase();
				Cursor data = database.query(DataHelper.SOURCE_TABLE_NAME, columns, null, null, null, null, null);
				data.moveToPosition(position);
				selectedItem = data.getString(data.getColumnIndex("name"));
				// launch AlterDialog for the clicked item
				showDialog(UPDATE_ITEM_DIALOG);
				

			}
		});
	}

	/*
	 * helper method to add the columns being queried from database
	 */
	private void setupList(){
		displayedColumns.add("name");
		displayedColumns.add("amount");
		displayedColumns.add(BaseColumns._ID); 
	}

	
}
