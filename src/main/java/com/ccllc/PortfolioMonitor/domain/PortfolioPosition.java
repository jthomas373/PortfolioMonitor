package com.ccllc.PortfolioMonitor.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PortfolioPosition {
	
	private Long positionId;
	private Long portfolioId;
	private String ticker;
	private String companyName;
	private Date purchaseDate;
	private String purchaseDateStr;
	private Double purchaseQty;
	private Double purchasePrice;
	private Double stopLossPct;
	private Double cagrPct;
	private Integer cagrGraceDays;
	private Boolean showActions;
	
	public Long getPositionId() {
		return positionId;
	}
	public void setPositionId(Long positionId) {
		this.positionId = positionId;
	}
	
	public Long getPortfolioId() {
		return portfolioId;
	}
	public void setPortfolioId(Long portfolioId) {
		this.portfolioId = portfolioId;
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
	
	public Date getPurchaseDate() {
		return purchaseDate;
	}
	public void setPurchaseDate(Date purchaseDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		this.purchaseDate = purchaseDate;
		this.purchaseDateStr = dateFormat.format(purchaseDate);
	}
	
	public String getPurchaseDateStr() {
		return purchaseDateStr;
	}
	public void setPurchaseDateStr(String purchaseDateStr) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		this.purchaseDateStr = purchaseDateStr;
		try {
			this.purchaseDate = dateFormat.parse(purchaseDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Double getPurchaseQty() {
		return purchaseQty;
	}
	public void setPurchaseQty(Double purchaseQty) {
		this.purchaseQty = purchaseQty;
	}
	
	public Double getPurchasePrice() {
		return purchasePrice;
	}
	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}
	
	public Double getStopLossPct() {
		return stopLossPct;
	}
	public void setStopLossPct(Double stopLossPct) {
		this.stopLossPct = stopLossPct;
	}
	
	public Double getCagrPct() {
		return cagrPct;
	}
	public void setCagrPct(Double cagrPct) {
		this.cagrPct = cagrPct;
	}
	
	public Integer getCagrGraceDays() {
		return cagrGraceDays;
	}
	public void setCagrGraceDays(Integer cagrGraceDays) {
		this.cagrGraceDays = cagrGraceDays;
	}

	public Boolean getShowActions() {
		return showActions;
	}
	public void setShowActions(Boolean showActions) {
		this.showActions = showActions;
	}

}
