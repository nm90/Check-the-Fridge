package com.wiscomfort.fridgeapp;

/*
 * Data structure to handle items in the fridge.
 */
public class FridgeItem {

		public String pk;
		public String amount;
		public String fridge;
		
		public FridgeItem(String pk, String amount, String fridge){
			setPK(pk);
			setAmount(amount);
			setFridge(fridge);
		}
		
		public void setPK(String pk){
			this.pk = pk;
		}
		
		public void setAmount(String amount){
			this.amount = amount;
		}
		
		public void setFridge(String fridge){
			this.fridge = fridge;
		}
		
		public String getPK(){
			return this.pk;
		}
		
		public String getAmount(){
			return this.amount;
		}
		
		public String getFridge(){
			return this.fridge;
		}
		
		public String toString(){
			return getPK() + ": " + getAmount() + " " + getFridge();
		}
		
		
}
