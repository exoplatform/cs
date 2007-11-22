/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;


/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 01, 2007 8:48:18 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIMessageFilter.gtmpl",
    events = {
      @EventConfig(listeners = UIMessageFilter.SelectFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.AddFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.DeleteFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.SaveActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.CancelActionListener.class)
    }
)
public class UIMessageFilter extends UIForm implements UIPopupComponent{
  
  private String selectedFilterId ;
  
  public UIMessageFilter() throws Exception {
    if (getFilters().size() > 0) {
      setSelectedFilterId(getFilters().get(0).getId());
    }
  }
  
  public String getSelectedFilterId() {return this.selectedFilterId; }
  public void setSelectedFilterId(String filterId) { this.selectedFilterId = filterId; }
  
  public MessageFilter getSelectedFilter() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    if (getSelectedFilterId() != null) {
      return null;//mailSrv.getFilterById(username, accountId, getSelectedFilterId());
    } else {
      return null;
    }
  }
  
  public List<MessageFilter> getFilters() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getFilters(username, accountId);
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class SelectFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      String filterId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMailPortlet mailPortlet = uiMessageFilter.getAncestorOfType(UIMailPortlet.class);
      uiMessageFilter.setSelectedFilterId(filterId);
      event.getRequestContext().addUIComponentToUpdateByAjax(mailPortlet.getChild(UIPopupAction.class)) ;
    }
  }
  
  static  public class AddFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiMessageFilter.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAddMessageFilter.class, 650) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static  public class DeleteFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      UIMailPortlet mailPortlet = uiMessageFilter.getAncestorOfType(UIMailPortlet.class);
      String filterId = uiMessageFilter.getSelectedFilterId();
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      MailService mailServ = MailUtils.getMailService();
      try {
        mailServ.removeFilter(username, accountId, filterId);
        event.getRequestContext().addUIComponentToUpdateByAjax(mailPortlet.getChild(UIPopupAction.class)) ;
      } catch(Exception e) {
        e.printStackTrace();
      } 
    }
  }

  static  public class SaveActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
