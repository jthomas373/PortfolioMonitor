package com.ccllc.PortfolioMonitor.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;

import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.Club;
import com.ccllc.PortfolioMonitor.domain.ClubMember;
import com.ccllc.PortfolioMonitor.domain.ClubMemberPosition;
import com.ccllc.PortfolioMonitor.domain.ClubMembership;
import com.ccllc.PortfolioMonitor.domain.Portfolio;
import com.ccllc.PortfolioMonitor.domain.PortfolioPosition;
import com.ccllc.PortfolioMonitor.domain.PortfolioPositionAssignment;
import com.ccllc.PortfolioMonitor.domain.SysUser;

@Repository("monitorDao")
public class MonitorDaoJdbcImpl implements MonitorDao {
	
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private String insertAuthoritySQL = "INSERT INTO authority (AUTHORITY_NAME, DESCRIPTION) VALUES (?,?)";
	private String updateAuthoritySQL = "UPDATE authority SET AUTHORITY_NAME = ?, DESCRIPTION = ? WHERE AUTHORITY_ID = ?";
	private String deleteAuthoritySQL = "DELETE FROM authority WHERE AUTHORITY_ID = ?";
	private String getAllAuthoritiesSQL = "SELECT * FROM authority";
	private String getAuthorityByIdSQL = "SELECT * FROM authority WHERE AUTHORITY_ID = ?";
	private String getAuthorityBySysUserIdSQL = "SELECT A.* FROM authority A, sysuser_authority SA WHERE A.AUTHORITY_ID = SA.AUTHORITY_ID AND SA.SYSUSER_ID = ?";
	
	private String grantAuthorityToSysUserSQL = "INSERT INTO sysuser_authority (SYSUSER_ID, AUTHORITY_ID) VALUES (?,?)";
	private String revokeAuthorityFromSysUserSQL = "DELETE FROM sysuser_authority WHERE SYSUSER_ID = ? AND AUTHORITY_ID = ?";
	private String revokeAuthorityFromAllSysUsersSQL = "DELETE FROM sysuser_authority WHERE AUTHORITY_ID = ?";
	
	private String insertSysUserSQL = "INSERT INTO sysuser (ACTIVE, USERNAME, PASSWORD, LAST_NAME, FIRST_NAME, MIDDLE_INIT) VALUES (?,?,?,?,?,?)";
	private String updateSysUserSQL = "UPDATE sysuser SET ACTIVE = ?, USERNAME = ?, LAST_NAME = ?, FIRST_NAME = ?, MIDDLE_INIT = ? WHERE SYSUSER_ID = ?";
	private String updateSysUserPwdSQL = "UPDATE sysuser SET PASSWORD = ? WHERE SYSUSER_ID = ?";
	private String deleteSysUserSQL = "DELETE FROM sysuser WHERE SYSUSER_ID = ?";
	private String getSysUserByIdSQL = "SELECT * FROM sysuser WHERE SYSUSER_ID = ?";
	private String getSysUserByUsernameSQL = "SELECT * FROM sysuser WHERE USERNAME = ?";
	private String searchSysUsersSQL = "SELECT * FROM sysuser WHERE USERNAME LIKE ? AND LAST_NAME LIKE ? AND ACTIVE = ? ORDER BY LAST_NAME, FIRST_NAME, MIDDLE_INIT";
	
	private String insertClubSQL = "INSERT INTO club (CLUB_NAME, DESCRIPTION, NOTES, CITY, STATE, COUNTRY) VALUES (?,?,?,?,?,?)";
	private String updateClubSQL = "UPDATE club SET CLUB_NAME = ?, DESCRIPTION = ?, NOTES = ?, CITY = ?, STATE = ?, COUNTRY = ? WHERE CLUB_ID = ?";
	private String deleteClubSQL = "DELETE FROM club WHERE CLUB_ID = ?";
	private String getClubByIdSQL = "SELECT * FROM club WHERE CLUB_ID = ?";
	//private String getClubByClubNameSQL = "SELECT * FROM CLUB WHERE CLUB_NAME = ?";
	private String searchClubsSQL = "SELECT * FROM club WHERE CLUB_NAME LIKE ? AND CITY LIKE ? AND STATE LIKE ? ORDER BY CLUB_NAME, CITY, STATE";
	private String getAllClubsSQL = "SELECT * FROM club ORDER BY CLUB_NAME, CITY, STATE";
	
