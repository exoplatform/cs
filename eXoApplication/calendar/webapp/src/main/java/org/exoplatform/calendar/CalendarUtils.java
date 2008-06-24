/**
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
 **/
package org.exoplatform.calendar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.jcr.PathNotFoundException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.SelectItem;
import org.exoplatform.calendar.webui.SelectItemOptionGroup;
import org.exoplatform.calendar.webui.popup.UIAddressForm.ContactData;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public class CalendarUtils {

  public static final String PRIVATE_CALENDARS = "privateCalendar".intern();
  public static final String SHARED_CALENDARS = "sharedCalendar".intern();
  public static final String PUBLIC_CALENDARS = "publicCalendar".intern();
  public static final String PRIVATE_TYPE = "0".intern();
  public static final String SHARED_TYPE = "1".intern();
  public static final String PUBLIC_TYPE = "2".intern();
  public static final String SEMICOLON = ";".intern();
  public static final String COLON = ":".intern();
  public static final String COMMA = ",".intern();
  public static final String STAR = "*".intern();
  public static final String SLASH = "/".intern();
  public static final String BACKSLASH = "\\".intern();
  public static final String DOUBLESCORE = "--".intern();
  public static final String UNDERSCORE = "_".intern();
  public static final String SLASH_COLON = "/:".intern() ;
  public static final String COLON_SLASH = ":/".intern() ;
  public static final String ANY = "*.*".intern();
  public static final String ANY_OF = "*.".intern();
  public static final String DOT = ".".intern();
  public static final String TIMEFORMAT  = "HH:mm".intern();
  public static final String DATEFORMAT = "MM/dd/yyyy".intern();
  public static final String DATETIMEFORMAT = DATEFORMAT +" " +TIMEFORMAT;   
  public static final int DEFAULT_TIMEITERVAL = 15;
  public static final long MILISECONS_OF_DAY = 24*60*59*1000;
  public static final String EXO_INVITATION = "X-Exo-Invitation".intern();
  public static final String SPECIALCHARACTER[] = {SEMICOLON,COMMA,SLASH,BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"};

  public static final String[] getUserGroups(String username) throws Exception {
    OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
    String[] groups = new String[objs.length] ;
    for(int i = 0; i < objs.length ; i ++) {
      groups[i] = ((GroupImpl)objs[i]).getId() ;
    }
    return groups ;
  }
  static public String[] getAllGroups() throws Exception {
    Object[] objs = getOrganizationService().getGroupHandler().getAllGroups().toArray() ;
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
  public static Calendar getInstanceTempCalendar() { 
    Calendar  calendar = GregorianCalendar.getInstance() ;
    calendar.setLenient(false) ;
    /* try {
      CalendarSetting setting = getCalendarService().getCalendarSetting(SessionsUtils.getSessionProvider(), getCurrentUser()) ;
      calendar.setTimeZone(TimeZone.getTimeZone(setting.getTimeZone())) ; 
    } catch (Exception e) {
      e.printStackTrace() ;
    }*/
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ; 
    return  calendar;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(cal) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT, locale) ;
    df.setCalendar(cal) ;
    int time = 0 ;
    while (time ++ < 24*60/(15)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, 15) ;
    }
    return options ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.DST_OFFSET, 0) ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat dfLabel = new SimpleDateFormat(labelFormat, locale) ;
    dfLabel.setCalendar(cal) ;
    DateFormat dfValue = new SimpleDateFormat(valueFormat, locale) ;
    dfValue.setCalendar(cal) ;
    int time = 0 ;
    while (time ++ < 24*60/(15)) {
      options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, 15) ;
    }
    return options ;
  }

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.DST_OFFSET, 0) ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat dfLabel = new SimpleDateFormat(labelFormat, locale) ;
    dfLabel.setCalendar(cal) ;
    DateFormat dfValue = new SimpleDateFormat(valueFormat, locale) ;
    dfValue.setCalendar(cal) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInteval)) {
      options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, (int)timeInteval) ;
    }
    return options ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String labelFormat, String valueFormat, long timeInteval, Locale locale) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.DST_OFFSET, 0) ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;

    DateFormat dfLabel = new SimpleDateFormat(labelFormat, locale) ;
    dfLabel.setCalendar(cal) ;
    DateFormat dfValue = new SimpleDateFormat(valueFormat, locale) ;
    dfValue.setCalendar(cal) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInteval)) {
      options.add(new SelectItemOption<String>(dfLabel.format(cal.getTime()), dfValue.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, (int)timeInteval) ;
    }
    return options ;
  }
  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat, int timeInteval) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.HOUR_OF_DAY, 0) ;
    cal.set(Calendar.MINUTE, 0) ;
    cal.set(Calendar.MILLISECOND, 0) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    Locale locale = context.getParentAppRequestContext().getLocale() ;
    DateFormat df = new SimpleDateFormat(timeFormat, locale) ;
    df.setCalendar(cal) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT, locale) ;
    df2.setCalendar(cal) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInteval)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, timeInteval) ;
    }
    return options ;
  }

  public static List<SelectItemOption<String>> getTimeZoneSelectBoxOptions(String[] timeZoneIds) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for (String tz : timeZoneIds){
      if(tz.lastIndexOf("/") > 0 && tz.toLowerCase().lastIndexOf("etc".toLowerCase()) < 0 && tz.toLowerCase().lastIndexOf("system") < 0) {
        TimeZone timeZone = TimeZone.getTimeZone(tz) ;
        int rawOffset = timeZone.getRawOffset() / 60000;
        int hours = rawOffset / 60;
        int minutes = Math.abs(rawOffset) % 60;
        String hrStr = "";
        if (Math.abs(hours) < 10) {
          if (hours < 0) {
            hrStr = "-0" + Math.abs(hours);
          } else {
            hrStr = "0" + Math.abs(hours);
          }
        } else {
          hrStr = Integer.toString(hours);
        }
        String minStr = (minutes < 10) ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
        String str = "(GMT " + ((timeZone.getRawOffset() >= 0) ? "+" : "") 
        + hrStr + ":" + minStr + ") " + tz ;
        //subZoneMap.put(tz,  str) ;
        //System.out.println("\n\n str "+ str + " saving " + timeZone.getDSTSavings() +" raw off " +  timeZone.getRawOffset()); 
        options.add(new SelectItemOption<String>(str, tz)) ;
      } 
    }
    return options ;
  }
  @SuppressWarnings("unchecked")
  public static List<SelectItemOption<String>> getLocaleSelectBoxOptions(Locale[] locale) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(Locale local :  locale) {
      String country = local.getISO3Country() ;
      if( country != null && country.trim().length() > 0)  options.add(new SelectItemOption<String>(local.getDisplayCountry() + "(" +local.getDisplayLanguage()+")" ,country)) ;
    }  
    Collections.sort(options, new SelectComparator()) ;
    return options ;
  }
  public static String parse(Date date, String timeFormat) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    return df.format(date) ;    
  }
  public static String parse(Date date, String timeFormat, Locale locale) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat, locale) ;
    return df.format(date) ;    
  }
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }

  public static boolean isAllDayEvent(CalendarEvent eventCalendar) {
    Calendar cal1 = getInstanceTempCalendar() ;
    Calendar cal2 = getInstanceTempCalendar() ;
    cal1.setTime(eventCalendar.getFromDateTime()) ;
    cal2.setTime(eventCalendar.getToDateTime()) ;
    return (cal1.get(Calendar.HOUR_OF_DAY) == 0  && 
        cal1.get(Calendar.MINUTE) == 0 &&
        cal2.get(Calendar.HOUR_OF_DAY) == cal2.getActualMaximum(Calendar.HOUR_OF_DAY)&& 
        cal2.get(Calendar.MINUTE) == cal2.getActualMaximum(Calendar.MINUTE) );
  }

  public static boolean isSameDate(java.util.Calendar date1, java.util.Calendar date2) {
    return ( date1.get(java.util.Calendar.DATE) == date2.get(java.util.Calendar.DATE) &&
        date1.get(java.util.Calendar.MONTH) == date2.get(java.util.Calendar.MONTH) &&
        date1.get(java.util.Calendar.YEAR) == date2.get(java.util.Calendar.YEAR)
    ) ;
  }
  public static boolean isSameDate(Date value1, Date value2) {
    Calendar date1 = getInstanceTempCalendar() ;
    date1.setTime(value1) ;
    Calendar date2 = getInstanceTempCalendar() ;
    date2.setTime(value2) ;
    return isSameDate(date1, date2) ;
  }

  public static Calendar getBeginDay(Calendar cal) {
    Calendar newCal = new GregorianCalendar() ;
    newCal.setTime(cal.getTime()) ;
    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
    return newCal ;
  }
  public static Calendar getEndDay(Calendar cal)  {
    Calendar newCal = new GregorianCalendar() ;
    newCal.setTime(cal.getTime()) ;
    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
    newCal.add(Calendar.HOUR_OF_DAY, 24) ;
    return newCal ;
  }

  public static Calendar getBeginDay(Date date) {
    Calendar cal = getInstanceTempCalendar() ;
    cal.setTime(date) ;
    return getBeginDay(cal) ;
  }
  public static Calendar getEndDay(Date date)  {
    Calendar cal = getInstanceTempCalendar() ;
    cal.setTime(date) ;
    return getEndDay(cal) ;
  }


  public static String getDataSource(Attachment attach, DownloadService dservice) throws Exception {      
    if (attach != null) {
      try {
        InputStream input = attach.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, attach.getMimeType() ) ;
          dresource.setDownloadName(attach.getName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        } 
      } catch (PathNotFoundException ex) {
        ex.printStackTrace() ;
        return null ;
      }
    }
    return null ;
  }

  public static boolean isNameValid(String name, String[] regexpression) {
    for(String c : regexpression){ if(name.contains(c)) return false ;}
    return true ;
  }

  public static boolean isNameEmpty(String name) {
    return (name == null || name.trim().length() == 0) ;
  }

  public static String getServerBaseUrl() {
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String url = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) 
    + "/" ;
    return url ;
  }
  static public String getTimeZone(String timezone) {
    TimeZone timeZone = TimeZone.getTimeZone(timezone) ;
    int rawOffset = timeZone.getRawOffset()  ;
    return String.valueOf(0 - (rawOffset /60000 + timeZone.getDSTSavings()/60000)) ;
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
  public static boolean isMemberShipType(Collection<Membership> mbsh, String value) {
    if(!isEmpty(value))
      for (String check : value.split(COMMA)) { 
        check = check.trim() ;
        if(check.lastIndexOf(ANY_OF) > -1) {
          if(ANY.equals(check)) return true ;
          value = check.substring(check.lastIndexOf(ANY_OF) + ANY_OF.length()) ;
          if(mbsh!= null && !mbsh.isEmpty()) {
            for(Membership mb : mbsh) {
              if(mb.getMembershipType().equals(value)) return true ; 
            }
          }
        }
      }
    return false ;
  }
  static public class SelectComparator implements Comparator{
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((SelectItemOption) o1).getLabel() ;
      String name2 = ((SelectItemOption) o2).getLabel() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  static public class ContactComparator implements Comparator{
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((ContactData) o1).getFullName() ;
      String name2 = ((ContactData) o2).getFullName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  public static List<SelectItem> getCalendarOption() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    /*
     * Modified by Philippe (philippe.aristote@gmail.com)
     * Uses SelectItemOptionGroup to differienciate private, shared and public groups
     */

    // private calendars group
    SelectItemOptionGroup privGrp = new SelectItemOptionGroup(CalendarUtils.PRIVATE_CALENDARS);
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      privGrp.addOption(new org.exoplatform.calendar.webui.SelectItemOption<String>(c.getName(), CalendarUtils.PRIVATE_TYPE + CalendarUtils.COLON + c.getId())) ;
    }
    if(privGrp.getOptions().size() > 0) options.add(privGrp);
    // shared calendars group
    GroupCalendarData gcd = calendarService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true);
    if(gcd != null) {
      SelectItemOptionGroup sharedGrp = new SelectItemOptionGroup(CalendarUtils.SHARED_CALENDARS);
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, c.getEditPermission(), username)){
          String owner = "" ;
          if(c.getCalendarOwner() != null) owner = c.getCalendarOwner() + "- " ;
          sharedGrp.addOption(new org.exoplatform.calendar.webui.SelectItemOption<String>(owner + c.getName(), CalendarUtils.SHARED_TYPE + CalendarUtils.COLON + c.getId())) ;
        }
      }
      if(sharedGrp.getOptions().size() > 0) options.add(sharedGrp);
    }
    // public calendars group
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(SessionProviderFactory.createSystemProvider(), CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null) {
      OrganizationService oService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      SelectItemOptionGroup pubGrp = new SelectItemOptionGroup(CalendarUtils.PUBLIC_CALENDARS);
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(CalendarUtils.canEdit(oService, c.getEditPermission(), username)){
            pubGrp.addOption(new org.exoplatform.calendar.webui.SelectItemOption<String>(c.getName(), CalendarUtils.PUBLIC_TYPE + CalendarUtils.COLON + c.getId())) ;
          }
        }

      }
      if(pubGrp.getOptions().size() > 0)  options.add(pubGrp);
    }
    return options ;
  }

  public static List<org.exoplatform.calendar.service.Calendar> getCalendars() throws Exception {
    List<org.exoplatform.calendar.service.Calendar> list = new ArrayList<org.exoplatform.calendar.service.Calendar>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<org.exoplatform.calendar.service.Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(org.exoplatform.calendar.service.Calendar c : calendars) {
      list.add(c) ;
    }
    GroupCalendarData gcd = calendarService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true);
    if(gcd != null) {
      for(org.exoplatform.calendar.service.Calendar c : gcd.getCalendars()) {
        if(CalendarUtils.canEdit(null, c.getEditPermission(), username)){
          list.add(c) ;
        }
      }
    }
    List<GroupCalendarData> lgcd = calendarService.getGroupCalendars(SessionProviderFactory.createSystemProvider(), CalendarUtils.getUserGroups(username), false, username) ;
    if(lgcd != null) {
      OrganizationService oService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      for(GroupCalendarData g : lgcd) {
        for(org.exoplatform.calendar.service.Calendar c : g.getCalendars()){
          if(CalendarUtils.canEdit(oService, c.getEditPermission(), username)){
            list.add(c) ; 
          }
        }

      }
    }
    return list ;
  }

  public static String encodeJCRText(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
    replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
  }

  public static String encodeHTML(String htmlContent) throws Exception {
    return htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
  }

  static public MailService getMailService() throws Exception {
    return (MailService)PortalContainer.getComponent(MailService.class) ;
  }

  public static String convertSize(long size) throws Exception {
    String str = "";
    DecimalFormat df = new DecimalFormat("0.00");
    if (size > 1024 * 1024) str += df.format(((double) size)/(1024 * 1024)) + " MB" ;
    else if (size > 1024) str += df.format(((double) size)/(1024)) + " KB" ;
    else str += size + " B" ;
    return str ;
  }

  public static boolean isValidEmailAddresses(String addressList) {
    boolean isInvalid = true ;
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
      for (int i = 0 ; i < iAdds.length; i ++) {
        if(!iAdds[i].getAddress().toString().matches(emailRegex)) isInvalid = false;
      }
    } catch(AddressException e) {
      return false ;
    }
    return isInvalid ;
  }

  public static String parseEmailAddress(String address) {
    try {
      InternetAddress[] iAdds = InternetAddress.parse(address, true);
      return iAdds[0].getAddress() ;
    }catch (Exception e) {
      e.printStackTrace() ;
      return null ;
    }
  }
  public static boolean isEmailValid(String value) {
    String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
    return (value!= null && value.trim().length() > 0 && value.trim().matches(emailRegex)) ;
  }
  public static boolean isAllEmailValid(String addressList) {
    boolean isValid = true ;
    if(CalendarUtils.isEmpty(addressList)) return false ;
    for(String s : addressList.split(CalendarUtils.COMMA)) {
      s = s.trim() ;
      if(!isEmailValid(s)) isValid = false ;
      break ;
    }
    return isValid  ;
  }
  public static boolean isUserExisted(OrganizationService orgSevice, String value) {
    try {
      return (!isEmpty(value) && orgSevice.getUserHandler().findUserByName(value) != null) ;
    } catch( Exception e) {
      e.printStackTrace() ;
      return false ;
    }
  }
}
