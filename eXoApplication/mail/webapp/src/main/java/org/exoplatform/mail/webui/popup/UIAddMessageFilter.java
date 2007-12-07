/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template =  "app:/templates/mail/webui/UIAddMessageFilter.gtmpl",
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
  public static final String FILTER_KEEP_INBOX = "filter-keep-in-inbox".intern();
  
  private MessageFilter currentFilter ;
  
  public UIAddMessageFilter() throws Exception {
    addUIFormInput(new UIFormStringInput(FILTER_NAME, FILTER_NAME , null));
    addUIFormInput(new UIFormStringInput(FILTER_FROM, FILTER_FROM , null));
    addUIFormInput(new UIFormStringInput(FILTER_TO, FILTER_TO , null));
    addUIFormInput(new UIFormStringInput(FILTER_SUBJECT, FILTER_SUBJECT , null));
    addUIFormInput(new UIFormStringInput(FILTER_BODY, FILTER_BODY , null));
    List<SelectItemOption<String>>  options1 = new ArrayList<SelectItemOption<String>>() ;
    options1.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options1.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(FILTER_BODY_CONDITION, FILTER_BODY_CONDITION, options1));
    List<SelectItemOption<String>>  options2 = new ArrayList<SelectItemOption<String>>() ;
    options2.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options2.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options2.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options2.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options2.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options2.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(FILTER_TO_CONDITION, FILTER_TO_CONDITION, options2));
    List<SelectItemOption<String>>  options3 = new ArrayList<SelectItemOption<String>>() ;
    options3.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options3.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options3.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options3.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options3.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options3.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(FILTER_FROM_CONDITION, FILTER_FROM_CONDITION, options3));
    List<SelectItemOption<String>>  options4 = new ArrayList<SelectItemOption<String>>() ;
    options4.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options4.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options4.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options4.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options4.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options4.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(FILTER_SUBJECT_CONDITION, FILTER_SUBJECT_CONDITION, options4));
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    List<SelectItemOption<String>> folderList = new ArrayList<SelectItemOption<String>>();   
    for (Folder folder : mailSrv.getFolders(SessionsUtils.getSessionProvider(), username, accountId)) {   
      folderList.add(new SelectItemOption<String>(folder.getName(), folder.getId()));       
    }    
    addUIFormInput(new UIFormSelectBox(FILTER_APPLY_FOLDER, FILTER_APPLY_FOLDER, folderList));
    List<SelectItemOption<String>> tagList = new ArrayList<SelectItemOption<String>>();   
    tagList.add(new SelectItemOption<String>("-- Choose tag --", ""));       
    for (Tag tag : mailSrv.getTags(SessionsUtils.getSessionProvider(), username, accountId)) {   
      tagList.add(new SelectItemOption<String>(tag.getName(), tag.getId()));       
    }    
    addUIFormInput(new UIFormSelectBox(FILTER_APPLY_TAG, FILTER_APPLY_TAG, tagList));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FILTER_KEEP_INBOX, FILTER_KEEP_INBOX, true));
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
    setKeepInInbox(filter.keepInInbox());
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
    return getUIStringInput(FILTER_APPLY_FOLDER).getValue();
  }
  
  public void setApplyFolder(String s) throws Exception {
    getUIStringInput(FILTER_APPLY_FOLDER).setValue(s);
  }
  
  public String getApplyTag() throws Exception {
    return getUIStringInput(FILTER_APPLY_TAG).getValue();
  }
  
  public void setApplyTag(String s) throws Exception {
    getUIStringInput(FILTER_APPLY_TAG).setValue(s);
  }
  
  public Boolean getKeepInInbox() throws Exception {
    return getUIFormCheckBoxInput(FILTER_KEEP_INBOX).isChecked();
  }
  
  public void setKeepInInbox(boolean s) throws Exception {
    getUIFormCheckBoxInput(FILTER_KEEP_INBOX).setChecked(s);
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static  public class SaveActionListener extends EventListener<UIAddMessageFilter> {
    public void execute(Event<UIAddMessageFilter> event) throws Exception {
      UIAddMessageFilter uiFilter = event.getSource();
      String filterName = uiFilter.getFilterName();
      String from = uiFilter.getFrom();
      String fromCondition = uiFilter.getFromCondition();
      String to = uiFilter.getTo();
      String toCondition = uiFilter.getToCondition();
      String subject = uiFilter.getSubject();
      String subjectCondition = uiFilter.getSubjectCondition();
      String body = uiFilter.getBody();
      String bodyCondition = uiFilter.getBodyCondition();
      String applyFolder = uiFilter.getApplyFolder();
      String applyTag = uiFilter.getApplyTag();
      boolean keepInbox = uiFilter.getKeepInInbox();
      MessageFilter filter; 
      if (uiFilter.getCurrentFilter() != null) {
        filter = uiFilter.getCurrentFilter();
      } else {
        filter = new MessageFilter(filterName);
      }
      filter.setAccountId(MailUtils.getAccountId());
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
      filter.setKeepInInbox(keepInbox);
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      MailService mailSrv = MailUtils.getMailService();
      try {
        mailSrv.saveFilter(SessionsUtils.getSessionProvider(), username, accountId, filter);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddMessageFilter> {
    public void execute(Event<UIAddMessageFilter> event) throws Exception {
      UIAddMessageFilter uiAddMessageFilter = event.getSource();
      uiAddMessageFilter.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax((uiAddMessageFilter.getAncestorOfType(UIPopupActionContainer.class))) ;
    }
  }
}
