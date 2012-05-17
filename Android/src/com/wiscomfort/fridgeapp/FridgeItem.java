package com.wiscomfort.fridgeapp;

/*
 * Data structure to handle items in the fridge.
 */
public class FridgeItem {

		private String name;
		private int amount;
		private int inital_amount;
		private String upc;
		private int fridge_id;
		
		public FridgeItem(String pk, int amount, int inital_amount, int fridge, String UPC){
			this.name = pk;
			this.amount = amount;
			this.inital_amount = inital_amount;
			this.fridge_id = fridge;
			this.upc = UPC;
		}
		public FridgeItem(String pk, int amount, int fridge, String UPC){
			this.name = pk;
			this.amount = amount;
			this.inital_amount = amount;
			this.fridge_id = fridge;
			this.upc = UPC;
		}
		
		public FridgeItem(DjangoModel model){
			this.name = model.getPK();
			this.amount = model.getFields().getAmount();
			this.inital_amount = model.getFields().getInintal_amount();
			this.fridge_id = model.getFields().getFridge();
			this.upc = model.getFields().getUPC();
		}
		
		public String getUPC(){
			return this.upc;
		}
		
		public String getName(){
			return this.name;
		}
		
		public int getInital_amount(){
			return this.inital_amount;
		}
		
		public int getAmount(){
			return this.amount;
		}
		
		public int getFridgeID(){
			return this.fridge_id;
		}
		
		@Override
		public String toString(){
			return "Item: " + name + ", amount: " + amount + ", fridge_id: " + fridge_id;
		}

		public String getAmountString() {
			return "" + this.amount;
		}
		
		public String getInitAmountString() {
			return "" + this.inital_amount;
		}

		public String getFridgeIDString() {
			// TODO Auto-generated method stub
			return "" + this.fridge_id;
		}
		
}
