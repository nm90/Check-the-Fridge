package com.wiscomfort.fridgeapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

public class DjangoParser {
	/*
	 * Create DjangoModel objects in java from json from server
	 */
	public static DjangoModel[] parseJsonModels(String json_items) {

		// Use JSONStringer to move json models to java objects
		// for every item in the fridge, create java object from json
		Gson gson = new Gson();

		DjangoModel[] models = gson.fromJson(json_items, DjangoModel[].class);
		return models;
		//String json = gson.toJson(models);
		//System.out.println(json);	
	}
	
	/*
	 * Create items after looking through DjangoModel objects
	 */
	public static ArrayList<FridgeItem> makeItemsFromModels(DjangoModel[] models) {

		ArrayList<FridgeItem> items = new ArrayList<FridgeItem>();

		for(DjangoModel model : models){
			if(model.isItem()){
				items.add(new FridgeItem(model));
			}
		}

		return items;
	}

	public static List<NameValuePair> getAttributesValuePairs(
			FridgeItem fridgeItem) {

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		nameValuePairs.add(new BasicNameValuePair("name", fridgeItem.getName()));
		nameValuePairs.add(new BasicNameValuePair("amount", fridgeItem.getAmountString()));
		nameValuePairs.add(new BasicNameValuePair("initial_amount", fridgeItem.getInitAmountString()));
		nameValuePairs.add(new BasicNameValuePair("upc", fridgeItem.getUPC()));
		nameValuePairs.add(new BasicNameValuePair("fridge_id", fridgeItem.getFridgeIDString()));
		
		return nameValuePairs;
	}
}