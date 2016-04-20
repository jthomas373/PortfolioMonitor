package com.ccllc.PortfolioMonitor.domain;

public class ClubMembership {
	
	private Long sysUserId;
	private Long clubId;
	private Boolean isClubAdmin;
	
	public Long getSysUserId() {
		return sysUserId;
	}
	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}
	
	public Long getClubId() {
		return clubId;
	}
	public void setClubId(Long clubId) {
		this.clubId = clubId;
	}

	public Boolean getIsClubAdmin() {
		return isClubAdmin;
	}
	public void setIsClubAdmin(Boolean isClubAdmin) {
		this.isClubAdmin = isClubAdmin;
	}

}
