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

import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
      @EventConfig(listeners = UISendCalendarForm.SendActionListener.class),      
      @EventConfig(phase=Phase.DECODE, listeners = UISendCalendarForm.CancelActionListener.class)
    }
)

public class UISendCalendarForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_CALENDAR = "calendar".intern() ;
  public static final String FIELD_EMAIL = "email".intern() ;
  private String calendarId_ = null ;

  public UISendCalendarForm() {
    addUIFormInput(new UIFormStringInput(FIELD_CALENDAR, FIELD_CALENDAR, null).setEditable(false)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_EMAIL, FIELD_EMAIL, null)) ;
  }

  public void init(String calendarId) throws Exception {
    calendarId_ = calendarId ;
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    Calendar cal = calService.getUserCalendar(SessionsUtils.getSessionProvider(), username, calendarId) ;
    setCalendarName(cal.getName()) ;
  }

  protected String getCalendarId() {
    return calendarId_ ;
  }
  protected String getCalendarName() {
    return getUIStringInput(FIELD_CALENDAR).getValue() ;
  }
  protected void setCalendarName(String value) {
    getUIStringInput(FIELD_CALENDAR).setValue(value) ;
  }

  protected String getEmailAddress() {
    return getUIFormTextAreaInput(FIELD_EMAIL).getValue() ;
  }  
  protected void setEmailAddress(String value) {
    getUIFormTextAreaInput(FIELD_EMAIL).setValue(value) ;
  } 
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
  static  public class SendActionListener extends EventListener<UISendCalendarForm> {
    public void execute(Event<UISendCalendarForm> event) throws Exception {
      System.out.println("\n\n SendActionListener");
    }
  }
  static  public class CancelActionListener extends EventListener<UISendCalendarForm> {
    public void execute(Event<UISendCalendarForm> event) throws Exception {
    }
  }
 
}
