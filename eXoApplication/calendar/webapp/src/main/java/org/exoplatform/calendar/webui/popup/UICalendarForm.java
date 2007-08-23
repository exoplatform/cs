/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarContainer;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.calendar.webui.UICalendarWorkingContainer;
import org.exoplatform.calendar.webui.UICalendars;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //template = "app:/templates/calendar/webui/UICalendarForm.gtmpl",
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarForm.SelectGroupActionListener.class),
      @EventConfig(listeners = UICalendarForm.SelectPermissionActionListener.class),
      @EventConfig(listeners = UICalendarForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UICalendarForm extends UIFormTabPane implements UIPopupComponent{
  final public static String DISPLAY_NAME = "displayName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String CATEGORY = "category" ;
  final public static String ISPUBLIC = "isPublic" ;
  final public static String SHARED_GROUPS = "sharedGroups" ;
  final public static String EDIT_PERMISSION = "editPermission" ;
  
  public UICalendarForm() throws Exception{
    super("UICalendarForm", false);
    
    UIFormInputWithActions calendarDetail = new UIFormInputWithActions("calendarDetail") ;
    calendarDetail.addUIFormInput(new UIFormStringInput(DISPLAY_NAME, DISPLAY_NAME, null).addValidator(EmptyFieldValidator.class)) ;
    calendarDetail.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    List<CalendarCategory> categories = calendarService.getCategories(username) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(CalendarCategory category : categories) {
      options.add(new SelectItemOption<String>(category.getName(), category.getId())) ;
    }
    calendarDetail.addUIFormInput(new UIFormSelectBox(CATEGORY, CATEGORY, options)) ;
    calendarDetail.setRendered(true) ;
    addChild(calendarDetail) ;
    
    UIFormInputWithActions sharing = new UIFormInputWithActions("sharing") ;
    List<ActionData> actions = new ArrayList<ActionData> () ;
    sharing.addUIFormInput(new UIFormCheckBoxInput<Boolean>(ISPUBLIC, ISPUBLIC, null)) ;
    sharing.addUIFormInput(new UIFormTextAreaInput(SHARED_GROUPS, SHARED_GROUPS, null)) ;
    
    ActionData sharedGroups = new ActionData() ;
    sharedGroups.setActionListener("SelectGroup") ;
    sharedGroups.setActionName("SharedGroups") ;
    sharedGroups.setActionType(ActionData.TYPE_ICON) ;
    sharedGroups.setCssIconClass("AddIcon16x16 SelectMemberIcon") ;    
    actions.add(sharedGroups) ;
    sharing.setActionField(SHARED_GROUPS, actions) ;
    
    actions = new ArrayList<ActionData> () ;
    sharing.addUIFormInput(new UIFormStringInput(EDIT_PERMISSION, null, null)) ;
    ActionData editPermissions = new ActionData() ;
    editPermissions.setActionListener("SelectPermission") ;
    editPermissions.setActionName("EditPermission") ;
    editPermissions.setActionType(ActionData.TYPE_ICON) ;
    editPermissions.setCssIconClass("AddIcon16x16 SelectUserIcon") ;    
    actions.add(editPermissions) ;
    sharing.setActionField(EDIT_PERMISSION, actions) ;
    sharing.setRendered(false) ;
    addChild(sharing) ;
  }
  
  public String[] getActions(){
    return new String[]{"Save", "Cancel"} ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  static  public class SelectGroupActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      System.out.println(" ============= > Link");
    }
  }
  
  static  public class SelectPermissionActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      System.out.println(" ============= > Hello");
    }
  }
  
  static  public class SaveActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
      Calendar calendar = new Calendar() ;
      calendar.setName(uiForm.getUIStringInput(DISPLAY_NAME).getValue()) ;
      calendar.setDescription(uiForm.getUIFormTextAreaInput(DESCRIPTION).getValue()) ;
      calendar.setCategoryId(uiForm.getUIFormSelectBox(CATEGORY).getValue()) ;
      boolean isPublic = uiForm.getUIFormCheckBoxInput(ISPUBLIC).isChecked() ;
      calendar.setPublic(isPublic) ;
      if(isPublic) {
        String groups = uiForm.getUIFormTextAreaInput(SHARED_GROUPS).getValue() ;
        if(groups != null && groups.length() > 0) {
          calendar.setGroups(groups.split(",")) ;
        }else {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendarForm.msg.group-empty", null, ApplicationMessage.WARNING) ) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        String editPermission = uiForm.getUIStringInput(EDIT_PERMISSION).getValue() ;
        if(editPermission != null && editPermission.length() > 0) {
          calendar.setEditPermission(editPermission.split(",")) ;
        }
        calendarService.saveGroupCalendar(calendar, true) ;
      }else {
        calendarService.saveUserCalendar(username, calendar, true) ;
      }
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
      UICalendars uiCalendars = calendarPortlet.getChild(UICalendarWorkingContainer.class).getChild(UICalendarContainer.class).getChild(UICalendars.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendars) ; ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarForm> {
    public void execute(Event<UICalendarForm> event) throws Exception {
      UICalendarForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
}
