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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;


/**
 * Author : Nguyen Quang Hung
 *          hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/templates/calendar/webui/UICalendarPortlet.gtmpl"
)
public class UICalendarPortlet extends UIPortletApplication {
//  private CalendarSetting calendarSetting_ ;
  public UICalendarPortlet() throws Exception {
//    calendarSetting_ = CalendarUtils.getCalendarService().getCalendarSetting(CalendarUtils.getCurrentUser()) ;
    UIActionBar uiActionBar = addChild(UIActionBar.class, null, null) ;
    uiActionBar.setCurrentView(UICalendarViewContainer.TYPES[Integer.parseInt(getCalendarSetting().getViewType())]) ;
    addChild(UICalendarWorkingContainer.class, null, null) ;
    UIPopupAction uiPopup =  addChild(UIPopupAction.class, null, null) ;
    uiPopup.setId("UICalendarPopupAction") ;
    uiPopup.getChild(UIPopupWindow.class).setId("UICalendarPopupWindow") ;
  }
  public CalendarSetting getCalendarSetting() throws Exception{
    return CalendarUtils.getCurrentCalendarSetting(); 
  }
  public void setCalendarSetting(CalendarSetting setting) throws Exception{
    CalendarUtils.setCurrentCalendarSetting(setting); 
  }
  
  /**
   * @return a calendar that contains configuration of the user, such as: Time zone, First day of week.
   * @throws Exception
   */
  public Calendar getUserCalendar() {    
    return CalendarUtils.getInstanceOfCurrentCalendar();
  }
  
  public String getSettingTimeZone() throws Exception {
    return String.valueOf(TimeZone.getTimeZone(getCalendarSetting().getTimeZone()).getRawOffset()/1000/60) ;
  }
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
  protected void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if(popupMess == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    popupMess.processRender(context);
  }
  public String getRemoteUser() throws Exception {
    return CalendarUtils.getCurrentUser() ;
  }
  public String getUserToken()throws Exception {
    ContinuationService continuation = CalendarUtils.getContinuationService() ;
    try {
        return continuation.getUserToken(this.getRemoteUser());
	  } catch (Exception e) {
		  System.out.println("\n\n can not get UserToken");
		  return "" ;
	  }
  }
  
  protected String getCometdContextName() {
    String cometdContextName = "cometd";
    try {
      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) PortalContainer.getInstance()
                                                                                .getComponentInstanceOfType(AbstractBayeux.class);
      return (bayeux == null ? "cometd" : bayeux.getCometdContextName());
    } catch (Exception e) {
    }
    return cometdContextName;
  }
  
  public String getRestContextName() {
    return PortalContainer.getInstance().getRestContextName();
  }
  
}