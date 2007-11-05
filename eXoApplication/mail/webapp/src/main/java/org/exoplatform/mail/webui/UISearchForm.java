/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAdvancedSearch;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 02, 2007 2:48:18 PM 
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
  final static private String FIELD_SEARCHVALUE = "search" ;
  
  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }
  
  static  public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiSearchForm = event.getSource();
      String text = uiSearchForm.getUIStringInput(FIELD_SEARCHVALUE).getValue();
      MessageFilter filter = new MessageFilter("Search"); 
      UIApplication uiApp = uiSearchForm.getAncestorOfType(UIApplication.class) ;
      if(text == null || text.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
        String whereQuery = "(jcr:contains(@" + Utils.EXO_TO + ", '" + text + "'))" +
        " or (jcr:contains(@" + Utils.EXO_FROM + ", '" + text + "'))" +
        " or (jcr:contains(@" + Utils.EXO_SUBJECT + ", '" + text + "'))" +
        " or (jcr:contains(@" + Utils.EXO_BODY + ", '" + text + "'))";
        filter.setViewQuery(whereQuery);
      }
      filter.setAccountId(MailUtils.getAccountId());
      
      
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      
      UIAdvancedSearch uiAdvancedSearch = uiPopupContainer.createUIComponent(UIAdvancedSearch.class, null, null);
      uiPopupContainer.addChild(uiAdvancedSearch) ;
      
      UIMessageList uiMessageList = uiAdvancedSearch.findFirstComponentOfType(UIMessageList.class);      
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      
      uiMessageList.setMessagePageList(mailService.getMessages(username, filter));
      uiMessageList.updateList();
      uiAdvancedSearch.showResult();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);

    }
  }
 
  static  public class AdvancedSearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiSearchForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      uiPopupContainer.setId("UIAdvancedSearch");
      
      UIAdvancedSearch uiAdvancedSearchForm = uiPopupContainer.createUIComponent(UIAdvancedSearch.class, null, null);          
      uiPopupContainer.addChild(uiAdvancedSearchForm) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;     
    }
  } 
}
