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

import javax.jcr.PathNotFoundException;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Aug 10, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/popup/UIMailSettings.gtmpl",
    events = {
        @EventConfig(listeners = UIMailSettings.SaveActionListener.class),
        @EventConfig(listeners = UIMailSettings.CancelActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIMailSettings.SelectTabActionListener.class, phase = Phase.DECODE)
    }
)
public class UIMailSettings extends UIFormTabPane implements UIPopupComponent {
//public class UIMailSettings extends UIForm implements UIPopupComponent {
  public static final String DEFAULT_ACCOUNT = "default-account".intern();
  public static final String NUMBER_MSG_PER_PAGE = "number-of-conversation".intern() ;
  public static final String PERIOD_CHECK_AUTO = "period-check-mail".intern() ;
  public static final String COMPOSE_MESSAGE_IN = "compose-message-in".intern();
  public static final String REPLY_WITH_ATTACH = "reply-message-with".intern();
  public static final String FORWARD_WITH_ATTACH = "forward-message-with".intern();
  public static final String SAVE_SENT_MESSAGE = "save-sent-message".intern();
  //modify
  public static final String TAB_GENERAL = "general";
  public static final String TAB_LAYOUT = "layout";
  
  
  public UIMailSettings() throws Exception {    
    super("UIMailSettings");
    
  }
  
