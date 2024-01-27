package com.springbatch.domain;

public class OSProduct extends Product {
	
	private Integer taxPercent;
	private String sku;
	private Integer shippingRate;
	public Integer getTaxPercent() {
		return taxPercent;
	}
	public void setTaxPercent(Integer taxPercent) {
		this.taxPercent = taxPercent;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public Integer getShippingRate() {
		return shippingRate;
	}
	public void setShippingRate(Integer shippingRate) {
		this.shippingRate = shippingRate;
	}
	@Override
	public String toString() {
		return "OSProduct [taxPercent=" + taxPercent + ", sku=" + sku + ", shippingRate=" + shippingRate + "]";
	}
	
}
