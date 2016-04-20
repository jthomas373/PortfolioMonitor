package com.ccllc.PortfolioMonitor.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.Club;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:**/portfolioMonitorTest-context.xml")
public class MySQLDaoTests {

	@Autowired
	private MonitorDao monitorDao;

	@Test
	public void testDaoReady() {
		assertNotNull(monitorDao);
	}
	
	@Test
	public void testGetAllAuthorities() {
		List<Authority> authList = monitorDao.getAllAuthorities();
		long count = authList.size();
		assertEquals(count, 3);
	}
	
	@Test
	public void testGetAllClubs() {
		List<Club> clubList = monitorDao.getAllClubs();
		long count = clubList.size();
		assertEquals(count, 1);
	}

}