	private String addMemberToClubSQL = "INSERT INTO club_membership (SYSUSER_ID, CLUB_ID, IS_ADMIN) VALUES (?,?,?)";
	private String removeMemberFromClubSQL = "DELETE FROM club_membership WHERE SYSUSER_ID = ? AND CLUB_ID = ?";
	private String removeMembersFromClubByClubIdSQL = "DELETE FROM club_membership WHERE CLUB_ID = ?";
	private String makeClubAdminSQL = "UPDATE club_membership SET IS_ADMIN = 1 WHERE CLUB_ID = ? AND SYSUSER_ID = ?";
	private String revokeClubAdminSQL = "UPDATE club_membership SET IS_ADMIN = 0 WHERE CLUB_ID = ? AND SYSUSER_ID = ?";
	private String getClubMembershipSQL = "SELECT * FROM club_membership WHERE CLUB_ID = ? AND SYSUSER_ID = ?";
	private String getClubsBySysUserIdSQL = "SELECT C.* FROM club C, club_membership CM WHERE C.CLUB_ID = CM.CLUB_ID AND CM.SYSUSER_ID = ? ORDER BY C.CLUB_NAME, C.CITY, C.STATE";
	private String getClubMembersByClubIdSQL = "SELECT SU.*, CM.CLUB_ID, CM.IS_ADMIN FROM sysuser SU, club_membership CM WHERE SU.SYSUSER_ID = CM.SYSUSER_ID AND CM.CLUB_ID = ? ORDER BY SU.LAST_NAME, SU.FIRST_NAME, SU.MIDDLE_INIT";
	private String getNewClubMembersByClubIdSQL = "SELECT SU.*, CM.CLUB_ID, CM.IS_ADMIN FROM sysuser SU LEFT JOIN club_membership CM ON SU.SYSUSER_ID = CM.SYSUSER_ID WHERE (CM.CLUB_ID IS NULL OR CM.CLUB_ID != ?) AND USERNAME LIKE ? AND LAST_NAME LIKE ? AND ACTIVE LIKE ? ORDER BY SU.LAST_NAME, SU.FIRST_NAME, SU.MIDDLE_INIT";
	private String getClubsByClubAdminIdSQL = "SELECT C.* FROM club C, club_membership CM WHERE C.CLUB_ID = CM.CLUB_ID AND CM.SYSUSER_ID = ? AND CM.IS_ADMIN = TRUE ORDER BY C.CLUB_NAME, C.CITY, C.STATE";
	
	private String addClubMemberPositionSQL = "INSERT INTO club_member_position (SYSUSER_ID, CLUB_ID, POSITION_ID) VALUES (?,?,?)";
	private String deleteClubMemberPositionSQL = "DELETE FROM club_member_position WHERE SYSUSER_ID = ? AND CLUB_ID = ? AND POSITION_ID = ?";
	private String deleteClubMemberPositionByClubIdSQL = "DELETE FROM club_member_position WHERE CLUB_ID = ?";
	private String deleteClubMemberPositionByPositionIdSQL = "DELETE FROM club_member_position WHERE POSITION_ID = ?";
	private String portPositionAssignmentsSQL = "SELECT PP.POSITION_ID, PP.TICKER, PP.COMPANY_NAME, CMP.SYSUSER_ID FROM portfolio_position PP left join club_member_position CMP on PP.POSITION_ID = CMP.POSITION_ID WHERE PP.PORTFOLIO_ID = ? ORDER BY PP.COMPANY_NAME, PP.PURCHASE_DATE";
	
	private String insertPortfolioSQL = "INSERT INTO portfolio (SYSUSER_ID, CLUB_ID, PORTFOLIO_NAME, DESCRIPTION, NOTES) VALUES (?,?,?,?,?)";
	private String updatePortfolioSQL = "UPDATE portfolio SET SYSUSER_ID = ?, CLUB_ID = ?, PORTFOLIO_NAME = ?, DESCRIPTION = ?, NOTES = ? WHERE PORTFOLIO_ID = ?";
	private String deletePortfolioSQL = "DELETE FROM portfolio WHERE PORTFOLIO_ID = ?";
	private String getPortfolioByIdSQL = "SELECT * FROM portfolio WHERE PORTFOLIO_ID = ?";
	private String getPortfolioByNameSQL = "SELECT * FROM portfolio WHERE PORTFOLIO_NAME = ?";
	private String getPortfoliosByClubIdSQL = "SELECT * FROM portfolio WHERE CLUB_ID = ?";
	private String getPortfoliosBySysUserIdSQL = "SELECT * FROM portfolio WHERE SYSUSER_ID = ?";
	private String getClubPortfoliosBySysUserIdSQL = "SELECT P.* FROM portfolio P, club_membership CM WHERE P.CLUB_ID = CM.CLUB_ID AND CM.SYSUSER_ID = ?";
	private String getAllPortfoliosBySysUserIdSQL = "(SELECT P.* FROM portfolio P WHERE SYSUSER_ID = ?) UNION (SELECT P.* FROM portfolio P, club_membership CM WHERE CM.CLUB_ID = P.CLUB_ID AND CM.SYSUSER_ID = ?)";
	
	private String insertPortfolioPositionSQL = "INSERT INTO portfolio_position (PORTFOLIO_ID, TICKER, COMPANY_NAME, PURCHASE_DATE, PURCHASE_QTY, PURCHASE_PRICE, STOP_LOSS_PCT, CAGR_PCT, CAGR_GRACE_DAYS) VALUES (?,?,?,?,?,?,?,?,?)";
	private String updatePortfolioPositionSQL = "UPDATE portfolio_position SET PORTFOLIO_ID = ?, TICKER = ?, COMPANY_NAME = ?, PURCHASE_DATE = ?, PURCHASE_QTY = ?, PURCHASE_PRICE = ?, STOP_LOSS_PCT = ?, CAGR_PCT = ?, CAGR_GRACE_DAYS = ? WHERE POSITION_ID = ?";
	private String deletePortfolioPositionSQL = "DELETE FROM portfolio_position WHERE POSITION_ID = ?";
	private String deletePortfolioPositionsByPortfolioIdSQL = "DELETE FROM portfolio_position WHERE PORTFOLIO_ID = ?";
	private String getPortfolioPositionByIdSQL = "SELECT * FROM portfolio_position WHERE POSITION_ID = ?";
	private String getAllPortfolioPositionsByPortfolioIdSQL = "SELECT * FROM portfolio_position WHERE PORTFOLIO_ID = ? ORDER BY TICKER ASC, PURCHASE_DATE ASC";
	
