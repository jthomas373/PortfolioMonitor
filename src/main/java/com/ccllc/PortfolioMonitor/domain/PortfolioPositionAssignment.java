package com.ccllc.PortfolioMonitor.domain;

public class PortfolioPositionAssignment {
	
	private Long PositionId;
	private String ticker;
	private String companyName;
	private Long sysUserId;
	
	public Long getPositionId() {
		return PositionId;
	}
	public void setPositionId(Long positionId) {
		PositionId = positionId;
	}
	
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	public Long getSysUserId() {
		return sysUserId;
	}
	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}
	

}
