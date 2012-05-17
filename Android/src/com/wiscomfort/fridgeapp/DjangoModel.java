package com.wiscomfort.fridgeapp;


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

	public String getPK(){
		return this.pk;
	}
	public Fields getFields(){
		return this.fields;
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

	public int getInintal_amount(){
		return this.initial_amount; 
	}

	public int getAmount(){
		return this.amount;
	}

	public String getUPC(){
		return this.upc;
	}

	public int getFridge(){
		return this.fridge;
	}
}