	/*
	public String getInsertAuthoritySQL() {
		return insertAuthoritySQL;
	}
	public void setInsertAuthoritySQL(String insertAuthoritySQL) {
		this.insertAuthoritySQL = insertAuthoritySQL;
	}

	public String getUpdateAuthoritySQL() {
		return updateAuthoritySQL;
	}
	public void setUpdateAuthoritySQL(String updateAuthoritySQL) {
		this.updateAuthoritySQL = updateAuthoritySQL;
	}

	public String getDeleteAuthoritySQL() {
		return deleteAuthoritySQL;
	}
	public void setDeleteAuthoritySQL(String deleteAuthoritySQL) {
		this.deleteAuthoritySQL = deleteAuthoritySQL;
	}

	public String getGetAllAuthoritiesSQL() {
		return getAllAuthoritiesSQL;
	}
	public void setGetAllAuthoritiesSQL(String getAllAuthoritiesSQL) {
		this.getAllAuthoritiesSQL = getAllAuthoritiesSQL;
	}

	public String getGetAuthorityByIdSQL() {
		return getAuthorityByIdSQL;
	}
	public void setGetAuthorityByIdSQL(String getAuthorityByIdSQL) {
		this.getAuthorityByIdSQL = getAuthorityByIdSQL;
	}

	public String getGetAuthorityBySysUserIdSQL() {
		return getAuthorityBySysUserIdSQL;
	}
	public void setGetAuthorityBySysUserIdSQL(String getAuthorityBySysUserIdSQL) {
		this.getAuthorityBySysUserIdSQL = getAuthorityBySysUserIdSQL;
	}

	public String getGrantAuthorityToSysUserSQL() {
		return grantAuthorityToSysUserSQL;
	}
	public void setGrantAuthorityToSysUserSQL(
			String grantAuthorityToSysUserSQL) {
		this.grantAuthorityToSysUserSQL = grantAuthorityToSysUserSQL;
	}

	public String getRevokeAuthorityFromSysUserSQL() {
		return revokeAuthorityFromSysUserSQL;
	}
	public void setRevokeAuthorityFromSysUserSQL(
			String revokeAuthorityFromSysUserSQL) {
		this.revokeAuthorityFromSysUserSQL = revokeAuthorityFromSysUserSQL;
	}

	public String getRevokeAuthorityFromAllSysUsersSQL() {
		return revokeAuthorityFromAllSysUsersSQL;
	}
	public void setRevokeAuthorityFromAllSysUsersSQL(
			String revokeAuthorityFromAllSysUsersSQL) {
		this.revokeAuthorityFromAllSysUsersSQL = revokeAuthorityFromAllSysUsersSQL;
	}

	public String getInsertSysUserSQL() {
		return insertSysUserSQL;
	}
	public void setInsertSysUserSQL(String insertSysUserSQL) {
		this.insertSysUserSQL = insertSysUserSQL;
	}

	public String getUpdateSysUserSQL() {
		return updateSysUserSQL;
	}
	public void setUpdateSysUserSQL(String updateSysUserSQL) {
		this.updateSysUserSQL = updateSysUserSQL;
	}

	public String getUpdateSysUserPwdSQL() {
		return updateSysUserPwdSQL;
	}
	public void setUpdateSysUserPwdSQL(String updateSysUserPwdSQL) {
		this.updateSysUserPwdSQL = updateSysUserPwdSQL;
	}

	public String getDeleteSysUserSQL() {
		return deleteSysUserSQL;
	}
	public void setDeleteSysUserSQL(String deleteSysUserSQL) {
		this.deleteSysUserSQL = deleteSysUserSQL;
	}

	public String getGetSysUserByIdSQL() {
		return getSysUserByIdSQL;
	}
	public void setGetSysUserByIdSQL(String getSysUserByIdSQL) {
		this.getSysUserByIdSQL = getSysUserByIdSQL;
	}

	public String getGetSysUserByUsernameSQL() {
		return getSysUserByUsernameSQL;
	}
	public void setGetSysUserByUsernameSQL(
			String getSysUserByUsernameSQL) {
		this.getSysUserByUsernameSQL = getSysUserByUsernameSQL;
	}

	public String getSearchSysUsersSQL() {
		return searchSysUsersSQL;
	}

	public void setSearchSysUsersSQL(String searchSysUsersSQL) {
		this.searchSysUsersSQL = searchSysUsersSQL;
	}

	public String getInsertClubSQL() {
		return insertClubSQL;
	}

	public void setInsertClubSQL(String insertClubSQL) {
		this.insertClubSQL = insertClubSQL;
	}

	public String getUpdateClubSQL() {
		return updateClubSQL;
	}

	public void setUpdateClubSQL(String updateClubSQL) {
		this.updateClubSQL = updateClubSQL;
	}

	public String getDeleteClubSQL() {
		return deleteClubSQL;
	}

	public void setDeleteClubSQL(String deleteClubSQL) {
		this.deleteClubSQL = deleteClubSQL;
	}

	public String getGetClubByIdSQL() {
		return getClubByIdSQL;
	}

	public void setGetClubByIdSQL(String getClubByIdSQL) {
		this.getClubByIdSQL = getClubByIdSQL;
	}

	public String getGetClubByClubNameSQL() {
		return getClubByClubNameSQL;
	}

	public void setGetClubByClubNameSQL(String getClubByClubNameSQL) {
		this.getClubByClubNameSQL = getClubByClubNameSQL;
	}

	public String getSearchClubsSQL() {
		return searchClubsSQL;
	}
	public void setSearchClubsSQL(String searchClubsSQL) {
		this.searchClubsSQL = searchClubsSQL;
	}

	public String getGetAllClubsSQL() {
		return getAllClubsSQL;
	}
	public void setGetAllClubsSQL(String getAllClubsSQL) {
		this.getAllClubsSQL = getAllClubsSQL;
	}

	public String getAddMemberToClubSQL() {
		return addMemberToClubSQL;
	}

	public void setAddMemberToClubSQL(String addMemberToClubSQL) {
		this.addMemberToClubSQL = addMemberToClubSQL;
	}

	public String getRemoveMemberFromClubSQL() {
		return removeMemberFromClubSQL;
	}

	public void setRemoveMemberFromClubSQL(String removeMemberFromClubSQL) {
		this.removeMemberFromClubSQL = removeMemberFromClubSQL;
	}

	public String getMakeClubAdministratorSQL() {
		return makeClubAdministratorSQL;
	}

	public void setMakeClubAdministratorSQL(String makeClubAdministratorSQL) {
		this.makeClubAdministratorSQL = makeClubAdministratorSQL;
	}

	public String getGetClubMembershipSQL() {
		return getClubMembershipSQL;
	}
	public void setGetClubMembershipSQL(String getClubMembershipSQL) {
		this.getClubMembershipSQL = getClubMembershipSQL;
	}

	public String getGetClubsBySysUserIdSQL() {
		return getClubsBySysUserIdSQL;
	}
	public void setGetClubsBySysUserIdSQL(String getClubsBySysUserIdSQL) {
		this.getClubsBySysUserIdSQL = getClubsBySysUserIdSQL;
	}

	public String getGetClubMembersByClubIdSQL() {
		return getClubMembersByClubIdSQL;
	}
	public void setGetClubMembersByClubIdSQL(String getClubMembersByClubIdSQL) {
		this.getClubMembersByClubIdSQL = getClubMembersByClubIdSQL;
	}
	
	public String getGetClubsByClubAdminIdSQL() {
		return getClubsByClubAdminIdSQL;
	}
	public void setGetClubsByClubAdminIdSQL(String getClubsByClubAdminIdSQL) {
		this.getClubsByClubAdminIdSQL = getClubsByClubAdminIdSQL;
	}

	public String getAddClubMemberPositionSQL() {
		return addClubMemberPositionSQL;
	}
	public void setAddClubMemberPositionSQL(String addClubMemberPositionSQL) {
		this.addClubMemberPositionSQL = addClubMemberPositionSQL;
	}
	
	public String getDeleteClubMemberPositionSQL() {
		return deleteClubMemberPositionSQL;
	}
	public void setDeleteClubMemberPositionSQL(String deleteClubMemberPositionSQL) {
		this.deleteClubMemberPositionSQL = deleteClubMemberPositionSQL;
	}

	public String getPortPositionAssignmentsSQL() {
		return portPositionAssignmentsSQL;
	}
	public void setPortPositionAssignmentsSQL(String portPositionAssignmentsSQL) {
		this.portPositionAssignmentsSQL = portPositionAssignmentsSQL;
	}

	public String getInsertPortfolioSQL() {
		return insertPortfolioSQL;
	}

	public void setInsertPortfolioSQL(String insertPortfolioSQL) {
		this.insertPortfolioSQL = insertPortfolioSQL;
	}

	public String getUpdatePortfolioSQL() {
		return updatePortfolioSQL;
	}

	public void setUpdatePortfolioSQL(String updatePortfolioSQL) {
		this.updatePortfolioSQL = updatePortfolioSQL;
	}

	public String getDeletePortfolioSQL() {
		return deletePortfolioSQL;
	}

	public void setDeletePortfolioSQL(String deletePortfolioSQL) {
		this.deletePortfolioSQL = deletePortfolioSQL;
	}

	public String getGetPortfolioByIdSQL() {
		return getPortfolioByIdSQL;
	}

	public void setGetPortfolioByIdSQL(String getPortfolioByIdSQL) {
		this.getPortfolioByIdSQL = getPortfolioByIdSQL;
	}

	public String getGetPortfolioByNameSQL() {
		return getPortfolioByNameSQL;
	}
	public void setGetPortfolioByNameSQL(String getPortfolioByNameSQL) {
		this.getPortfolioByNameSQL = getPortfolioByNameSQL;
	}

	public String getGetPortfoliosByClubIdSQL() {
		return getPortfoliosByClubIdSQL;
	}

	public void setGetPortfoliosByClubIdSQL(String getPortfoliosByClubIdSQL) {
		this.getPortfoliosByClubIdSQL = getPortfoliosByClubIdSQL;
	}

	public String getGetPortfoliosBySysUserIdSQL() {
		return getPortfoliosBySysUserIdSQL;
	}

	public void setGetPortfoliosBySysUserIdSQL(String getPortfoliosBySysUserIdSQL) {
		this.getPortfoliosBySysUserIdSQL = getPortfoliosBySysUserIdSQL;
	}

	public String getGetClubPortfoliosBySysUserIdSQL() {
		return getClubPortfoliosBySysUserIdSQL;
	}
	public void setGetClubPortfoliosBySysUserIdSQL(
			String getClubPortfoliosBySysUserIdSQL) {
		this.getClubPortfoliosBySysUserIdSQL = getClubPortfoliosBySysUserIdSQL;
	}

	public String getGetAllPortfoliosBySysUserIdSQL() {
		return getAllPortfoliosBySysUserIdSQL;
	}

	public void setGetAllPortfoliosBySysUserIdSQL(
			String getAllPortfoliosBySysUserIdSQL) {
		this.getAllPortfoliosBySysUserIdSQL = getAllPortfoliosBySysUserIdSQL;
	}

	public String getInsertPortfolioPositionSQL() {
		return insertPortfolioPositionSQL;
	}

	public void setInsertPortfolioPositionSQL(String insertPortfolioPositionSQL) {
		this.insertPortfolioPositionSQL = insertPortfolioPositionSQL;
	}

	public String getUpdatePortfolioPositionSQL() {
		return updatePortfolioPositionSQL;
	}

	public void setUpdatePortfolioPositionSQL(String updatePortfolioPositionSQL) {
		this.updatePortfolioPositionSQL = updatePortfolioPositionSQL;
	}

	public String getDeletePortfolioPositionSQL() {
		return deletePortfolioPositionSQL;
	}

	public void setDeletePortfolioPositionSQL(String deletePortfolioPositionSQL) {
		this.deletePortfolioPositionSQL = deletePortfolioPositionSQL;
	}

	public String getGetPortfolioPositionByIdSQL() {
		return getPortfolioPositionByIdSQL;
	}

	public void setGetPortfolioPositionByIdSQL(String getPortfolioPositionByIdSQL) {
		this.getPortfolioPositionByIdSQL = getPortfolioPositionByIdSQL;
	}

	public String getGetAllPortfolioPositionsByPortfolioIdSQL() {
		return getAllPortfolioPositionsByPortfolioIdSQL;
	}

	public void setGetAllPortfolioPositionsByPortfolioIdSQL(
			String getAllPortfolioPositionsByPortfolioIdSQL) {
		this.getAllPortfolioPositionsByPortfolioIdSQL = getAllPortfolioPositionsByPortfolioIdSQL;
	}
*/
	
	
	// Authority DAO methods
	
