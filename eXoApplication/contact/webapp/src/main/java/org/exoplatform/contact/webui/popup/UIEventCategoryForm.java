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
package org.exoplatform.contact.webui.popup;

import javax.jcr.RepositoryException;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.contact.CalendarUtils;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

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
  private static final Log log = ExoLogger.getExoLogger(UIEventCategoryForm.class);
  
  final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  private boolean isAddNew_ = true ;
  private EventCategory eventCategory_ = null ;
  public UIEventCategoryForm() throws Exception{
    addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
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
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIContactPortlet uiPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIEventForm uiEventForm = uiPortlet.findFirstComponentOfType(UIEventForm.class) ;
      String name = uiForm.getUIStringInput(UIEventCategoryForm.EVENT_CATEGORY_NAME).getValue() ;
      /*if(Utils.isEmptyField(name)) {
        uiApp.addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }*/
      /*if(!ContactUtils.isNameValid(name, CalendarUtils.EXTENDEDCHARACTER)) {
        uiApp.addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
        return ;
      }*/
      CalendarService calendarService = CalendarUtils.getCalendarService();
      String description = uiForm.getUIStringInput(UIEventCategoryForm.DESCRIPTION).getValue() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      EventCategory eventCat = new EventCategory() ;
      eventCat.setName(name) ;
      eventCat.setDescription(description) ;
      try {
        if(uiForm.isAddNew_) calendarService.saveEventCategory(username, eventCat, true) ;
        else { 
          eventCat = uiForm.getEventCategory() ;
          eventCat.setName(name) ;
          eventCat.setDescription(description) ;
          calendarService.saveEventCategory(username, eventCat, false) ; 
        }
        /*ActionResponse actResponse = event.getRequestContext().getResponse() ;
        actResponse.setEvent(new QName("RefreshCalendar"), null) ;*/
        UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTDETAIL) ;
        uiEventForm.refreshCategory() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
        uiPopupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } catch (RepositoryException e) {
        uiApp.addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
        return ;
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class SaveActionListener", e);
        }
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
