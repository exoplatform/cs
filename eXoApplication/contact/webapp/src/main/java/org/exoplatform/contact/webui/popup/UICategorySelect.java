/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
      @EventConfig(listeners = UICategorySelect.AddCategoryActionListener.class),
      @EventConfig(listeners = UICategorySelect.OnchangeActionListener.class)    
    }
)
public class UICategorySelect extends UIForm {
  public static final String INPUT_CATEGORY = "categoryInput";
  public static final String FIELD_CATEGORY = "category";
  
  public UICategorySelect() throws Exception {
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

  public String getSelectedCategory() {
    UIFormInputWithActions input = getChildById(INPUT_CATEGORY) ;
    return input.getUIFormSelectBox(FIELD_CATEGORY).getValue() ;
  }

  public List<SelectItemOption<String>> getCategoryList() throws Exception {
    ContactService contactService = ContactUtils.getContactService() ;
    String username = ContactUtils.getCurrentUser();
    List<ContactGroup> contactGroups =  contactService.getGroups(username);
    List<SelectItemOption<String>> categories = new ArrayList<SelectItemOption<String>>() ; 
    for(ContactGroup contactGroup : contactGroups)
      categories.add(new SelectItemOption<String>(contactGroup.getName(),contactGroup.getId() )) ;
    return categories ;
  }
  
  public void setCategoryList(List<SelectItemOption<String>> options ) {
    UIFormInputWithActions iput = getChildById(INPUT_CATEGORY) ;
     iput.getUIFormSelectBox(FIELD_CATEGORY).setOptions(options) ;
  }
  
  public void setValue(String contactId) throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    Contact contact = contactService.getContact(username, contactId);
    if (contact != null && contact.getCategories().length > 0) getUIFormSelectBox(FIELD_CATEGORY).setValue(contact.getCategories()[0]) ;
  }
  public void disableSelect() { getUIFormSelectBox(FIELD_CATEGORY).setEnable(false) ; }

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
      UICategoryForm uiCategoryForm = popupAction.createUIComponent(UICategoryForm.class, null, "UICategoryForm") ;
      popupAction.activate(uiCategoryForm, 600, 0 , true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

}
