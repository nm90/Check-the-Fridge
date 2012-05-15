package com.wiscomfort.fridgeapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 *		
 * Helper class to initialize database to store info between sessions
 *
 * @author neil
 *
 */
public class DataHelper extends SQLiteOpenHelper {

		protected static final String DATABASE_NAME = "fridge.db";
	    private static final int DATABASE_VERSION = 2;
	    protected static final String SOURCE_TABLE_NAME = "tbl_fridge_contents";
	    
		private static final String CREATE_RSS_TABLE =
    			"CREATE TABLE "+SOURCE_TABLE_NAME+"(	"+
    					BaseColumns._ID +
    						" INTEGER PRIMARY KEY AUTOINCREMENT,	"+
    					"name VARCHAR,								"+
    					"amount INTEGER,                            "+
    					"start_amount INTEGER,                      "+
    					"UPC VARCHAR,								"+
    					"UNIQUE(name)								"+
    			")										";			
	    
	    DataHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(CREATE_RSS_TABLE);
	    }

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

}
