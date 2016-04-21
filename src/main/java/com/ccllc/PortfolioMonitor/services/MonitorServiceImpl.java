package com.ccllc.PortfolioMonitor.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ccllc.PortfolioMonitor.dao.MonitorDao;
import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.Club;
import com.ccllc.PortfolioMonitor.domain.ClubMember;
import com.ccllc.PortfolioMonitor.domain.ClubMembership;
import com.ccllc.PortfolioMonitor.domain.ClubPortfoliosPositions;
import com.ccllc.PortfolioMonitor.domain.DailyStockData;
import com.ccllc.PortfolioMonitor.domain.MonitorMenu;
import com.ccllc.PortfolioMonitor.domain.MonthlyStockData;
import com.ccllc.PortfolioMonitor.domain.Portfolio;
import com.ccllc.PortfolioMonitor.domain.PortfolioPosition;
import com.ccllc.PortfolioMonitor.domain.PortfolioPositionAssignment;
import com.ccllc.PortfolioMonitor.domain.PortfolioPositions;
import com.ccllc.PortfolioMonitor.domain.SysUser;
import com.ccllc.PortfolioMonitor.domain.WeeklyStockData;
//import common.Logger;

@Service("monitorService")
public class MonitorServiceImpl implements MonitorService {
	
	@Autowired
	private MonitorDao monitorDao;
	
