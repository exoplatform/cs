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
package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
    template =  "app:/templates/mail/webui/popup/UIMessageFilter.gtmpl",
    events = {
      @EventConfig(listeners = UIMessageFilter.SelectFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.AddFilterActionListener.class), 
      @EventConfig(listeners = UIMessageFilter.EditFilterActionListener.class),
      @EventConfig(listeners = UIMessageFilter.DeleteFilterActionListener.class, confirm="UIMessageFilter.msg.confirm-remove-filter"), 
      @EventConfig(listeners = UIMessageFilter.CloseActionListener.class)
    }
)
public class UIMessageFilter extends UIForm implements UIPopupComponent{
  private static final Log log = ExoLogger.getExoLogger(UIMessageFilter.class);
  
  public static final String CONDITION_CONTAIN = "contains".intern();
  public static final String CONDITION_NOT_CONTAIN = "doesn't contain".intern();
  public static final String CONDITION_IS = "is".intern();
  public static final String CONDITION_NOT_IS = "isn't".intern();
  public static final String CONDITION_END_WITH = "ends with".intern();
  public static final String CONDITION_START_WITH = "starts with".intern();
  
  private String selectedFilterId ;
  private String accountId_ ;
  
  public UIMessageFilter() throws Exception { }
  
  public void init(String accountId) throws Exception {
    accountId_ = accountId;
    List<MessageFilter> filterList = getFilters() ;
    if (filterList != null && filterList.size() > 0) {
      setSelectedFilterId(filterList.get(0).getId());
    }
  }
  
  public String getSelectedFilterId() {return this.selectedFilterId; }
  public void setSelectedFilterId(String filterId) { this.selectedFilterId = filterId; }
  
  public MessageFilter getSelectedFilter() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = getAncestorOfType(UIMailPortlet.class).getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;
    MailService mailSrv = MailUtils.getMailService();
    if (getSelectedFilterId() != null) {
      return mailSrv.getFilterById(username, accountId, getSelectedFilterId());
    } else {
      return null;
    }
  }
  
  public List<MessageFilter> getFilters() throws Exception {
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getFilters(username, accountId_);
  }
  
  public Folder getFolder() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = getAncestorOfType(UIMailPortlet.class).getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getFolder(username, accountId, getSelectedFilter().getApplyFolder());
  }
  
  public Tag getTag() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = getAncestorOfType(UIMailPortlet.class).getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;
    MailService mailSrv = MailUtils.getMailService();
    return mailSrv.getTag(username, accountId, getSelectedFilter().getApplyTag());
  }
  
  //TODO should replace text with resource boundle
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
      UIMailPortlet mailPortlet = uiMessageFilter.getAncestorOfType(UIMailPortlet.class);
      UIPopupActionContainer uiActionContainer = uiMessageFilter.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddMessageFilter uiAddMessageFilter = uiChildPopup.createUIComponent(UIAddMessageFilter.class, null, null);
      String accountId = mailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiAddMessageFilter.init(accountId);
      uiChildPopup.activate(uiAddMessageFilter, 650, 0, false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static  public class EditFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      MessageFilter filter = uiMessageFilter.getSelectedFilter();
      UIApplication uiApp = uiMessageFilter.getAncestorOfType(UIApplication.class) ;
      if(filter == null) {
        uiApp.addMessage(new ApplicationMessage("UIMessageFilter.msg.select-no-filter", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      UIPopupActionContainer uiActionContainer = uiMessageFilter.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddMessageFilter uiEditMessageFilter = uiChildPopup.createUIComponent(UIAddMessageFilter.class, null, null);
      UIMailPortlet mailPortlet = uiMessageFilter.getAncestorOfType(UIMailPortlet.class);
      String accountId = mailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiEditMessageFilter.init(accountId);
      uiChildPopup.activate(uiEditMessageFilter, 650, 0, false) ;
      uiEditMessageFilter.setCurrentFilter(filter);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static  public class DeleteFilterActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      UIMessageFilter uiMessageFilter = event.getSource() ;
      UIMailPortlet mailPortlet = uiMessageFilter.getAncestorOfType(UIMailPortlet.class);
      String filterId = uiMessageFilter.getSelectedFilterId();
      String username = MailUtils.getCurrentUser();
      String accountId = mailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = MailUtils.getMailService();
      try {
        mailServ.removeFilter(username, accountId, filterId);
        List<MessageFilter> msgFilters = mailServ.getFilters(username, accountId);
        if (msgFilters != null && msgFilters.size() > 0) {
          uiMessageFilter.setSelectedFilterId(msgFilters.get(0).getId());  
        } else { 
          uiMessageFilter.setSelectedFilterId(null);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(mailPortlet.getChild(UIPopupAction.class)) ;
      } catch(Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class DeleteFilterActionListener", e);
        }
      } 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageFilter) ;
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIMessageFilter> {
    public void execute(Event<UIMessageFilter> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
