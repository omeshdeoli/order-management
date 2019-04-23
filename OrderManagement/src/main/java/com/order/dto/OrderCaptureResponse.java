package com.order.dto;

import java.util.List;

public class OrderCaptureResponse {

	private Long orderId;
	private List<OrderLine> orderLine;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public List<OrderLine> getOrderLine() {
		return orderLine;
	}

	public void setOrderLine(List<OrderLine> orderLine) {
		this.orderLine = orderLine;
	}

}
