/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UISearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UISearchForm.SearchActionListener.class),
      @EventConfig(listeners = UISearchForm.AdvancedSearchActionListener.class)
    }
)
public class UISearchForm extends UIForm {
  final static  private String FIELD_SEARCHVALUE = "inputValue" ;
  
  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }
  
  static  public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      //UISearchForm uiForm = event.getSource() ;
      System.out.println("========> SearchActionListener");
    }
  }
 
  static  public class AdvancedSearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiSearchForm = event.getSource() ;
      System.out.println("========> AdvancedSearchActionListener");   
      
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      uiPopupContainer.setId("AdvancedSearchActionListener");
      
      UIAdvancedSearchForm uiAdvancedSearchForm = uiPopupContainer.createUIComponent(UIAdvancedSearchForm.class, null, null);
      
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiSearchForm.getApplicationComponent(MailService.class);
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelectAccount = uiNavigation.getChild(UISelectAccount.class) ;
      String accountId = uiSelectAccount.getSelectedValue() ; 
   
      List<Folder> folderList = new ArrayList<Folder>();
      folderList.addAll(mailService.getFolders(username, accountId, false)); 
      folderList.addAll(mailService.getFolders(username, accountId, true));            
      uiAdvancedSearchForm.setFolderList(folderList);
      uiPopupContainer.addChild(uiAdvancedSearchForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      
      
    }
  } 
}
