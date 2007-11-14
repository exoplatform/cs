/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.JCRPageList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(   
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UIListView.CloseSearchActionListener.class),
      @EventConfig(listeners = UIListView.ViewDetailActionListener.class),
      @EventConfig(listeners = UIListView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIListView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UIListView.ShowPageActionListener.class )    
    }
)
public class UIListView extends UICalendarView {
  private LinkedHashMap<String, CalendarEvent> eventMap_ = new LinkedHashMap<String, CalendarEvent>() ;
  private JCRPageList pageList_ = null ;
  private String selectedEvent_ = null ;
  private boolean isShowEventAndTask = true ;
  private boolean isSearchResult = false ;
  private long currentPage_ = 1 ;
  public UIListView() throws Exception{
    if(getEvents().length > 0 ) {
      selectedEvent_ = getEvents()[0].getId() ;
    }
  } 

  public String getTemplate() {
    if( getViewType().equals(TYPE_TASK)) {
      return "app:/templates/calendar/webui/UITaskView.gtmpl" ;
    } else if(getViewType().equals(TYPE_EVENT)) {
      return "app:/templates/calendar/webui/UIEventView.gtmpl" ;
    } else {
      return "app:/templates/calendar/webui/UIListView.gtmpl" ;
    }
  }
  public void refresh() throws Exception{
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = getBeginDay(new GregorianCalendar(getCurrentYear(),  getCurrentMonth(),  getCurrentDay())) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = getEndDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    eventQuery.setToDate(tocalendar) ;
    if(!getViewType().equals(TYPE_BOTH)) {
      eventQuery.setEventType(getViewType()) ;
    }
    update(calendarService.searchEvent(username, eventQuery, getPublicCalendars())) ; 
  }
  public void update(JCRPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateCurrentPage() ;
  }
  protected void updateCurrentPage() throws Exception{
    getChildren().clear() ;
    initCategories() ;
    eventMap_.clear();
    if(pageList_ != null) {
      for(CalendarEvent calendarEvent : pageList_.getPage(getCurrentPage() ,CalendarUtils.getCurrentUser())) {
        UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(calendarEvent.getId(),calendarEvent.getId(), false) ;
        addUIFormInput(checkbox);
        if(getViewType().equals(TYPE_BOTH)) eventMap_.put(calendarEvent.getId(), calendarEvent) ;
        else if(getViewType().equals(calendarEvent.getEventType())) {
          eventMap_.put(calendarEvent.getId(), calendarEvent) ;
        }
      }
    }
  }

  public CalendarEvent[] getEvents() throws Exception {
    if(eventMap_ == null || eventMap_.size() == 0) return new CalendarEvent[]{} ;
    return eventMap_.values().toArray(new CalendarEvent[]{}) ;    
  }
  public long getAvailablePage(){
    return pageList_.getAvailablePage() ; 
  }
  public long getCurrentPage() { return currentPage_;}
  public void setCurrentPage(long page) { currentPage_ = page ;}
  protected boolean isShowEvent() {return isShowEvent_ ;}

  protected boolean isShowEventAndTask() {return isShowEventAndTask ;}
  public void setShowEventAndTask(boolean show) {isShowEventAndTask = show ;}

  protected boolean isDisplaySearchResult() {return isSearchResult ;}
  public void setDisplaySearchResult(boolean show) {isSearchResult = show ;}

  public void setSelectedEvent(String selectedEvent) { this.selectedEvent_ = selectedEvent ; }
  public String getSelectedEvent() { return selectedEvent_ ;}

  public LinkedHashMap<String, CalendarEvent> getDataMap(){
    return eventMap_ ;
  }
  static public class ViewDetailActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource();
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID);
      String calType = event.getRequestContext().getRequestParameter(CALTYPE);
      String username = event.getRequestContext().getRemoteUser() ;
      UIListContainer uiListContainer = uiListView.getAncestorOfType(UIListContainer.class);
      UIPreview uiPreview = uiListContainer.getChild(UIPreview.class);
      CalendarEvent calendarEvent = null ;
      if(uiListView.getDataMap() != null) {
        calendarEvent = uiListView.getDataMap().get(eventId) ;
        /*if(CalendarUtils.PUBLIC_TYPE.equals(calType)) {
        calendarEvent = CalendarUtils.getCalendarService().getGroupEvent(calendarId, eventId) ;
      } else {
        calendarEvent = CalendarUtils.getCalendarService().getUserEvent(username, calendarId, eventId) ;
      }*/
        if(calendarEvent != null) {
          uiListView.setSelectedEvent(calendarEvent.getId()) ;
          uiPreview.setEvent(calendarEvent);
        } else {
          uiListView.setSelectedEvent(null) ;
          uiPreview.setEvent(null);
        } 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiListContainer);
      }
    }
  }
  public List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(CalendarEvent ce : eventMap_.values()) {
      UIFormCheckBoxInput<Boolean>  checkbox = getChildById(ce.getId())  ;
      if(checkbox != null && checkbox.isChecked()) events.add(ce) ;
    }
    return events ; 
  }
  static public class CloseSearchActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      uiListView.setDisplaySearchResult(false) ;
      uiListView.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent()) ;
    }
  }
  static  public class ShowPageActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiListView.setCurrentPage(page) ;
      uiListView.updateCurrentPage() ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent());           
    }
  }
  static  public class MoveNextActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView calendarview = event.getSource() ;
      calendarview.calendar_.add(Calendar.DATE, 1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
  static  public class MovePreviousActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView calendarview = event.getSource() ;
      calendarview.calendar_.add(Calendar.DATE, -1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}

