/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

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
      @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
      @EventConfig(listeners = UISharedForm.SelectUserActionListener.class, phase = Phase.DECODE),      
      @EventConfig(listeners = UISharedForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final static public String USER_NAME = "username".intern() ;
  private String calendarId_ ;
  public UISharedForm() throws Exception{
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    inputset.addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectUser") ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    actions.add(selectUserAction) ;
    inputset.setActionField(USER_NAME, actions) ;
    addChild(inputset) ;

  }
  public void setSelectedCalendarId(String id) { calendarId_ = id ;}
  
  public String[] getActions() {
    return new String[] {"Save","Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public void updateSelect(String selectField, String value) throws Exception {
    getUIStringInput(selectField).setValue(value) ;
    
  }  
  static  public class SaveActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(USER_NAME).getValue() ;
      if(names == null || names.length() < 1) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }      
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      List<String> receiverUser = new ArrayList<String>() ;
      if(names.indexOf(",") > 0) {
        String[] array = names.split(",") ;
        for(String name : array) {
          receiverUser.add(name.trim()) ;
        }
      }else {
        receiverUser.add(names.trim()) ;
      }      
      calendarService.shareCalendar(CalendarUtils.getCurrentUser(), uiForm.calendarId_, receiverUser) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendars uiCalendars = calendarPortlet.findFirstComponentOfType(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ;
      calendarPortlet.cancelAction() ;       
    }
  }
  static  public class SelectUserActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UIPopupAction childPopup = uiForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setSelectUser(true) ;
      uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.USER_NAME}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
  
}
