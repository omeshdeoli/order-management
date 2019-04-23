package com.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("systems.url")
public class SystemConfig {

	private String prodListUrl;

	private String stockListUrl;

	public String getProdListUrl() {
		return prodListUrl;
	}

	public void setProdListUrl(String prodListUrl) {
		this.prodListUrl = prodListUrl;
	}

	public String getStockListUrl() {
		return stockListUrl;
	}

	public void setStockListUrl(String stockListUrl) {
		this.stockListUrl = stockListUrl;
	}
	

}