	public void insertAuthority(Authority authority) {
		jdbcTemplate.update(
				this.insertAuthoritySQL,
				new Object[] {
					authority.getAuthorityName(),
					authority.getDescription()});
	}

	public Authority updateAuthority(Authority authority) {
		jdbcTemplate.update(
				this.updateAuthoritySQL,
				new Object[] {
					authority.getAuthorityName(),
					authority.getDescription(),
					authority.getAuthorityId()});
		
		return this.getAuthorityById(authority.getAuthorityId());
	}

	public void deleteAuthority(Long authorityId) {
		jdbcTemplate.update(this.deleteAuthoritySQL, authorityId);
	}

	public List<Authority> getAllAuthorities() {
		return jdbcTemplate.query(this.getAllAuthoritiesSQL, new AuthorityMapper());
	}

	public Authority getAuthorityById(Long authorityId) {
		return jdbcTemplate.queryForObject(this.getAuthorityByIdSQL, new AuthorityMapper(), authorityId);
	}

	// SysUserAuthority DAO methods
	
	public List<Authority> getSysUserAuthorities(Long sysUserId) {
		return jdbcTemplate.query(this.getAuthorityBySysUserIdSQL, new AuthorityMapper(), sysUserId);
	}

	public void grantAuthorityToSysUser(Long sysUserId, Long authorityId) {
		jdbcTemplate.update(
				this.grantAuthorityToSysUserSQL,
				new Object[] {
					sysUserId,
					authorityId});
	}

