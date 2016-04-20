package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

public class PortfolioPositions {
	
	private Portfolio portfolio;
	private List<PortfolioPosition> positions;
	
	public Portfolio getPortfolio() {
		return portfolio;
	}
	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
	}
	
	public List<PortfolioPosition> getPositions() {
		return positions;
	}
	public void setPositions(List<PortfolioPosition> positions) {
		this.positions = positions;
	}

}
