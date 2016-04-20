package com.ccllc.PortfolioMonitor.domain;

import java.util.Date;

public class Split {
	
	private Date date;
	private Float ratio;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Float getRatio() {
		return ratio;
	}
	public void setRatio(Float ratio) {
		this.ratio = ratio;
	}

}
