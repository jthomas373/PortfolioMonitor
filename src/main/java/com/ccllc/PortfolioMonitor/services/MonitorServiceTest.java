package com.ccllc.PortfolioMonitor.services;

//import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class MonitorServiceTest {
	
	//static MonitorService monitorService = new MonitorServiceImpl();
	//static ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
	static ApplicationContext ctx = new FileSystemXmlApplicationContext("/WebRoot/WEB-INF/applicationContext.xml");
	static MonitorService monitorService = (MonitorService) ctx.getBean("monitorService");

	public static void main(String[] args) {
		
		//SimpleDateFormat yahooDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			/*
			testGetDayData("AAPL", yahooDateFormat.parse("2009-06-15"));
			testGetDailyData(
					"ACAS", 
					yahooDateFormat.parse("2009-06-01"),
					yahooDateFormat.parse("2009-06-15"));
			testGetWeeklyData(
					"MON", 
					yahooDateFormat.parse("2009-01-01"),
					yahooDateFormat.parse("2009-06-15"));
			testGetMonthlyData(
					"CLB", 
					yahooDateFormat.parse("2009-01-01"),
					yahooDateFormat.parse("2009-06-15"));
			*/
			System.out.println();
			//testInitializeDatabase();
			
			// Test DAOs
			// Insert Position
			/*
			Position position = new Position();
			position.setTicker("MON");
			position.setCompanyName("Monsanto");
			position.setPurchaseDate(yahooDateFormat.parse("2009-03-05"));
			position.setPurchaseQty(35);
			position.setPurchasePrice(73.22);
			position.setStopLossPct(15.00);
			position.setCagrPct(15.00);
			position.setCagrGraceDays(90);
			
			System.out.println("Testing Insert Position");
			testInsertPosition(position);
			List<Position> positionList = testGetAllPositions();
			position.setId(positionList.get(0).getId());
			
			System.out.println("Testing Update Position");
			position.setPurchaseQty(100);
			testUpdatePosition(position);
			Position updatedPosition = testGetPositionById(position.getId());
			
			System.out.println("Testing Delete Position");
			testDeletePosition(updatedPosition.getId());
			positionList = testGetAllPositions();
			*/
			
			//List<Position> positionList = testGetAllPositions();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unused")
	private static void testGetDayData (String ticker, Date date) {
		//Date today = new Date();
		monitorService.getDayData(ticker, date);
	}

	@SuppressWarnings("unused")
	private static void testGetDailyData (String ticker, Date startDate, Date endDate) {
		monitorService.getDailyData(ticker, startDate, endDate);
	}

	@SuppressWarnings("unused")
	private static void testGetWeeklyData (String ticker, Date startDate, Date endDate) {
		monitorService.getWeeklyData(ticker, startDate, endDate);
	}

	@SuppressWarnings("unused")
	private static void testGetMonthlyData (String ticker, Date startDate, Date endDate) {
		monitorService.getMonthlyData(ticker, startDate, endDate);
	}

	/*
	private static void testInsertPosition(Position position) {
		monitorService.insertPosition(position);
	}
	
	private static List<Position> testGetAllPositions() {
		List<Position> positionList = monitorService.getAllPositions();
		for(Position position : positionList) {
			System.out.println("Position ID=" + position.getId() + "; Ticker=" + position.getTicker() + "; Shares Purchased=" + position.getPurchaseQty());
		}
		return positionList;
	}

	private static Position testGetPositionById(Long positionId) {
		Position position = monitorService.getPositionById(positionId);
		System.out.println("Position ID=" + position.getId() + "; Ticker=" + position.getTicker() + "; Shares Purchased=" + position.getPurchaseQty());
		
		return position;
	}

	private static void testDeletePosition(Long positionId) {
		monitorService.deletePosition(positionId);
	}

	private static void testUpdatePosition(Position position) {
		Position updatedPosition = monitorService.updatePosition(position);
		System.out.println("The updated shared purchased = " + updatedPosition.getPurchaseQty());
	}
	*/
}
