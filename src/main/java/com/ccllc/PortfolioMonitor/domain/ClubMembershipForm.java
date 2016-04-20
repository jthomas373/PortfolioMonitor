package com.ccllc.PortfolioMonitor.domain;

import java.util.List;

public class ClubMembershipForm {
	
	private List<Club> clubList;
	private List<SysUser> clubMembers;
	private String selection;
	
	public List<Club> getClubList() {
		return clubList;
	}
	public void setClubList(List<Club> clubList) {
		this.clubList = clubList;
	}
	
	public List<SysUser> getClubMembers() {
		return clubMembers;
	}
	public void setClubMembers(List<SysUser> clubMembers) {
		this.clubMembers = clubMembers;
	}

	public String getSelection() {
		return selection;
	}
	public void setSelection(String selection) {
		this.selection = selection;
	}
	
}
