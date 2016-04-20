package com.ccllc.PortfolioMonitor.domain;

import java.util.Date;

public class Transaction {
	
	private Date date;
	private TransactionType type;
	private Float shares;
	private Float price;
	private Float commission;
	private Float fees;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public TransactionType getType() {
		return type;
	}
	public void setType(TransactionType type) {
		this.type = type;
	}
	
	public Float getShares() {
		return shares;
	}
	public void setShares(Float shares) {
		this.shares = shares;
	}
	
	public Float getPrice() {
		return price;
	}
	public void setPrice(Float price) {
		this.price = price;
	}
	
	public Float getCommission() {
		return commission;
	}
	public void setCommission(Float commission) {
		this.commission = commission;
	}
	
	public Float getFees() {
		return fees;
	}
	public void setFees(Float fees) {
		this.fees = fees;
	}

}
