package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

public class MonitorMenu {
	
	private List<PortfolioPositions> personalPortfolioList;
	private List<ClubPortfoliosPositions> clubPortfoliosPositions;
	public List<PortfolioPositions> getPersonalPortfolioList() {
		return personalPortfolioList;
	}
	public void setPersonalPortfolioList(
			List<PortfolioPositions> personalPortfolioList) {
		this.personalPortfolioList = personalPortfolioList;
	}
	public List<ClubPortfoliosPositions> getClubPortfoliosPositions() {
		return clubPortfoliosPositions;
	}
	public void setClubPortfoliosPositions(
			List<ClubPortfoliosPositions> clubPortfoliosPositions) {
		this.clubPortfoliosPositions = clubPortfoliosPositions;
	}
	
}
