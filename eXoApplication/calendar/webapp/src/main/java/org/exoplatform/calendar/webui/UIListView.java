/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.JCRPageList;
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
    template = "app:/templates/calendar/webui/UIListView.gtmpl",
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class), 
      @EventConfig(listeners = UIListView.ViewDetailActionListener.class)
    }
)
public class UIListView extends UICalendarView {
  private Map<String, CalendarEvent> eventMap_ = new HashMap<String, CalendarEvent>() ;
  private JCRPageList pageList_ = null ;
  private String selectedEvent_ = null ;
  private boolean isShowEventAndTask = false ;
  private boolean isSearchResult = false ;
  public UIListView() throws Exception{
    super() ;
  } 

  public void refresh() {}
  
  public void update(JCRPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateCurrentPage() ;
  }
  private void updateCurrentPage() throws Exception{
    getChildren().clear() ;
    initCategories() ;
    eventMap_.clear();
    if(pageList_ != null) {
      for(CalendarEvent calendarEvent : pageList_.getPage(pageList_.getCurrentPage(),CalendarUtils.getCurrentUser())) {
        UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(calendarEvent.getId(),calendarEvent.getId(), false) ;
        addUIFormInput(checkbox);
        eventMap_.put(calendarEvent.getId(), calendarEvent) ;
      }
      CalendarEvent[] array = eventMap_.values().toArray(new CalendarEvent[]{}) ;
      if (array.length > 0)
        getAncestorOfType(UIListContainer.class).getChild(UIPreview.class).setEvent(array[0]) ;
      else 
        getAncestorOfType(UIListContainer.class).getChild(UIPreview.class).setEvent(null) ;
    }
  }
  
  public CalendarEvent[] getEvents() throws Exception {
    if(eventMap_ == null || eventMap_.size() == 0) return new CalendarEvent[]{} ;
    return eventMap_.values().toArray(new CalendarEvent[]{}) ;    
  }
  
  protected boolean isShowEvent() {return isShowEvent_ ;}

  protected boolean isShowEventAndTask() {return isShowEventAndTask ;}
  public void setShowEventAndTask(boolean show) {isShowEventAndTask = show ;}
  
  protected boolean isDisplaySearchResult() {return isSearchResult ;}
  public void setDisplaySearchResult(boolean show) {isSearchResult = show ;}
  
  public void setSelectedEvent(String selectedEvent) { this.selectedEvent_ = selectedEvent ; }
  public String getSelectedEvent() { return selectedEvent_ ;}

  static public class ViewDetailActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiListView = event.getSource();
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiListView.setSelectedEvent(eventId) ;
      UIListContainer uiListContainer = uiListView.getAncestorOfType(UIListContainer.class);
      UIPreview uiPreview = uiListContainer.getChild(UIPreview.class);
      uiPreview.setEvent(uiListView.eventMap_.get(eventId));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListContainer);
    }
  }
}

