/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import javax.jcr.RepositoryException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarViewContainer;
import org.exoplatform.calendar.webui.UIMiniCalendar;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
      @EventConfig(listeners = UIEventCategoryForm.ResetActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIEventCategoryForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEventCategoryForm extends UIForm {
  final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  private boolean isAddNew_ = true ;
  private EventCategory eventCategory_ = null ;
  public UIEventCategoryForm() throws Exception{
    addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null)
    .addValidator(EmptyFieldValidator.class)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
  }
  protected String getCategoryName() {return getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) {getUIStringInput(EVENT_CATEGORY_NAME).setValue(value) ;}

  protected String getCategoryDescription() {return getUIStringInput(DESCRIPTION).getValue() ;}
  protected void setCategoryDescription(String value) {getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;}

  public void reset() {
    super.reset() ;
    setAddNew(true);
    setEventCategory(null);
  }

  protected void setAddNew(boolean isAddNew) {
    this.isAddNew_ = isAddNew;
  }
  protected boolean isAddNew() {
    return isAddNew_;
  }

  protected void setEventCategory(EventCategory eventCategory) {
    this.eventCategory_ = eventCategory;
  }
  protected EventCategory getEventCategory() {
    return eventCategory_;
  }

  static  public class SaveActionListener extends EventListener<UIEventCategoryForm> {
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      UIEventCategoryManager uiManager = uiForm.getAncestorOfType(UIEventCategoryManager.class) ;
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String name = uiForm.getUIStringInput(UIEventCategoryForm.EVENT_CATEGORY_NAME).getValue() ;
      String description = uiForm.getUIStringInput(UIEventCategoryForm.DESCRIPTION).getValue() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      EventCategory eventCat = new EventCategory() ;
      eventCat.setName(name) ;
      eventCat.setDescription(description) ;
      try {
        if(uiForm.isAddNew_) calendarService.saveEventCategory(SessionsUtils.getSessionProvider(), username, eventCat, null, true) ;
        else { 
          eventCat = uiForm.getEventCategory() ;
          EventCategory newEventCategory = new EventCategory() ;
          newEventCategory.setName(name) ;
          newEventCategory.setDescription(uiForm.getCategoryDescription()) ;
          calendarService.saveEventCategory(SessionsUtils.getSessionProvider(), username, eventCat, newEventCategory, false) ; 
        }
        uiManager.updateGrid() ;
        uiForm.reset() ;
        UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
        UIMiniCalendar uiMiniCalendar = calendarPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.updateMiniCal() ;
        UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UICalendarViewContainer uiViewContainer = calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        uiViewContainer.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        if(uiPopupContainer != null) {
          System.out.println("\n\n uiPopupContainer " + uiPopupContainer.getId());
          UIEventForm uiEventForm = uiPopupContainer.getChild(UIEventForm.class) ;
          UITaskForm uiTaskForm = uiPopupContainer.getChild(UITaskForm.class) ;
          if(uiEventForm != null) uiEventForm.refreshCategory() ;
          if(uiTaskForm != null) uiTaskForm.refreshCategory() ;
        }
      } catch (RepositoryException e) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
        return ;
      } catch (Exception e) {
        e.printStackTrace() ;        
      }
    }
  }
  static  public class ResetActionListener extends EventListener<UIEventCategoryForm> {
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      uiForm.reset() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static  public class CancelActionListener extends EventListener<UIEventCategoryForm> {
    public void execute(Event<UIEventCategoryForm> event) throws Exception {
      UIEventCategoryForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      if(uiPopupAction.getAncestorOfType(UIPopupAction.class) != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction.getAncestorOfType(UIPopupAction.class));
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }
}
