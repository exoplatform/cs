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
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Jan 07, 2008  
 */

@ComponentConfig(
    template = "app:/templates/calendar/webui/UIPopup/UICalendarSettingTab.gtmpl"
) 
public class UICalendarSettingTab extends UIFormInputWithActions {
  final private static String VIEW_TYPE = "viewType".intern() ;
  final private static String TIME_INTERVAL = "timeInterval".intern() ;
  final private static String WEEK_START_ON = "weekStartOn".intern() ;
  final private static String DATE_FORMAT = "dateFormat".intern() ;
  final private static String TIME_FORMAT = "timeFormat".intern() ;
  final private static String LOCATION = "location".intern() ;
  final private static String TIMEZONE = "timeZone".intern() ;
  final private static String ISSHOWWORKINGTIME = "showWorkingTime".intern() ;
  final public static String WORKINGTIME_BEGIN = "beginTime".intern() ;
  final public static String WORKINGTIME_END = "endTime".intern() ;
  final private static String BASE_URL = "baseURL".intern() ;
  
  
  public UICalendarSettingTab(String compId) throws Exception {
    super(compId);
    setComponentConfig(getClass(), null) ;
    List<SelectItemOption<String>> viewTypes = new ArrayList<SelectItemOption<String>>() ;
    viewTypes.add(new SelectItemOption<String>("Day view", CalendarSetting.DAY_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Week view", CalendarSetting.WEEK_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Month view", CalendarSetting.MONTH_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Year view", CalendarSetting.YEAR_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("List view", CalendarSetting.LIST_VIEW)) ;
    viewTypes.add(new SelectItemOption<String>("Schedule view", CalendarSetting.SCHEDULE_VIEW)) ;
    
    addUIFormInput(new UIFormSelectBox(VIEW_TYPE, VIEW_TYPE, viewTypes)) ;

    List<SelectItemOption<String>> timeInterval = new ArrayList<SelectItemOption<String>>() ;
    int i = 5 ;
    while(i < 121) {
      timeInterval.add(new SelectItemOption<String>(i + " minutes", String.valueOf(i))) ;
      i += 5;
    }
    addUIFormInput(new UIFormSelectBox(TIME_INTERVAL, TIME_INTERVAL, timeInterval)) ;

    List<SelectItemOption<String>> weekStartOn = new ArrayList<SelectItemOption<String>>() ;
    DateFormatSymbols dfs = new DateFormatSymbols() ;  ;
    for(int id =1 ;id<  dfs.getWeekdays().length; id++) {
      weekStartOn.add(new SelectItemOption<String>(dfs.getWeekdays()[id], String.valueOf(id))) ;
    }

    addUIFormInput(new UIFormSelectBox(WEEK_START_ON, WEEK_START_ON, weekStartOn)) ;

    List<SelectItemOption<String>> dateFormat = new ArrayList<SelectItemOption<String>>() ;
    dateFormat.add(new SelectItemOption<String>("dd/mm/yyyy", "dd/MM/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("dd-mm-yyyy", "dd-MM-yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm/dd/yyyy", "MM/dd/yyyy")) ;
    dateFormat.add(new SelectItemOption<String>("mm-dd-yyyy", "MM-dd-yyyy")) ;
    addUIFormInput(new UIFormSelectBox(DATE_FORMAT, DATE_FORMAT, dateFormat)) ;

    List<SelectItemOption<String>> timeFormat = new ArrayList<SelectItemOption<String>>() ;
    timeFormat.add(new SelectItemOption<String>("AM/PM", "hh:mm a")) ;
    timeFormat.add(new SelectItemOption<String>("24 Hours", "HH:mm")) ;

    addUIFormInput(new UIFormSelectBox(TIME_FORMAT, TIME_FORMAT, timeFormat)) ;
    addUIFormInput(new UIFormSelectBox(LOCATION, LOCATION, getLocales())) ;
    addUIFormInput(new UIFormSelectBox(TIMEZONE, TIMEZONE, getTimeZones())) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(ISSHOWWORKINGTIME, ISSHOWWORKINGTIME, false)) ;
    List<SelectItemOption<String>> startTimes = new ArrayList<SelectItemOption<String>>() ;
    List<SelectItemOption<String>> endTimes = CalendarUtils.getTimesSelectBoxOptions(CalendarUtils.TIMEFORMAT, 30) ;
    addUIFormInput(new UIFormSelectBox(WORKINGTIME_BEGIN, WORKINGTIME_BEGIN, startTimes)) ;
    addUIFormInput(new UIFormSelectBox(WORKINGTIME_END, WORKINGTIME_END, endTimes)) ;

    addUIFormInput(new UIFormStringInput(BASE_URL, BASE_URL, null)) ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  protected String getViewType() {
    return getUIFormSelectBox(VIEW_TYPE).getValue() ;
  }
  protected void setViewType(String value) {
    getUIFormSelectBox(VIEW_TYPE).setValue(value) ;
  }
  protected String getTimeInterval() {
    return getUIFormSelectBox(TIME_INTERVAL).getValue() ;
  }
  protected void setTimeInterval(String value) {
    getUIFormSelectBox(TIME_INTERVAL).setValue(value) ;
  }
  protected String getWeekStartOn() {
    return getUIFormSelectBox(WEEK_START_ON).getValue() ;
  }
  protected void setWeekStartOn(String value) {
    getUIFormSelectBox(WEEK_START_ON).setValue(value) ;
  }
  protected String getDateFormat() {
    return getUIFormSelectBox(DATE_FORMAT).getValue() ;
  }
  protected void setDateFormat(String value) {
    getUIFormSelectBox(DATE_FORMAT).setValue(value) ;
  }
  protected String getTimeFormat() {
    return getUIFormSelectBox(TIME_FORMAT).getValue() ;
  }
  protected void setTimeFormat(String value) {
    getUIFormSelectBox(TIME_FORMAT).setValue(value) ;
  }
  protected String getLocale() {
    return getUIFormSelectBox(LOCATION).getValue() ;
  }
  protected void setLocale(String value) {
    getUIFormSelectBox(LOCATION).setValue(value) ;
  }
  protected String getTimeZone() {
    return getUIFormSelectBox(TIMEZONE).getValue() ;
  }
  protected void setTimeZone(String value) {
    getUIFormSelectBox(TIMEZONE).setValue(value) ;
  }
  protected boolean getShowWorkingTimes() {
    return getUIFormCheckBoxInput(ISSHOWWORKINGTIME).isChecked() ;
  }
  protected void setShowWorkingTimes(boolean value) {
    getUIFormCheckBoxInput(ISSHOWWORKINGTIME).setChecked(value) ;
  }
  protected String getWorkingBegin() throws Exception {
    java.util.Calendar cal = GregorianCalendar.getInstance() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(getTimeFormat()) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return timeFormat.format(cal.getTime()) ;
  }

  protected Date getWorkingBeginTime() throws Exception {
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceTempCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_BEGIN).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime()  ;
  }
  protected void setWorkingBegin(String value, String format) throws Exception {
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(format) ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    getUIFormSelectBox(WORKINGTIME_BEGIN).setValue(timeFormat.format(cal.getTime())) ;
  }
  protected String getWorkingEnd() throws Exception{
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(getTimeFormat()) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_END).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return timeFormat.format(cal.getTime()) ;
  }

  protected Date getWorkingEndTime() throws Exception{
    java.util.Calendar cal = CalendarUtils.getBeginDay(CalendarUtils.getInstanceTempCalendar()) ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(CalendarUtils.DATETIMEFORMAT) ;
    String value = getUIFormSelectBox(WORKINGTIME_END).getValue() ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    return  cal.getTime();
  }
  protected void setWorkingEnd(String value, String format) throws Exception {
    java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
    DateFormat dateFormat = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    DateFormat timeFormat = new SimpleDateFormat(CalendarUtils.TIMEFORMAT) ;
    DateFormat dateTimeFormat = new SimpleDateFormat(format) ;
    String date = dateFormat.format(cal.getTime()) + " " + value ;
    cal.setTime(dateTimeFormat.parse(date)); 
    getUIFormSelectBox(WORKINGTIME_END).setValue(timeFormat.format(cal.getTime())) ;
  }
  protected String getBaseUrl() {
    return getUIStringInput(BASE_URL).getValue() ;
  }
  protected void setBaseUrl(String value) {
    getUIStringInput(BASE_URL).setValue(value) ;
  }
  private List<SelectItemOption<String>> getTimeZones() {
    return CalendarUtils.getTimeZoneSelectBoxOptions(TimeZone.getAvailableIDs()) ;
  }
  private List<SelectItemOption<String>> getLocales() {
    return CalendarUtils.getLocaleSelectBoxOptions(java.util.Calendar.getAvailableLocales()) ;
  }
}
