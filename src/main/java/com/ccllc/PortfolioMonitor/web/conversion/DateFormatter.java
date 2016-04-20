package com.ccllc.PortfolioMonitor.web.conversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.format.Formatter;

public class DateFormatter implements Formatter<Date> {
	
	public DateFormatter() {
		super();
	}
	
	public Date parse(final String text, final Locale locale) throws ParseException {
		
		final SimpleDateFormat dateFormat = createDateFormat();
		return dateFormat.parse(text);
	}
	
	public String print(final Date object, final Locale locale) {
		
		final SimpleDateFormat dateFormat = createDateFormat();
		return dateFormat.format(object);
	}
	
	private SimpleDateFormat createDateFormat() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		return dateFormat;
	}

}