	public void revokeAuthorityFromSysUser(Long sysUserId, Long authorityId) {
		jdbcTemplate.update(
				this.revokeAuthorityFromSysUserSQL, 
				new Object[] {
					sysUserId,
					authorityId});
	}

	public void revokeAuthorityFromAllSysUsers(Long authorityId) {
		jdbcTemplate.update(
				this.revokeAuthorityFromAllSysUsersSQL, authorityId);
	}

	// SysUser DAO methods
	
	public void insertSysUser(SysUser sysUser) {
		jdbcTemplate.update(
				this.insertSysUserSQL,
				new Object[] {
					sysUser.getActive(),
					sysUser.getUsername(),
					sysUser.getPassword(),
					sysUser.getLastName(),
					sysUser.getFirstName(),
					sysUser.getMiddleInit()});
	}

	public SysUser updateSysUser(SysUser sysUser) {
		jdbcTemplate.update(
				this.updateSysUserSQL,
				new Object[] {
					sysUser.getActive(),
					sysUser.getUsername(),
					sysUser.getLastName(),
					sysUser.getFirstName(),
					sysUser.getMiddleInit(),
					sysUser.getSysUserId()});
		
		return this.getSysUserById(sysUser.getSysUserId());
	}
	
	public SysUser updateSysUserPwd(Long sysUserId, String password) {
		jdbcTemplate.update(
				this.updateSysUserPwdSQL,
				new Object[] {
					password,
					sysUserId});
		
		return this.getSysUserById(sysUserId);
	}

