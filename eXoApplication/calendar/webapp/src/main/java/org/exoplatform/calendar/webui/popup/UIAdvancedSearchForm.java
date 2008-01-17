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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.UIActionBar;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.calendar.webui.UIPreview;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent{
  final static  private String TEXT = "text" ;
  final static  private String TYPE = "type" ;
  final static  private String CALENDAR = "calendar" ;
  final static  private String CATEGORY = "category" ;
  final static  private String PRIORITY = "priority" ;
  final static  private String STATE = "state" ;
  final static  private String FROMDATE = "fromDate" ;
  final static  private String TODATE = "toDate" ;
  public UIAdvancedSearchForm() throws Exception{
    addChild(new UIFormStringInput(TEXT, TEXT, null)) ;
    List<SelectItemOption<String>> types = new ArrayList<SelectItemOption<String>>() ;
    types.add(new SelectItemOption<String>("Event and Task", "")) ;
    types.add(new SelectItemOption<String>(CalendarEvent.TYPE_EVENT, CalendarEvent.TYPE_EVENT)) ;
    types.add(new SelectItemOption<String>(CalendarEvent.TYPE_TASK, CalendarEvent.TYPE_TASK)) ;
    addChild(new UIFormSelectBox(TYPE, TYPE, types)) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    String username = CalendarUtils.getCurrentUser() ;
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    options.add(new SelectItemOption<String>("", "")) ;
    for(Calendar cal : cservice.getUserCalendars(SessionsUtils.getSessionProvider(), username, true)) {
      options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
    }
    List<GroupCalendarData> groupCals  = cservice.getGroupCalendars(SessionsUtils.getSessionProvider(), CalendarUtils.getUserGroups(username), true, username) ;
    for(GroupCalendarData groupData : groupCals) {
      if(groupData != null) {
        for(Calendar cal : groupData.getCalendars()) {
          options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
        }
      }
    }
    GroupCalendarData sharedData  = cservice.getSharedCalendars(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser(), true) ;
    if(sharedData != null) {
      for(Calendar cal : sharedData.getCalendars()) {
        options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
      }
    }
    addChild(new UIFormSelectBox(CALENDAR, CALENDAR, options)) ;
    options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    for(EventCategory cat : cservice.getEventCategories(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cat.getName(), cat.getName())) ;
    }
    addChild(new UIFormSelectBox(CATEGORY, CATEGORY, options)) ;
    addChild(new UIFormSelectBox(PRIORITY, PRIORITY, getPriority())) ;
    addChild( new UIFormDateTimeInput(FROMDATE, FROMDATE, null, false)) ;
    addChild(new UIFormDateTimeInput(TODATE, TODATE, null, false)) ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {

  }
  public void setSearchValue(String searchValue) {
    getUIStringInput(TEXT).setValue(searchValue) ;
  }  
  public String getFromDateValue() {
    return getUIFormDateTimeInput(FROMDATE).getValue() ;
  }
  public String getToDateValue() {
    return getUIFormDateTimeInput(TODATE).getValue() ;
  }
  public Date getFromDate() {
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    if(getFromDateValue() != null) 
      try {
        return df.parse(getFromDateValue()) ;
      }  catch (Exception e) {
        return null ;
      }
      return null ;
  }
  public Date getToDate() {
    DateFormat df = new SimpleDateFormat(CalendarUtils.DATEFORMAT) ;
    if(getToDateValue() != null) 
      try {
        return df.parse(getToDateValue()) ;
      }  catch (Exception e) {
        return null ;
      }
      return null ;
  }
  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    options.add(new SelectItemOption<String>("normal", "2")) ;
    options.add(new SelectItemOption<String>("high", "1")) ;
    options.add(new SelectItemOption<String>("low", "3")) ;
    return options ;
  }
  public String[] getPublicCalendars() throws Exception{
    String[] groups = CalendarUtils.getUserGroups(CalendarUtils.getCurrentUser()) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    Map<String, String> map = new HashMap<String, String> () ;    
    for(GroupCalendarData group : calendarService.getGroupCalendars(SessionsUtils.getSystemProvider(), groups, true, CalendarUtils.getCurrentUser())) {
      for(org.exoplatform.calendar.service.Calendar calendar : group.getCalendars()) {
        map.put(calendar.getId(), calendar.getId()) ;          
      }
    }
    return map.values().toArray(new String[map.values().size()] ) ;
  }
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;

      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;   
      if(!CalendarUtils.isEmpty(uiForm.getFromDateValue()) && uiForm.getFromDate() == null){
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.from-date-time-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }  
      if(!CalendarUtils.isEmpty(uiForm.getToDateValue()) && uiForm.getToDate() == null)  {
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.to-date-time-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiForm.getFromDate() != null && uiForm.getToDate() != null) {
        if(uiForm.getFromDate().after(uiForm.getToDate())){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      try {
        EventQuery query = new EventQuery() ;
        query.setText(uiForm.getUIStringInput(UIAdvancedSearchForm.TEXT).getValue()) ;
        query.setEventType(uiForm.getUIFormSelectBox(UIAdvancedSearchForm.TYPE).getValue()) ;
        String calendarId = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.CALENDAR).getValue() ;
        if(calendarId != null && calendarId.trim().length() > 0) query.setCalendarId(new String[]{calendarId}) ;
        String categoryId = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.CATEGORY).getValue() ;
        if(categoryId != null && categoryId.trim().length() > 0) query.setCategoryId(new String[]{categoryId}) ;
        java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
        if(uiForm.getFromDate() != null && uiForm.getToDate() != null) {
          cal.setTime(uiForm.getFromDate()) ;
          query.setFromDate(CalendarUtils.getBeginDay(cal)) ;
          cal.setTime(uiForm.getToDate()) ;
          query.setToDate(CalendarUtils.getEndDay(cal)) ;
        } else if (uiForm.getFromDate() !=null) {
          cal.setTime(uiForm.getFromDate()) ;
          query.setFromDate(CalendarUtils.getBeginDay(cal)) ;
          query.setToDate(CalendarUtils.getEndDay(cal)) ;
        } else if (uiForm.getToDate() !=null) {
          cal.setTime(uiForm.getToDate()) ;
          query.setFromDate(CalendarUtils.getBeginDay(cal)) ;
          query.setToDate(CalendarUtils.getEndDay(cal)) ;
        }

        String priority = uiForm.getUIFormSelectBox(UIAdvancedSearchForm.PRIORITY).getValue() ;
        if(priority != null && priority.trim().length() > 0) query.setPriority(priority) ;
        String username = CalendarUtils.getCurrentUser() ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UICalendarViewContainer calendarViewContainer = 
          calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        calendarViewContainer.initView(UICalendarViewContainer.LIST_VIEW) ;
        UIListView uiListView = calendarViewContainer.findFirstComponentOfType(UIListView.class) ;
        EventPageList resultPageList =  
          CalendarUtils.getCalendarService().searchEvent(SessionsUtils.getSystemProvider(), username, query, uiForm.getPublicCalendars()) ;
        calendarPortlet.cancelAction() ;
        uiListView.update(resultPageList) ;
        calendarViewContainer.setRenderedChild(UICalendarViewContainer.LIST_VIEW) ;
        uiListView.setViewType(UIListView.TYPE_BOTH) ;
        uiListView.setDisplaySearchResult(true) ;
        uiListView.setSelectedEvent(null) ;
        uiListView.setLastUpdatedEventId(null) ;
        calendarViewContainer.findFirstComponentOfType(UIPreview.class).setEvent(null) ;
        UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
        uiActionBar.setCurrentView(UICalendarViewContainer.LIST_VIEW) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarViewContainer) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
}
