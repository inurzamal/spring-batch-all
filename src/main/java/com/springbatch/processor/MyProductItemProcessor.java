package com.springbatch.processor;

import org.springframework.batch.item.ItemProcessor;

import com.springbatch.domain.OSProduct;
import com.springbatch.domain.Product;

public class MyProductItemProcessor implements ItemProcessor<Product, OSProduct> {

	@Override
	public OSProduct process(Product item) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("processor() executed");
		OSProduct osProduct = new OSProduct();
		osProduct.setProductId(item.getProductId());
		osProduct.setProductName(item.getProductName());
		osProduct.setProductCategory(item.getProductCategory());
		osProduct.setProductPrice(item.getProductPrice());
		osProduct.setTaxPercent(item.getProductCategory().equals("Sports Accessories") ? 5 : 18);
		osProduct.setSku(item.getProductCategory().substring(0,3) + item.getProductId());
		osProduct.setShippingRate(item.getProductPrice() < 1000 ? 75 : 0);
		return osProduct;
	}

}
