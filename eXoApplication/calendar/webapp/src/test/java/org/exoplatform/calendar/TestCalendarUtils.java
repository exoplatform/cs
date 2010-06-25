package org.exoplatform.calendar;


import junit.framework.TestCase;

public class TestCalendarUtils extends TestCase {
  
  public void testGetLocationDisplayString() {
    assertEquals("", CalendarUtils.getLocationDisplayString(""));
    
    assertEquals("Viet nam", CalendarUtils.getLocationDisplayString("Viet nam"));
    
    String vietnamLocation = "Vietnam(Vietnamese)";
    String countryname = "VNM";
    
    assertEquals(vietnamLocation, CalendarUtils.getLocationDisplayString(countryname));
  }
  
  public void testGenerateTimeZoneLabel() {
    assertEquals("", CalendarUtils.generateTimeZoneLabel(""));
    
    assertEquals("Viet nam", CalendarUtils.generateTimeZoneLabel("Viet nam"));
    
    String vietnamTimezone = "Asia/Ho_Chi_Minh";
    String display = "(GMT +07:00) Asia/Ho_Chi_Minh";
    
    assertEquals(display, CalendarUtils.generateTimeZoneLabel(vietnamTimezone));
  }
}
