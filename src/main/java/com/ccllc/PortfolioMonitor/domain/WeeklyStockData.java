package com.ccllc.PortfolioMonitor.domain;

public class WeeklyStockData extends StockData{
	
	private Long avgVolume;
	
	public Long getAvgVolume() {
		return avgVolume;
	}
	public void setAvgVolume(Long avgVolume) {
		this.avgVolume = avgVolume;
	}
	
}
