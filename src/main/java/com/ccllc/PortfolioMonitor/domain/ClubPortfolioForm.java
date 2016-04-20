package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

public class ClubPortfolioForm {
	
	private List<Club> clubList;
	private List<Portfolio> clubPortfolios;
	private Portfolio addedPortfolio;
	private String selection;
	private String selectedClubId;
	private String selectedClubName;
	
	public List<Club> getClubList() {
		return clubList;
	}
	public void setClubList(List<Club> clubList) {
		this.clubList = clubList;
	}

	public List<Portfolio> getClubPortfolios() {
		return clubPortfolios;
	}
	public void setClubPortfolios(List<Portfolio> clubPortfolios) {
		this.clubPortfolios = clubPortfolios;
	}

	public Portfolio getAddedPortfolio() {
		return addedPortfolio;
	}
	public void setAddedPortfolio(Portfolio addedPortfolio) {
		this.addedPortfolio = addedPortfolio;
	}

	public String getSelection() {
		return selection;
	}
	public void setSelection(String selection) {
		this.selection = selection;
	}
	
	public String getSelectedClubId() {
		return selectedClubId;
	}
	public void setSelectedClubId(String selectedClubId) {
		this.selectedClubId = selectedClubId;
	}
	
	public String getSelectedClubName() {
		return selectedClubName;
	}
	public void setSelectedClubName(String selectedClubName) {
		this.selectedClubName = selectedClubName;
	}
	
}
