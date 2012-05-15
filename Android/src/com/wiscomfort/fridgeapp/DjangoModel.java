package com.wiscomfort.fridgeapp;

import java.util.ArrayList;


public class DjangoModel {

	String pk;
	String model;
	Fields fields;
	
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

class Fields {

	int amount;
	int fridge;
	
	@Override
	public String toString(){
		return "amount: " + amount + ", fridge: " + fridge;
	}
	
}