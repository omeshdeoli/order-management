package com.order.dto;

import java.util.List;

public class StockUpdate {

	private List<OrderLine> stockList;

	public StockUpdate(List<OrderLine> stockList) {
		super();
		this.stockList = stockList;
	}

	public List<OrderLine> getStockList() {
		return stockList;
	}

	public void setStockList(List<OrderLine> stockList) {
		this.stockList = stockList;
	}
}
