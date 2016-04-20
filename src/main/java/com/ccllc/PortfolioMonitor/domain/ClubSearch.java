package com.ccllc.PortfolioMonitor.domain;

import java.io.Serializable;

public class ClubSearch implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String clubName = "%";
	private String city = "%";
	private String state = "%";
	
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
}
