package com.order.dto;

import java.util.List;

public class Order {

	List<OrderLine> order;

	public List<OrderLine> getOrder() {
		return order;
	}

	public void setOrder(List<OrderLine> order) {
		this.order = order;
	}
	
}
