package com.order.dto;

import java.util.List;

public class StockUpdateResponse {

	private List<OrderLine> stockList;

	public List<OrderLine> getStockList() {
		return stockList;
	}

	public void setStockList(List<OrderLine> stockList) {
		this.stockList = stockList;
	}
}
