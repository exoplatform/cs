/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //template = "app:/templates/calendar/webui/UIEventForm.gtmpl",
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", 
    events = {
      @EventConfig(listeners = UIEventForm.SaveActionListener.class),
      @EventConfig(listeners = UIEventForm.CancelActionListener.class)
    }
)
public class UIEventForm extends UIFormTabPane implements UIPopupComponent, UISelector{
  final public static String TAB_EVENTDETAIL = "eventDetail".intern() ;
  final public static String TAB_EVENTSHARE = "eventShare".intern() ;
  final public static String TAB_EVENTATTENDER = "eventAttender".intern() ;
  
  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_CHECKALL = "allDay".intern() ;
  final public static String FIELD_REPEAT = "repeat".intern() ;
  final public static String FIELD_PLACE = "place".intern() ;
  final public static String FIELD_REMINDER = "reminder".intern() ;
  final public static String FIELD_TIMEREMINDER = "timeReminder".intern() ;
  final public static String FIELD_ATTACHMENT = "attachment".intern() ;
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  
  
  
  public UIEventForm() throws Exception {
    super("UIEventForm", false);
    UIFormInputWithActions eventDetailTab =  new UIFormInputWithActions(TAB_EVENTDETAIL) ;
    UIFormInputWithActions eventShareTab =  new UIFormInputWithActions(TAB_EVENTSHARE) ;
    UIEventAttenderTab eventAttenderTab = new UIEventAttenderTab(TAB_EVENTATTENDER) ;
    addChild(eventDetailTab) ;
    addChild(eventShareTab) ;
    addChild(eventAttenderTab) ;
    setRenderedChild(TAB_EVENTDETAIL) ;
    
  }
  
  public String[] getActions() {
    return new String[]{"Save", "Cancel"} ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateSelect(String selectField, String value) throws Exception {
    // TODO Auto-generated method stub
    
  }
  static  public class SaveActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIEventForm> {
    public void execute(Event<UIEventForm> event) throws Exception {
      UIEventForm uiForm = event.getSource() ;
    }
  }
}
