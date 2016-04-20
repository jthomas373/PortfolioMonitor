package com.ccllc.PortfolioMonitor.domain;

import java.io.Serializable;

public class Authority implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long authorityId;
	private String authorityName;
	private String description;
	
	public Long getAuthorityId() {
		return authorityId;
	}
	public void setAuthorityId(Long authorityId) {
		this.authorityId = authorityId;
	}
	
	public String getAuthorityName() {
		return authorityName;
	}
	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