	public void deleteSysUser(Long sysUserId) {
		jdbcTemplate.update(this.deleteSysUserSQL, sysUserId);
	}

	public SysUser getSysUserById(Long sysUserId) {
		return jdbcTemplate.queryForObject(this.getSysUserByIdSQL, new SysUserMapper(), sysUserId);
	}

	public SysUser getSysUserByUsername(String username) {
		String sql = this.getSysUserByUsernameSQL;
		return jdbcTemplate.queryForObject(sql, new SysUserMapper(), username);
	}

	public List<SysUser> searchSysUsers(String usernameCrit, String lastNameCrit, Boolean activeCrit) {
		return jdbcTemplate.query(this.searchSysUsersSQL, new SysUserMapper(), usernameCrit, lastNameCrit, activeCrit);
	}

	// Club DAOs
	
	public void insertClub(Club club) {
		jdbcTemplate.update(
				this.insertClubSQL,
				new Object[] {
					club.getClubName(),
					club.getDescription(),
					club.getNotes(),
					club.getCity(),
					club.getState(),
					club.getCountry()});
	}

	public Club updateClub(Club club) {
		jdbcTemplate.update(
				this.updateClubSQL,
				new Object[] {
					club.getClubName(),
					club.getDescription(),
					club.getNotes(),
					club.getCity(),
					club.getState(),
					club.getCountry(),
					club.getClubId()});
		
		return this.getClubById(club.getClubId());
	}

	public void deleteClub(Long clubId) {
		jdbcTemplate.update(this.deleteClubSQL, clubId);
	}

	public Club getClubByClubName(String clubName) {
		return jdbcTemplate.queryForObject(this.getClubByIdSQL, new ClubMapper(), clubName);
	}

	public Club getClubById(Long clubId) {
		return jdbcTemplate.queryForObject(this.getClubByIdSQL, new ClubMapper(), clubId);
	}

	public List<Club> searchClubs(String clubNameCrit, String cityCrit, String stateCrit) {
		return jdbcTemplate.query(this.searchClubsSQL, new ClubMapper(), clubNameCrit, cityCrit, stateCrit);
	}

	public List<Club> getAllClubs() {
		return jdbcTemplate.query(this.getAllClubsSQL, new ClubMapper());
	}

	// ClubMembership DAOs
	
	public void addMemberToClub(Long sysUserId, Long clubId, Boolean isClubAdministrator) {
		if(isClubAdministrator == null) isClubAdministrator = false;
		jdbcTemplate.update(
				this.addMemberToClubSQL,
				new Object[] {
					sysUserId,
					clubId,
					isClubAdministrator});
	}

	public void removeMemberFromClub(Long sysUserId, Long clubId) {
		jdbcTemplate.update(this.removeMemberFromClubSQL, sysUserId, clubId);
	}

	public void removeMembersFromClubByClubId(Long clubId) {
		jdbcTemplate.update(this.removeMembersFromClubByClubIdSQL, clubId);
	}

	public void makeClubAdmin(Long clubId, Long sysUserId) {
		jdbcTemplate.update(this.makeClubAdminSQL, clubId, sysUserId);
	}

	public void revokeClubAdmin(Long clubId, Long sysUserId) {
		jdbcTemplate.update(this.revokeClubAdminSQL, clubId, sysUserId);
	}

	public List<ClubMembership> getClubMembership(Long clubId, Long sysUserId) {
		return jdbcTemplate.query(this.getClubMembershipSQL, new ClubMembershipMapper(),clubId,sysUserId);
	}

	public List<Club> getClubsBySysUserId(Long sysUserId) {
		return jdbcTemplate.query(this.getClubsBySysUserIdSQL, new ClubMapper(),sysUserId);
	}
	
	public List<ClubMember> getClubMembersByClubId(Long clubId) {
		return jdbcTemplate.query(this.getClubMembersByClubIdSQL, new ClubMemberMapper(), clubId);
	}

	public List<ClubMember> getNewClubMembersByClubId(Long clubId, String usernameCrit, String lastNameCrit, Boolean activeCrit) {
		return jdbcTemplate.query(this.getNewClubMembersByClubIdSQL, new ClubMemberMapper(), clubId, usernameCrit, lastNameCrit, activeCrit);
	}

	public List<Club> getClubsByClubAdminId(Long sysUserId) {
		return jdbcTemplate.query(this.getClubsByClubAdminIdSQL, new ClubMapper(), sysUserId);
	}

	// ClubMemberPosition DAOs

	public void addMemberPosition(Long clubId, Long sysUserId, Long positionId) {
		jdbcTemplate.update(
				this.addClubMemberPositionSQL,
				new Object[] {
					sysUserId,
					clubId,
					positionId});
	}
	public void deleteMemberPosition(Long clubId, Long sysUserId,	Long positionId) {
		jdbcTemplate.update(this.deleteClubMemberPositionSQL, sysUserId, clubId, positionId);
	}

