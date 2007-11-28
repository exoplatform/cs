/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
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
      @EventConfig(listeners = UIMessageFilter.EditFilterActionListener.class),
      @EventConfig(listeners = UIMessageFilter.DeleteFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.CloseActionListener.class)
    }
)
public class UIMessageFilter extends UIForm implements UIPopupComponent{
  public static final String CONDITION_CONTAIN = "contains".intern();
  public static final String CONDITION_NOT_CONTAIN = "doesn't contain".intern();
  public static final String CONDITION_IS = "is".intern();
  public static final String CONDITION_NOT_IS = "isn't".intern();
  public static final String CONDITION_END_WITH = "ends with".intern();
  public static final String CONDITION_START_WITH = "starts with".intern();
  
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
      return mailSrv.getFilterById(username, accountId, getSelectedFilterId());
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
  
  public Folder getFolder() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getFolder(username, accountId, getSelectedFilter().getApplyFolder());
  }
  
  public Tag getTag() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getTag(username, accountId, getSelectedFilter().getApplyTag());
  }
  
  public String getCondition(int i) throws Exception {
    switch(i) {
      case Utils.CONDITION_CONTAIN :
        return CONDITION_CONTAIN;
      case Utils.CONDITION_NOT_CONTAIN :
        return CONDITION_NOT_CONTAIN ;
      case Utils.CONDITION_IS :
        return CONDITION_IS;
      case Utils.CONDITION_NOT_IS:
        return CONDITION_NOT_CONTAIN;
      case Utils.CONDITION_STARTS_WITH:
        return CONDITION_START_WITH;
      case Utils.CONDITION_ENDS_WITH:
        return CONDITION_END_WITH;
      default :
        return CONDITION_CONTAIN;
    }
  }
  
  public String[] getActions() { return new String[]{"Close"}; }
  
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
  
  static  public class EditFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiMessageFilter.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddMessageFilter uiEditMessageFilter = uiChildPopup.createUIComponent(UIAddMessageFilter.class, null, null);
      uiChildPopup.activate(uiEditMessageFilter, 650, 0, false) ;
      uiEditMessageFilter.setCurrentFilter(uiMessageFilter.getSelectedFilter());
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
  
  static  public class CloseActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
