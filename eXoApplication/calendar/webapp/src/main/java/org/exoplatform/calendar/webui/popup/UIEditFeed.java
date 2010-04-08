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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.calendar.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.list;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
 * Mar 25, 2010 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIEditFeed.gtmpl",
    events = {
      @EventConfig(listeners = UIEditFeed.DeleteCalendarActionListener.class, phase = Phase.DECODE),  
      @EventConfig(listeners = UIEditFeed.ResetURLActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEditFeed.GenerateURLActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEditFeed.SaveActionListener.class),
      @EventConfig(listeners = UIEditFeed.AddCalendarActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEditFeed.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEditFeed extends UIForm implements UIPopupComponent{
  //final static private String SELECT_CALENDAR = "selectCalendar".intern() ;
  final static private String URL = "url".intern() ;
  final static private String NAME = "name".intern() ;
  final static private String CALENDARS = "calendars".intern() ;
  final static private String ADDMORE = "addMore".intern() ;
  private Map<String, List<ActionData>> actionField_ = new HashMap<String, List<ActionData>>() ;
  private LinkedHashMap<String, String> feedCalendars = new LinkedHashMap<String, String>() ;
  private FeedData feedData  = null;
  private boolean isNew_ = false;  
  private static String DEFAULT_FEED_NAME = "defaultFeedName".intern();
  
  private String getURL(String feedName) throws Exception {     
    String portalName = CalendarUtils.getServerBaseUrl() + PortalContainer.getCurrentPortalContainerName();
    String restName = PortalContainer.getCurrentRestContextName();
    return portalName + Utils.SLASH + restName + CalendarWebservice.BASE_RSS_URL  + Utils.SLASH 
    + CalendarUtils.getCurrentUser() + Utils.SLASH + feedName + Utils.SLASH + IdGenerator.generate() + Utils.RSS_EXT ;
  }
  
  public UIEditFeed() throws Exception {
    
    String feedName = getDefaultFeedName();
    addUIFormInput(new UIFormStringInput(NAME, NAME, feedName).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
    addUIFormInput(new UIFormStringInput(URL, URL, getURL(feedName)).addValidator(MandatoryValidator.class)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData resetURL = new ActionData() ;
    resetURL.setActionListener("ResetURL") ;
    resetURL.setCssIconClass("BackIcon");
    resetURL.setActionType(ActionData.TYPE_ICON) ;
    resetURL.setActionName("ResetURL") ;
    actions.add(resetURL) ;
    
    ActionData generateURL = new ActionData() ;
    generateURL.setActionListener("GenerateURL") ;
    generateURL.setActionType(ActionData.TYPE_ICON) ;
    generateURL.setActionName("GenerateURL") ;
    actions.add(generateURL) ;
    setActionField(URL, actions) ;
    
    addUIFormInput(new UIFormInputInfo(CALENDARS, CALENDARS, null)) ;

    List<SelectItem> options = new ArrayList<SelectItem>();
    UIFormSelectBoxWithGroups formSelectBox = new UIFormSelectBoxWithGroups(ADDMORE, ADDMORE, CalendarUtils.getCalendarOption());
    addUIFormInput(formSelectBox);
    List<ActionData> actions2 = new ArrayList<ActionData>() ;
    ActionData addCalendar = new ActionData() ;
    addCalendar.setActionListener("AddCalendar") ;
    addCalendar.setActionType(ActionData.TYPE_ICON) ;
    addCalendar.setActionName("AddCalendar") ;
    actions2.add(addCalendar) ;
    setActionField(ADDMORE, actions2) ;
  }
  
  private String getDefaultFeedName() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    try {
      return res.getString("UIEditFeed.label.defaultFeedName");
    } catch (MissingResourceException e) {      
      e.printStackTrace() ;
      return DEFAULT_FEED_NAME;
    }
  }
  
  public void setNew(boolean isNew) { isNew_ = isNew ; }
  /*
  private List<SelectItemOption<String>> getCalendarsHasEvent() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(SELECT_CALENDAR, SELECT_CALENDAR));
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;    
    for(Calendar cal : calendarService.getUserCalendars(username, true)) {
      if (feedCalendars.containsKey(cal.getId())) continue;
      List<String> calendarIds = Arrays.asList(new String[]{cal.getId()}) ;
      if (calendarService.getUserEventByCalendar(username, calendarIds).size() > 0) {
        if (cal.getId().equals(Utils.getDefaultCalendarId(username)) && cal.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
          String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
          cal.setName(newName);
        }
        options.add(new SelectItemOption<String>(cal.getName(),cal.getId() + Utils.SPLITTER + Utils.PRIVATE_TYPE)) ;
      }
    }
    List<GroupCalendarData> groupCals  = calendarService.getGroupCalendars(CalendarUtils.getUserGroups(username), true, username) ;
    for(GroupCalendarData groupData : groupCals) {
      if(groupData != null) {
        for(Calendar cal : groupData.getCalendars()) {
          if (feedCalendars.containsKey(cal.getId())) continue;
          List<String> calendarIds = Arrays.asList(new String[]{cal.getId()}) ;
          if (calendarService.getGroupEventByCalendar(calendarIds).size() > 0) {
            options.add(new SelectItemOption<String>(cal.getName(), cal.getId() + Utils.SPLITTER + Utils.PUBLIC_TYPE)) ;
          }
        }
      }
    }
    GroupCalendarData sharedData  = calendarService.getSharedCalendars(CalendarUtils.getCurrentUser(), true) ;
    if(sharedData != null) {
      for(Calendar cal : sharedData.getCalendars()) {
        if (feedCalendars.containsKey(cal.getId())) continue;
        List<String> calendarIds = Arrays.asList(new String[]{cal.getId()}) ;
        if (calendarService.getSharedEventByCalendars(username, calendarIds).size() > 0) {        
          if (cal.getId().equals(Utils.getDefaultCalendarId(cal.getCalendarOwner())) && cal.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
            String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
            cal.setName(newName);
          }
          options.add(new SelectItemOption<String>(CalendarUtils.getDisplayShared(cal.getCalendarOwner(), cal.getName()), cal.getId() + Utils.SPLITTER + Utils.SHARED_TYPE)) ;
        }
      }     
    }
    return options;
  }*/
  
  public String[] getActions() {
    return new String[]{"Save", "Close"} ;
  }
  
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}  

  @SuppressWarnings("unchecked")
  public void setFeed(FeedData feed) throws Exception {
    feedData = feed;
    getUIStringInput(NAME).setValue(feed.getTitle());
    getUIStringInput(URL).setValue(feed.getUrl());
    
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed syndFeed = input.build(new XmlReader(new ByteArrayInputStream(feed.getContent())));
    List<SyndEntry> entries = new ArrayList<SyndEntry>(syndFeed.getEntries());
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    for (int i = 0; i < entries.size(); i ++) {
      SyndEntry entry = entries.get(i);
      String calendarId = entry.getLink().substring(entry.getLink().lastIndexOf("/")+1) ;
      Calendar calendar = null;
      try {
        calendar = calendarService.getUserCalendar(username, calendarId) ;
        if (calendar != null) {
          if (calendar.getId().equals(Utils.getDefaultCalendarId(username)) && calendar.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
            String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
            calendar.setName(newName);
          }          
          feedCalendars.put(Utils.PRIVATE_TYPE + Utils.COLON +  calendar.getId() , calendar.getName());
        } else {
          try {
            calendar = calendarService.getSharedCalendars(username, false).getCalendarById(calendarId);
          } catch (NullPointerException e) {}
          if (calendar != null) {
            if (calendar.getId().equals(Utils.getDefaultCalendarId(calendar.getCalendarOwner())) && calendar.getName().equals(NewUserListener.DEFAULT_CALENDAR_NAME)) {
              String newName = CalendarUtils.getResourceBundle("UICalendars.label." + NewUserListener.DEFAULT_CALENDAR_ID);
              calendar.setName(newName);
            }
            feedCalendars.put(Utils.SHARED_TYPE + Utils.COLON + calendar.getId()
                              , CalendarUtils.getDisplayShared(calendar.getCalendarOwner(), calendar.getName()));
          } else {
            calendar = calendarService.getGroupCalendar(calendarId);
            if (calendar != null)
              feedCalendars.put(Utils.PUBLIC_TYPE + Utils.COLON + calendar.getId(), calendar.getName());
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }      
     
    }    
  }
  public LinkedHashMap<String, String> getFeedCalendars() {
    return feedCalendars;
  }
  
  static  public class AddCalendarActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      UIFormSelectBoxWithGroups selectBox = (UIFormSelectBoxWithGroups)uiForm.getChildById(UIEditFeed.ADDMORE);
      String selectedCal = selectBox.getValue() ;
    /*  if (selectedCal.equals(selectBox.getOptions().get(0).getValue())) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIEditFeed.msg.selectCalendar", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }*/
      String calName = null;
      for (SelectItem item : selectBox.getOptions()) {
        SelectOptionGroup selectOptionGroup = (SelectOptionGroup)item;
        boolean isFound = false ;
        for (SelectOption selectOption : selectOptionGroup.getOptions()) {
          if (selectOption.getValue().equals(selectedCal)) {
            calName = selectOption.getLabel();
            isFound = true;
            break;
          }
        }
        if (isFound) break;
      }
      uiForm.feedCalendars.put(selectedCal, calName);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(uiForm.feedCalendars.size() < 0) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        uiApp.addMessage(new ApplicationMessage("UIEditFeed.msg.selectCalendar", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String username = CalendarUtils.getCurrentUser();
      LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
      for (String key : uiForm.feedCalendars.keySet()) {
        String calId = key.split(Utils.COLON)[1];
        String type = key.split(Utils.COLON)[0];
        if (type.equals(CalendarUtils.PRIVATE_TYPE)) calendars.put(key, calendarService.getUserCalendar(username, calId));
        else if (type.equals(CalendarUtils.SHARED_TYPE))
          calendars.put(key,calendarService.getSharedCalendars(username, false).getCalendarById(calId));
        else calendars.put(key, calendarService.getGroupCalendar(calId));
      }
      RssData rssData = new RssData() ;
      String tempName = uiForm.getUIStringInput(NAME).getValue().trim() ;
      UICalendarSettingFeedTab settingFeedTab = uiForm.getAncestorOfType(UIPopupContainer.class)
      .findFirstComponentOfType(UICalendarSettingFeedTab.class);
      for (FeedData feedData : settingFeedTab.getData())
        if (feedData.getTitle().equals(tempName) && (uiForm.isNew_ || !tempName.equals(uiForm.feedData.getTitle()))) {
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
          uiApp.addMessage(new ApplicationMessage("UIEditFeed.msg.feedName_existed", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      
      if(tempName.length() > 4 && tempName.substring(tempName.length() - 4).equals(Utils.RSS_EXT)) rssData.setName(tempName);
      else rssData.setName(tempName + Utils.RSS_EXT) ;
      String url = uiForm.getUIStringInput(URL).getValue();
      if (!url.contains(tempName)) url = uiForm.getURL(tempName.replaceAll(Utils.SPACE, CalendarUtils.UNDERSCORE)); 
      rssData.setUrl(url) ;
      String title = uiForm.getUIStringInput(NAME).getValue() ;
      rssData.setTitle(title) ;
      rssData.setDescription(title);
      rssData.setLink(url);
      rssData.setVersion("rss_2.0") ;
      int result ;
      if (uiForm.isNew_) {
        result = calendarService.generateRss(username, calendars, rssData);        
      } else {
        calendarService.removeFeedData(username, uiForm.feedData.getTitle());
        result = calendarService.generateRss(username, calendars, rssData);
      }
      if(result < 0) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        uiApp.addMessage(new ApplicationMessage("UIEditFeed.msg.no-data-generated", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      settingFeedTab.setFeedList(calendarService.getFeeds(username));
      UIPopupAction popupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(settingFeedTab);
      Object[] object = new Object[]{title} ;
      uiApp.addMessage(new ApplicationMessage("UIEditFeed.msg.feed-has-been-generated", object)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    }
  }
  static  public class DeleteCalendarActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      String calId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiForm.feedCalendars.remove(calId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);      
    }
  }
  
  static  public class ResetURLActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      UIFormStringInput url = (UIFormStringInput)uiForm.getChildById(UIEditFeed.URL);
      if (uiForm.feedData != null)
        url.setValue(uiForm.feedData.getUrl());
      else {
        uiForm.getUIStringInput(UIEditFeed.URL).setValue(uiForm.getURL(uiForm.getDefaultFeedName()));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static  public class GenerateURLActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      String feedName = uiForm.getUIStringInput(UIEditFeed.NAME).getValue();
      if (CalendarUtils.isEmpty(feedName)) feedName = uiForm.getDefaultFeedName();
      String newURL = uiForm.getURL(feedName.replaceAll(Utils.SPACE, CalendarUtils.UNDERSCORE));
      uiForm.getUIStringInput(UIEditFeed.URL).setValue(newURL);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIEditFeed> {
    public void execute(Event<UIEditFeed> event) throws Exception {
      UIEditFeed uiForm = event.getSource() ;
      UIPopupAction popupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
  
  
}
