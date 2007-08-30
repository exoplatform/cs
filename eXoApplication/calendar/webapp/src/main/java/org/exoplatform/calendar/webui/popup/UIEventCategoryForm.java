/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UIListView;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

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
      @EventConfig(listeners = UIEventCategoryForm.SaveActionListener.class),
      @EventConfig(listeners = UIEventCategoryForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEventCategoryForm extends UIForm implements UIPopupComponent{
  final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  public UIEventCategoryForm() throws Exception{
    addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null)
                       .addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  static  public class SaveActionListener extends EventListener<UIEventCategoryForm> {
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      CalendarService calendarService = 
        (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
      String name = uiForm.getUIStringInput(UIEventCategoryForm.EVENT_CATEGORY_NAME).getValue() ;
      String description = uiForm.getUIStringInput(UIEventCategoryForm.DESCRIPTION).getValue() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      EventCategory eventCat = new EventCategory() ;
      eventCat.setName(name) ;
      eventCat.setDescription(description) ;
      calendarService.saveEventCategory(username, eventCat, true) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiPopupContainer == null) calendarPortlet.cancelAction() ;
      else {
        uiPopupContainer.getChild(UIPopupAction.class).deActivate() ;
        UIEventForm uiEventForm = uiPopupContainer.getChild(UIEventForm.class) ;
        uiEventForm.refreshCategory() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getChild(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
      }
      UIListView uiListView = calendarPortlet.getChild(UICalendarWorkingContainer.class)
      .getChild(UICalendarViewContainer.class).getChild(UIListView.class) ;
      uiListView.update() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiListView) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventCategoryForm> {
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      if(uiPopupContainer == null) calendarPortlet.cancelAction() ;
      else {
        uiPopupContainer.getChild(UIPopupAction.class).deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getChild(UIPopupAction.class));
      }
    }
  }
}
