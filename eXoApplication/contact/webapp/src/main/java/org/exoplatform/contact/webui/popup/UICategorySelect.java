/*
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
 */
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Aug 24, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UICategorySelect.gtmpl", 
    events = {
      @EventConfig(listeners = UICategorySelect.AddCategoryActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UICategorySelect.OnchangeActionListener.class)    
    }
)
public class UICategorySelect extends UIForm {
  public static final String INPUT_CATEGORY = "categoryInput";
  public static final String FIELD_CATEGORY = "category";
  private Map<String, String> privateGroupMap_ = new LinkedHashMap<String, String>() ;  
  public UICategorySelect() { }
  
  public void setPrivateGroupMap(Map<String, String> map) throws Exception { 
    privateGroupMap_ = map ; 
    UIFormInputWithActions input = new UIFormInputWithActions(INPUT_CATEGORY) ;
    input.addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, getCategoryList())) ;
    UIFormSelectBox uiSelectBox = input.getUIFormSelectBox(FIELD_CATEGORY) ;
    uiSelectBox.setOnChange("Onchange") ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData addAction = new ActionData() ;
    addAction.setActionType(ActionData.TYPE_ICON) ;
    addAction.setActionListener("AddCategory") ;
    addAction.setActionName("AddCategory") ;
    actions.add(addAction) ;
    input.setActionField(FIELD_CATEGORY, actions) ;
    addUIFormInput(input) ;  
  }
  public Map<String, String> getPrivateGroupMap() { return privateGroupMap_ ; }
  
  public String getSelectedCategory() {
    UIFormInputWithActions input = getChildById(INPUT_CATEGORY) ;
    return input.getUIFormSelectBox(FIELD_CATEGORY).getValue() ;
  }
  public void setValue(String groupId) throws Exception {
    UIFormInputWithActions input = getChildById(INPUT_CATEGORY) ;
    input.getUIFormSelectBox(FIELD_CATEGORY).setValue(groupId) ;
  }

  public List<SelectItemOption<String>> getCategoryList() throws Exception {
    List<SelectItemOption<String>> categories = new ArrayList<SelectItemOption<String>>() ;
    for(String group : privateGroupMap_.keySet())
      categories.add(new SelectItemOption<String>(ContactUtils.encodeHTML(privateGroupMap_.get(group)), group)) ;
    return categories ;
  }
  
  public void setCategoryList(List<SelectItemOption<String>> options ) {
    UIFormInputWithActions input = getChildById(INPUT_CATEGORY) ;
      input.getUIFormSelectBox(FIELD_CATEGORY).setOptions(options) ;
    // cs- 1628
    privateGroupMap_.clear() ;
    for (SelectItemOption<String> option : options) {
      privateGroupMap_.put(option.getValue(), option.getLabel()) ;
    }
  }

  static  public class OnchangeActionListener extends EventListener<UICategorySelect> {
    public void execute(Event<UICategorySelect> event) throws Exception {
      UICategorySelect uiCategorySelect = event.getSource() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategorySelect) ;
    }
  }
  
  static  public class AddCategoryActionListener extends EventListener<UICategorySelect> {
    public void execute(Event<UICategorySelect> event) throws Exception {
      UICategorySelect uiCategorySelect = event.getSource() ;  
      UIPopupContainer popupContainer = uiCategorySelect.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UICategoryForm.class, 425) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

}
