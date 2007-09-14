/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIRssForm.GenerateRssActionListener.class),      
      @EventConfig(listeners = UIRssForm.CancelActionListener.class)
    }
)
public class UIRssForm extends UIFormTabPane implements UIPopupComponent{
  final static private String NAME = "name".intern() ;
  final static private String URL = "url".intern() ;
  final static private String DESCRIPTION = "description".intern() ;
  final static private String LINK = "link".intern() ;
  final static private String COPYRIGHT = "copyright".intern() ;
  final static private String TITLE = "title".intern() ;
  final static private String VERSION = "version".intern() ;
  final static private String PUBLIC_DATE = "pubDate".intern() ;
  final static private String[] version = 
    new String[]{"rss_2.0","rss_1.0","rss_0.94","rss_0.93","rss_0.92","rss_0.91","rss_0.90"} ;
  public UIRssForm() throws Exception{
    super("UIRssForm", false);
    UIFormInputWithActions rssInfo = new UIFormInputWithActions("rssInfo") ;
    rssInfo.addUIFormInput(new UIFormStringInput(NAME, NAME, "eXoCalendar.rss")) ;
    rssInfo.addUIFormInput(new UIFormStringInput(TITLE, TITLE, null)) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String vs : version) {
      options.add(new SelectItemOption<String>(vs, vs)) ;
    }
    rssInfo.addUIFormInput(new UIFormSelectBox(VERSION, VERSION, options)) ;
    rssInfo.addUIFormInput(new UIFormStringInput(URL, URL, "http://localhost:8080/calendar/iCalRss")) ;
    rssInfo.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, "This RSS provided by eXo Platform opensource company")) ;
    rssInfo.addUIFormInput(new UIFormStringInput(COPYRIGHT, COPYRIGHT, "Copyright by 2000-2005 eXo Platform SARL")) ;
    rssInfo.addUIFormInput(new UIFormStringInput(LINK, LINK, "www.exoplatform.org")) ;    
    rssInfo.addUIFormInput(new UIFormDateTimeInput(PUBLIC_DATE, PUBLIC_DATE, new Date())) ;
    rssInfo.setRendered(true) ;
    addUIFormInput(rssInfo) ;
    UIFormInputWithActions rssCalendars = new UIFormInputWithActions("rssCalendars") ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    List<Calendar> calendars = calendarService.getUserCalendars(Util.getPortalRequestContext().getRemoteUser()) ;
    for(Calendar calendar : calendars) {
      rssCalendars.addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getName(), calendar.getId(), true)) ;
    }
    rssCalendars.setRendered(false) ;
    addUIFormInput(rssCalendars) ;
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class GenerateRssActionListener extends EventListener<UIRssForm> {
    public void execute(Event<UIRssForm> event) throws Exception {
      UIRssForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      CalendarService calendarService = CalendarUtils.getCalendarService();
      UIFormInputWithActions rssCalendars = uiForm.getChildById("rssCalendars") ;
      List<UIComponent> children = rssCalendars.getChildren() ;
      List<String> calendarIds = new ArrayList<String> () ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            calendarIds.add(((UIFormCheckBoxInput)child).getBindingField()) ;
          }
        }
      }
      if(calendarIds.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIRssForm.msg.there-is-not-calendar", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      RssData rssData = new RssData() ;
      String tempName = uiForm.getUIStringInput(uiForm.NAME).getValue() ;
      if(tempName != null && tempName.length() > 0) {
        if(tempName.length() > 4 && tempName.substring(tempName.length() - 4).equals(".rss")) rssData.setName(tempName);
        else rssData.setName(tempName + ".rss") ;
      }else {
        rssData.setName("eXoCalendar.rss") ;
      }      
      rssData.setUrl(uiForm.getUIStringInput(uiForm.URL).getValue()) ;
      rssData.setDescription(uiForm.getUIStringInput(uiForm.DESCRIPTION).getValue()) ;
      rssData.setCopyright(uiForm.getUIStringInput(uiForm.COPYRIGHT).getValue()) ;
      rssData.setLink(uiForm.getUIStringInput(uiForm.LINK).getValue()) ;
      rssData.setTitle(uiForm.getUIStringInput(uiForm.TITLE).getValue()) ;
      rssData.setVersion(uiForm.getUIFormSelectBox(uiForm.VERSION).getValue()) ;
      rssData.setPubDate(uiForm.getUIFormDateTimeInput(uiForm.PUBLIC_DATE).getCalendar().getTime()) ;
      calendarService.generateRss(Util.getPortalRequestContext().getRemoteUser(), calendarIds, rssData) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIRssForm> {
    public void execute(Event<UIRssForm> event) throws Exception {
      UIRssForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
