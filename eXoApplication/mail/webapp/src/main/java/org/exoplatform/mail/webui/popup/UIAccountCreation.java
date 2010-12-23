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

import java.util.Date;

import java.util.HashMap;
import java.util.Map;

import javax.mail.AuthenticationFailedException;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.Selector;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIFormDateTimePicker;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormTabPane;
/**
 * Created by The eXo Platform SARL
 * Author : Philippe Aristote <philippe.aristote@gmail.com>
 *          Tuan Pham <phamtuanchip@gmail.com>
 *          Nam Phung <phunghainam@gmail.com>
 * Aug 10, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/popup/UIAccountCreation.gtmpl",
    events = {
      @EventConfig(listeners = UIAccountCreation.ViewStepActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.ChangeServerTypeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.ChangeCheckedActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.ChangeOutgoingSslActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.NextActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.FinishActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.CheckFromDateActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAccountCreation.CancelActionListener.class, phase = Phase.DECODE, confirm = "UIAccountCreation.msg.confirm-cancel") 
    }
)
public class UIAccountCreation extends UIFormTabPane implements UIPopupComponent, Selector {

  private int wizardMaxStep_ = 5 ;
  private int currentStep_ = 1 ;
  private int wizardMinStep_ = 1 ;
  private boolean isChildPopup_ = false ;
  private boolean isShowStepActions_ = true ;

  private Map<Integer, String> chidrenMap_ = new HashMap<Integer, String>() ;
  public String password_ = null ; 

  final static public String POPUPID = "UIAccountCreationWizardPopup" ;
  final static public String INPUT_STEP1 = "step1" ;
  final static public String INPUT_STEP2 = "step2" ;
  final static public String INPUT_STEP3 = "step3" ;
  final static public String INPUT_STEP4 = "step4" ;
  final static public String INPUT_STEP5 = "step5" ;
  final static public String[] ACT_SELETFOLDER = {"SelectFolder"} ;
  final static public String ACT_CHECKSAVEPASS =  "CheckSavePass" ;
  final static public String[] ACT_CHECKGETMAIL = {"CheckGetMail"} ;
  final static public String ACT_CHANGE_TYPE = "ChangeServerType".intern()  ;
  final static public String ACT_CHANGE_ACT = "ChangeType".intern()  ;
  final static public String ACT_CHANGE_SSL =  "ChangeChecked".intern()  ;
  final static public String ACT_CHANGE_OUTGOINGSSL =  "ChangeOutgoingSsl".intern()  ;
  final static public String FD_INBOX = "Inbox".intern();
  final static public String FD_DRAFTS = "Drafts".intern() ;
  final static public String FD_SENT = "Sent".intern() ;
  final static public String FD_SPAM = "Spam".intern() ;
  final static public String FD_TRASH = "Trash".intern() ;
  final static public String[] defaultFolders_ =  {FD_INBOX ,FD_DRAFTS, FD_SENT, FD_SPAM, FD_TRASH} ;
  
  public static final String DEFAULT_POP_SERVER = "pop.gmail.com".intern() ;
  public static final String DEFAULT_IMAP_SERVER = "imap.gmail.com".intern() ;
  public static final String DEFAULT_SMTP_SERVER = "smtp.gmail.com".intern() ;
  public static final String DEFAULT_POP_PORT = "110".intern() ;
  public static final String DEFAULT_SMTP_PORT = "25".intern() ;
  public static final String DEFAULT_POPSSL_PORT = "995".intern() ;
  public static final String DEFAULT_SMTPSSL_PORT = "465".intern() ;
  public static final String DEFAULT_SERVER_FOLDER = "Inbox".intern() ;
  public static final String DEFAULT_IMAP_PORT = "143".intern() ;
  public static final String DEFAULT_IMAPSSL_PORT = "993".intern() ;

  public UIAccountCreation() throws Exception {
    super("UIAccountCreation") ;
    UIAccountWizardStep1 uiAccountWizardStep1 =  new UIAccountWizardStep1(INPUT_STEP1) ;
    setSelectedTab(uiAccountWizardStep1.getId()) ;
    addUIComponentInput(new UIAccountWizardStep1(INPUT_STEP1)) ;
    addUIComponentInput(new UIAccountWizardStep2(INPUT_STEP2)) ;
    addUIComponentInput(new UIAccountWizardStep3(INPUT_STEP3)) ;
    addUIComponentInput(new UIAccountWizardStep4(INPUT_STEP4)) ;
    addUIComponentInput(new UIAccountWizardStep5(INPUT_STEP5)) ;

    for(UIComponent c : getChildren()) {
      chidrenMap_.put(getChildren().indexOf(c) + 1, c.getId()) ;
    }
    setSelectedTab(getCurrentChild()) ;
  }

  public boolean showStepActions() {return isShowStepActions_ ;}
  
  protected void setShowStepActions(boolean isShow) {isShowStepActions_ = isShow ;}
  
  public void setCurrentSep(int step){ currentStep_ = step ;}
  
  public int getCurrentStep() { return currentStep_; }
  
  public int getMaxStep(){return wizardMaxStep_ ;}
  
  public String getCurrentChild() {return chidrenMap_.get(currentStep_) ;}
  
  public int getNumberSteps() {return wizardMaxStep_ ;}

  protected boolean isChildPopup(){return isChildPopup_ ;} 
  protected void setChildPopup(boolean isChildPopup) {isChildPopup_ = isChildPopup ;}

  protected void viewStep(int step) {  
    currentStep_ = step ;
    setSelectedTab(chidrenMap_.get(getCurrentStep())) ;
  }
  
  protected void nextStep() {
    currentStep_ ++ ;
    setSelectedTab(chidrenMap_.get(getCurrentStep())) ;
  }
  protected void backStep() {
    currentStep_ -- ;
    setSelectedTab(chidrenMap_.get(getCurrentStep())) ;
  }

  public String[] getActions(){
    if(currentStep_ == wizardMinStep_) {
      return new String[]{"Next", "Cancel"} ;
    }
    if(currentStep_ == wizardMaxStep_) {
      return new String[]{"Back", "Finish", "Cancel"} ;
    }
    return new String[]{"Back", "Next", "Cancel"} ;
  }
  
  protected void saveForm(String currentUser, Account account) throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    mailSvr.createAccount(currentUser, account) ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class) ;
    String username = uiPortlet.getCurrentUser() ;
    for(String folderName : defaultFolders_) {
      String folderId = Utils.generateFID(account.getId(), folderName, false);
      Folder folder = mailSvr.getFolder(username, account.getId(), folderId) ;
      if(folder == null) {
        folder = new Folder() ;
        folder.setId(folderId);
        folder.setName(folderName) ;
        folder.setPersonalFolder(false) ;
        mailSvr.saveFolder(username, account.getId(), folder) ;
      }
    }
  }
  
  protected void getMail(String accountId) throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class) ;
    String username = uiPortlet.getCurrentUser() ;
    mailSvr.checkMail(username, accountId) ;
  }

  protected void resetForm() {
    UIAccountWizardStep1 uiAccWs1 = getChildById(UIAccountCreation.INPUT_STEP1) ;
    UIAccountWizardStep2 uiAccWs2 = getChildById(UIAccountCreation.INPUT_STEP2) ;
    UIAccountWizardStep3 uiAccWs3 = getChildById(UIAccountCreation.INPUT_STEP3) ;
    UIAccountWizardStep4 uiAccWs4 = getChildById(UIAccountCreation.INPUT_STEP4) ;
    UIAccountWizardStep5 uiAccWs5 = getChildById(UIAccountCreation.INPUT_STEP5) ;
    uiAccWs1.resetFields() ;
    uiAccWs2.resetFields() ;
    uiAccWs3.resetFields() ;
    uiAccWs4.resetFields() ;
    uiAccWs5.resetFields() ;
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  public void updateValue(String fieldId, String value) { }
  
  public static class ViewStepActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      String step = event.getRequestContext().getRequestParameter(OBJECTID) ;
      int nextStep = Integer.parseInt(step) ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(nextStep > uiAccCreation.getCurrentStep()) {
        if(wss.isFieldsValid()) { 
          UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP4) ;
          if(uiAccWs4.isRendered()) {
            UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP1) ;
            UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP2) ;
            UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP3) ;
            UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP5) ;
            String accname = uiAccWs1.getAccName() ;
            String accOutgoingName = uiAccWs2.getOutgoingName() ;
            String email = uiAccWs2.getEmailAddress() ;
            String serverName = uiAccWs3.getIncomingServer();
            String serverType = uiAccWs3.getServerType(); 
            String storeFolder = uiAccWs3.getStoreFolder() ;
            uiAccCreation.password_  = uiAccWs4.getPassword() ;
            uiAccWs5.fillFields(accname, accOutgoingName, email, serverName, serverType, storeFolder) ;
          }
          uiAccCreation.viewStep(Integer.parseInt(step)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
        } else {
          UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
      } else {
        UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP4) ;
        if(uiAccWs4.isRendered()) {
          UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP1) ;
          UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP2) ;
          UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP3) ;
          UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP5) ;
          String accname = uiAccWs1.getAccName() ;
          String accOutgoingName = uiAccWs2.getOutgoingName() ;
          String email = uiAccWs2.getEmailAddress() ;
          String serverName = uiAccWs3.getIncomingServer();
          String serverType = uiAccWs3.getServerType(); 
          String storeFolder = uiAccWs3.getStoreFolder() ;
          uiAccCreation.password_  = uiAccWs4.getPassword() ;
          uiAccWs5.fillFields(accname, accOutgoingName, email, serverName, serverType, storeFolder) ;
        }
        uiAccCreation.viewStep(Integer.parseInt(step)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
      }
    }
  }

  public static class CheckFromDateActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep4 uiWs4 = uiAccCreation.getChildById(INPUT_STEP4) ;
      UIFormDateTimePicker fromDateField = ((UIFormDateTimePicker) uiWs4.getChildById(UIAccountWizardStep4.FROM_DATE));
      boolean checkAllMail = uiWs4.checkFromDate();
      
      if (!checkAllMail) {
        fromDateField.setRendered(false);
      } else {
        fromDateField.setRendered(true);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  
  public static class FinishActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIMailPortlet uiPortlet = uiAccCreation.getAncestorOfType(UIMailPortlet.class) ;
      for(UIComponent ws : uiAccCreation.getChildren()) {
        if(!((WizardStep)ws).isFieldsValid()) {
          int index = uiAccCreation.getChildren().indexOf(ws) + 1;
          uiAccCreation.viewStep(index) ;
          UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP1) ;
      UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP2) ;
      UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP3) ;
      UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP4) ;
      UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP5) ;
      String accname, description, displayName, email, replyMail, signature, protocol, popHost, 
      popPort, smtpHost, smtpPort, storeFolder, incomingUserName, incomingPassword ;
      boolean isSSL, isOutgoingSsl, isSavePass ;
      accname = uiAccWs1.getAccName() ;
      description = uiAccWs1.getAccDescription() ;
      displayName = uiAccWs2.getOutgoingName() ;
      email = uiAccWs2.getEmailAddress() ;
      replyMail = uiAccWs2.getEmailReply() ;
      signature = uiAccWs2.getSignature() ;
      protocol = uiAccWs3.getServerType() ;
      popHost = uiAccWs3.getIncomingServer() ;
      popPort = uiAccWs3.getIncomingPort() ;
      smtpHost = uiAccWs3.getOutgoingServer() ;
      smtpPort = uiAccWs3.getOutgoingPort() ;
      storeFolder = uiAccWs3.getStoreFolder() ;
      isSSL = uiAccWs3.getIsSSL() ;
      isOutgoingSsl = uiAccWs3.getOutgoingSsl();
      incomingUserName = uiAccWs4.getUserName() ;
      incomingPassword = uiAccWs4.getPassword() ;
      isSavePass = uiAccWs4.getIsSavePass() ;
      
      Account acc = new Account() ;
      
      acc.setLabel(accname) ;
      acc.setDescription(description) ;
      acc.setUserDisplayName(displayName) ;
      acc.setEmailAddress(email) ;
      acc.setEmailReplyAddress(replyMail) ;
      acc.setSignature(signature) ;
      acc.setIncomingUser(incomingUserName); 
      
      if (isSavePass || uiAccWs5.isGetmail()) acc.setIncomingPassword(incomingPassword);
      else acc.setIncomingPassword(null);
      
      acc.setIncomingHost(popHost);
      acc.setIncomingPort(popPort);  
      acc.setProtocol(protocol);  
      acc.setIncomingSsl(isSSL);
      acc.setOutgoingSsl(isOutgoingSsl);
      acc.setIncomingFolder(storeFolder) ;
      acc.setServerProperty(Utils.SVR_SMTP_USER, incomingUserName);
      acc.setOutgoingHost(smtpHost);
      acc.setOutgoingPort(smtpPort);
      acc.setIsSavePassword(isSavePass) ;
      acc.setIsOutgoingAuthentication(true);
      acc.setUseIncomingForAuthentication(true);
      acc.setOutgoingUserName(incomingUserName);
      acc.setOutgoingPassword(incomingPassword);
      acc.setIsCustomInbox(false);
      
      UIFormDateTimePicker fromDateField = ((UIFormDateTimePicker) uiAccWs4.getChildById(UIAccountWizardStep4.FROM_DATE));
      acc.setCheckAll(!uiAccWs4.checkFromDate());
      
      if (!acc.isCheckAll()) {
        try {
          acc.setCheckFromDate(fromDateField.getCalendar().getTime());
        } catch(Exception e) {
          acc.setCheckFromDate(new Date());
        }
      }
      
      UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      try {
        uiAccCreation.saveForm(uiPortlet.getCurrentUser(), acc) ;
        UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
        uiSelectAccount.updateAccount();
        uiSelectAccount.setSelectedValue(acc.getId());
        uiPortlet.findFirstComponentOfType(UIFolderContainer.class).init(acc.getId());
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
        uiMessageList.setMessageFilter(null);
        uiMessageList.init(acc.getId());
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.create-acc-unsuccessfully", null, ApplicationMessage.ERROR)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      } 
      if(uiAccWs5.isGetmail()) {
        try {
          uiPortlet.findFirstComponentOfType(UIMessageList.class).init(acc.getId());
          UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
          uiSelectAccount.setSelectedValue(acc.getId());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          uiAccCreation.getAncestorOfType(UIPopupAction.class).deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
          WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.initService('checkMailInfobar', '" + MailUtils.getCurrentUser() + "', '" + acc.getId() + "') ;") ;
          /*context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.setCheckmailTimeout(" + 
              uiAccCreation.getApplicationComponent(MailService.class).getMailSetting(MailUtils.getCurrentUser()).getPeriodCheckAuto() + ") ;") ;*/
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true) ;");
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.showStatusBox('checkmail-notice') ;");        
        } catch (AuthenticationFailedException afe) {
          uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.userName-password-incorrect", null, ApplicationMessage.ERROR)) ;
          uiAccCreation.viewStep(4) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.getMail-unsuccessfully", null, ApplicationMessage.ERROR)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
          return ;
        }
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        uiAccCreation.getAncestorOfType(UIPopupAction.class).deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation) ;
    }
  }

  public static class NextActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP1) ;
      UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP2) ;
      UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP3) ;
      UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP4) ;
      UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.INPUT_STEP5) ;
      UIComponent selectedTab = uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      WizardStep wss = (WizardStep) selectedTab ;
      if(!wss.isFieldsValid()) {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
        if(uiAccWs4.isRendered()) {
          String accname = uiAccWs1.getAccName() ;
          String accOutgoingName = uiAccWs2.getOutgoingName() ;
          String email = uiAccWs2.getEmailAddress() ;
          String serverName = uiAccWs3.getIncomingServer();
          String serverType = uiAccWs3.getServerType(); 
          String storeFolder = uiAccWs3.getStoreFolder() ;
          uiAccCreation.password_  = uiAccWs4.getPassword() ;
          uiAccWs5.fillFields(accname, accOutgoingName, email, serverName, serverType, storeFolder) ;
        }
        uiAccCreation.nextStep() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
      }
    }
  }

  public static class BackActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      uiAccCreation.backStep();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
    }
  }
  public static class CancelActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIMailPortlet uiPortlet = uiAccCreation.getAncestorOfType(UIMailPortlet.class) ;
      if (uiAccCreation.isChildPopup_) uiAccCreation.deActivate();
      else uiPortlet.cancelAction();
    }
  }

  public static class ChangeServerTypeActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep3 uiWs3 = uiAccCreation.getChildById(INPUT_STEP3) ;
      uiWs3.setDefaultValue(uiWs3.getServerType(), uiWs3.getIsSSL()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
    } 
  }
  public static class ChangeCheckedActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception { 
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep3 uiWs3 = uiAccCreation.getChildById(INPUT_STEP3) ;
      uiWs3.setDefaultValue(uiWs3.getServerType(), uiWs3.getIsSSL()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
    } 
  }
  public static class ChangeOutgoingSslActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception { 
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep3 uiWs3 = uiAccCreation.getChildById(INPUT_STEP3) ;
      if (uiWs3.getOutgoingSsl()) {
        uiWs3.getUIStringInput(UIAccountWizardStep3.FIELD_OUTGOINGPORT).setValue(UIAccountCreation.DEFAULT_SMTPSSL_PORT) ;
      } else {
        uiWs3.getUIStringInput(UIAccountWizardStep3.FIELD_OUTGOINGPORT).setValue(UIAccountCreation.DEFAULT_SMTP_PORT) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
    } 
  }
}
