package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

import org.jfree.data.time.TimeSeries;

public class ChartData {
	
	private List<TimeSeries> timeSeriesList;
	private double adjPurchasePrice;
	
	public List<TimeSeries> getTimeSeriesList() {
		return timeSeriesList;
	}
	public void setTimeSeriesList(List<TimeSeries> timeSeriesList) {
		this.timeSeriesList = timeSeriesList;
	}
	public double getAdjPurchasePrice() {
		return adjPurchasePrice;
	}
	public void setAdjPurchasePrice(double adjPurchasePrice) {
		this.adjPurchasePrice = adjPurchasePrice;
	}
	
}
