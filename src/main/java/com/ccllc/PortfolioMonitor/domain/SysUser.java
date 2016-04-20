package com.ccllc.PortfolioMonitor.domain;

import java.io.Serializable;

public class SysUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long sysUserId;
	private Boolean active;
	private String username;
	private String password;
	private String lastName;
	private String firstName;
	private String middleInit;
	
	public Long getSysUserId() {
		return sysUserId;
	}
	public void setSysUserId(Long sysUserId) {
		this.sysUserId = sysUserId;
	}
	
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
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getMiddleInit() {
		return middleInit;
	}
	public void setMiddleInit(String middleInit) {
		this.middleInit = middleInit;
	}

}
