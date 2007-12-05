/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

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
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
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
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    options.add(new SelectItemOption<String>("", "")) ;
    for(Calendar cal : cservice.getUserCalendars(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
    }
    addChild(new UIFormSelectBox(CALENDAR, CALENDAR, options)) ;
    
    options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    for(CalendarCategory cat : cservice.getCategories(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cat.getName(), cat.getId())) ;
    }
    addChild(new UIFormSelectBox(CATEGORY, CATEGORY, options)) ;
    
    options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("", "")) ;
    options.add(new SelectItemOption<String>(CalendarEvent.PRIORITY_LOW, CalendarEvent.PRIORITY_LOW)) ;
    options.add(new SelectItemOption<String>(CalendarEvent.PRIORITY_NORMAL, CalendarEvent.PRIORITY_NORMAL)) ;
    options.add(new SelectItemOption<String>(CalendarEvent.PRIORITY_HIGH, CalendarEvent.PRIORITY_HIGH)) ;
    addChild(new UIFormSelectBox(PRIORITY, PRIORITY, options)) ;
    UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FROMDATE, FROMDATE, new Date(), false) ;
    fromDate.addValidator(EmptyFieldValidator.class) ;
    addChild(fromDate) ;
    java.util.Calendar calendar = CalendarUtils.getInstanceTempCalendar() ;
    calendar.add(java.util.Calendar.DATE, 1) ;
    addChild(new UIFormDateTimeInput(TODATE, TODATE, calendar.getTime(), false).addValidator(EmptyFieldValidator.class)) ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {
    
  }
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;      
      EventQuery query = new EventQuery() ;
      query.setText(uiForm.getUIStringInput(uiForm.TEXT).getValue()) ;
      query.setEventType(uiForm.getUIFormSelectBox(uiForm.TYPE).getValue()) ;
      String calendarId = uiForm.getUIFormSelectBox(uiForm.CALENDAR).getValue() ;
      if(calendarId != null && calendarId.length() > 0) query.setCalendarId(new String[]{calendarId}) ;
      String categoryId = uiForm.getUIFormSelectBox(uiForm.CATEGORY).getValue() ;
      if(categoryId != null && categoryId.length() > 0) query.setCategoryId(new String[]{categoryId}) ;
      query.setFromDate(uiForm.getUIFormDateTimeInput(uiForm.FROMDATE).getCalendar()) ;
      query.setToDate(uiForm.getUIFormDateTimeInput(uiForm.TODATE).getCalendar()) ;
      if(query.getToDate().getTimeInMillis() <= query.getFromDate().getTimeInMillis()) {
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String username = CalendarUtils.getCurrentUser() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer calendarViewContainer = 
        calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      calendarViewContainer.setRenderedChild(UICalendarViewContainer.LIST_VIEW) ;
      UIListView uiListView = calendarViewContainer.findFirstComponentOfType(UIListView.class) ;
      EventPageList resultPageList =  
        CalendarUtils.getCalendarService().searchEvent(SessionsUtils.getSystemProvider(), username, query, uiListView.getPublicCalendars()) ;
      calendarPortlet.cancelAction() ;
      uiListView.update(resultPageList) ;
      uiListView.setViewType(UIListView.TYPE_BOTH) ;
      uiListView.setDisplaySearchResult(true) ;
      uiListView.setSelectedEvent(null) ;
      calendarViewContainer.findFirstComponentOfType(UIPreview.class).setEvent(null) ;
      /*if(query.getEventType() == null || query.getEventType().equals("")) 
        uiListView.setShowEventAndTask(true) ;
      else{
        uiListView.setShowEventAndTask(false) ;
        if(query.getEventType().equals(CalendarEvent.TYPE_EVENT)) uiListView.isShowEvent_ = true ;
        else uiListView.isShowEvent_ = false ;
      }*/ 
      UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(UICalendarViewContainer.LIST_VIEW) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarViewContainer) ;
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
