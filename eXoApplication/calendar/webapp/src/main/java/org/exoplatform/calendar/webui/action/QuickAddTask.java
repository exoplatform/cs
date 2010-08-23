/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.calendar.webui.action;

import org.exoplatform.calendar.webui.listener.ActionListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 18, 2010  
 */
@ComponentConfig(

                 events = {
                     @EventConfig(listeners = QuickAddTask.AddActionListener.class)
                 }
)

public class QuickAddTask extends UIComponent {

  static public class AddActionListener extends ActionListener<QuickAddTask> {
    protected void processEvent(Event<QuickAddTask> event) throws Exception {
      // TODO Auto-generated method stub
      
    }
    /*
    public void pro(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.getCalendarOption().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<EventCategory> eventCategories = CalendarUtils.getCalendarService().getEventCategories(CalendarUtils.getCurrentUser()) ;
      if(eventCategories.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-category-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String formTime = CalendarUtils.getCurrentTime(uiActionBar) ;//event.getRequestContext().getRequestParameter(CURRENTTIME) ;
      String categoryId = event.getRequestContext().getRequestParameter(CATEGORYID) ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarWorkingContainer workContainer = uiPortlet.findFirstComponentOfType(UICalendarWorkingContainer.class) ;
      workContainer.getChild(UIPopupWindow.class).setShow(true) ;
    }
     */
  }

}
