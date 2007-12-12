/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
@ComponentConfig(template = "app:/templates/calendar/webui/UIPopup/UIEventAttenderTab.gtmpl")
public class UIEventAttenderTab extends UIFormInputWithActions {
  final public static String FIELD_FROM_DATE = "dateFrom".intern() ;
  final public static String FIELD_TO_DATE = "dateTo".intern();
  final public static String FIELD_FROM_TIME = "timeFrom".intern() ;
  final public static String FIELD_TO_TIME = "timeTo".intern();

  final public static String FIELD_DATEALL = "dateAll".intern();
  final public static String FIELD_CURRENTATTENDER = "currentAttender".intern() ;

  //private Map<String, String> participance_ = new HashMap<String, String>() ;
  private Map<String, UIComponent> participanceCheckBox_ = new HashMap<String, UIComponent>() ;
  private Calendar calendar_ ;
  public UIEventAttenderTab(String arg0) {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    calendar_ = GregorianCalendar.getInstance();
    calendar_.setLenient(false) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM_DATE, FIELD_FROM_DATE, new Date(), false)) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO_DATE, FIELD_TO_DATE, new Date(), false)) ;
    addUIFormInput(new UIFormComboBox(FIELD_FROM_TIME, FIELD_FROM_TIME, getTimes())) ;
    addUIFormInput(new UIFormComboBox(FIELD_TO_TIME, FIELD_TO_TIME, getTimes())) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_DATEALL, FIELD_DATEALL, null)) ;
  }

  private List<SelectItemOption<String>> getTimes() {
    return CalendarUtils.getTimesSelectBoxOptions(CalendarUtils.TIMEFORMAT) ;
  }

  protected UIFormComboBox getUIFormComboBox(String id) {
    return findComponentById(id) ;
  }
  private Map<String, String> getParticipancs() {
    return getAncestorOfType(UIEventForm.class).participants_; 
  }
  /*protected void setParticipancs(Map<String, String> participance) {
    participance_  = participance;
    for(String oldId : participanceCheckBox_.keySet()) {
      removeChildById(oldId) ;
    }
    participanceCheckBox_.clear() ;
    for(String id : participance_.keySet()) {
      UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(id,id, null) ;
      addChild(uiCheckBox) ;
      participanceCheckBox_.put(id, uiCheckBox) ;
    }
  }*/
  
  protected void moveNextDay() {
    calendar_.add(Calendar.DATE, 1) ;
  }
  protected void movePreviousDay() {
    calendar_.add(Calendar.DATE, -1) ;
  }
  protected List<String> getDisplayTimes(String timeFormat, int timeInterval) {
    List<String> times = new ArrayList<String>() ;
    Calendar cal = CalendarUtils.getBeginDay(GregorianCalendar.getInstance()) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    for(int i = 0; i < 24*(60/timeInterval); i++) {
      times.add(df.format(cal.getTime())) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval) ;
    }
    return times ;
  }
  private UIForm getParentFrom() {
    return getAncestorOfType(UIForm.class) ;
  }
  private String getFormName() { 
    UIForm uiForm = getAncestorOfType(UIForm.class);
    return uiForm.getId() ; 
  }
  private List<UIComponent> getAttenderFlields() {
    return new ArrayList<UIComponent>(participanceCheckBox_.values()) ;
  }
  private UIComponent getFromField() {
    return getChildById(FIELD_FROM_DATE) ;
  }
  private UIComponent getFromTimeField() {
    return getChildById(FIELD_FROM_TIME) ;
  }
  private UIComponent getToField() {
    return getChildById(FIELD_TO_DATE) ;
  }
  private UIComponent getToTimeField() {
    return getChildById(FIELD_TO_TIME) ;
  }
  private UIComponent getAllDateField() {
    return getChildById(FIELD_DATEALL) ;
  }
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }


}
