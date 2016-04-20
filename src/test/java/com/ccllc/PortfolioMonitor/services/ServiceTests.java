package com.ccllc.PortfolioMonitor.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ccllc.PortfolioMonitor.domain.Authority;
import com.ccllc.PortfolioMonitor.domain.Club;
import com.ccllc.PortfolioMonitor.services.MonitorService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:**/portfolioMonitorTest-context.xml")
public class ServiceTests {

	@Autowired
	MonitorService monitorService;
	
	@Test
	public void testServiceReady() {
		assertNotNull(monitorService);
	}
	
	@Test
	public void testGetAllAuthorities() {
		List<Authority> authList = monitorService.getAllAuthorities();
		long count = authList.size();
		assertEquals(count, 3);
	}
	
	@Test
	public void testGetAllClubs() {
		List<Club> clubList = monitorService.getAllClubs();
		long count = clubList.size();
		assertEquals(count, 1);
	}

}
