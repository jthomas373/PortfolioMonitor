package com.ccllc.PortfolioMonitor.web.controllers;

import java.awt.Color;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.ChangePasswordForm;
import com.ccllc.PortfolioMonitor.domain.ChartData;
import com.ccllc.PortfolioMonitor.domain.Club;
import com.ccllc.PortfolioMonitor.domain.ClubMember;
import com.ccllc.PortfolioMonitor.domain.ClubSearch;
import com.ccllc.PortfolioMonitor.domain.DailyStockData;
import com.ccllc.PortfolioMonitor.domain.MonitorMenu;
import com.ccllc.PortfolioMonitor.domain.Portfolio;
import com.ccllc.PortfolioMonitor.domain.PortfolioPosition;
import com.ccllc.PortfolioMonitor.domain.SysUser;
import com.ccllc.PortfolioMonitor.domain.SysUserSearch;
import com.ccllc.PortfolioMonitor.services.MonitorService;
import com.ccllc.PortfolioMonitor.domain.WeeklyStockData;

@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
	private MonitorService monitorService;
	
	SimpleDateFormat yahooDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model, HttpSession session) {
		if (this.getCurrentUser().getUsername() != null) {
			logger.info("On the HOME page. The client locale is {}. sysUser: {}", locale, this.getCurrentUser().getUsername());
		} else {
			logger.info("On the HOME page. The client locale is {}. No logged in sysUser.", locale);
		}
		return "home";
	}
	
	@RequestMapping("/sellPoints.html")
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String sellPoints() {
		return "sellPoints";
	}
	
	@RequestMapping(value="/changePassword.html", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String changePassword(ChangePasswordForm changePasswordForm, Model model) {
		String errorMsg = "";
		model.addAttribute("errorMsg", errorMsg);
		return "changePassword";
	}
	
	@RequestMapping(value="/changePassword.html", method=RequestMethod.POST)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String changePasswordPost(ChangePasswordForm changePasswordForm, Model model) {
		SysUser updatedSysUser;
		String errorMsg = "";
		String returnTemplate = "";
		
		// VALIDATIONS
		// First check that the "oldPassword" matches the current password for the logged in user
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userName = auth.getName();
		SysUser sysUser = monitorService.getSysUserByUsername(userName);
		
		String encodedOldPwd = monitorService.encodePassword(userName, changePasswordForm.getOldPassword());
		if(encodedOldPwd.equals(sysUser.getPassword())) {
			// If the oldPassword matches, then check if the "newPassword" matches "confirmPassword"
			if(changePasswordForm.getNewPassword().equals(changePasswordForm.getConfirmPassword())) {
				updatedSysUser = monitorService.updateSysUserPwd(sysUser.getSysUserId(), 
						changePasswordForm.getNewPassword());
				//updatedSysUser = monitorService.getLoggedInSysUser();  // for testing
				logger.info("Password successfully update for " + updatedSysUser.getUsername() + ".");
				returnTemplate = "home";
			} else {
				if(errorMsg.length() == 0) {
					errorMsg = "Confirmation password does not match new password.";
				} else {
					errorMsg.concat("  Confirmation password does not match new password.");
				}
				logger.info("Password update failed for " + userName + ".");
				returnTemplate = "changePassword";
			}
		} else {
			if(errorMsg.length() == 0) {
				errorMsg = "Old password does not match the current password.";
			} else {
				errorMsg.concat("  Old password does not match the current password.");
			}
			logger.info("Password update failed for " + userName + ".");
			returnTemplate = "changePassword";
		}
		model.addAttribute("errorMsg", errorMsg);
		return returnTemplate;
	}
	
	@RequestMapping(value="/adminSysUser.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String adminSysUser(SysUserSearch sysUserSearch, Model model) {
		List<SysUser> sysUserList = new ArrayList<SysUser>();
		model.addAttribute("sysUserList", sysUserList);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/adminSysUser.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String adminSysUserPost(SysUserSearch sysUserSearch, Model model) {
		List<SysUser> sysUserList = monitorService.searchSysUsers(
				sysUserSearch.getUsername(), sysUserSearch.getLastName(), sysUserSearch.getActive());
		model.addAttribute("sysUserList", sysUserList);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/editSysUser.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editSysUser(SysUser sysUser, HttpServletRequest request, Model model) {
		Long sysUserId = Long.valueOf(request.getParameter("sysUserId"));
		sysUser = monitorService.getSysUserById(sysUserId);
		model.addAttribute("sysUser", sysUser);
		return "editSysUser";
	}
	
	@RequestMapping(value="/editSysUser.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editSysUserPost(SysUser sysUser, Model model) {
		sysUser = monitorService.updateSysUser(sysUser);
		
		SysUserSearch sysUserSearch = new SysUserSearch();
		model.addAttribute("sysUserSearch", sysUserSearch);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/editSysUserPwd.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editSysUserPwd(SysUser sysUser, HttpServletRequest request, Model model) {
		Long sysUserId = Long.valueOf(request.getParameter("sysUserId"));
		sysUser = monitorService.getSysUserById(sysUserId);

		model.addAttribute("sysUser", sysUser);
		return "editSysUserPwd";
	}
	
	@RequestMapping(value="/editSysUserPwd.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editSysUserPwdPost(SysUser sysUser, Model model) {
		sysUser = monitorService.updateSysUserPwd(sysUser.getSysUserId(), sysUser.getPassword());
		
		SysUserSearch sysUserSearch = new SysUserSearch();
		model.addAttribute("sysUserSearch", sysUserSearch);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/deleteSysUser.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String deleteSysUser(SysUser sysUser, HttpServletRequest request, Model model) {
		Long sysUserId = Long.valueOf(request.getParameter("sysUserId"));
		
		// First revoke all authorities granted to this user
		List<Authority> sysUserAuthorities = monitorService.getSysUserAuthorities(sysUserId);
		for (Authority authority : sysUserAuthorities) {
			monitorService.revokeAuthorityFromSysUser(sysUserId, authority.getAuthorityId());
		}
		
		// Next, remove user from any clubs
		List<Club> sysUserClubs = monitorService.getClubsBySysUserId(sysUserId);
		for (Club club : sysUserClubs) {
			monitorService.removeMemberFromClub(sysUserId, club.getClubId());
		}
		
		// Next, find and delete all portfolios and positions owned by this user.
		// Get all portfolios owned by this user
		List<Portfolio> sysUserPortfolios = monitorService.getAllPortfoliosBySysUserId(sysUserId);
		for (Portfolio portfolio : sysUserPortfolios) {
			// for each portfolio, get all positions
			List<PortfolioPosition> portPositions = monitorService.getAllPortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
			for (PortfolioPosition position : portPositions) {
				// delete all positions in the portfolio
				monitorService.deletePortfolioPosition(position.getPositionId());
			}
			// after deleting all positions, delete the portfolio
			monitorService.deletePortfolio(portfolio.getPortfolioId());
		}
		// after deleting all portfolios, delete the user
		monitorService.deleteSysUser(sysUserId);
		
		SysUserSearch sysUserSearch = new SysUserSearch();
		model.addAttribute("sysUserSearch", sysUserSearch);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/addSysUser.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String addSysUser(SysUser sysUser, Model model) {
		return "addSysUser";
	}
	
	@RequestMapping(value="/addSysUser.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String addSysUserPost(SysUser sysUser, Model model) {
		monitorService.insertSysUser(sysUser);
		sysUser = monitorService.getSysUserByUsername(sysUser.getUsername());
		monitorService.grantAuthorityToSysUser(sysUser.getSysUserId(), 1L);
		
		SysUserSearch sysUserSearch = new SysUserSearch();
		model.addAttribute("sysUserSearch", sysUserSearch);
		return "adminSysUser";
	}
	
	@RequestMapping(value="/adminClub.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String adminClub(ClubSearch clubSearch, Model model) {
		List<Club> clubList = new ArrayList<Club>();
		model.addAttribute("clubList", clubList);
		return "adminClub";
	}
	
	@RequestMapping(value="/adminClub.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String adminClubPost(ClubSearch clubSearch, Model model) {
		List<Club> clubList = monitorService.searchClubs(clubSearch.getClubName(), clubSearch.getCity(), clubSearch.getState());
		model.addAttribute("clubList", clubList);
		return "adminClub";
	}
	
	@RequestMapping(value="/addClub.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String addClub(Club club, Model model) {
		return "addClub";
	}
	
	@RequestMapping(value="/addClub.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String addClubPost(Club club, Model model) {
		monitorService.insertClub(club);
		
		ClubSearch clubSearch = new ClubSearch();
		model.addAttribute("clubSearch", clubSearch);
		return "adminClub";
	}
	
	@RequestMapping(value="/editClub.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editClub(Club club, HttpServletRequest request, Model model) {
		Long clubId = Long.valueOf(request.getParameter("clubId"));
		club = monitorService.getClubById(clubId);
		model.addAttribute("club", club);
		return "editClub";
	}
	
	@RequestMapping(value="/editClub.html", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String editClubPost(Club club, Model model) {
		club = monitorService.updateClub(club);
		
		ClubSearch clubSearch = new ClubSearch();
		model.addAttribute("clubSearch", clubSearch);
		return "adminClub";
	}
	
	@RequestMapping(value="/deleteClub.html", method=RequestMethod.GET)
	@Secured({"ROLE_SYS_ADMIN"})
	public String deleteClub(Club sysUser, HttpServletRequest request, Model model) {
		Long clubId = Long.valueOf(request.getParameter("clubId"));
		
		// First delete all club member position records for this club
		monitorService.deleteMemberPositionByClubId(clubId);
		
		// Next, delete all club membership records for this club
		monitorService.removeMembersFromClubByClubId(clubId);
		
		// Next, remove all portfolio and their associated positions for this club
		List<Portfolio> clubPortfolioList = monitorService.getPortfoliosByClubId(clubId);
		for (Portfolio portfolio : clubPortfolioList) {
			monitorService.deletePortfolioPositionsByPortfolioId(portfolio.getPortfolioId());
			monitorService.deletePortfolio(portfolio.getPortfolioId());
		}
		
		// Finally, delete the club
		monitorService.deleteClub(clubId);
		
		ClubSearch clubSearch = new ClubSearch();
		model.addAttribute("clubSearch", clubSearch);
		return "adminClub";
	}
	
	@RequestMapping(value="/adminClubMembers", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String adminClubMembers(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		
		return "adminClubMembers";
	}
	
	@RequestMapping(value="/adminClubMembers", method=RequestMethod.POST)
	@Secured({"ROLE_SYS_ADMIN"})
	public String adminClubMembersPost(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		List<ClubMember> newClubMemberList = monitorService.getNewClubMembersByClubId(clubId, sysUserSearch.getUsername(), sysUserSearch.getLastName(), sysUserSearch.getActive());
		
		model.addAttribute("club", club);
		model.addAttribute("clubId", selectedClubId);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		model.addAttribute("newClubMemberList", newClubMemberList);
		
		return "adminClubMembers";
}
	
	@RequestMapping(value="/makeClubAdmin", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String makeClubAdmin(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		String selectedSysUserId = request.getParameter("sysUserId");

		Long clubId = Long.valueOf(selectedClubId);
		Long sysUserId = Long.valueOf(selectedSysUserId);
		
		monitorService.makeClubAdmin(clubId, sysUserId);
		
		Authority currentAuth = new Authority();
		List<Authority> currentAuthList = monitorService.getSysUserAuthorities(sysUserId);
		if (currentAuthList.size() > 0) {
			currentAuth = monitorService.getSysUserAuthorities(sysUserId).get(0);
		} else {
			monitorService.grantAuthorityToSysUser(sysUserId, 1L);	// authorityId = 1 = ROLE_USER
		}
		if (currentAuth.getAuthorityId() < 2L) {
			// If the granted authority is less than or equal to the authority currently associated
			// with this sysUser, then do nothing.  Otherwise, revoke the current authority and 
			// grant the new one.
			monitorService.revokeAuthorityFromSysUser(sysUserId, currentAuth.getAuthorityId());
			monitorService.grantAuthorityToSysUser(sysUserId, 2L);
		}
		
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		
		return "adminClubMembers";
	}
	
	@RequestMapping(value="/revokeClubAdmin", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String revokeClubAdmin(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		String selectedSysUserId = request.getParameter("sysUserId");

		Long clubId = Long.valueOf(selectedClubId);
		Long sysUserId = Long.valueOf(selectedSysUserId);
		
		monitorService.revokeClubAdmin(clubId, sysUserId);
		
		Authority currentAuth = monitorService.getSysUserAuthorities(sysUserId).get(0);
		if (currentAuth.getAuthorityId() <= 2L) {
			// If the revoked authority is less than or equal to the authority currently associated
			// with this sysUser, then do nothing.  Otherwise, revoke the current authority and 
			// grant ROLE_USER authority to the sysUser.
			monitorService.revokeAuthorityFromSysUser(sysUserId, currentAuth.getAuthorityId());
			monitorService.grantAuthorityToSysUser(sysUserId, 1L);	// authorityId = 1 = ROLE_USER
		}

		
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		
		return "adminClubMembers";
	}
	
	@RequestMapping(value="/addClubMember", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addClubMember(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		String selectedSysUserId = request.getParameter("sysUserId");

		Long clubId = Long.valueOf(selectedClubId);
		Long sysUserId = Long.valueOf(selectedSysUserId);
		
		monitorService.addMemberToClub(sysUserId, clubId, false);
		
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		
		return "adminClubMembers";
	}
	
	@RequestMapping(value="/removeClubMember", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String removeClubMember(SysUserSearch sysUserSearch, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getAllClubs();
		String selectedClubId = request.getParameter("clubId");
		String selectedSysUserId = request.getParameter("sysUserId");

		Long clubId = Long.valueOf(selectedClubId);
		Long sysUserId = Long.valueOf(selectedSysUserId);
		
		monitorService.removeMemberFromClub(sysUserId, clubId);
		
		Club club = monitorService.getClubById(clubId);
		List<ClubMember> clubMemberList = monitorService.getClubMembersByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubMemberList", clubMemberList);
		
		return "adminClubMembers";
	}
	
	@RequestMapping(value="/adminClubPortfolios", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String adminClubPortfolios(HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		
		List<Portfolio> clubPortfolioList = monitorService.getPortfoliosByClubId(clubId);

		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("clubPortfolioList", clubPortfolioList);
		
		return "adminClubPortfolios";
	}
	
	@RequestMapping(value="/addClubPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addClubPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		
		portfolio.setClubId(clubId);

		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("portfolio", portfolio);
		
		return "addClubPortfolio";
	}
	
	@RequestMapping(value="/addClubPortfolio", method=RequestMethod.POST)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addClubPortfolioPost(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		
		monitorService.insertPortfolio(portfolio);
		List<Portfolio> clubPortfolioList = monitorService.getPortfoliosByClubId(clubId);

		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("portfolio", portfolio);
		model.addAttribute("clubPortfolioList", clubPortfolioList);
		
		return "adminClubPortfolios";
	}
	
	@RequestMapping(value="/editClubPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editClubPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		
		portfolio = monitorService.getPortfolioById(Long.valueOf(request.getParameter("portfolioId")));

		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("portfolio", portfolio);
		
		return "editClubPortfolio";
	}
	
	@RequestMapping(value="/editClubPortfolio", method=RequestMethod.POST)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editClubPortfolioPost(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		
		monitorService.updatePortfolio(portfolio);
		List<Portfolio> clubPortfolioList = monitorService.getPortfoliosByClubId(clubId);

		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("portfolio", portfolio);
		model.addAttribute("clubPortfolioList", clubPortfolioList);
		
		return "adminClubPortfolios";
	}
	
	@RequestMapping(value="/deleteClubPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String deleteClubPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		monitorService.deletePortfolio(Long.valueOf(request.getParameter("portfolioId")));
		
		List<Club> allClubs = monitorService.getClubsBySysUserId(this.getCurrentUser().getSysUserId());
		String selectedClubId = request.getParameter("clubId");
		if (selectedClubId == null) {
			selectedClubId = allClubs.get(0).getClubId().toString();
		}
		Long clubId = Long.valueOf(selectedClubId);
		Club club = monitorService.getClubById(clubId);
		List<Portfolio> clubPortfolioList = monitorService.getPortfoliosByClubId(clubId);
		
		model.addAttribute("club", club);
		model.addAttribute("allClubs", allClubs);
		model.addAttribute("portfolio", portfolio);
		model.addAttribute("clubPortfolioList", clubPortfolioList);
		
		return "adminClubPortfolios";
	}
	
	@RequestMapping(value="/addPersonalPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addPersonalPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		SysUser currentSysUser = this.getCurrentUser();
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(currentSysUser);
		portfolio.setSysUserId(currentSysUser.getSysUserId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolio", portfolio);
		
		return "addPersonalPortfolio";
	}
	
	@RequestMapping(value="/addPersonalPortfolio", method=RequestMethod.POST)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addPersonalPortfolioPost(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		SysUser currentSysUser = this.getCurrentUser();
		
		monitorService.insertPortfolio(portfolio);
		portfolio = monitorService.getPortfolioByName(portfolio.getPortfolioName());
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(currentSysUser);

		String selectedPortfolioId = portfolio.getPortfolioId().toString();
		if (selectedPortfolioId == null) {
			if (maintainMenu.getPersonalPortfolioList().size() > 0) {
				selectedPortfolioId = maintainMenu.getPersonalPortfolioList().get(0).getPortfolio().getPortfolioId().toString();
			} else if (maintainMenu.getClubPortfoliosPositions().size() > 0){
				selectedPortfolioId = maintainMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPortfolio().getPortfolioId().toString();
			}
		}

		Portfolio selectedPortfolio = new Portfolio();
		List<PortfolioPosition> allPositions = new ArrayList<PortfolioPosition>();
		if (selectedPortfolioId != null) {
			Long selPortId = Long.valueOf(selectedPortfolioId);
			selectedPortfolio = monitorService.getPortfolioById(selPortId);
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selPortId);
		}
		
		boolean noPortfolios = false;
		boolean noPositions = false;
		boolean isPersonalPortfolio = false;
		if(maintainMenu.getPersonalPortfolioList().isEmpty() && maintainMenu.getClubPortfoliosPositions().isEmpty()) noPortfolios = true;
		if(noPortfolios || allPositions.isEmpty()) noPositions = true;
		if(!noPortfolios && selectedPortfolio.getSysUserId() > 0) isPersonalPortfolio = true;
		

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("noPortfolios", noPortfolios);
		model.addAttribute("noPositions", noPositions);
		model.addAttribute("isPersonalPortfolio", isPersonalPortfolio);
		
		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/maintainPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String maintainPortfolio(HttpServletRequest request,  Model model) {
		logger.info("In maintainPortfolio method. sysUser: " + this.getCurrentUser().getUsername());
		
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(this.getCurrentUser());

		String selectedPortfolioId = request.getParameter("portfolioId");
		if (selectedPortfolioId == null) {
			if (maintainMenu.getPersonalPortfolioList().size() > 0) {
				selectedPortfolioId = maintainMenu.getPersonalPortfolioList().get(0).getPortfolio().getPortfolioId().toString();
			} else if (maintainMenu.getClubPortfoliosPositions().size() > 0){
				selectedPortfolioId = maintainMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPortfolio().getPortfolioId().toString();
			}
		}

		Portfolio selectedPortfolio = new Portfolio();
		List<PortfolioPosition> allPositions = new ArrayList<PortfolioPosition>();
		if (selectedPortfolioId != null) {
			Long selPortId = Long.valueOf(selectedPortfolioId);
			selectedPortfolio = monitorService.getPortfolioById(selPortId);
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selPortId);
		}
		
		boolean noPortfolios = false;
		boolean noPositions = false;
		boolean isPersonalPortfolio = false;
		if(maintainMenu.getPersonalPortfolioList().isEmpty() && maintainMenu.getClubPortfoliosPositions().isEmpty()) noPortfolios = true;
		if(noPortfolios || allPositions.isEmpty()) noPositions = true;
		if(!noPortfolios && selectedPortfolio.getSysUserId() > 0) isPersonalPortfolio = true;
		

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("noPortfolios", noPortfolios);
		model.addAttribute("noPositions", noPositions);
		model.addAttribute("isPersonalPortfolio", isPersonalPortfolio);
		
		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/editPersonalPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editPersonalPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		String portfolioIdStr = request.getParameter("portfolioId");
		portfolio = monitorService.getPortfolioById(Long.valueOf(portfolioIdStr));
		
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(this.getCurrentUser());

		String selectedPortfolioId = portfolio.getPortfolioId().toString();
		if (selectedPortfolioId == null) {
			if (maintainMenu.getPersonalPortfolioList().size() > 0) {
				selectedPortfolioId = maintainMenu.getPersonalPortfolioList().get(0).getPortfolio().getPortfolioId().toString();
			} else if (maintainMenu.getClubPortfoliosPositions().size() > 0){
				selectedPortfolioId = maintainMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPortfolio().getPortfolioId().toString();
			}
		}

		Portfolio selectedPortfolio = new Portfolio();
		List<PortfolioPosition> allPositions = new ArrayList<PortfolioPosition>();
		if (selectedPortfolioId != null) {
			Long selPortId = Long.valueOf(selectedPortfolioId);
			selectedPortfolio = monitorService.getPortfolioById(selPortId);
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selPortId);
		}
		
		boolean noPortfolios = false;
		boolean noPositions = false;
		boolean isPersonalPortfolio = false;
		if(maintainMenu.getPersonalPortfolioList().isEmpty() && maintainMenu.getClubPortfoliosPositions().isEmpty()) noPortfolios = true;
		if(noPortfolios || allPositions.isEmpty()) noPositions = true;
		if(!noPortfolios && selectedPortfolio.getSysUserId() > 0) isPersonalPortfolio = true;
		
		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("noPortfolios", noPortfolios);
		model.addAttribute("noPositions", noPositions);
		model.addAttribute("isPersonalPortfolio", isPersonalPortfolio);

		model.addAttribute("portfolio", portfolio);
		
		return "editPersonalPortfolio";
	}
	
	@RequestMapping(value="/editPersonalPortfolio", method=RequestMethod.POST)
	@Secured({"ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editPersonalPortfolioPost(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		portfolio = monitorService.updatePortfolio(portfolio);
		
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(this.getCurrentUser());

		String selectedPortfolioId = portfolio.getPortfolioId().toString();
		if (selectedPortfolioId == null) {
			if (maintainMenu.getPersonalPortfolioList().size() > 0) {
				selectedPortfolioId = maintainMenu.getPersonalPortfolioList().get(0).getPortfolio().getPortfolioId().toString();
			} else if (maintainMenu.getClubPortfoliosPositions().size() > 0){
				selectedPortfolioId = maintainMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPortfolio().getPortfolioId().toString();
			}
		}

		Portfolio selectedPortfolio = new Portfolio();
		List<PortfolioPosition> allPositions = new ArrayList<PortfolioPosition>();
		if (selectedPortfolioId != null) {
			Long selPortId = Long.valueOf(selectedPortfolioId);
			selectedPortfolio = monitorService.getPortfolioById(selPortId);
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selPortId);
		}
		
		boolean noPortfolios = false;
		boolean noPositions = false;
		boolean isPersonalPortfolio = false;
		if(maintainMenu.getPersonalPortfolioList().isEmpty() && maintainMenu.getClubPortfoliosPositions().isEmpty()) noPortfolios = true;
		if(noPortfolios || allPositions.isEmpty()) noPositions = true;
		if(!noPortfolios && selectedPortfolio.getSysUserId() > 0) isPersonalPortfolio = true;
		
		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("noPortfolios", noPortfolios);
		model.addAttribute("noPositions", noPositions);
		model.addAttribute("isPersonalPortfolio", isPersonalPortfolio);
		
		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/deletePersonalPortfolio", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String deletePersonalPortfolio(Portfolio portfolio, HttpServletRequest request, Model model) {
		
		String portfolioIdStr = request.getParameter("portfolioId");
		monitorService.deletePortfolio(Long.valueOf(portfolioIdStr));
		
		MonitorMenu maintainMenu = monitorService.getMaintainMenu(this.getCurrentUser());

		String selectedPortfolioId = null;
		if (selectedPortfolioId == null) {
			if (maintainMenu.getPersonalPortfolioList().size() > 0) {
				selectedPortfolioId = maintainMenu.getPersonalPortfolioList().get(0).getPortfolio().getPortfolioId().toString();
			} else if (maintainMenu.getClubPortfoliosPositions().size() > 0){
				selectedPortfolioId = maintainMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPortfolio().getPortfolioId().toString();
			}
		}

		Portfolio selectedPortfolio = new Portfolio();
		List<PortfolioPosition> allPositions = new ArrayList<PortfolioPosition>();
		if (selectedPortfolioId != null) {
			Long selPortId = Long.valueOf(selectedPortfolioId);
			selectedPortfolio = monitorService.getPortfolioById(selPortId);
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selPortId);
		}
		
		boolean noPortfolios = false;
		boolean noPositions = false;
		boolean isPersonalPortfolio = false;
		if(maintainMenu.getPersonalPortfolioList().isEmpty() && maintainMenu.getClubPortfoliosPositions().isEmpty()) noPortfolios = true;
		if(noPortfolios || allPositions.isEmpty()) noPositions = true;
		if(!noPortfolios && selectedPortfolio.getSysUserId() > 0) isPersonalPortfolio = true;
		
		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		model.addAttribute("noPortfolios", noPortfolios);
		model.addAttribute("noPositions", noPositions);
		model.addAttribute("isPersonalPortfolio", isPersonalPortfolio);

		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/addPosition", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addPosition(PortfolioPosition position, HttpServletRequest request, Model model) {
		
		MonitorMenu maintainMenu = monitorService.getMonitorMenu(this.getCurrentUser());
		
		String selectedPortfolioId = position.getPortfolioId().toString();
		Portfolio selectedPortfolio = monitorService.getPortfolioById(position.getPortfolioId());
		List<PortfolioPosition> allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(position.getPortfolioId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		
		model.addAttribute("position", position);
		
		return "addPosition";
	}
	
	@RequestMapping(value="/addPosition", method=RequestMethod.POST)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String addPositionPost(PortfolioPosition position, Model model) {
		
		monitorService.insertPortfolioPosition(position);

		MonitorMenu maintainMenu = monitorService.getMonitorMenu(this.getCurrentUser());
		
		String selectedPortfolioId = position.getPortfolioId().toString();
		Portfolio selectedPortfolio = monitorService.getPortfolioById(position.getPortfolioId());
		List<PortfolioPosition> allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(position.getPortfolioId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		
		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/editPosition", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editPosition(PortfolioPosition position, HttpServletRequest request, Model model) {
		
		Long positionId = Long.valueOf(request.getParameter("positionId"));
		
		position = monitorService.getPortfolioPositionById(positionId);
		
		MonitorMenu maintainMenu = monitorService.getMonitorMenu(this.getCurrentUser());
		
		String selectedPortfolioId = position.getPortfolioId().toString();
		Portfolio selectedPortfolio = monitorService.getPortfolioById(position.getPortfolioId());
		List<PortfolioPosition> allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(position.getPortfolioId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		
		model.addAttribute("position", position);
		return "editPosition";
	}
	
	@RequestMapping(value="/editPosition", method=RequestMethod.POST)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String editPositionPost(PortfolioPosition position, Model model) {
		
		MonitorMenu maintainMenu = monitorService.getMonitorMenu(this.getCurrentUser());
		
		monitorService.updatePortfolioPosition(position);
		
		String selectedPortfolioId = position.getPortfolioId().toString();
		Portfolio selectedPortfolio = monitorService.getPortfolioById(position.getPortfolioId());
		List<PortfolioPosition> allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(position.getPortfolioId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);
		
		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/deletePosition", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String deletePosition(HttpServletRequest request, Model model) {
		Long positionId = Long.valueOf(request.getParameter("positionId"));
		
		PortfolioPosition position = monitorService.getPortfolioPositionById(positionId);
		
		// First delete all club member position records for this position
		monitorService.deleteMemberPositionByPositionId(positionId);
		
		// Now delete the position
		monitorService.deletePortfolioPosition(positionId);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userName = auth.getName();
		SysUser sysUser = monitorService.getSysUserByUsername(userName);
		
		MonitorMenu maintainMenu = monitorService.getMonitorMenu(sysUser);
		
		String selectedPortfolioId = position.getPortfolioId().toString();
		Portfolio selectedPortfolio = monitorService.getPortfolioById(position.getPortfolioId());
		List<PortfolioPosition> allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(position.getPortfolioId());

		model.addAttribute("maintainMenu", maintainMenu);
		model.addAttribute("portfolioId", selectedPortfolioId);
		model.addAttribute("selectedPortfolio", selectedPortfolio);
		model.addAttribute("allPositions", allPositions);

		return "maintainPortfolio";
	}
	
	@RequestMapping(value="/monitorPortfolio.html", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String monitorPortfolio(SysUser sysUser,HttpServletRequest request,  Model model) {
		logger.info("In monitorPortfolio method. sysUser: " + this.getCurrentUser().getUsername());
		
		MonitorMenu monitorMenu = monitorService.getMonitorMenu(this.getCurrentUser());

		String selectedPositionId = request.getParameter("positionId");
		if (selectedPositionId == null) {
			if (!monitorMenu.getPersonalPortfolioList().isEmpty() 
					&& !monitorMenu.getPersonalPortfolioList().get(0).getPositions().isEmpty()) {
				selectedPositionId = monitorMenu.getPersonalPortfolioList().get(0).getPositions().get(0).getPositionId().toString();
			} else if (!monitorMenu.getClubPortfoliosPositions().isEmpty() 
					&& !monitorMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().isEmpty()
					&& !monitorMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPositions().isEmpty()) {
				selectedPositionId = monitorMenu.getClubPortfoliosPositions().get(0).getPortfolioPositions().get(0).getPositions().get(0).getPositionId().toString();
			}
		}
		
		PortfolioPosition selectedPosition = new PortfolioPosition();
		DailyStockData mostRecentDay = new DailyStockData();
		Date recentClosingDate = getRecentCloseDate();
		
		if (selectedPositionId != null) {
			Long selPosId = Long.valueOf(selectedPositionId);
			selectedPosition = monitorService.getPortfolioPositionById(selPosId);
			mostRecentDay = monitorService.getDayData(selectedPosition.getTicker(), recentClosingDate);
		}


		model.addAttribute("monitorMenu", monitorMenu);
		model.addAttribute("positionId", selectedPositionId);
		model.addAttribute("mostRecentDay", mostRecentDay);
		model.addAttribute("mostRecentDateString", yahooDateFormat.format(recentClosingDate));
		
		return "monitorPortfolio";
	}
	
	@RequestMapping(value="/monitorPortfolioGraph.html", method=RequestMethod.GET)
	@Secured({"ROLE_USER", "ROLE_CLUB_ADMIN", "ROLE_SYS_ADMIN"})
	public String monitorPortfolioGraph(HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
		logger.info("In monitorPortfolioGraph method - setting up...");
		
		List<Portfolio> allPortfolios = null;
		Portfolio selectedPortfolio = null;
		List<PortfolioPosition> allPositions = null;
		PortfolioPosition selectedPosition = null;

		HttpSession session = request.getSession();
		Long loggedInSysUserId = null;
		if(session.getAttribute("sysUser") == null) {
			SysUser sysUser = monitorService.getLoggedInSysUser();
			if(sysUser != null) {
			loggedInSysUserId = sysUser.getSysUserId();
			session.setAttribute("sysUser", sysUser);
			
			List<Authority> authorities = monitorService.getSysUserAuthorities(loggedInSysUserId);
			session.setAttribute("authorities", authorities);
			}
		} else {
			SysUser sysUser = (SysUser) session.getAttribute("sysUser");
			loggedInSysUserId = sysUser.getSysUserId();
		}

		if(loggedInSysUserId != null) {
			String selectedPositionId = request.getParameter("positionId");
			String selectedPortfolioId = request.getParameter("portfolioId");
			allPortfolios = monitorService.getAllPortfoliosBySysUserId(loggedInSysUserId);
			if(allPortfolios.size() > 0) selectedPortfolio = allPortfolios.get(0);
			if(selectedPortfolioId == null || selectedPortfolioId.trim().equals("")) {
				if(allPortfolios.size() > 0) {
					selectedPortfolio = allPortfolios.get(0);
					selectedPortfolioId = selectedPortfolio.getPortfolioId().toString();
					allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(Long.valueOf(selectedPortfolioId));
				}
				request.setAttribute("portfolioId", selectedPortfolioId);
				request.setAttribute("selectedPortfolio", selectedPortfolio);
			}
			if(selectedPositionId == null || selectedPositionId.trim().equals("")) {
				if(allPositions != null && allPositions.size() > 0) {
					selectedPositionId = allPositions.get(0).getPositionId().toString();
					request.setAttribute("positionId", selectedPositionId);
				}
			}
			if(selectedPositionId != null) 	selectedPosition = monitorService.getPortfolioPositionById(Long.valueOf(selectedPositionId));
			if(selectedPortfolioId != null) selectedPortfolio = monitorService.getPortfolioById(Long.valueOf(selectedPortfolioId));
		}
		
		
		OutputStream out = response.getOutputStream();
		response.setContentType("image/png");
		JFreeChart chart;
		String selectedPositionId = request.getParameter("positionId");
		//String selectedId = request.getParameter("id");
		if(selectedPositionId != null && !selectedPositionId.equals("")) {
			chart = createStockMonitorChart(selectedPositionId);
		} else {
			selectedPortfolio = (Portfolio) request.getAttribute("selectedPortfolio");
			allPositions = monitorService.getAllPortfolioPositionsByPortfolioId(selectedPortfolio.getPortfolioId());
			chart = createStockMonitorChart(allPositions.get(0).getPositionId().toString());
		}
		ChartUtilities.writeChartAsPNG(out, chart, 800, 600);
		out.close();

		return null;
	}
	
	@RequestMapping("/login.html")
	public String login(HttpSession session) {
		return "login";
	}
	
	@RequestMapping("/login-error.html")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login";
	}
	
	@RequestMapping("/error.html")
	public String error(HttpServletRequest request, Model model) {
		model.addAttribute("errorCode", "Error " + request.getAttribute("javax.servlet.error.status_code"));
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("<ul>");
		while (throwable != null) {
			errorMessage.append("<li>").append(escapeTags(throwable.getMessage())).append("</li>");
			throwable = throwable.getCause();
		}
		errorMessage.append("</ul>");
		model.addAttribute("errorMessage", errorMessage.toString());
		return "error";
	}
	
	@RequestMapping("/denied.html")
	public String denied() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userName = auth.getName();
		
		@SuppressWarnings("unchecked")
		List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) auth.getAuthorities();
		
		StringBuffer logMessage = new StringBuffer();
		logMessage.append("Unauthorized access attempted and denied.  Username: " + userName + "; Authorization: ");
		for (SimpleGrantedAuthority authority : authorities) {
			logMessage.append(authority.getAuthority() + " ");
		}
		logger.info(logMessage.toString());
		return "denied";
	}
	private String escapeTags(String text) {
		if (text == null) {
			return null;
		}
		return text.replaceAll(",", "&lt;").replaceAll(">", "&gt;");
	}
	
	private JFreeChart createStockMonitorChart(String positionId) {
		// This chart is for monitoring stocks in a portfolio
		// against predetermined criteria for selling
		
		SimpleDateFormat yahooDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		PortfolioPosition position = monitorService.getPortfolioPositionById(Long.valueOf(positionId));
		
		String ticker = position.getTicker();
		String companyName = position.getCompanyName();

		Date purchaseDate = position.getPurchaseDate();
		
		//List<TimeSeries> timeSeriesList = getWeeklyPriceSeries(position);
		ChartData chartData = getWeeklyPriceSeries(position);
		List<TimeSeries> timeSeriesList = chartData.getTimeSeriesList();
		double adjPurchasePrice = chartData.getAdjPurchasePrice();
		
		TimeSeries wklyStockPrc = timeSeriesList.get(0);
		TimeSeries wklySellPts = timeSeriesList.get(1);
		TimeSeries wklyCagrPrc = timeSeriesList.get(2);
		
		//Double purchasePrice = position.getPurchasePrice();
		Double purchasePrice = adjPurchasePrice;
		TimeSeries stockPurchPoint = new TimeSeries(ticker + " Purchase");
		stockPurchPoint.add(Day.parseDay(yahooDateFormat.format(purchaseDate)), purchasePrice);
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(wklyStockPrc);
		dataset.addSeries(stockPurchPoint);
		dataset.addSeries(wklySellPts);
		dataset.addSeries(wklyCagrPrc);
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				companyName + "(" + ticker + ") Monitoring Chart", "Date", "Share Price ($/sh.)",
				dataset, true, true, false);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		
		// Set the plot background color
		float hsb[] = {0.0F, 0.0F, 0};
		Color.RGBtoHSB(246, 246, 246, hsb);	// Light gray
		plot.setBackgroundPaint(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		
		// Set the Range and Domain axis grid line colors
		Color.RGBtoHSB(80, 80, 80, hsb); // Dark gray
		plot.setDomainGridlinePaint(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		plot.setRangeGridlinePaint(Color.getHSBColor(hsb[0], hsb[1], hsb[2]));
		
		XYItemRenderer r = plot.getRendererForDataset(dataset);
		
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setSeriesPaint(0, Color.RED);		// Stock Price Series
			renderer.setSeriesPaint(1, Color.BLUE);		// Position Purchase Point
			renderer.setSeriesPaint(2, Color.GREEN);	// Stop-Loss Series
			renderer.setSeriesPaint(3, Color.MAGENTA);	// CAGR Required Series
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}
		DateAxis catAxis = (DateAxis) plot.getDomainAxis();
		
		int numDataPoints = wklyStockPrc.getItemCount();
		//logger.info("The number of price history points is: " + numDataPoints);
		int tickDays = 14;
		if(numDataPoints <= 90) {
			tickDays = 14;
		} else if(numDataPoints <= 135) {
			tickDays = 21;
		} else if(numDataPoints <= 180) {
			tickDays = 28;
		} else if(numDataPoints <= 225) {
			tickDays = 35;
		} else if(numDataPoints <= 270) {
			tickDays = 42;
		} else if(numDataPoints <= 305) {
			tickDays = 49;
		} else if(numDataPoints <= 350) {
			tickDays = 56;
		} else if(numDataPoints <= 395) {
			tickDays = 63;
		} else if(numDataPoints <= 440) {
			tickDays = 70;
		} else {
			tickDays = 90;
		}
		
		//logger.info("tickDays = " + tickDays);
		
		DateTickUnit tickUnit = new DateTickUnit(DateTickUnitType.DAY, tickDays);
		catAxis.setVerticalTickLabels(true);
		catAxis.setTickUnit(tickUnit);
		
		
		return chart;
	}
	
	//private List<TimeSeries> getWeeklyPriceSeries(PortfolioPosition position) {
	private ChartData getWeeklyPriceSeries(PortfolioPosition position) {
		ChartData chartData = new ChartData();
		List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
		String ticker = position.getTicker();
		
		// Calculate start date - looking for a Monday eight weeks prior to 
		// purchase date.
		Calendar calStart = Calendar.getInstance();
		calStart.setTime(position.getPurchaseDate());
		calStart.add(Calendar.DAY_OF_MONTH, 2 - calStart.get(Calendar.DAY_OF_WEEK));	// Move to Monday of purchase week
		calStart.add(Calendar.DAY_OF_MONTH, -56); // Move back 8 weeks
		Date startDate = calStart.getTime();
		
		// Calculate end date - looking for most recent Monday.
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(new Date());	// Now
		if(calEnd.get(Calendar.DAY_OF_WEEK) == 1) {
			calEnd.add(Calendar.DAY_OF_MONTH, -6); // If Sunday, then subtract 6 days
		} else {
			calEnd.add(Calendar.DAY_OF_MONTH, 2 - calEnd.get(Calendar.DAY_OF_WEEK));	// Move to Monday of this week
		}
		Date endDate = calEnd.getTime();
		
		TimeSeries wklyStockPrc = new TimeSeries(ticker + " Price");
		TimeSeries wklySellPts = new TimeSeries("Sell Point");
		TimeSeries wklyCagrPrc = new TimeSeries("CAGR Price");
		
		List<WeeklyStockData> wklyDataList = monitorService.getWeeklyData(ticker, startDate, endDate); // Returned in descending order
		List<WeeklyStockData> ascendingWklyDataList = new ArrayList<WeeklyStockData>();
		// Make list in ascending order
		for(int i = wklyDataList.size(); i > 0 ; i--) {
			ascendingWklyDataList.add(wklyDataList.get(i-1));
		}
		
		double adjPurchasePrice = position.getPurchasePrice() * ascendingWklyDataList.get(8).getAdjClose() / ascendingWklyDataList.get(8).getClose();
		//double currentSellPoint = position.getPurchasePrice() * (1 - ((position.getStopLossPct()/100.00)));
		double currentSellPoint = adjPurchasePrice * (1 - ((position.getStopLossPct()/100.00)));
		double dailyCagrFactor = Math.pow((1.00000 + (position.getCagrPct()/100.00000)), (1.00/365.00));
		boolean sellPtAboveCagr = false;
		
		Calendar purchaseCal = Calendar.getInstance();
		purchaseCal.setTime(position.getPurchaseDate());
		Calendar currentCal = Calendar.getInstance();
		
		
		for(WeeklyStockData wklyData : ascendingWklyDataList) {
			wklyStockPrc.add(Day.parseDay(yahooDateFormat.format(wklyData.getDate())), wklyData.getAdjClose());
			//wklyStockPrc.add(Day.parseDay(yahooDateFormat.format(wklyData.getDate())), wklyData.getClose());
			
			currentCal.setTime(wklyData.getDate());
			
			Long daysSincePurchase = ((currentCal.getTimeInMillis() - purchaseCal.getTimeInMillis()) / (24 * 3600 * 1000));
			if(daysSincePurchase >= 0) {
				//double currentCagrReqPrice = position.getPurchasePrice() * Math.pow(dailyCagrFactor, daysSincePurchase);
				double currentCagrReqPrice = adjPurchasePrice * Math.pow(dailyCagrFactor, daysSincePurchase);
				double calculatedSellPoint = wklyData.getAdjClose() * (1 - ((position.getStopLossPct()/100.00)));
				//double calculatedSellPoint = wklyData.getClose() * (1 - ((position.getStopLossPct()/100.00)));
				if((currentSellPoint > currentCagrReqPrice) && !sellPtAboveCagr) sellPtAboveCagr = true;
				currentSellPoint = Math.max(currentSellPoint, calculatedSellPoint);
				if(sellPtAboveCagr) currentSellPoint = Math.max(currentSellPoint, currentCagrReqPrice);

				wklySellPts.add(Day.parseDay(yahooDateFormat.format(wklyData.getDate())), currentSellPoint);
				wklyCagrPrc.add(Day.parseDay(yahooDateFormat.format(wklyData.getDate())), currentCagrReqPrice);
			}
		}
		
		timeSeriesList.add(wklyStockPrc);
		timeSeriesList.add(wklySellPts);
		timeSeriesList.add(wklyCagrPrc);
		
		chartData.setTimeSeriesList(timeSeriesList);
		chartData.setAdjPurchasePrice(adjPurchasePrice);
		
		//return timeSeriesList;
		return chartData;
	}

	private Date getRecentCloseDate() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if((cal.get(Calendar.DAY_OF_WEEK) > 2) && (cal.get(Calendar.DAY_OF_WEEK) <= 7)) {
			cal.add(Calendar.DAY_OF_MONTH, -1); //if Tues. thru Sat. then set to yesterday
		} else if (cal.get(Calendar.DAY_OF_WEEK) == 2) {
			cal.add(Calendar.DAY_OF_MONTH, -3); //if Mon. then set to previous Friday
		} else {
			cal.add(Calendar.DAY_OF_MONTH, -2); //if Sun. then set to previous Friday
		}
		
		return cal.getTime();
	}

	private SysUser getCurrentUser() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userName = auth.getName();
		SysUser sysUser = new SysUser();
		if (userName != "anonymousUser") {
			sysUser = monitorService.getSysUserByUsername(userName);
		}
		
		return sysUser;
	}
	
}
