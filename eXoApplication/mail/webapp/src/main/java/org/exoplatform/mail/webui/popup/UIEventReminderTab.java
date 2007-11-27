/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 *          Nam Phung
 *          phunghainam@gmail.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    template = "app:/templates/mail/webui/UIEventReminderTab.gtmpl"
) 
public class UIEventReminderTab extends UIFormInputWithActions {
  final public static String FIELD_EMAIL_REMINDER = "mailReminder".intern() ;
  final public static String FIELD_EMAIL_TIME = "mailReminderTime".intern() ;
  final public static String FIELD_EMAIL_ADDRESS = "mailReminderAddress".intern() ;

  final public static String FIELD_POPUP_REMINDER = "popupReminder".intern() ;
  final public static String FIELD_POPUP_TIME = "popupReminderTime".intern() ;
  final public static String FIELD_SNOOZE_TIME = "snooze".intern() ;

  private Map<String, List<ActionData>> actionField_ ;
  public UIEventReminderTab(String arg0) throws Exception {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    actionField_ = new HashMap<String, List<ActionData>>() ;

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_EMAIL_REMINDER, FIELD_EMAIL_REMINDER, false)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_EMAIL_TIME, FIELD_EMAIL_TIME, getReminderTimes(5,60)));
    addUIFormInput(new UIFormTextAreaInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null)) ;
   
    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON) ;
    addEmailAddress.setActionName(UIEventForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UIEventForm.ACT_ADDEMAIL) ;
    
    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;
    setActionField(FIELD_EMAIL_ADDRESS, addMailActions) ;

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_POPUP_REMINDER, FIELD_POPUP_REMINDER, false)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_POPUP_TIME, FIELD_POPUP_TIME, getReminderTimes(5,60)));
    addUIFormInput(new UIFormSelectBox(FIELD_SNOOZE_TIME, FIELD_SNOOZE_TIME, getReminderTimes(5,60)));
  }
  
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  
  public List<SelectItemOption<String>> getReminderTimes(int steps, int maxValue) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(int i = 1; i <= maxValue/steps ; i++) {
      options.add(new SelectItemOption<String>(String.valueOf(i * steps) + " minutes", String.valueOf(i * steps))) ;
    }
    return options ;
  }
  
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }


}
