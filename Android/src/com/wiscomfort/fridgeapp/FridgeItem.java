package com.wiscomfort.fridgeapp;

/*
 * Data structure to handle items in the fridge.
 */
public class FridgeItem {

		private String name;
		private int amount;
		private int fridge_id;
		
		public FridgeItem(String pk, int amount, int fridge){
			this.name = pk;
			this.amount = amount;
			this.fridge_id = fridge;
		}
		
		public int getAmount(){
			return this.amount;
		}
		
		public int getFridgeID(){
			return this.fridge_id;
		}
		
		public String toString(){
			return "Item: " + name + ", amount: " + amount + ", fridge_id: " + fridge_id;
		}
		
		
}
