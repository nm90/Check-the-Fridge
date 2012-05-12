package com.wiscomfort.fridgeapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class start extends Activity{

	 protected int splashTime = 6000; 
	 
     public void onCreate(Bundle savedInstanceState) { 
       super.onCreate(savedInstanceState); 
       setContentView(R.layout.start); 
       new Handler().postDelayed(new Runnable(){ 
           public void run() { 
               Intent intent = new Intent(start.this, FridgeViewActivity.class); 
               start.this.startActivity(intent); 
           start.this.finish(); 
             } 
       }, splashTime); 
     } 
} 

