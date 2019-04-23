package com.order.dto;

import java.util.List;
import java.util.Set;

public class OrderCaptureRequest {

 private Long customerId;
 
 private List<OrderLine> orderLine;

public Long getCustomerId() {
	return customerId;
}

public void setCustomerId(Long customerId) {
	this.customerId = customerId;
}

public List<OrderLine> getOrderLine() {
	return orderLine;
}

public void setOrderLine(List<OrderLine> orderLine) {
	this.orderLine = orderLine;
}
  
}
