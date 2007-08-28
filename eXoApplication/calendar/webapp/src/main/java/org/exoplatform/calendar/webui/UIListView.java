/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.List;


import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
    //template = "app:/templates/calendar/webui/UIListView.gtmpl",
    events = {
      @EventConfig(listeners = UIListView.AddEventActionListener.class),      
      @EventConfig(listeners = UIListView.DeleteEventActionListener.class),
      @EventConfig(listeners = UIListView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UIListView.AddCategoryActionListener.class)
    }
)
public class UIListView extends UIForm {
  private boolean isShowEvent = true;
  private List<String> privateCalendarIds = new ArrayList<String>() ;
  private List<String> publicCalendarIds = new ArrayList<String>() ;
  
  public UIListView() throws Exception{
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    List<EventCategory> eventCategories = calendarService.getEventCategories(Util.getPortalRequestContext().getRemoteUser()) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(EventCategory category : eventCategories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getName())) ;
    }
    addUIFormInput(new UIFormSelectBox("eventCategories", "eventCategories", options)) ;
  }
  
  public String getTemplate(){
    if(isShowEvent) return "app:/templates/calendar/webui/UIEventView.gtmpl" ;
    return "app:/templates/calendar/webui/UITaskView.gtmpl" ;
  }
  
  public List<org.exoplatform.calendar.service.Event> getList() throws Exception {
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    List<org.exoplatform.calendar.service.Event> events = new ArrayList<org.exoplatform.calendar.service.Event>() ;
    if(privateCalendarIds.size() > 0) {
      events = calendarService.getUserEventByCalendar(Util.getPortalRequestContext().getRemoteUser(), privateCalendarIds)  ;
    }
    if(publicCalendarIds.size() > 0) {
      if(events.size() > 0) {
        List<org.exoplatform.calendar.service.Event> publicEvents = 
          calendarService.getGroupEventByCalendar(publicCalendarIds) ;
        for(org.exoplatform.calendar.service.Event event : publicEvents) {
          events.add(event) ;
        }
      }else {
        events = calendarService.getGroupEventByCalendar(publicCalendarIds)  ;
      }
    }
    return events ;
         
  }
  
  static  public class AddEventActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> AddEventActionListener") ;
    }
  }
  static  public class DeleteEventActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> DeleteEventActionListener") ;
    }
  }
  static  public class ChangeCategoryActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> ChangeCategoryActionListener") ;
    }
  }
  static  public class AddCategoryActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> AddCategoryActionListener") ;
    }
  }
  
}
