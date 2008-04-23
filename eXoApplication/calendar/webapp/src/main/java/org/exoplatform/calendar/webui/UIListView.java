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
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

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
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.ViewActionListener.class),
      @EventConfig(listeners = UICalendarView.EditActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UIListView.CloseSearchActionListener.class),
      @EventConfig(listeners = UIListView.ViewDetailActionListener.class),
      @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UIListView.ShowPageActionListener.class ),
      @EventConfig(listeners = UIListView.OnchangeActionListener.class )   

    }
)
public class UIListView extends UICalendarView {
  private LinkedHashMap<String, CalendarEvent> eventMap_ = new LinkedHashMap<String, CalendarEvent>() ;
  private EventPageList pageList_ = null ;
  private String selectedEvent_ = null ;
  private boolean isShowEventAndTask = true ;
  private boolean isSearchResult = false ;
  private String lastViewId_ = null ;
  private String categoryId_ = null ;
  public UIListView() throws Exception{
    if(getEvents().length > 0 ) {
      selectedEvent_ = getEvents()[0].getId() ;
    }
  } 

  public String getTemplate() {
    if( getViewType().equals(TYPE_TASK)) {
      return "app:/templates/calendar/webui/UIListTask.gtmpl" ;
    } else if(getViewType().equals(TYPE_EVENT)) {
      return "app:/templates/calendar/webui/UIListEvent.gtmpl" ;
    } else {
      return "app:/templates/calendar/webui/UIListView.gtmpl" ;
    }
  }
  public void refresh() throws Exception{
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    if(!CalendarUtils.isEmpty(categoryId_) && !categoryId_.toLowerCase().equals("null")) eventQuery.setCategoryId(new String[]{categoryId_}) ;
    java.util.Calendar fromcalendar = getBeginDay(new GregorianCalendar(getCurrentYear(),  getCurrentMonth(),  getCurrentDay())) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = getEndDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    eventQuery.setToDate(tocalendar) ;
    if(!getViewType().equals(TYPE_BOTH)) {
      eventQuery.setEventType(getViewType()) ;
    }
    UIListContainer uiListContainer = getParent() ;
    if(uiListContainer.isDisplaySearchResult()) update(pageList_) ;
    else update(new EventPageList(calendarService.getEvents(getSession(), username, eventQuery, getPublicCalendars()), 10)) ;
    UIFormSelectBox uiCategory = getUIFormSelectBox(EVENT_CATEGORIES) ;
    uiCategory.setValue(categoryId_) ;
    uiCategory.setOnChange("Onchange") ;
    UIListContainer uiContainer = getParent() ;
    UIPreview view = uiContainer.getChild(UIPreview.class) ;
    if(CalendarUtils.isEmpty(getSelectedEvent())) {
      if(getEvents().length > 0) { 
        setSelectedEvent(getEvents()[0].getId()) ;  
        view.setEvent(getEvents()[0]) ;
        setLastUpdatedEventId(getEvents()[0].getId()) ;
      } else {
        setSelectedEvent(null) ;
        view.setEvent(null) ;
        setLastUpdatedEventId(null) ;
      }
    } else {
      if(getEvents().length > 0) {
        for(CalendarEvent cal : getEvents()) {
          if(cal.getId().equals(getSelectedEvent())) {
            view.setEvent(cal) ;
            setLastUpdatedEventId(getSelectedEvent()) ;
            break ;
          }
        }

      } else {
        setSelectedEvent(null) ;
        view.setEvent(null) ;
        setLastUpdatedEventId(null) ;
      }
    }
  }
  public void update(EventPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateCurrentPage(pageList_.getCurrentPage()) ;
  }
  protected void updateCurrentPage(long page) throws Exception{
    getChildren().clear() ;
    initCategories() ;
    eventMap_.clear();
    if(pageList_ != null) {
      for(CalendarEvent calendarEvent : pageList_.getPage(page ,CalendarUtils.getCurrentUser())) {
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
  public long getCurrentPage() { return pageList_.getCurrentPage();}
  protected boolean isShowEvent() {return isShowEvent_ ;}

  protected boolean isShowEventAndTask() {return isShowEventAndTask ;}
  public void setShowEventAndTask(boolean show) {isShowEventAndTask = show ;}

  public boolean isDisplaySearchResult() {return isSearchResult ;}
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
      UIListContainer uiListContainer = uiListView.getAncestorOfType(UIListContainer.class);
      UIPreview uiPreview = uiListContainer.getChild(UIPreview.class);
      CalendarEvent calendarEvent = null ;
      if(uiListView.getDataMap() != null) {
        calendarEvent = uiListView.getDataMap().get(eventId) ;
        if(calendarEvent != null) {
          uiListView.setLastUpdatedEventId(eventId) ;
          uiListView.setSelectedEvent(calendarEvent.getId()) ;
          uiPreview.setEvent(calendarEvent);
        } else {
          uiListView.setLastUpdatedEventId(eventId) ;
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
  public void setLastViewId(String lastViewId_) {
    this.lastViewId_ = lastViewId_;
  }

  public String getLastViewId() {
    return lastViewId_;
  }
  public void setCategoryId(String catetoryId) {
    categoryId_  = catetoryId ;
    setSelectedCategory(catetoryId) ;
  }
  public String getSelectedCategory() {
    return categoryId_ ;
  }
  static public class CloseSearchActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      uiListView.setDisplaySearchResult(false) ;
      uiListView.setCategoryId(null) ;
      uiListView.refresh() ;
      UICalendarPortlet uiPortlet = uiListView.getAncestorOfType(UICalendarPortlet.class) ;
      UISearchForm uiSearchForm = uiPortlet.findFirstComponentOfType(UISearchForm.class) ;
      uiSearchForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent()) ;
    }
  }
  static  public class ShowPageActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiListView.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent());           
    }
  }
  static  public class OnchangeActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource() ;
      String categoryId = uiListView.getUIFormSelectBox(EVENT_CATEGORIES).getValue() ;
      uiListView.setCategoryId(categoryId) ;
      uiListView.refresh() ;
      UIMiniCalendar uiMiniCalendar = uiListView.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.setCategoryId(categoryId) ;
      UIPreview uiPreview = uiListView.getAncestorOfType(UIListContainer.class).getChild(UIPreview.class) ;
      if(uiListView.getEvents().length >0) {
        uiPreview.setEvent(uiListView.getEvents()[0]) ;
      } else {
        uiPreview.setEvent(null) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView.getParent());           
    }
  }
}

