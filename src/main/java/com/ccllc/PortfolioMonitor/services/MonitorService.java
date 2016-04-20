package com.ccllc.PortfolioMonitor.services;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.Club;
import com.ccllc.PortfolioMonitor.domain.ClubMembership;
import com.ccllc.PortfolioMonitor.domain.DailyStockData;
import com.ccllc.PortfolioMonitor.domain.MonitorMenu;
import com.ccllc.PortfolioMonitor.domain.MonthlyStockData;
import com.ccllc.PortfolioMonitor.domain.Portfolio;
import com.ccllc.PortfolioMonitor.domain.PortfolioPosition;
import com.ccllc.PortfolioMonitor.domain.PortfolioPositionAssignment;
import com.ccllc.PortfolioMonitor.domain.SysUser;
import com.ccllc.PortfolioMonitor.domain.ClubMember;
import com.ccllc.PortfolioMonitor.domain.WeeklyStockData;

public interface MonitorService {
	
	public DailyStockData getDayData(String ticker, Date date);
	public List<DailyStockData> getDailyData(String ticker, Date startDate, Date endDate);
	public List<WeeklyStockData> getWeeklyData(String ticker, Date startDate, Date endDate);
	public List<MonthlyStockData> getMonthlyData(String ticker, Date startDate, Date endDate);
	
	// Security
	public void checkAuthenticatedSession(HttpServletRequest request);
	public SysUser getLoggedInSysUser();
	
	// Utility Services
	public MonitorMenu getMonitorMenu(SysUser loggedInSysUser);
	public MonitorMenu getMaintainMenu(SysUser loggedInSysUser);
	
	
	// SysUser DAOs
	public void insertSysUser(SysUser sysUser);
	public SysUser updateSysUser(SysUser sysUser);
	public SysUser updateSysUserPwd(Long sysUserId, String password);
	public String encodePassword (String username, String password);
	public void deleteSysUser(Long sysUserId);
	public SysUser getSysUserById(Long sysUserId);
	public SysUser getSysUserByUsername(String username);
	public List<SysUser> searchSysUsers(String usernameCrit, String lastNameCrit, Boolean activeCrit);
	
	// Authority DAOs
	public void insertAuthority(Authority authority);
	public Authority updateAuthority(Authority authority);
	public void deleteAuthority(Long authorityId);
	public Authority getAuthorityById(Long authorityId);
	public List<Authority> getAllAuthorities();
	public List<Authority> getSysUserAuthorities(Long sysUserId);
	
	// SysUserAuthority DAOs
	public void grantAuthorityToSysUser(Long sysUserId, Long authorityId);
	public void revokeAuthorityFromSysUser(Long sysUserId, Long authorityId);
	public void revokeAuthorityFromAllSysUsers(Long authorityId);
	
	// Club DAOs
	public void insertClub(Club club);
	public Club updateClub(Club club);
	public void deleteClub(Long clubId);
	public Club getClubById(Long clubId);
	public Club getClubByClubName(String clubName);
	public List<Club> searchClubs(String clubNameCrit, String cityCrit, String stateCrit);
	public List<Club> getAllClubs();
	
	// ClubMembership DAOs
	public void addMemberToClub(Long sysUserId, Long clubId, Boolean isClubAdministrator);
	public void removeMemberFromClub(Long sysUserId, Long clubId);
	public void removeMembersFromClubByClubId(Long clubId);
	public void makeClubAdmin(Long clubId, Long sysUserId);
	public void revokeClubAdmin(Long clubId, Long sysUserId);
	public List<ClubMembership> getClubMembership(Long clubId, Long sysUserId);
	public List<Club> getClubsBySysUserId(Long sysUserId);
	public List<ClubMember> getClubMembersByClubId(Long clubId);
	public List<ClubMember> getNewClubMembersByClubId(Long clubId, String usernameCrit, String lastNameCrit, Boolean activeCrit);
	public List<Club> getClubsByClubAdminId(Long sysUserId);
	
	// ClubMemberPosition DAOs
	public void addMemberPosition(Long clubId, Long sysUserId, Long positionId);
	public void deleteMemberPosition(Long clubId, Long sysUserId, Long positionId);
	public void deleteMemberPositionByClubId(Long clubId);
	public void deleteMemberPositionByPositionId(Long positionId);
	public List<PortfolioPositionAssignment> getPortPositionAssignments(Long portfolioId);
	
	// Portfolio DAOs
	public void insertPortfolio(Portfolio portfolio);
	public Portfolio updatePortfolio(Portfolio portfolio);
	public void deletePortfolio(Long portfolioId);
	public Portfolio getPortfolioById(Long portfolioId);
	public Portfolio getPortfolioByName(String portfolioName);
	public List<Portfolio> getPortfoliosByClubId(Long clubId);
	public List<Portfolio> getPortfoliosBySysUserId(Long sysUserId);
	public List<Portfolio> getClubPortfoliosBySysUserId(Long sysUserId);
	public List<Portfolio> getAllPortfoliosBySysUserId(Long sysUserId);
	
	// PortfolioPosition DAOs
	public void insertPortfolioPosition(PortfolioPosition position);
	public PortfolioPosition updatePortfolioPosition(PortfolioPosition position);
	public void deletePortfolioPosition(Long positionId);
	public void deletePortfolioPositionsByPortfolioId(Long portfolioId);
	public PortfolioPosition getPortfolioPositionById(Long positionId);
	public List<PortfolioPosition> getAllPortfolioPositionsByPortfolioId(Long portfolioId);

}