	private static Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);
	SimpleDateFormat yahooDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public MonitorDao getMonitorDao() {
		return monitorDao;
	}
	public void setMonitorDao(MonitorDao monitorDao) {
		this.monitorDao = monitorDao;
	}

	public DailyStockData getDayData(String ticker, Date date) {
		DailyStockData dayData = new DailyStockData();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		String dateMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (dateMonth.length() == 1) dateMonth = "0" + dateMonth;
		String dateDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (dateDay.length() == 1) dateDay = "0" + dateDay;
		String dateYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		String address = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + 
			"&a=" + dateMonth + "&b=" + dateDay + "&c=" + dateYear + 
			"&d=" + dateMonth + "&e=" + dateDay + "&f=" + dateYear + 
			"&g=d&ignore=.csv";
		Reader charIn = null;
		URLConnection conn = null;
		StringBuffer strBuffer = new StringBuffer();
		int lineNumber = 0;
		char[] buffer = new char[1024];
		//int numRead;
		//long numWritten = 0;
		try {
			URL url = new URL(address);
			conn = url.openConnection();
			charIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//while ((numRead = charIn.read(buffer)) != -1) {
			while ((charIn.read(buffer)) != -1) {
				for(char c : buffer) {
					if(c != '\r' && c != '\n') {  // character is not carriage return or line feed
						strBuffer.append(c);
					} else if(c == '\r') {
						// do nothing
					} else if(c == '\n') {
						// The first line (lineNumber == 0) is the column names - IGNORE.  Since
						// we are getting data for one day, only the second line is relevant.
						// There should only be two lines.
						if (lineNumber == 1) {
							String csvData = strBuffer.toString();
							StringTokenizer st = new StringTokenizer(csvData, ","); // comma delimited (CSV file)
							dayData.setDate(yahooDateFormat.parse(st.nextToken()));
							dayData.setOpen(Float.valueOf(st.nextToken()));
							dayData.setHigh(Float.valueOf(st.nextToken()));
							dayData.setLow(Float.valueOf(st.nextToken()));
							dayData.setClose(Float.valueOf(st.nextToken()));
							dayData.setVolume(Long.valueOf(st.nextToken()));
							dayData.setAdjClose(Float.valueOf(st.nextToken()));
						} else {
							strBuffer.setLength(0);
						}
						lineNumber++;
					}
				}
				//numWritten += numRead;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (charIn != null) {
					charIn.close();
				}
			} catch (IOException ioe) {
			}
		}
		return dayData;
	}

	public List<DailyStockData> getDailyData(String ticker, Date startDate, Date endDate) {
		List<DailyStockData> dailyStockDataList = new ArrayList<DailyStockData>();
		GregorianCalendar cal = new GregorianCalendar();
		
		cal.setTime(startDate);
		String startMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (startMonth.length() == 1) startMonth = "0" + startMonth;
		String startDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (startDay.length() == 1) startDay = "0" + startDay;
		String startYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		cal.setTime(endDate);
		String endMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (endMonth.length() == 1) endMonth = "0" + endMonth;
		String endDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (endDay.length() == 1) endDay = "0" + endDay;
		String endYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		String address = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + 
			"&a=" + startMonth + "&b=" + startDay + "&c=" + startYear + 
			"&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + 
			"&d=w&ignore=.csv";
		Reader charIn = null;
		URLConnection conn = null;
		StringBuffer strBuffer = new StringBuffer();
		int lineNumber = 0;
		char[] buffer = new char[1024];
		//int numRead;
		//long numWritten = 0;
		try {
			URL url = new URL(address);
			conn = url.openConnection();
			charIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//while ((numRead = charIn.read(buffer)) != -1) {
			while ((charIn.read(buffer)) != -1) {
				for(char c : buffer) {
					if(c != '\r' && c != '\n') {  // character is not carriage return or line feed
						strBuffer.append(c);
					} else if(c == '\r') {
						// do nothing
					} else if(c == '\n') {
						// The first line (lineNumber == 0) is the column names - IGNORE.  Since
						// we are getting data for a date range, all lines after the first are relevant.
						if (lineNumber > 0) {
							String csvData = strBuffer.toString();
							StringTokenizer st = new StringTokenizer(csvData, ","); // comma delimited (CSV file)
							DailyStockData dailyData = new DailyStockData();
							dailyData.setDate(yahooDateFormat.parse(st.nextToken()));
							dailyData.setOpen(Float.valueOf(st.nextToken()));
							dailyData.setHigh(Float.valueOf(st.nextToken()));
							dailyData.setLow(Float.valueOf(st.nextToken()));
							dailyData.setClose(Float.valueOf(st.nextToken()));
							dailyData.setVolume(Long.valueOf(st.nextToken()));
							dailyData.setAdjClose(Float.valueOf(st.nextToken()));
							dailyStockDataList.add(dailyData);
							strBuffer.setLength(0);
						} else {
							strBuffer.setLength(0);
						}
						lineNumber++;
					}
				}
				buffer = new char[1024];
				//numWritten += numRead;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (charIn != null) {
					charIn.close();
				}
			} catch (IOException ioe) {
			}
		}
		return dailyStockDataList;
	}

	public List<WeeklyStockData> getWeeklyData(String ticker, Date startDate, Date endDate) {
		List<WeeklyStockData> weeklyStockDataList = new ArrayList<WeeklyStockData>();
		GregorianCalendar cal = new GregorianCalendar();
		
		cal.setTime(startDate);
		String startMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (startMonth.length() == 1) startMonth = "0" + startMonth;
		String startDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (startDay.length() == 1) startDay = "0" + startDay;
		String startYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		cal.setTime(endDate);
		String endMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (endMonth.length() == 1) endMonth = "0" + endMonth;
		String endDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (endDay.length() == 1) endDay = "0" + endDay;
		String endYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		String address = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + 
			"&a=" + startMonth + "&b=" + startDay + "&c=" + startYear + 
			"&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + 
			"&g=w&ignore=.csv";
		Reader charIn = null;
		URLConnection conn = null;
		StringBuffer strBuffer = new StringBuffer();
		int lineNumber = 0;
		char[] buffer = new char[1024];
		//int numRead;
		//long numWritten = 0;
		String csvData = "";
		try {
			URL url = new URL(address);
			conn = url.openConnection();
			charIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//while ((numRead = charIn.read(buffer)) != -1) {
			while ((charIn.read(buffer)) != -1) {
				for(char c : buffer) {
					if(c != '\r' && c != '\n') {  // character is not carriage return or line feed
						strBuffer.append(c);
					} else if(c == '\r') {
						// do nothing
					} else if(c == '\n') {
						// The first line (lineNumber == 0) is the column names - IGNORE.  Since
						// we are getting data for a date range, all lines after the first are relevant.
						if (lineNumber > 0) {
							//String csvData = strBuffer.toString();
							csvData = strBuffer.toString().trim();
							if(csvData != null && (csvData.substring(0, 1) != "1" || csvData.substring(0, 1) != "2")) {
								boolean validData = true;
								try {
									StringTokenizer st = new StringTokenizer(csvData, ","); // comma delimited (CSV file)

									// Parse and validate data.  Log and skip records with errors
									// Purchase Date
									String dateString = st.nextToken().trim();
									//if(dateString.length() != 10 || ((dateString.substring(0,1) != "1" && dateString.substring(0,1) != "2"))) {
									if(dateString.length() != 10 || (!dateString.substring(0, 1).equals("1") && !dateString.substring(0, 1).equals("2"))) {
										validData = false;
										logger.error("Invalid purchase date string: " + dateString + ".  Date string must be 10 characters long and begin with '1' or '2'.");
										logger.info("dateString.length() = " + dateString.length());
										logger.info("dateString.substring(0,1) = " + dateString.substring(0,1));
									}
									// Open price
									String openString = st.nextToken().trim();;
									if(openString.length() == 0 || !openString.contains(".")) {
										validData = false;
										logger.error("Invalid Open price: " + openString + ".  Open price string must not be zero length and must contain a decimal point.");
									}
									// High price
									String highString = st.nextToken().trim();
									if(highString.length() == 0 || !highString.contains(".")) {
										validData = false;
										logger.error("Invalid High price: " + highString + ".  High price string must not be zero length and must contain a decimal point.");
									}
									// Low price
									String lowString = st.nextToken().trim();
									if(lowString.length() == 0 || !lowString.contains(".")) {
										validData = false;
										logger.error("Invalid Low price: " + lowString + ".  Low price string must not be zero length and must contain a decimal point.");
									}
									// Close price
									String closeString = st.nextToken().trim();
									if(closeString.length() == 0 || !closeString.contains(".")) {
										validData = false;
										logger.error("Invalid Close price: " + closeString + ".  Close price string must not be zero length and must contain a decimal point.");
									}
									// Average Volume
									String avgVolumeString = st.nextToken().trim();
									if(avgVolumeString.length() == 0 || avgVolumeString.contains(".")) {
										validData = false;
										logger.error("Invalid Close price: " + avgVolumeString + ".  Close price string must not be zero length and must NOT contain a decimal point.");
									}
									// Adjusted Close
									String adjCloseString = st.nextToken();
									//if(adjCloseString.length() == 0 || adjCloseString.length() > 10 || !adjCloseString.contains(".")) {
									if(adjCloseString.length() == 0 || adjCloseString.length() > 11) {
										validData = false;
										logger.error("Invalid Adjusted Close price: " + adjCloseString + ".  Adjusted Close price string length must not be 0 or greater than 11.");
										logger.info("adjCloseString.length() = " + adjCloseString.length());
										logger.info("adjCloseString = " + adjCloseString);
									}
									
									if(validData) {
										WeeklyStockData weeklyData = new WeeklyStockData();
										weeklyData.setDate(yahooDateFormat.parse(dateString));
										weeklyData.setOpen(Float.valueOf(openString));
										weeklyData.setHigh(Float.valueOf(highString));
										weeklyData.setLow(Float.valueOf(lowString));
										weeklyData.setClose(Float.valueOf(closeString));
										weeklyData.setAvgVolume(Long.valueOf(avgVolumeString));
										weeklyData.setAdjClose(Float.valueOf(adjCloseString));
										weeklyStockDataList.add(weeklyData);
									}
									strBuffer.setLength(0);
								} catch(Exception ex) {
									logger.info("Exception parsing csvData: " + csvData, ex);
								}
							} else {
								strBuffer.setLength(0);
							}
						} else {
							strBuffer.setLength(0);
						}
						lineNumber++;
					}
				}
				buffer = new char[1024];
				//numWritten += numRead;
			}
		} catch (Exception exception) {
			logger.error("Error processing csvData: " + csvData);
			exception.printStackTrace();
		} finally {
			try {
				if (charIn != null) {
					charIn.close();
				}
			} catch (IOException ioe) {
			}
		}
		return weeklyStockDataList;
	}

	public List<MonthlyStockData> getMonthlyData(String ticker, Date startDate, Date endDate) {
		List<MonthlyStockData> monthlyStockDataList = new ArrayList<MonthlyStockData>();
		GregorianCalendar cal = new GregorianCalendar();
		
		cal.setTime(startDate);
		String startMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (startMonth.length() == 1) startMonth = "0" + startMonth;
		String startDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (startDay.length() == 1) startDay = "0" + startDay;
		String startYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		cal.setTime(endDate);
		String endMonth = Integer.valueOf(cal.get(Calendar.MONTH)).toString();
		if (endMonth.length() == 1) endMonth = "0" + endMonth;
		String endDay = Integer.valueOf(cal.get(Calendar.DAY_OF_MONTH)).toString();
		if (endDay.length() == 1) endDay = "0" + endDay;
		String endYear = Integer.valueOf(cal.get(Calendar.YEAR)).toString();
		
		String address = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker + 
			"&a=" + startMonth + "&b=" + startDay + "&c=" + startYear + 
			"&d=" + endMonth + "&e=" + endDay + "&f=" + endYear + 
			"&g=m&ignore=.csv";
		Reader charIn = null;
		URLConnection conn = null;
		StringBuffer strBuffer = new StringBuffer();
		int lineNumber = 0;
		char[] buffer = new char[1024];
		//int numRead;
		//long numWritten = 0;
		try {
			URL url = new URL(address);
			conn = url.openConnection();
			charIn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			//while ((numRead = charIn.read(buffer)) != -1) {
			while ((charIn.read(buffer)) != -1) {
				for(char c : buffer) {
					if(c != '\r' && c != '\n') {  // character is not carriage return or line feed
						strBuffer.append(c);
					} else if(c == '\r') {
						// do nothing
					} else if(c == '\n') {
						// The first line (lineNumber == 0) is the column names - IGNORE.  Since
						// we are getting data for a date range, all lines after the first are relevant.
						if (lineNumber > 0) {
							String csvData = strBuffer.toString();
							StringTokenizer st = new StringTokenizer(csvData, ","); // comma delimited (CSV file)
							MonthlyStockData monthlyData = new MonthlyStockData();
							monthlyData.setDate(yahooDateFormat.parse(st.nextToken()));
							monthlyData.setOpen(Float.valueOf(st.nextToken()));
							monthlyData.setHigh(Float.valueOf(st.nextToken()));
							monthlyData.setLow(Float.valueOf(st.nextToken()));
							monthlyData.setClose(Float.valueOf(st.nextToken()));
							monthlyData.setAvgVolume(Long.valueOf(st.nextToken()));
							monthlyData.setAdjClose(Float.valueOf(st.nextToken()));
							monthlyStockDataList.add(monthlyData);
							strBuffer.setLength(0);
						} else {
							strBuffer.setLength(0);
						}
						lineNumber++;
					}
				}
				buffer = new char[1024];
				//numWritten += numRead;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (charIn != null) {
					charIn.close();
				}
			} catch (IOException ioe) {
			}
		}
		return monthlyStockDataList;
	}
	
	// Authority Services
	public void insertAuthority(Authority authority) {
		monitorDao.insertAuthority(authority);
	}

	public Authority updateAuthority(Authority authority) {
		return monitorDao.updateAuthority(authority);
	}

	public void deleteAuthority(Long authorityId) {
		monitorDao.deleteAuthority(authorityId);
	}
	
	public List<Authority> getAllAuthorities() {
		return monitorDao.getAllAuthorities();
	}
	
	public Authority getAuthorityById(Long authorityId) {
		return monitorDao.getAuthorityById(authorityId);
	}
	

	// SysUser Services
	public void insertSysUser(SysUser sysUser) {
		// First the supplied password must be encrypted before being
		// stored in the database.  We are using MD5 encryption, and using
		// the user's username as a salt value.
		
		String encPassword = encodePassword(sysUser.getUsername(), sysUser.getPassword());
		sysUser.setPassword(encPassword);
		
		monitorDao.insertSysUser(sysUser);
	}
	
	public SysUser updateSysUser(SysUser sysUser) {
		return monitorDao.updateSysUser(sysUser);
	}
	
	public SysUser updateSysUserPwd(Long sysUserId, String password) {
		// First the supplied password must be encrypted before being
		// stored in the database.  We are using MD5 encryption, and using
		// the user's username as a salt value.
		
		SysUser sysUser = monitorDao.getSysUserById(sysUserId);
		SysUser updatedSysUser = monitorDao.updateSysUserPwd(sysUserId, encodePassword(sysUser.getUsername(), password));
		return updatedSysUser;
	}

	public void deleteSysUser(Long sysUserId) {
		monitorDao.deleteSysUser(sysUserId);
	}
	
	public SysUser getSysUserByUsername(String username) {
		return monitorDao.getSysUserByUsername(username);
	}
	
	public SysUser getSysUserById(Long sysUserId) {
		return monitorDao.getSysUserById(sysUserId);
	}

	public List<SysUser> searchSysUsers(String usernameCrit, String lastNameCrit, Boolean activeCrit) {
		return monitorDao.searchSysUsers(usernameCrit, lastNameCrit, activeCrit);
	}

	// SysUserAuthority Services
	public List<Authority> getSysUserAuthorities(Long sysUserId) {
		return monitorDao.getSysUserAuthorities(sysUserId);
	}
	
	public void grantAuthorityToSysUser(Long sysUserId, Long authorityId) {
		monitorDao.grantAuthorityToSysUser(sysUserId, authorityId);
	}
	
	public void revokeAuthorityFromAllSysUsers(Long authorityId) {
		monitorDao.revokeAuthorityFromAllSysUsers(authorityId);
	}
	
	public void revokeAuthorityFromSysUser(Long sysUserId, Long authorityId) {
		monitorDao.revokeAuthorityFromSysUser(sysUserId, authorityId);
	}
	
	
	// Club Services
	public void insertClub(Club club) {
		monitorDao.insertClub(club);
	}
	
	public Club updateClub(Club club) {
		return monitorDao.updateClub(club);
	}
	
	public void deleteClub(Long clubId) {
		monitorDao.deleteClub(clubId);
	}
	
	public Club getClubByClubName(String clubName) {
		return monitorDao.getClubByClubName(clubName);
	}
	
	public Club getClubById(Long clubId) {
		return monitorDao.getClubById(clubId);
	}

	public List<Club> searchClubs(String clubNameCrit, String cityCrit, String stateCrit) {
		return monitorDao.searchClubs(clubNameCrit, cityCrit, stateCrit);
	}

	public List<Club> getAllClubs() {
		return monitorDao.getAllClubs();
	}
	
	// ClubMembership Service
	public void addMemberToClub(Long sysUserId, Long clubId, Boolean isClubAdministrator) {
		monitorDao.addMemberToClub(sysUserId, clubId, isClubAdministrator);
	}
	
	public void makeClubAdmin(Long clubId, Long sysUserId) {
		monitorDao.makeClubAdmin(clubId, sysUserId);
	}
	
	public void revokeClubAdmin(Long clubId, Long sysUserId) {
		monitorDao.revokeClubAdmin(clubId, sysUserId);
	}
	
	public void removeMemberFromClub(Long sysUserId, Long clubId) {
		monitorDao.removeMemberFromClub(sysUserId, clubId);
	}
	
	public void removeMembersFromClubByClubId(Long clubId) {
		monitorDao.removeMembersFromClubByClubId(clubId);
	}
	
	public List<ClubMembership> getClubMembership(Long clubId, Long sysUserId) {
		return monitorDao.getClubMembership(clubId, sysUserId);
	}

	public List<Club> getClubsBySysUserId(Long sysUserId) {
		return monitorDao.getClubsBySysUserId(sysUserId);
	}
	
	public List<ClubMember> getClubMembersByClubId(Long clubId) {
		return monitorDao.getClubMembersByClubId(clubId);
	}
	
	public List<ClubMember> getNewClubMembersByClubId(Long clubId, String usernameCrit, String lastNameCrit, Boolean activeCrit) {
		return monitorDao.getNewClubMembersByClubId(clubId, usernameCrit, lastNameCrit, activeCrit);
	}
	
	public List<Club> getClubsByClubAdminId(Long sysUserId) {
		return monitorDao.getClubsByClubAdminId(sysUserId);
	}
	
	
	// ClubMembershipPosition Services
	public void addMemberPosition(Long clubId, Long sysUserId, Long positionId) {
		monitorDao.addMemberPosition(clubId, sysUserId, positionId);
	}

	public void deleteMemberPosition(Long clubId, Long sysUserId, Long positionId) {
		monitorDao.deleteMemberPosition(clubId, sysUserId, positionId);
	}

	public void deleteMemberPositionByClubId(Long clubId) {
		monitorDao.deleteMemberPositionByClubId(clubId);
	}

	public void deleteMemberPositionByPositionId(Long positionId) {
		monitorDao.deleteMemberPositionByPositionId(positionId);
	}

	public List<PortfolioPositionAssignment> getPortPositionAssignments(Long portfolioId) {
		return monitorDao.getPortPositionAssignments(portfolioId);
	}
	
	// Portfolio Services
	public void insertPortfolio(Portfolio portfolio) {
		monitorDao.insertPortfolio(portfolio);
	}
	
	public Portfolio updatePortfolio(Portfolio portfolio) {
		return monitorDao.updatePortfolio(portfolio);
	}
	
	public void deletePortfolio(Long portfolioId) {
		monitorDao.deletePortfolio(portfolioId);
	}
	
	public Portfolio getPortfolioById(Long portfolioId) {
		return monitorDao.getPortfolioById(portfolioId);
	}
	
	public Portfolio getPortfolioByName(String portfolioName) {
		return monitorDao.getPortfolioByName(portfolioName);
	}
	
	public List<Portfolio> getPortfoliosByClubId(Long clubId) {
		return monitorDao.getPortfoliosByClubId(clubId);
	}
	
	public List<Portfolio> getPortfoliosBySysUserId(Long sysUserId) {
		return monitorDao.getPortfoliosBySysUserId(sysUserId);
	}

	public List<Portfolio> getClubPortfoliosBySysUserId(Long sysUserId) {
		return monitorDao.getClubPortfoliosBySysUserId(sysUserId);
	}
	
	public List<Portfolio> getAllPortfoliosBySysUserId(Long sysUserId) {
		return monitorDao.getAllPortfoliosBySysUserId(sysUserId);
	}

	
	// PortfolioPosition Services
	public void insertPortfolioPosition(PortfolioPosition position) {
		monitorDao.insertPortfolioPosition(position);
	}
	
	public PortfolioPosition updatePortfolioPosition(PortfolioPosition position) {
		return monitorDao.updatePortfolioPosition(position);
	}
	
	public void deletePortfolioPosition(Long positionId) {
		monitorDao.deletePortfolioPosition(positionId);
	}
	
	public void deletePortfolioPositionsByPortfolioId(Long portfolioId) {
		monitorDao.deletePortfolioPositionsByPortfolioId(portfolioId);
	}
	
	public List<PortfolioPosition> getAllPortfolioPositionsByPortfolioId(Long portfolioId) {
		return monitorDao.getAllPortfolioPositionsByPortfolioId(portfolioId);
	}
	
	public PortfolioPosition getPortfolioPositionById(Long positionId) {
		return monitorDao.getPortfolioPositionById(positionId);
	}
	
	
	public String encodePassword (String username, String password) {
		Md5PasswordEncoder pwdEnc = new Md5PasswordEncoder();
		String encPassword = pwdEnc.encodePassword(password, username);
		
		return encPassword;
	}
	
	public void checkAuthenticatedSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		SysUser loggedInUser = (SysUser) session.getAttribute("sysUser");
		if(loggedInUser == null) SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	public SysUser getLoggedInSysUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Object obj;
		if(auth != null) {
			obj = auth.getPrincipal();
		} else {
			obj = null;
		}
		
		SysUser sysUser;
		if(obj instanceof UserDetails) {
			sysUser = getSysUserByUsername(((UserDetails)obj).getUsername());
		} else {
			sysUser = null;
		}
		
		return sysUser;
	}
	public MonitorMenu getMonitorMenu(SysUser loggedInSysUser) {
		MonitorMenu monitorMenu = new MonitorMenu();
		List<PortfolioPositions> personalPortfolioList = new ArrayList<PortfolioPositions>();
		List<ClubPortfoliosPositions> clubPortfoliosList = new ArrayList<ClubPortfoliosPositions>();
		
		// Get list of portfolios for logged in user
		List<Portfolio> userPortfolios = this.getPortfoliosBySysUserId(loggedInSysUser.getSysUserId());
		for(Portfolio portfolio : userPortfolios) {
			// Get list of positions for each portfolio then setup a PortfolioPositions object and added it to the 
			// personalPortfolioList object
			List<PortfolioPosition> positionList = this.getAllPortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
			PortfolioPositions portfolioPositions = new PortfolioPositions();
			portfolioPositions.setPortfolio(portfolio);
			portfolioPositions.setPositions(positionList);
			personalPortfolioList.add(portfolioPositions);
		}
		// The personalPortfolioList is now fully populated - set it in the monitorMenu
		monitorMenu.setPersonalPortfolioList(personalPortfolioList);
		
		// Now for the clubs...
		// Get a list of clubs for which this user is a member
		List<Club> clubList = this.getClubsBySysUserId(loggedInSysUser.getSysUserId());
		
		// For each club, get the list of portfolios associated with it
		for(Club club : clubList) {
			List<Portfolio> clubPortfolios = this.getPortfoliosByClubId(club.getClubId());
			
			// Create a ClubPortfoliosPositions object for this club and set the club info
			ClubPortfoliosPositions clubPortfoliosPositions = new ClubPortfoliosPositions();
			clubPortfoliosPositions.setClubId(club.getClubId());
			clubPortfoliosPositions.setClubName(club.getClubName());
			
			// Create a PortfolioPositions list for this club portfolio
			List<PortfolioPositions> portfolioPositionsList = new ArrayList<PortfolioPositions>();
			
			// For each portfolio, get a list of positions
			for(Portfolio portfolio : clubPortfolios) {
				List<PortfolioPosition> positionList = this.getAllPortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
				
				// Create a PortfolioPositions object and populate it
				PortfolioPositions portfolioPositions = new PortfolioPositions();
				portfolioPositions.setPortfolio(portfolio);
				portfolioPositions.setPositions(positionList);
				
				// now add it to the list
				portfolioPositionsList.add(portfolioPositions);
			}
			// Now we can complete the ClubPortfoliosPosition object ...
			clubPortfoliosPositions.setPortfolioPositions(portfolioPositionsList);
			// ... and add it to the list
			clubPortfoliosList.add(clubPortfoliosPositions);
		}
		// The clubPortfoliosList is now fully populated - set it in the mmonitorMenu
		monitorMenu.setClubPortfoliosPositions(clubPortfoliosList);
		
		return monitorMenu;
	}
	
	public MonitorMenu getMaintainMenu(SysUser loggedInSysUser) {
		MonitorMenu monitorMenu = new MonitorMenu();
		List<PortfolioPositions> personalPortfolioList = new ArrayList<PortfolioPositions>();
		List<ClubPortfoliosPositions> clubPortfoliosList = new ArrayList<ClubPortfoliosPositions>();
		
		// Get list of portfolios for logged in user
		List<Portfolio> userPortfolios = this.getPortfoliosBySysUserId(loggedInSysUser.getSysUserId());
		for(Portfolio portfolio : userPortfolios) {
			// Get list of positions for each portfolio then setup a PortfolioPositions object and added it to the 
			// personalPortfolioList object
			List<PortfolioPosition> positionList = this.getAllPortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
			PortfolioPositions portfolioPositions = new PortfolioPositions();
			portfolioPositions.setPortfolio(portfolio);
			portfolioPositions.setPositions(positionList);
			personalPortfolioList.add(portfolioPositions);
		}
		// The personalPortfolioList is now fully populated - set it in the monitorMenu
		monitorMenu.setPersonalPortfolioList(personalPortfolioList);
		
		// Now for the clubs...
		// A user must have ROLE_CLUB_ADMIN to maintain club portfolios.  Check the authority of this user.
		// if the user is ROLE_CLUB_ADMIN or ROLE_SYS_ADMIN, the proceed.  Otherwise, do nothing.
		List<Authority> authList = monitorDao.getSysUserAuthorities(loggedInSysUser.getSysUserId());
		Authority sysUserAuth = new Authority();
		if (authList.size() > 0) {
			sysUserAuth = authList.get(0);
		}
		if (sysUserAuth != null && sysUserAuth.getAuthorityId() > 1) {
			// Get a list of clubs for which this user is a member
			List<Club> clubList = this.getClubsBySysUserId(loggedInSysUser.getSysUserId());
			
			// For each club, get the list of portfolios associated with it
			for(Club club : clubList) {
				List<Portfolio> clubPortfolios = this.getPortfoliosByClubId(club.getClubId());
				
				// Create a ClubPortfoliosPositions object for this club and set the club info
				ClubPortfoliosPositions clubPortfoliosPositions = new ClubPortfoliosPositions();
				clubPortfoliosPositions.setClubId(club.getClubId());
				clubPortfoliosPositions.setClubName(club.getClubName());
				
				// Create a PortfolioPositions list for this club portfolio
				List<PortfolioPositions> portfolioPositionsList = new ArrayList<PortfolioPositions>();
				
				// For each portfolio, get a list of positions
				for(Portfolio portfolio : clubPortfolios) {
					List<PortfolioPosition> positionList = this.getAllPortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
					
					// Create a PortfolioPositions object and populate it
					PortfolioPositions portfolioPositions = new PortfolioPositions();
					portfolioPositions.setPortfolio(portfolio);
					portfolioPositions.setPositions(positionList);
					
					// now add it to the list
					portfolioPositionsList.add(portfolioPositions);
				}
				// Now we can complete the ClubPortfoliosPosition object ...
				clubPortfoliosPositions.setPortfolioPositions(portfolioPositionsList);
				// ... and add it to the list
				clubPortfoliosList.add(clubPortfoliosPositions);
			}
		}
		// The clubPortfoliosList is now fully populated (or it is empty) - set it in the monitorMenu
		monitorMenu.setClubPortfoliosPositions(clubPortfoliosList);
		return monitorMenu;
	}
	
}
