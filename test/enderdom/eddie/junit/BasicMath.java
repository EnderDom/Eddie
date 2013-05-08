package enderdom.eddie.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import enderdom.eddie.tools.Tools_System;

public class BasicMath {

	
	@SuppressWarnings("unused")
	@Test
	public void testMyMath(){
		//1 day
		int day = 1;
		long dayaslong = 1*24*60*60*1000;
		//4 hours
		int hour = 4;
		long houraslong = 4*60*60*1000;
		//30 minutes
		int minute = 30;
		long minuteaslong = 30*60*1000;
		
		int seconds = 53;
		long secondaslong = 53*1000;
		
		int millseconds = 12;
		long millisaslong = 12;
		
		long value = dayaslong+houraslong+minuteaslong+secondaslong+millisaslong;
		System.out.println(Tools_System.long2DayHourMin(value));
		assertEquals("1days 4hours 30mins",Tools_System.long2DayHourMin(value));
	}
}
