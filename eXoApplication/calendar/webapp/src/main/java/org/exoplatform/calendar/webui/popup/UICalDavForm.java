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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
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
      @EventConfig(listeners = UICalDavForm.GenerateActionListener.class),      
      @EventConfig(listeners = UICalDavForm.CancelActionListener.class)
    }
)
public class UICalDavForm extends UIFormTabPane implements UIPopupComponent{
  final static private String URL = "url".intern() ;
  final static private String DESCRIPTION = "description".intern() ;
  final static private String COPYRIGHT = "copyright".intern() ;
  final static private String TITLE = "title".intern() ;
  final static private String PUBLIC_DATE = "pubDate".intern() ;
  final static private String INFOR = "info".intern() ;
  final static private String MESSAGE = "message".intern() ;
  final static private String DESCRIPTIONS = "descriptions".intern() ;
  final static private String COPYRIGHTS = "copyrights".intern() ;
  
  public UICalDavForm() throws Exception{
    super("UICalDavForm");
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    UIFormInputWithActions rssInfo = new UIFormInputWithActions("rssInfo") ;
    rssInfo.addUIFormInput(new UIFormStringInput(TITLE, TITLE, "eXoCalendarCalDav")) ;
    String url = calendarService.getCalendarSetting(SessionProviderFactory.createSessionProvider(), username).getBaseURL();
    if(CalendarUtils.isEmpty(url)) url = CalendarUtils.getServerBaseUrl() + "calendar/iCalRss" ;
    rssInfo.addUIFormInput(new UIFormStringInput(URL, URL, url)) ;
    rssInfo.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, DESCRIPTIONS)) ;
    rssInfo.addUIFormInput(new UIFormStringInput(COPYRIGHT, COPYRIGHT, "Copyright by 2000-2005 eXo Platform SARL")) ;
    rssInfo.addUIFormInput(new UIFormDateTimeInput(PUBLIC_DATE, PUBLIC_DATE, new Date())) ;
    setSelectedTab(rssInfo.getId()) ;
    addUIFormInput(rssInfo) ;
    UIFormInputWithActions rssCalendars = new UIFormInputWithActions("rssCalendars") ;
    rssCalendars.addUIFormInput(new UIFormInputInfo(INFOR,INFOR, null)) ; 
    List<Calendar> calendars = calendarService.getUserCalendars(SessionProviderFactory.createSessionProvider(), username, true) ;
    for(Calendar calendar : calendars) {
      rssCalendars.addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getName(), calendar.getId(), true)) ;
    }
    addUIFormInput(rssCalendars) ;
  }
  public void init() throws Exception{
    UIFormInputWithActions rssInfo = getChildById("rssInfo") ;
    rssInfo.getUIFormTextAreaInput(DESCRIPTION).setValue(getLabel(DESCRIPTIONS)) ;
    rssInfo.getUIStringInput(COPYRIGHT).setValue(getLabel(COPYRIGHTS)) ;
    UIFormInputWithActions rssTab = getChildById("rssCalendars") ;
    rssTab.getUIFormInputInfo(INFOR).setValue(getLabel(MESSAGE)) ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public String getWebDAVServerPrefix() throws Exception {    
   String prefixWebDAV = CalendarUtils.getServerBaseUrl() + 
           getPortalName() + "/rest/jcr/"+getRepository().getConfiguration().getName() +"/" + getWorkspaceName()  ;
    return prefixWebDAV ;
  }
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getWorkspaceName() throws Exception {
    return getRepository().getConfiguration().getDefaultWorkspaceName() ;
  }
  private ManageableRepository getRepository() throws Exception{         
    RepositoryService repositoryService  = getApplicationComponent(RepositoryService.class) ;      
    return repositoryService.getCurrentRepository() ;
  }
  static  public class GenerateActionListener extends EventListener<UICalDavForm> {
    public void execute(Event<UICalDavForm> event) throws Exception {
      UICalDavForm uiForm = event.getSource() ;
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
      String tempName = uiForm.getUIStringInput(UICalDavForm.TITLE).getValue() ;
      if(tempName != null && tempName.length() > 0) {
        if(tempName.length() > 4 && tempName.substring(tempName.length() - 4).equals(".rss")) rssData.setName(tempName);
        else rssData.setName(tempName + ".rss") ;
      }else {
        rssData.setName("eXoCalendar.rss") ;
      }      
      rssData.setUrl(uiForm.getUIStringInput(UICalDavForm.URL).getValue()) ;
      rssData.setDescription(uiForm.getUIStringInput(UICalDavForm.DESCRIPTION).getValue()) ;
      rssData.setCopyright(uiForm.getUIStringInput(UICalDavForm.COPYRIGHT).getValue()) ;
      rssData.setLink(uiForm.getWebDAVServerPrefix()) ;
      String title = uiForm.getUIStringInput(UICalDavForm.TITLE).getValue() ;
      rssData.setTitle(title) ;
      rssData.setVersion("rss_2.0") ;
      rssData.setPubDate(uiForm.getUIFormDateTimeInput(UICalDavForm.PUBLIC_DATE).getCalendar().getTime()) ;
      calendarService.generateCalDav(SessionProviderFactory.createSystemProvider(), Util.getPortalRequestContext().getRemoteUser(), calendarIds, rssData) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;  
      Object[] object = new Object[]{title} ;
      uiApp.addMessage(new ApplicationMessage("UIRssForm.msg.feed-has-been-generated", object)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
    }
  }

  static  public class CancelActionListener extends EventListener<UICalDavForm> {
    public void execute(Event<UICalDavForm> event) throws Exception {
      UICalDavForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
