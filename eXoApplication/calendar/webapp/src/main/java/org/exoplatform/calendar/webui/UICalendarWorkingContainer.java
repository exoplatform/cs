/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,  
    template =  "app:/templates/calendar/webui/UICalendarWorkingContainer.gtmpl"

)
public class UICalendarWorkingContainer extends UIContainer  {
  private CalendarSetting calendarSetting_ ;
  
  public UICalendarWorkingContainer() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    calendarSetting_ = calService.getCalendarSetting(username) ;
    addChild(UICalendarContainer.class, null, null).setRendered(true) ;
    addChild(UICalendarViewContainer.class, null, null).setRendered(true) ;
  }

  public void setCalendarSetting(CalendarSetting calendarSetting) {
    this.calendarSetting_ = calendarSetting;
  }

  public CalendarSetting getCalendarSetting() {
    return calendarSetting_;
  }
}
