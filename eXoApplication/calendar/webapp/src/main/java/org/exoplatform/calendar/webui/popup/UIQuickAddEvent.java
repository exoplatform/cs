/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

    @ComponentConfig(
        lifecycle = UIFormLifecycle.class,
        template = "app:/templates/calendar/webui/UIPopup/UIQuickAddEvent.gtmpl",
        events = {
          @EventConfig(listeners = UIQuickAddEvent.SaveActionListener.class),
          @EventConfig(listeners = UIQuickAddEvent.AddCategoryActionListener.class, phase = Phase.DECODE),
          //@EventConfig(listeners = UIEventForm.AddAttachmentActionListener.class, phase = Phase.DECODE),
          @EventConfig(listeners = UIQuickAddEvent.CancelActionListener.class)
        }
    )
public class UIQuickAddEvent extends UIForm {

  private Map<String, String> participance_ = new HashMap<String, String>() ;
  private Map<String, UIComponent> participanceCheckBox_ = new HashMap<String, UIComponent>() ;
  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;

  public UIQuickAddEvent() throws Exception {
    UIPageIterator uiPageUIterator = createUIComponent(UIPageIterator.class, "UIPageIterator",null) ;
    setComponentConfig(getClass(), null) ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CALENDAR, FIELD_CALENDAR, getCalendar())) ;
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_CATEGORY) ;
    inputSet.addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, UIEventForm.getCategory())) ;
    ActionData addCategoryAction = new ActionData() ;
    addCategoryAction.setActionType(ActionData.TYPE_ICON) ;
    addCategoryAction.setActionName("AddCategory") ;
    addCategoryAction.setActionListener("AddCategory") ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    actions.add(addCategoryAction) ;
    inputSet.setActionField(FIELD_CATEGORY,actions) ;
    addUIFormInput(inputSet) ;

    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date()));
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date()));
    addUIFormInput(new UIFormSelectBox(FIELD_FROM_TIME, FIELD_FROM_TIME, getTimes()));
    addUIFormInput(new UIFormSelectBox(FIELD_TO_TIME, FIELD_TO_TIME, getTimes()));
  }

  private List<SelectItemOption<String>> getCalendar() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Calendar> calendars = calendarService.getUserCalendars(username) ;
    for(Calendar c : calendars) {
      options.add(new SelectItemOption<String>(c.getName(), c.getId())) ;
    }
    return options ;
  }
  private List<SelectItemOption<String>> getTimes() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(UIEventForm.ITEM_REPEAT, UIEventForm.ITEM_REPEAT)) ;
    options.add(new SelectItemOption<String>(UIEventForm.ITEM_UNREPEAT, UIEventForm.ITEM_UNREPEAT)) ;
    return options ;
  }

  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
    }
  }

  static  public class AddCategoryActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
    }
  }

  static  public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
    }
  }
}
