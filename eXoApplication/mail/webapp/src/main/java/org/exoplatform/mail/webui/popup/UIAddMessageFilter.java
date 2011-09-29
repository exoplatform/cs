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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UISelectFolder;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;


/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 01, 2007 8:48:18 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/popup/UIAddMessageFilter.gtmpl",
    events = {
      @EventConfig(listeners = UIAddMessageFilter.SaveActionListener.class), 
      @EventConfig(listeners = UIAddMessageFilter.CancelActionListener.class)
    }
)
public class UIAddMessageFilter extends UIForm implements UIPopupComponent{
  public static final String FILTER_NAME = "filter-name".intern();
  public static final String FILTER_FROM = "filter-from".intern();
  public static final String FILTER_FROM_CONDITION = "filter-from-condition".intern();
  public static final String FILTER_TO = "filter-to".intern();
  public static final String FILTER_TO_CONDITION = "filter-to-condition".intern();
  public static final String FILTER_SUBJECT = "filter-subject".intern();
  public static final String FILTER_SUBJECT_CONDITION = "filter-subject-condition".intern();
  public static final String FILTER_BODY = "filter-body".intern();
  public static final String FILTER_BODY_CONDITION = "filter-body-condition".intern();
  public static final String FILTER_APPLY_FOLDER = "filter-aplly-folder".intern();
  public static final String FILTER_APPLY_TAG = "filter-aplly-tag".intern();
  //public static final String FILTER_KEEP_INBOX = "filter-keep-in-inbox".intern();
  public static final String APPLY_ALL_MESSAGE = "apply-all-messages".intern();
  
  private MessageFilter currentFilter ;
  
  public UIAddMessageFilter() throws Exception {}
  
