package com.ccllc.PortfolioMonitor.domain;

public class Club {
	
	private Long clubId;
	private String clubName;
	private String description;
	private String notes;
	private String city;
	private String state;
	private String country;
	
	public Long getClubId() {
		return clubId;
	}
	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}
	
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
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
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
}
