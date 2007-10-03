/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.io.Writer;
import java.util.List;

import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
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
      @EventConfig(listeners = UICalendarCategoryManager.EditActionListener.class),
      @EventConfig(listeners = UICalendarCategoryManager.DeleteActionListener.class, confirm = "UICalendarCategoryManager.msg.confirm-delete")
    }
)
public class UICalendarCategoryManager extends UIContainer implements UIPopupComponent {
  public static String[] BEAN_FIELD = {"name"};
  private static String[] ACTION = {"Edit", "Delete"} ;
  public UICalendarCategoryManager() throws Exception {
    this.setName("UICalendarCategoryManager") ;
    UIGrid categoryList = addChild(UIGrid.class, null , "UICategoryList") ;
    categoryList.configure("id", BEAN_FIELD, ACTION) ;
    categoryList.getUIPageIterator().setId("CategoryIterator");
    addChild(UICalendarCategoryForm.class, null, null) ;
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
    w.write("<div id=\"UICalendarCategoryManager\" class=\"UICalendarCategoryManager\">");
    renderChildren();
    w.write("</div>");
  }
  public void updateGrid() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<GroupCalendarData> categories = calService.getCalendarCategories(username) ;
    UIGrid uiGrid = getChild(UIGrid.class) ; 
    ObjectPageList objPageList = new ObjectPageList(categories, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;   
  }

  static  public class EditActionListener extends EventListener<UICalendarCategoryManager> {
    public void execute(Event<UICalendarCategoryManager> event) throws Exception {
      UICalendarCategoryManager uiManager = event.getSource() ;
      UICalendarCategoryForm uiForm = uiManager.getChild(UICalendarCategoryForm.class) ;
      uiForm.setAddNew(false) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      CalendarCategory category = calService.getCalendarCategory(username, categoryId) ;
      uiForm.setCategoryId(category.getId()) ;
      uiForm.setCategoryName(category.getName()) ;
      uiForm.setCategoryDescription(category.getDescription()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UICalendarCategoryManager> {
    public void execute(Event<UICalendarCategoryManager> event) throws Exception {
      UICalendarCategoryManager uiManager = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiManager.getAncestorOfType(UICalendarPortlet.class) ;
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiManager.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      calService.removeCalendarCategory(username, calendarCategoryId) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManager) ;
    }
  }
}