  public void init() throws Exception {
    /*addUIFormInput(new UIFormSelectBox(DEFAULT_ACCOUNT, DEFAULT_ACCOUNT, getAccounts()));
        
    List<SelectItemOption<String>> numberPerPage = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i <= 7; i++) {
      numberPerPage.add(new SelectItemOption<String>(String.valueOf(10*i)));
    }
    addUIFormInput(new UIFormSelectBox(NUMBER_MSG_PER_PAGE, NUMBER_MSG_PER_PAGE, numberPerPage));  
    //TODO should replace text by resource boundle
    List<SelectItemOption<String>> periodCheckAuto = new ArrayList<SelectItemOption<String>>();
    periodCheckAuto.add(new SelectItemOption<String>("Never", "period." + String.valueOf(MailSetting.NEVER_CHECK_AUTO)));
    //TODO change to id
    periodCheckAuto.add(new SelectItemOption<String>("5 minutes", "period." + String.valueOf(MailSetting.FIVE_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("10 minutes", "period." + String.valueOf(MailSetting.TEN_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("20 minutes", "period." + String.valueOf(MailSetting.TWENTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("30 minutes", "period." + String.valueOf(MailSetting.THIRTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("1 hour", "period." + String.valueOf(MailSetting.ONE_HOUR)));
    addUIFormInput(new UIFormSelectBox(PERIOD_CHECK_AUTO, PERIOD_CHECK_AUTO, periodCheckAuto));
    
    List<SelectItemOption<String>> useWysiwyg = new ArrayList<SelectItemOption<String>>();
    useWysiwyg.add(new SelectItemOption<String>("Rich text editor (HTML format)", "editor.true"));
    useWysiwyg.add(new SelectItemOption<String>("Plain text", "editor.false"));
    addUIFormInput(new UIFormSelectBox(COMPOSE_MESSAGE_IN, COMPOSE_MESSAGE_IN, useWysiwyg));
    
    List<SelectItemOption<String>> replyWithAtt = new ArrayList<SelectItemOption<String>>();
    replyWithAtt.add(new SelectItemOption<String>("Original message included attachment", "replywith.true"));
    replyWithAtt.add(new SelectItemOption<String>("Original message", "replywith.false"));
    addUIFormInput(new UIFormSelectBox(REPLY_WITH_ATTACH, REPLY_WITH_ATTACH, replyWithAtt));
    
    List<SelectItemOption<String>> forwardWithAtt = new ArrayList<SelectItemOption<String>>();
    forwardWithAtt.add(new SelectItemOption<String>("Original message included attachment", "forwardwith.true"));
    forwardWithAtt.add(new SelectItemOption<String>("Original message", "forwardwith.false"));
    addUIFormInput(new UIFormSelectBox(FORWARD_WITH_ATTACH, FORWARD_WITH_ATTACH, forwardWithAtt));
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SAVE_SENT_MESSAGE, SAVE_SENT_MESSAGE, false));
    fillData() ;
    */
    
    //creat InputSet for General Tab
    UIFormInputSet  generalInputSet = new UIFormInputSet(TAB_GENERAL);
    generalInputSet.addUIFormInput(new UIFormSelectBox(DEFAULT_ACCOUNT, DEFAULT_ACCOUNT, getAccounts()));
    List<SelectItemOption<String>> numberPerPage = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i <= 7; i++) {
      numberPerPage.add(new SelectItemOption<String>(String.valueOf(10*i)));
    }
    generalInputSet.addUIFormInput(new UIFormSelectBox(NUMBER_MSG_PER_PAGE, NUMBER_MSG_PER_PAGE, numberPerPage));  
    //TODO should replace text by resource boundle
    List<SelectItemOption<String>> periodCheckAuto = new ArrayList<SelectItemOption<String>>();
    periodCheckAuto.add(new SelectItemOption<String>("Never", "period." + String.valueOf(MailSetting.NEVER_CHECK_AUTO)));
    //TODO change to id
    periodCheckAuto.add(new SelectItemOption<String>("5 minutes", "period." + String.valueOf(MailSetting.FIVE_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("10 minutes", "period." + String.valueOf(MailSetting.TEN_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("20 minutes", "period." + String.valueOf(MailSetting.TWENTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("30 minutes", "period." + String.valueOf(MailSetting.THIRTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("1 hour", "period." + String.valueOf(MailSetting.ONE_HOUR)));
    generalInputSet.addUIFormInput(new UIFormSelectBox(PERIOD_CHECK_AUTO, PERIOD_CHECK_AUTO, periodCheckAuto));
    
    List<SelectItemOption<String>> useWysiwyg = new ArrayList<SelectItemOption<String>>();
    useWysiwyg.add(new SelectItemOption<String>("Rich text editor (HTML format)", "editor.true"));
    useWysiwyg.add(new SelectItemOption<String>("Plain text", "editor.false"));
    generalInputSet.addUIFormInput(new UIFormSelectBox(COMPOSE_MESSAGE_IN, COMPOSE_MESSAGE_IN, useWysiwyg));
    
    List<SelectItemOption<String>> replyWithAtt = new ArrayList<SelectItemOption<String>>();
    replyWithAtt.add(new SelectItemOption<String>("Original message included attachment", "replywith.true"));
    replyWithAtt.add(new SelectItemOption<String>("Original message", "replywith.false"));
    generalInputSet.addUIFormInput(new UIFormSelectBox(REPLY_WITH_ATTACH, REPLY_WITH_ATTACH, replyWithAtt));
    
    List<SelectItemOption<String>> forwardWithAtt = new ArrayList<SelectItemOption<String>>();
    forwardWithAtt.add(new SelectItemOption<String>("Original message included attachment", "forwardwith.true"));
    forwardWithAtt.add(new SelectItemOption<String>("Original message", "forwardwith.false"));
    generalInputSet.addUIFormInput(new UIFormSelectBox(FORWARD_WITH_ATTACH, FORWARD_WITH_ATTACH, forwardWithAtt));
    
    generalInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(SAVE_SENT_MESSAGE, SAVE_SENT_MESSAGE, false));
    
    UIMailLayoutTab  layoutInputSet = new UIMailLayoutTab(TAB_LAYOUT);
    addUIFormInput(generalInputSet);
    addUIFormInput(layoutInputSet);
    setSelectedTab(generalInputSet.getId()) ;
    fillData() ;
  }
  
  public List<SelectItemOption<String>> getAccounts() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    for(Account acc : mailSrv.getAccounts(SessionProviderFactory.createSystemProvider(), username)) {
      SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getLabel() + " &lt;" + acc.getEmailAddress() + "&gt;", acc.getId());
      options.add(itemOption) ;
    }
    return options ;
  }
  
  public void fillData() throws Exception {    
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    MailSetting setting = mailSrv.getMailSetting(SessionProviderFactory.createSystemProvider(), username);
    if (setting != null) {
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(DEFAULT_ACCOUNT).setValue(setting.getDefaultAccount()) ;
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(NUMBER_MSG_PER_PAGE).setValue(String.valueOf(setting.getNumberMsgPerPage()));
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(PERIOD_CHECK_AUTO).setValue("period." + String.valueOf(setting.getPeriodCheckAuto()));
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(COMPOSE_MESSAGE_IN).setValue("editor." + String.valueOf(setting.useWysiwyg()));
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(REPLY_WITH_ATTACH).setValue("replywith." + String.valueOf(setting.replyWithAttach()));
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormSelectBox(FORWARD_WITH_ATTACH).setValue("forwardwith." + String.valueOf(setting.forwardWithAtt()));
      ((UIFormInputSet) getChildById(TAB_GENERAL)).getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).setChecked(setting.saveMessageInSent());
    }
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class SaveActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      
      UIMailSettings uiSetting = event.getSource();
      UIMailLayoutTab uiSettingTab = uiSetting.getChildById(TAB_LAYOUT) ;
      UIFormRadioBoxInput uiRadio = (UIFormRadioBoxInput)uiSettingTab.getChildById(UIMailLayoutTab.HORIZONTAL_LAYOUT) ;
      //TODO save to data base
      System.out.println("\n\n " + uiRadio.getValue());
      
       
		  UIMailPortlet uiPortlet = uiSetting.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
      String accountId = uiSelectAccount.getSelectedValue();
		  
      MailService mailSrv = MailUtils.getMailService();
		  MailSetting setting = mailSrv.getMailSetting(SessionProviderFactory.createSystemProvider(), username);
      String defaultAcc = uiSetting.getUIFormSelectBox(DEFAULT_ACCOUNT).getValue() ;
		  setting.setDefaultAccount(defaultAcc) ;
      setting.setNumberMsgPerPage(Long.valueOf(uiSetting.getUIFormSelectBox(NUMBER_MSG_PER_PAGE).getValue())) ;
      
      String period = uiSetting.getUIFormSelectBox(PERIOD_CHECK_AUTO).getValue() ;
      period = period.substring(period.indexOf(".") + 1, period.length());
		  setting.setPeriodCheckAuto(Long.valueOf(period)) ;
      
      String editor = uiSetting.getUIFormSelectBox(COMPOSE_MESSAGE_IN).getValue() ;
      setting.setUseWysiwyg(Boolean.valueOf(editor.substring(editor.indexOf(".") + 1, editor.length()))) ;
      
      String replyWith = uiSetting.getUIFormSelectBox(REPLY_WITH_ATTACH).getValue() ;
      setting.setReplyWithAttach(Boolean.valueOf(replyWith.substring(replyWith.indexOf(".") + 1, replyWith.length())));
      String forwardWith = uiSetting.getUIFormSelectBox(FORWARD_WITH_ATTACH).getValue() ;
      setting.setForwardWithAtt(Boolean.valueOf(forwardWith.substring(forwardWith.indexOf(".") + 1, forwardWith.length())));
      setting.setSaveMessageInSent(uiSetting.getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).isChecked());
      mailSrv.saveMailSetting(SessionProviderFactory.createSystemProvider(), username, setting);
		  UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      MessageFilter filter = uiMessageList.getMessageFilter() ;
      if (defaultAcc != null && (!accountId.equals(setting.getDefaultAccount()) || accountId.equals(defaultAcc))){
        uiSelectAccount.updateAccount() ;
        uiSelectAccount.setSelectedValue(accountId);
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(SessionProviderFactory.createSystemProvider(), username, filter));
      } else {
        try {
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(SessionProviderFactory.createSystemProvider(), username, filter));
        } catch (PathNotFoundException e) {
          uiMessageList.setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      uiSetting.getAncestorOfType(UIPopupAction.class).deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      if (setting.getPeriodCheckAuto() != Long.valueOf(period)) { 
        context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.initService('checkMailInfobar', '" + MailUtils.getCurrentUser() + "', '" + defaultAcc + "') ;") ;
        context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.setCheckmailTimeout(" + period + ") ;") ;
      }
	  }
  }

  static  public class CancelActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
  
  static public class SelectTabActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;      
    }
  }
}


