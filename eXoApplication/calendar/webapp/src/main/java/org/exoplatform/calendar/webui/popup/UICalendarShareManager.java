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

import java.io.Writer;
import java.util.List;

import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 3, 2007  
 */
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,
    events = {
      @EventConfig(listeners = UICalendarShareManager.EditActionListener.class),
      @EventConfig(listeners = UICalendarShareManager.DeleteActionListener.class, confirm = "UICalendarCategoryManager.msg.confirm-delete")
    }
)
public class UICalendarShareManager extends UIContainer implements UIPopupComponent {
  public static String[] BEAN_FIELD = {"name"};
  private static String[] ACTION = {"Edit", "Delete"} ;
  public UICalendarShareManager() throws Exception {
    this.setName("UICalendarCategoryManager") ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UIPermissionList") ;
    categoryList.configure("id", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("PermissionIterator");
    addChild(UISharedForm.class, null, null) ;
    updateGrid() ;
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    w.write("<div id=\"UICalendarShareManager\" class=\"UICalendarShareManager\">");
    renderChildren();
    w.write("</div>");
  }
  public void updateGrid() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<GroupCalendarData> categories = calService.getCalendarCategories(SessionsUtils.getSessionProvider(), username) ;
    UIGrid uiGrid = getChild(UIGrid.class) ; 
    ObjectPageList objPageList = new ObjectPageList(categories, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;   
  }
  public void resetForm() {
    getChild(UICalendarCategoryForm.class).reset() ;
  }
  static  public class EditActionListener extends EventListener<UICalendarShareManager> {
    public void execute(Event<UICalendarShareManager> event) throws Exception {
      UICalendarShareManager uiManager = event.getSource() ;
      UICalendarCategoryForm uiForm = uiManager.getChild(UICalendarCategoryForm.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.init(categoryId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UICalendarShareManager> {
    public void execute(Event<UICalendarShareManager> event) throws Exception {
      UICalendarShareManager uiManager = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      calService.removeCalendarCategory(SessionsUtils.getSessionProvider(), username, calendarCategoryId) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      uiManager.updateGrid() ;
      uiManager.resetForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
