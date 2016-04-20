package com.ccllc.PortfolioMonitor.domain;

import java.io.Serializable;

public class SysUserSearch implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Boolean active = true;
	private String username = "%";
	private String lastName = "%";
	
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
}
