/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
    //template = "app:/templates/calendar/webui/UICalendarCategoryForm.gtmpl",
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarCategoryForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarCategoryForm.CancelActionListener.class)
    }
)
public class UICalendarCategoryForm extends UIForm implements UIPopupComponent{
  final static String CATEGORY_NAME = "categoryName" ;
  final static String DESCRIPTION = "description" ;
  private boolean isAddNew = true ;
  public UICalendarCategoryForm() {
    addUIFormInput(new UIFormStringInput(CATEGORY_NAME, null)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  static  public class SaveActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      String categoryName = uiForm.getUIStringInput(CATEGORY_NAME).getValue() ;
      String description = uiForm.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
      UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
      if(CalendarUtils.isEmpty(categoryName)) {
        app.addMessage(new ApplicationMessage("UICalendarCategoryForm.msg.category-name-required", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      CalendarCategory category = new CalendarCategory() ;
      category.setId("category" + IdGenerator.generate()) ;
      category.setName(categoryName) ;
      category.setDescription(description) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      calendarService.saveCalendarCategory(username, category, uiForm.isAddNew) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarForm uiCalendarForm = calendarPortlet.findFirstComponentOfType(UICalendarForm.class) ;
      if(uiCalendarForm != null) uiCalendarForm.reloadCategory() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      if(uiPopupContainer != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getAncestorOfType(UIPopupAction.class)) ;
      }  
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; 
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      if(uiPopupContainer != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getAncestorOfType(UIPopupAction.class)) ;
      }
    }
  }

}
