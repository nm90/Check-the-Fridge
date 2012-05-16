package com.wiscomfort.fridgeapp;

import java.util.ArrayList;

/*
 * 
 */
public class DjangoModel {

	private String pk;
	private String model;
	private Fields fields;
	
	@Override
	public String toString(){
		return "pk: " + pk + ", fields: " + fields.toString();
	}
	
	public boolean isItem(){
		if(this.model.equals("fridge.item")){
			return true;
		}else{
			return false;
		}
	}
	
}


/*
 * 
 */
class Fields {
	private int initial_amount;
	private int amount;
	private int fridge;
	private String upc;
	
	
	@Override
	public String toString(){
		return "amount: " + amount + ", fridge: " + fridge;
	}
	
}