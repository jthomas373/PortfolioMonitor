package com.ccllc.PortfolioMonitor.domain;

public class MonthlyStockData extends StockData{
	
	private Long avgVolume;
	
	public Long getAvgVolume() {
		return avgVolume;
	}
	public void setAvgVolume(Long avgVolume) {
		this.avgVolume = avgVolume;
	}
	
}
