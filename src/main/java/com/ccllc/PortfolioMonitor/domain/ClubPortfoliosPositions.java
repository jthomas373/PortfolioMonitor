package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

public class ClubPortfoliosPositions {
	
	private String clubName;
	private Long clubId;
	private List<PortfolioPositions> portfolioPositions;
	
	public String getClubName() {
		return clubName;
	}
	public void setClubName(String clubName) {
		this.clubName = clubName;
	}
	
	public Long getClubId() {
		return clubId;
	}
	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}
	
	public List<PortfolioPositions> getPortfolioPositions() {
		return portfolioPositions;
	}
	public void setPortfolioPositions(List<PortfolioPositions> portfolioPositions) {
		this.portfolioPositions = portfolioPositions;
	}
	
}