  public void init(String accountId) throws Exception {
    addUIFormInput(new UIFormStringInput(FILTER_NAME, FILTER_NAME , null));
    addUIFormInput(new UIFormStringInput(FILTER_FROM, FILTER_FROM , null));
    addUIFormInput(new UIFormStringInput(FILTER_TO, FILTER_TO , null));
    addUIFormInput(new UIFormStringInput(FILTER_SUBJECT, FILTER_SUBJECT , null));
    addUIFormInput(new UIFormStringInput(FILTER_BODY, FILTER_BODY , null));
    List<SelectItemOption<String>>  options1 = new ArrayList<SelectItemOption<String>>() ;
    options1.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options1.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(FILTER_BODY_CONDITION, FILTER_BODY_CONDITION, options1));
    List<SelectItemOption<String>>  options2 = new ArrayList<SelectItemOption<String>>() ;
    options2.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options2.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(FILTER_TO_CONDITION, FILTER_TO_CONDITION, options2));
    List<SelectItemOption<String>>  options3 = new ArrayList<SelectItemOption<String>>() ;
    options3.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options3.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(FILTER_FROM_CONDITION, FILTER_FROM_CONDITION, options3));
    List<SelectItemOption<String>>  options4 = new ArrayList<SelectItemOption<String>>() ;
    options4.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options4.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN))); 
    addUIFormInput(new UIFormSelectBox(FILTER_SUBJECT_CONDITION, FILTER_SUBJECT_CONDITION, options4));
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = MailUtils.getMailService();
    UISelectFolder uiSelectFolder = new UISelectFolder() ;
    addUIFormInput(uiSelectFolder);
    uiSelectFolder.init(accountId) ;
    
    List<SelectItemOption<String>> tagList = new ArrayList<SelectItemOption<String>>();   
    tagList.add(new SelectItemOption<String>("Choose a tag", "choose-tag"));       
    for (Tag tag : mailSrv.getTags(username, accountId)) {   
      tagList.add(new SelectItemOption<String>(tag.getName(), tag.getId()));       
    }    
    addUIFormInput(new UIFormSelectBox(FILTER_APPLY_TAG, FILTER_APPLY_TAG, tagList));
    //addUIFormInput(new UIFormCheckBoxInput<Boolean>(FILTER_KEEP_INBOX, FILTER_KEEP_INBOX, true));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(APPLY_ALL_MESSAGE, APPLY_ALL_MESSAGE, false));
  }
  public MessageFilter getCurrentFilter() { return currentFilter; }
  
  public void setCurrentFilter(MessageFilter filter) throws Exception { 
    this.currentFilter = filter; 
    setFilterName(filter.getName());
    setFrom(filter.getFrom());
    setFromCondition(String.valueOf(filter.getFromCondition()));
    setTo(filter.getTo());
    setToCondition(String.valueOf(filter.getToCondition()));
    setSubject(filter.getSubject());
    setSubjectCondition(String.valueOf(filter.getSubjectCondition()));
    setBody(filter.getBody());
    setBodyCondition(String.valueOf(filter.getBodyCondition()));
    setApplyFolder(filter.getApplyFolder());
    setApplyTag(filter.getApplyTag());
    //setKeepInInbox(filter.keepInInbox());
    setApplyAll(filter.applyForAll()) ;
  }
  
  public String getFilterName() throws Exception {
    return getUIStringInput(FILTER_NAME).getValue();
  }
  
  public void setFilterName(String s) throws Exception {
    getUIStringInput(FILTER_NAME).setValue(s);
  }
 
  public String getFrom() throws Exception {
    return getUIStringInput(FILTER_FROM).getValue();
  }
  
  public void setFrom(String s) throws Exception {
    getUIStringInput(FILTER_FROM).setValue(s);
  }
  
  public String getFromCondition() throws Exception {
    return getUIFormSelectBox(FILTER_FROM_CONDITION).getValue();
  }
  
  public void setFromCondition(String s) throws Exception {
    getUIFormSelectBox(FILTER_FROM_CONDITION).setValue(s);
  }
  
  public String getTo() throws Exception {
    return getUIStringInput(FILTER_TO).getValue();
  }
  
  public void setTo(String s) throws Exception {
    getUIStringInput(FILTER_TO).setValue(s);
  }
  
  public String getToCondition() throws Exception {
    return getUIFormSelectBox(FILTER_TO_CONDITION).getValue();
  }
  
  public void setToCondition(String s) throws Exception {
    getUIFormSelectBox(FILTER_TO_CONDITION).setValue(s);
  }
  
  public String getSubject() throws Exception {
    return getUIStringInput(FILTER_SUBJECT).getValue();
  }
  
  public void setSubject(String s) throws Exception {
    getUIStringInput(FILTER_SUBJECT).setValue(s);
  }
  
  public String getSubjectCondition() throws Exception {
    return getUIFormSelectBox(FILTER_SUBJECT_CONDITION).getValue();
  }
  
  public void setSubjectCondition(String s) throws Exception {
    getUIFormSelectBox(FILTER_SUBJECT_CONDITION).setValue(s);
  }
  
  public String getBody() throws Exception {
    return getUIStringInput(FILTER_BODY).getValue();
  }
  
  public void setBody(String s) throws Exception {
    getUIStringInput(FILTER_BODY).setValue(s);
  }
  
  public String getBodyCondition() throws Exception {
    return getUIFormSelectBox(FILTER_BODY_CONDITION).getValue();
  }
  
  public void setBodyCondition(String s) throws Exception {
    getUIFormSelectBox(FILTER_BODY_CONDITION).setValue(s);
  }
  
  public String getApplyFolder() throws Exception {
    return getChild(UISelectFolder.class).getSelectedValue();
  }
  
  public void setApplyFolder(String s) throws Exception {
    getChild(UISelectFolder.class).setSelectedValue(s);
  }
  
  public String getApplyTag() throws Exception {
    String tagId = getUIStringInput(FILTER_APPLY_TAG).getValue();
    return tagId.equals("choose-tag") ? "" : tagId ;
  }
  
  public void setApplyTag(String s) throws Exception {
    getUIStringInput(FILTER_APPLY_TAG).setValue(s);
  }
  
  /*public Boolean getKeepInInbox() throws Exception {
    return getUIFormCheckBoxInput(FILTER_KEEP_INBOX).isChecked();
  }*/
  
  /*public void setKeepInInbox(boolean s) throws Exception {
    getUIFormCheckBoxInput(FILTER_KEEP_INBOX).setChecked(s);
  }*/
  
  public Boolean getApplyAll() throws Exception {
    return getUIFormCheckBoxInput(APPLY_ALL_MESSAGE).isChecked();
  }
  
  public void setApplyAll(boolean b) throws Exception {
    getUIFormCheckBoxInput(APPLY_ALL_MESSAGE).setChecked(b);
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static  public class SaveActionListener extends EventListener<UIAddMessageFilter> {
    public void execute(Event<UIAddMessageFilter> event) throws Exception {
      UIAddMessageFilter uiAddFilter = event.getSource();
      UIMailPortlet uiPortlet = uiAddFilter.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String username = MailUtils.getCurrentUser();
      String accountId = dataCache.getSelectedAccountId();
      MailService mailSrv = MailUtils.getMailService();
      String filterName = uiAddFilter.getFilterName();
      String from = uiAddFilter.getFrom();
      String fromCondition = uiAddFilter.getFromCondition();
      String to = uiAddFilter.getTo();
      String toCondition = uiAddFilter.getToCondition();
      String subject = uiAddFilter.getSubject();
      String subjectCondition = uiAddFilter.getSubjectCondition();
      String body = uiAddFilter.getBody();
      String bodyCondition = uiAddFilter.getBodyCondition();
      String applyFolder = uiAddFilter.getApplyFolder();
      String applyTag = uiAddFilter.getApplyTag();
      boolean applyForAll = uiAddFilter.getApplyAll();
      
      // Verify
      UIApplication uiApp = uiAddFilter.getAncestorOfType(UIApplication.class) ;
      if (Utils.isEmptyField(filterName)) {
        uiApp.addMessage(new ApplicationMessage("UIAddMessageFilter.msg.filter-name-blank", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } else if (Utils.isEmptyField(from) && Utils.isEmptyField(to) && Utils.isEmptyField(subject) && Utils.isEmptyField(body)) {
        uiApp.addMessage(new ApplicationMessage("UIAddMessageFilter.msg.fill-at-lease-field", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      
      MessageFilter filter = new MessageFilter(filterName);       
      if (uiAddFilter.getCurrentFilter() != null) filter = uiAddFilter.getCurrentFilter();
      filter.setName(filterName) ;
      filter.setAccountId(accountId);
      filter.setName(filterName) ;
      filter.setFrom(from);
      filter.setFromCondition(Integer.valueOf(fromCondition));
      filter.setTo(to);
      filter.setToCondition(Integer.valueOf(toCondition));
      filter.setSubject(subject);
      filter.setSubjectCondition(Integer.valueOf(subjectCondition));
      filter.setBody(body);
      filter.setBodyCondition(Integer.valueOf(bodyCondition));
      filter.setApplyFolder(applyFolder);
      filter.setApplyTag(applyTag);
      filter.setApplyForAll(applyForAll);
      
      try {
        mailSrv.saveFilter(username, accountId, filter, uiAddFilter.getApplyAll());
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAddMessageFilter.msg.contain-special-characters", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIMessageFilter uiMsgFilter = uiPortlet.findFirstComponentOfType(UIMessageFilter.class);
      if (uiMsgFilter != null) {
        uiMsgFilter.setSelectedFilterId(filter.getId());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddFilter.getAncestorOfType(UIPopupActionContainer.class));
      }
      
      UIPopupAction uiPopupAction = uiAddFilter.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      
      if (filter.applyForAll()) {
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
        uiMessageList.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UINavigationContainer.class));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddMessageFilter> {
    public void execute(Event<UIAddMessageFilter> event) throws Exception {
      UIAddMessageFilter uiAddMessageFilter = event.getSource();
      UIPopupAction uiPopupAction = uiAddMessageFilter.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}
