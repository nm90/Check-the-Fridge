package com.wiscomfort.fridgeapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

public class start extends Activity{

	 protected int splashTime = 4000; 
	
	 
     @Override
	public void onCreate(Bundle savedInstanceState) { 
       super.onCreate(savedInstanceState); 
       overridePendingTransition(R.anim.animate2, R.anim.animate2); 
       setContentView(R.layout.start); 
       final MediaPlayer mp = MediaPlayer.create(start.this, R.raw.duff);
       
       
       mp.start();
              
       new Handler().postDelayed(new Runnable(){ 
           public void run() { 
               Intent intent = new Intent(start.this, FridgeViewActivity.class); 
               start.this.startActivity(intent); 
               //change default screen transitions
               
               overridePendingTransition(R.anim.animate2, 0); 
               
           start.this.finish(); 
           mp.release();
             } 
       }, splashTime); 
     } 
} 

