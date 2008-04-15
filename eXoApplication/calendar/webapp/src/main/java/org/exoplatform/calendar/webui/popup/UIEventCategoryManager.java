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

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
      @EventConfig(listeners = UIEventCategoryManager.EditActionListener.class),
      @EventConfig(listeners = UIEventCategoryManager.DeleteActionListener.class, confirm = "UIEventCategoryManager.msg.confirm-delete")
    }
)
public class UIEventCategoryManager extends UIContainer implements UIPopupComponent {
  public static String[] BEAN_FIELD = {"name"};
  private static String[] ACTION = {"Edit", "Delete"} ;
  public UIEventCategoryManager() throws Exception {
    this.setName("UIEventCategoryManager") ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UIEventCategoryList") ;
    categoryList.configure("name", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("EventCategoryIterator");
    addChild(UIEventCategoryForm.class, null, null) ;
    updateGrid() ;
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void resetForm() {
    getChild(UIEventCategoryForm.class).reset() ;
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    w.write("<div id=\"UIEventCategoryManager\" class=\"UIEventCategoryManager\">");
    renderChildren();
    w.write("</div>");
  }
  public void updateGrid() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<EventCategory>  categories = calService.getEventCategories(getSession(), username) ;
    UIGrid uiGrid = getChild(UIGrid.class) ; 
    ObjectPageList objPageList = new ObjectPageList(categories, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;   
  }
  private SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  static  public class EditActionListener extends EventListener<UIEventCategoryManager> {
    public void execute(Event<UIEventCategoryManager> event) throws Exception {
      UIEventCategoryManager uiManager = event.getSource() ;
      UIEventCategoryForm uiForm = uiManager.getChild(UIEventCategoryForm.class) ;
      uiForm.setAddNew(false) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      EventCategory category = calService.getEventCategory(uiManager.getSession(), username, categoryId) ;
      uiForm.setEventCategory(category) ;
      uiForm.setCategoryName(category.getName()) ;
      uiForm.setCategoryDescription(category.getDescription()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UIEventCategoryManager> {
    public void execute(Event<UIEventCategoryManager> event) throws Exception {
      UIEventCategoryManager uiManager = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
      String eventCategoryName = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      calService.removeEventCategory(uiManager.getSession(), username, eventCategoryName) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
      UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.updateCategory() ;
      uiViewContainer.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      //uiMiniCalendar.updateMiniCal() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
      uiManager.updateGrid() ;
      uiManager.resetForm() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
      UIEventDetailTab uiEventDetailTab = calendarPortlet.findFirstComponentOfType(UIEventDetailTab.class) ;
      UITaskDetailTab uiTaskDetailTab = calendarPortlet.findFirstComponentOfType(UITaskDetailTab.class) ;
      if(uiEventDetailTab != null) { 
        uiEventDetailTab.getUIFormSelectBox(UIEventDetailTab.FIELD_CATEGORY).setOptions(UIEventForm.getCategory());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventDetailTab) ;
       }
      if(uiTaskDetailTab != null) {
        uiTaskDetailTab.getUIFormSelectBox(UITaskDetailTab.FIELD_CATEGORY).setOptions(UIEventForm.getCategory());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTaskDetailTab) ;
      }
    }
  }
}
