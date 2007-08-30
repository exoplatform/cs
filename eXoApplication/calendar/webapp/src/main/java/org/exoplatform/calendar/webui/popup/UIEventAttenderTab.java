/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
@ComponentConfig(template = "app:/templates/calendar/webui/UIPopup/UIEventAttenderTab.gtmpl")
public class UIEventAttenderTab extends UIFormInputWithActions {
  final public static String FIELD_DATEFROM = "dateFrom".intern() ;
  final public static String FIELD_DATETO = "dateTo".intern();
  final public static String FIELD_DATEALL = "dateAll".intern();
  final public static String FIELD_CURRENTATTENDER = "currentAttender".intern() ;
  
  private Map<String, String> participance_ = new HashMap<String, String>() ;
  private Map<String, UIComponent> participanceCheckBox_ = new HashMap<String, UIComponent>() ;
  
  public UIEventAttenderTab(String arg0) {
    super(arg0);
    setComponentConfig(getClass(), null) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_DATEFROM, FIELD_DATEFROM, new Date())) ;
    addUIFormInput(new UIFormDateTimeInput(FIELD_DATETO, FIELD_DATETO, new Date())) ;
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_DATEALL, FIELD_DATEALL, null)) ;
  }
  private Map<String, String> getParticipancs() {
    return participance_ ;
  }
  protected void setParticipancs(Map<String, String> participance) {
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
  }
  
  private String getFormName() { 
    UIForm uiForm = getAncestorOfType(UIForm.class);
    return uiForm.getId() ; 
  }
  private List<UIComponent> getAttenderFlields() {
    return new ArrayList<UIComponent>(participanceCheckBox_.values()) ;
  }
  private UIComponent getFromField() {
    return getChildById(FIELD_DATEFROM) ;
  }
  private UIComponent getToField() {
    return getChildById(FIELD_DATETO) ;
  }
  private UIComponent getAllDateField() {
    return getChildById(FIELD_DATEALL) ;
  }
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  

}