	public void deleteMemberPositionByClubId(Long clubId) {
		jdbcTemplate.update(this.deleteClubMemberPositionByClubIdSQL, clubId);
	}

	public void deleteMemberPositionByPositionId(Long positionId) {
		jdbcTemplate.update(this.deleteClubMemberPositionByPositionIdSQL, positionId);
	}

	public List<PortfolioPositionAssignment> getPortPositionAssignments(Long portfolioId) {
		return jdbcTemplate.query(this.portPositionAssignmentsSQL, new PortfolioPositionAssignmentMapper(), portfolioId);
	}

	// Portfolio DAOs
	
	public void insertPortfolio(Portfolio portfolio) {
		jdbcTemplate.update(
				this.insertPortfolioSQL,
				new Object[] {
					portfolio.getSysUserId(),
					portfolio.getClubId(),
					portfolio.getPortfolioName(),
					portfolio.getDescription(),
					portfolio.getNotes()});
	}

	public Portfolio updatePortfolio(Portfolio portfolio) {
		jdbcTemplate.update(
				this.updatePortfolioSQL,
				new Object[] {
					portfolio.getSysUserId(),
					portfolio.getClubId(),
					portfolio.getPortfolioName(),
					portfolio.getDescription(),
					portfolio.getNotes(),
					portfolio.getPortfolioId()});
		
		return this.getPortfolioById(portfolio.getPortfolioId());
	}

	public void deletePortfolio(Long portfolioId) {
		jdbcTemplate.update(this.deletePortfolioSQL, portfolioId);
	}

	public Portfolio getPortfolioById(Long portfolioId) {
		return jdbcTemplate.queryForObject(this.getPortfolioByIdSQL, new PortfolioMapper(), portfolioId);
	}

	public Portfolio getPortfolioByName(String portfolioName) {
		return jdbcTemplate.queryForObject(this.getPortfolioByNameSQL, new PortfolioMapper(), portfolioName);
	}

	public List<Portfolio> getPortfoliosByClubId(Long clubId) {
		return jdbcTemplate.query(this.getPortfoliosByClubIdSQL, new PortfolioMapper(), clubId);
	}

	public List<Portfolio> getPortfoliosBySysUserId(Long sysUserId) {
		return jdbcTemplate.query(this.getPortfoliosBySysUserIdSQL, new PortfolioMapper(), sysUserId);
	}

	public List<Portfolio> getClubPortfoliosBySysUserId(Long sysUserId) {
		return jdbcTemplate.query(this.getClubPortfoliosBySysUserIdSQL, new PortfolioMapper(), sysUserId);
	}
	
	public List<Portfolio> getAllPortfoliosBySysUserId(Long sysUserId) {
		return jdbcTemplate.query(this.getAllPortfoliosBySysUserIdSQL, new PortfolioMapper(), sysUserId, sysUserId);
	}

	// PortfolioPosition DAOs
	
	public void insertPortfolioPosition(PortfolioPosition position) {
		jdbcTemplate.update(
				this.insertPortfolioPositionSQL,
				new Object[] {
					position.getPortfolioId(),
					position.getTicker(),
					position.getCompanyName(),
					position.getPurchaseDate(),
					position.getPurchaseQty(),
					position.getPurchasePrice(),
					position.getStopLossPct(),
					position.getCagrPct(),
					position.getCagrGraceDays()});
	}

	public PortfolioPosition updatePortfolioPosition(PortfolioPosition position) {
		jdbcTemplate.update(
				this.updatePortfolioPositionSQL,
				new Object[] {
					position.getPortfolioId(),
					position.getTicker(),
					position.getCompanyName(),
					position.getPurchaseDate(),
					position.getPurchaseQty(),
					position.getPurchasePrice(),
					position.getStopLossPct(),
					position.getCagrPct(),
					position.getCagrGraceDays(),
					position.getPositionId()});
		
		return this.getPortfolioPositionById(position.getPositionId());
	}

	public void deletePortfolioPosition(Long positionId) {
		jdbcTemplate.update(this.deletePortfolioPositionSQL, positionId);
	}

	public void deletePortfolioPositionsByPortfolioId(Long portfolioId) {
		jdbcTemplate.update(this.deletePortfolioPositionsByPortfolioIdSQL, portfolioId);
	}

	public List<PortfolioPosition> getAllPortfolioPositionsByPortfolioId(	Long portfolioId) {
		return jdbcTemplate.query(this.getAllPortfolioPositionsByPortfolioIdSQL, new PortfolioPositionMapper(), portfolioId);
	}

	public PortfolioPosition getPortfolioPositionById(Long positionId) {
		return jdbcTemplate.queryForObject(this.getPortfolioPositionByIdSQL, new PortfolioPositionMapper(), positionId);
	}


	
	
	
	// Row Mappers
	private class AuthorityMapper implements ParameterizedRowMapper<Authority> {
		public Authority mapRow(ResultSet rs, int rowNum) throws SQLException {
			Authority authority = new Authority();
			authority.setAuthorityId(rs.getLong("authority_id"));
			authority.setAuthorityName(rs.getString("authority_name"));
			authority.setDescription(rs.getString("description"));
			return authority;
		}
	}

