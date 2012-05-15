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
}

class Fields {

	int amount;
	int fridge;
	
	@Override
	public String toString(){
		return "amount: " + amount + ", fridge: " + fridge;
	}
	
}