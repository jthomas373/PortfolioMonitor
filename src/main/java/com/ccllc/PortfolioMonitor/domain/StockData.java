package com.ccllc.PortfolioMonitor.domain;

import java.util.Date;

public abstract class StockData {
	
	private Date date;
	private Float open;
	private Float high;
	private Float low;
	private Float close;
	private Float adjClose;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Float getOpen() {
		return open;
	}
	public void setOpen(Float open) {
		this.open = open;
	}
	
	public Float getHigh() {
		return high;
	}
	public void setHigh(Float high) {
		this.high = high;
	}

	public Float getLow() {
		return low;
	}
	public void setLow(Float low) {
		this.low = low;
	}
	
	public Float getClose() {
		return close;
	}
	public void setClose(Float close) {
		this.close = close;
	}
	
	public Float getAdjClose() {
		return adjClose;
	}
	public void setAdjClose(Float adjClose) {
		this.adjClose = adjClose;
	}
	
}