	private class SysUserMapper implements ParameterizedRowMapper<SysUser> {
		public SysUser mapRow(ResultSet rs, int rowNum) throws SQLException {
			SysUser sysuser = new SysUser();
			sysuser.setSysUserId(rs.getLong("sysuser_id"));
			sysuser.setUsername(rs.getString("username"));
			sysuser.setPassword(rs.getString("password"));
			sysuser.setActive(rs.getBoolean("active"));
			sysuser.setFirstName(rs.getString("first_name"));
			sysuser.setLastName(rs.getString("last_name"));
			sysuser.setMiddleInit(rs.getString("middle_init"));
			return sysuser;
		}
	}

	private class ClubMemberMapper implements ParameterizedRowMapper<ClubMember> {
		public ClubMember mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClubMember clubMember = new ClubMember();
			
			SysUser sysuser = new SysUser();
			sysuser.setSysUserId(rs.getLong("sysuser_id"));
			sysuser.setUsername(rs.getString("username"));
			sysuser.setPassword(rs.getString("password"));
			sysuser.setActive(rs.getBoolean("active"));
			sysuser.setFirstName(rs.getString("first_name"));
			sysuser.setLastName(rs.getString("last_name"));
			sysuser.setMiddleInit(rs.getString("middle_init"));
			
			clubMember.setSysUser(sysuser);
			clubMember.setClubId(rs.getLong("club_id"));
			clubMember.setIsAdmin(rs.getBoolean("is_admin"));
			
			return clubMember;
		}
	}

	private class ClubMapper implements ParameterizedRowMapper<Club> {
		public Club mapRow(ResultSet rs, int rowNum) throws SQLException {
			Club club = new Club();
			club.setClubId(rs.getLong("club_id"));
			club.setClubName(rs.getString("club_name"));
			club.setDescription(rs.getString("description"));
			club.setNotes(rs.getString("notes"));
			club.setCity(rs.getString("city"));
			club.setState(rs.getString("state"));
			club.setCountry(rs.getString("country"));
			return club;
		}
	}

	private class ClubMembershipMapper implements ParameterizedRowMapper<ClubMembership> {
		public ClubMembership mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClubMembership clubMembership = new ClubMembership();
			clubMembership.setSysUserId(rs.getLong("sysuser_id"));
			clubMembership.setClubId(rs.getLong("club_id"));
			clubMembership.setIsClubAdmin(rs.getBoolean("is_admin"));
			return clubMembership;
		}
	}

	@SuppressWarnings("unused")
	private class ClubMemberPositionMapper implements ParameterizedRowMapper<ClubMemberPosition> {
		public ClubMemberPosition mapRow(ResultSet rs, int rowNum) throws SQLException {
			ClubMemberPosition clubMemberPosition = new ClubMemberPosition();
			clubMemberPosition.setSysUserId(rs.getLong("sysuser_id"));
			clubMemberPosition.setClubId(rs.getLong("club_id"));
			clubMemberPosition.setPositionId(rs.getLong("position_id"));
			return clubMemberPosition;
		}
	}

	private class PortfolioPositionAssignmentMapper implements ParameterizedRowMapper<PortfolioPositionAssignment> {
		public PortfolioPositionAssignment mapRow(ResultSet rs, int rowNum) throws SQLException {
			PortfolioPositionAssignment portPosAsgn = new PortfolioPositionAssignment();
			portPosAsgn.setPositionId(rs.getLong("position_id"));
			portPosAsgn.setTicker(rs.getString("ticker"));
			portPosAsgn.setCompanyName(rs.getString("company_name"));
			portPosAsgn.setSysUserId(rs.getLong("sysuser_id"));
			return portPosAsgn;
		}
	}

	private class PortfolioMapper implements ParameterizedRowMapper<Portfolio> {
		public Portfolio mapRow(ResultSet rs, int rowNum) throws SQLException {
			Portfolio portfolio = new Portfolio();
			portfolio.setPortfolioId(rs.getLong("portfolio_id"));
			portfolio.setSysUserId(rs.getLong("sysuser_id"));
			portfolio.setClubId(rs.getLong("club_id"));
			portfolio.setPortfolioName(rs.getString("portfolio_name"));
			portfolio.setDescription(rs.getString("description"));
			portfolio.setNotes(rs.getString("notes"));
			return portfolio;
		}
	}

	private class PortfolioPositionMapper implements ParameterizedRowMapper<PortfolioPosition> {
		public PortfolioPosition mapRow(ResultSet rs, int rowNum) throws SQLException {
			PortfolioPosition position = new PortfolioPosition();
			position.setPositionId(rs.getLong("position_id"));
			position.setPortfolioId(rs.getLong("portfolio_id"));
			position.setTicker(rs.getString("ticker"));
			position.setCompanyName(rs.getString("company_name"));
			position.setPurchaseDate(rs.getDate("purchase_date"));
			position.setPurchaseQty(rs.getDouble("purchase_qty"));
			position.setPurchasePrice(rs.getDouble("purchase_price"));
			position.setStopLossPct(rs.getDouble("stop_loss_pct"));
			position.setCagrPct(rs.getDouble("cagr_pct"));
			position.setCagrGraceDays(rs.getInt("cagr_grace_days"));
			return position;
		}
	}

}
