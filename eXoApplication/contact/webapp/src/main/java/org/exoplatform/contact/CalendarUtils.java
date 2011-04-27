/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.core.model.SelectItemOption;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarUtils {
  final public static String PRIVATE_CALENDARS = "privateCalendar".intern() ;
  final public static String SHARED_CALENDARS = "sharedCalendar".intern() ;
  final public static String PUBLIC_CALENDARS = "publicCalendar".intern() ;
  
  public static final String PRIVATE_TYPE = "0".intern() ;
  public static final String SHARED_TYPE = "1".intern() ;
  public static final String PUBLIC_TYPE = "2".intern() ;
  public static final String ANY = "*.*".intern();
  public static final String ANY_OF = "*.".intern();
  public static final String DOT = ".".intern();
  final public static String SEMICOLON = ";".intern() ;
  final public static String COLON = ",".intern() ;
  final public static String COMMA = ",".intern() ;
  public static final String COLON_SLASH = ":/".intern() ;
  public static final String SLASH_COLON = "/:".intern() ;
  final public static String STAR = "*".intern() ;
  final public static String UNDERSCORE = "_".intern() ;
  final public static String DOUBLESCORE = "--".intern() ;
  public static final int DENY = 0 ;
  public static final int ACCEPT = 1 ;
  public static final int NOTSURE = 2 ;
  
  final public static String TIMEFORMAT  = "HH:mm".intern() ;
  final public static String DATEFORMAT = "MM/dd/yyyy".intern() ;
  final public static String DATETIMEFORMAT = DATEFORMAT + " " +TIMEFORMAT ;   
  final public static int DEFAULT_TIMEITERVAL = 15 ;
  final public static long MILISECONS_OF_DAY = 24*60*60*1000 ;
  public static final String SLASH = "/".intern();
  public static final String BACKSLASH = "\\".intern();
  public static final String GREATER_THAN = ">".intern() ;
  public static final String SMALLER_THAN = "<".intern() ;
  public static final String EXTENDEDCHARACTER[] = {SEMICOLON,COMMA,SLASH,BACKSLASH,"'","|",GREATER_THAN,SMALLER_THAN,"\"", "?", "!", "@", "#", "$", "%","^","&","*","+","]","["};
  public static final String SIMPLECHARACTER[] = {GREATER_THAN,SMALLER_THAN};
  
  static public String[] getUserGroups(String username) throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
  static public String[] getAllGroups() throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().getAllGroups().toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
  public static boolean isEmpty(String value) {
    return (value == null || value.trim().length() == 0) ;
  }

  static public CalendarService getCalendarService() throws Exception {
    return (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
  }
  static public OrganizationService getOrganizationService() throws Exception {
    return (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getBeginDay(GregorianCalendar.getInstance()) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
    int time = 0 ;
    while (time ++ < 24*60/(15)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, 15) ;
    }
    return options ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat, int timeInteval) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getBeginDay(GregorianCalendar.getInstance()) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInteval)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, timeInteval) ;
    }
    return options ;
  }
  public static String parse(Date date, String timeFormat) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    return df.format(date) ;    
  }

  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }

  public static boolean isAllDayEvent(CalendarEvent eventCalendar) {
    Calendar cal1 = new GregorianCalendar() ;
    Calendar cal2 = new GregorianCalendar() ;
    cal1.setTime(eventCalendar.getFromDateTime()) ;
    cal2.setTime(eventCalendar.getToDateTime()) ;
    return (cal1.get(Calendar.HOUR_OF_DAY) == 0  && 
        cal1.get(Calendar.MINUTE) == 0 &&
        cal2.get(Calendar.HOUR_OF_DAY) == 0 && 
        cal2.get(Calendar.MINUTE) == 0 );
  }

  public static boolean isSameDate(java.util.Calendar date1, java.util.Calendar date2) {
    return ( date1.get(java.util.Calendar.DATE) == date2.get(java.util.Calendar.DATE) &&
        date1.get(java.util.Calendar.MONTH) == date2.get(java.util.Calendar.MONTH) &&
        date1.get(java.util.Calendar.YEAR) == date2.get(java.util.Calendar.YEAR)
    ) ;
  }
  public static boolean isSameDate(Date value1, Date value2) {
    Calendar date1 = GregorianCalendar.getInstance() ;
    date1.setTime(value1) ;
    Calendar date2 = GregorianCalendar.getInstance() ;
    date2.setTime(value2) ;
    return isSameDate(date1, date2) ;
  }

  public static Calendar getBeginDay(Calendar cal) {
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.SECOND, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    return cal ;
  }
  public static Calendar getEndDay(Calendar cal)  {
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.SECOND, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    cal.add(Calendar.HOUR_OF_DAY, 24) ;
    return cal ;
  }

  public static Calendar getBeginDay(Date date) {
    Calendar cal = GregorianCalendar.getInstance() ;
    cal.setTime(date) ;
    return getBeginDay(cal) ;
  }
  public static Calendar getEndDay(Date date)  {
    Calendar cal = getInstanceTempCalendar() ;
    cal.setTime(date) ;
    return getEndDay(cal) ;
  }
  public static String getImageSource(Attachment attach, DownloadService dservice) throws Exception {      
    if (attach != null) {
      InputStream input = attach.getInputStream() ;
      byte[] imageBytes = null ;
      if (input != null) {
        imageBytes = new byte[input.available()] ;
        input.read(imageBytes) ;
        ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
        dresource.setDownloadName(attach.getName()) ;
        return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
      }
    }
    return null ;
  }
  
  public static List<SelectItemOption<String>> getCalendarOption() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    options.add(new SelectItemOption<String>(CalendarUtils.PRIVATE_CALENDARS, "")) ;
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.PRIVATE_TYPE + CalendarUtils.COLON + c.getId())) ;
    }
    GroupCalendarData gcd = calendarService.getSharedCalendars(username, true);
    if(gcd != null) {
      options.add(new SelectItemOption<String>(CalendarUtils.SHARED_CALENDARS, "")) ;
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, c.getEditPermission(), username)){
          options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE  + c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
        }
      }
    }
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null) {
      OrganizationService oService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      options.add(new SelectItemOption<String>(CalendarUtils.PUBLIC_CALENDARS, "")) ;
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(CalendarUtils.canEdit(oService, c.getEditPermission(), username)){
            options.add(new SelectItemOption<String>(CalendarUtils.DOUBLESCORE + c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }

      }
    }
    return options ;
  }
  
  public static boolean hasEditPermission(String[] savePerms, String[] checkPerms) {
    if(savePerms != null)
      for(String sp : savePerms) {
        for (String cp : checkPerms) {
          if(sp.equals(cp)) {return true ;}      
        }
      }
    return false ;
  } 

  @SuppressWarnings("unchecked")
 public static boolean canEdit(OrganizationService oService, String[] savePerms, String username) throws Exception {
    StringBuffer sb = new StringBuffer(username) ;
    if(oService != null) {
      Collection<Membership> memberShipsType = oService.getMembershipHandler().findMembershipsByUser(username) ;
      Collection<Group> groups = oService.getGroupHandler().findGroupsOfUser(username) ;
      for(Group g : groups) {
        sb.append(CalendarUtils.COMMA).append(g.getId()).append(SLASH_COLON).append(ANY) ;
        sb.append(CalendarUtils.COMMA).append(g.getId()).append(SLASH_COLON).append(username) ;
        for(Membership mp : memberShipsType) {
          sb.append(CalendarUtils.COMMA).append(g.getId()).append(SLASH_COLON).append(ANY_OF + mp.getMembershipType()) ;
        }
      }
    }
    return CalendarUtils.hasEditPermission(savePerms, sb.toString().split(CalendarUtils.COMMA)) ;
  }
  public static boolean isNameValid(String name, String[] regexpression) {
    for(String c : regexpression){ if(name.contains(c)) return false ;}
    return true ;
  }
  
  public static boolean isValidEmailAddresses(String value) {
    if (isEmpty(value)) return true ;
    value = StringUtils.remove(value, " ");
    value = StringUtils.replace(value, SEMICOLON, COMMA);
    try {
      InternetAddress[] iAdds = InternetAddress.parse(value, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,6}" ;
      for (int i = 0 ; i < iAdds.length; i ++) {
        if(!iAdds[i].getAddress().matches(emailRegex)) return false;
      }
    } catch(AddressException e) {
      return false ;
    }
    return true ;
  }
  
  public static Calendar getInstanceTempCalendar() { 
    Calendar  calendar = GregorianCalendar.getInstance() ;
    calendar.setLenient(false) ;
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ; 
    return  calendar;
  }
}
