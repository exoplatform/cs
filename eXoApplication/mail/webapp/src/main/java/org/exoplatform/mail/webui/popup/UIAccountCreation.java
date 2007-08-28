/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.AuthenticationFailedException;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.Selector;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.services.jcr.util.IdGenerator ;
/**
 * Created by The eXo Platform SARL
 * Author : Philippe Aristote
 *          philippe.aristote@gmail.com
 *          Tuan Pham
 *          phamtuanchip@gmail.com
 * Aug 10, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAccountCreation.gtmpl",
    events = {
      @EventConfig(listeners = UIAccountCreation.ViewStep1ActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ViewStep2ActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ViewStep3ActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ChangeServerTypeActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ChangeCheckedActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ViewStep4ActionListener.class),
      @EventConfig(listeners = UIAccountCreation.ViewStep5ActionListener.class),
      @EventConfig(listeners = UIAccountCreation.SelectFolderActionListener.class),
      @EventConfig(listeners = UIAccountCreation.NextActionListener.class),
      @EventConfig(listeners = UIAccountCreation.BackActionListener.class),
      @EventConfig(listeners = UIAccountCreation.FinishActionListener.class),
      @EventConfig(listeners = UIAccountCreation.CancelActionListener.class, confirm = "UIAccountCreation.msg.confirm-cancel") 
    }
)
public class UIAccountCreation extends UIFormTabPane implements UIPopupComponent, Selector {

  private int wizardMaxStep_ = 5 ;
  private int selectedStep_ = 1 ;
  private int currentStep_ = 0 ;
  private boolean isAddNew_ = true ;
  private boolean isShowStepActions_ = true ;

  private Map<Integer, String> chidrenMap_ = new HashMap<Integer, String>() ; 
  private Map<Integer, String[]> actionMap_ = new HashMap<Integer, String[]>() ;

  final static public String POPUPID = "UIAccountCreationWizardPopup" ;
  final static public String FIELD_STEP1 = "step1" ;
  final static public String FIELD_STEP2 = "step2" ;
  final static public String FIELD_STEP3 = "step3" ;
  final static public String FIELD_STEP4 = "step4" ;
  final static public String FIELD_STEP5 = "step5" ;
  final static public String[] ACT_SELETFOLDER = {"SelectFolder"} ;
  final static public String ACT_CHECKSAVEPASS =  "CheckSavePass" ;
  final static public String[] ACT_CHECKGETMAIL = {"CheckGetMail"} ;
  final static public String  ACT_CHANGE_TYPE = "ChangeServerType".intern()  ;
  final static public String  ACT_CHANGE_SSL =  "ChangeChecked".intern()  ;

  public UIAccountCreation() throws Exception {
    super("UIAccountCreation") ;
    chidrenMap_.put(1, FIELD_STEP1) ;
    chidrenMap_.put(2, FIELD_STEP2) ;
    chidrenMap_.put(3, FIELD_STEP3) ;
    chidrenMap_.put(4, FIELD_STEP4) ;
    chidrenMap_.put(5, FIELD_STEP5) ;
    actionMap_.put(1, new String[]{"Next", "Cancel"}) ;
    actionMap_.put(2, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(3, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(4, new String[]{"Back", "Next", "Cancel"}) ;
    actionMap_.put(5, new String[]{"Back", "Finish", "Cancel"}) ;
    addUIComponentInput(new UIAccountWizardStep1(FIELD_STEP1)) ;
    addUIComponentInput(new UIAccountWizardStep2(FIELD_STEP2)) ;
    addUIComponentInput(new UIAccountWizardStep3(FIELD_STEP3)) ;
    addUIComponentInput(new UIAccountWizardStep4(FIELD_STEP4)) ;
    addUIComponentInput(new UIAccountWizardStep5(FIELD_STEP5)) ;
    setRenderedChild(getCurrentChild()) ;
  }

  public boolean showStepActions() {return isShowStepActions_ ;}
  protected void setShowStepActions(boolean isShow) {isShowStepActions_ = isShow ;}
  public void setCurrentSep(int step){ currentStep_ = step ;}
  public int getCurrentStep() { return currentStep_; }
  public void setSelectedStep(int step){ selectedStep_ = step ;}
  public int getSelectedStep() { return selectedStep_; }
  public int getMaxStep(){return wizardMaxStep_ ;}
  public String[] getActions(){return actionMap_.get(selectedStep_) ;}
  public String getCurrentChild() {return chidrenMap_.get(selectedStep_) ;}
  public String[] getCurrentAction() {return actionMap_.get(selectedStep_) ;}
  public int getNumberSteps() {return wizardMaxStep_ ;}

  protected boolean isAddNew(){return isAddNew_ ;} 
  protected void addNew(boolean isNew) {isAddNew_ = isNew ;}

  protected void viewStep(int step) {  
    selectedStep_ = step ;
    currentStep_ = step - 1 ;    
    List<UIComponent> children = getChildren(); 
    for(int i=0; i<children.size(); i++){
      if(i == getCurrentStep()) {
        children.get(i).setRendered(true);
      } else {
        children.get(i).setRendered(false);
      }
    }
  }
  protected void nextStep() {
    int step = getCurrentStep() ;
    List<UIComponent> children = getChildren() ;
    if(step < getMaxStep()) {
      step++ ;
      setCurrentSep(step) ;
      for(int i = 0 ; i< children.size(); i++) {
        if(i == step) {
          children.get(i).setRendered(true);
          setSelectedStep(step+1) ;
        } else {
          children.get(i).setRendered(false);
        } 
      } 
    }
  }
  protected void backStep() {
    int step = getCurrentStep() ;
    List<UIComponent> children = getChildren() ;
    if(step > 0) {
      step-- ;
      setCurrentSep(step) ;
      for(int i = 0 ; i< children.size(); i++) {
        if(i == step) {
          children.get(i).setRendered(true);
          setSelectedStep(step+1) ;
        } else {
          children.get(i).setRendered(false);
        } 
      }
    }
  }

  protected void loadForm() {

  }
  protected void saveForm(String currentUser, Account account) throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    mailSvr.createAccount(currentUser, account) ;
    String[] defaultFolders =  {"Drafts","Sent", "Spam", "Trash"} ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class) ;
    String username = uiPortlet.getCurrentUser() ;
    for(String defaultFolerName : defaultFolders) {
      Folder folder = mailSvr.getFolder(username, account.getId(), defaultFolerName) ;
      if(folder == null) {
        folder = new Folder() ;
        folder.setName(defaultFolerName) ;
        folder.setLabel(defaultFolerName) ;
        folder.setPersonalFolder(false) ;
        mailSvr.saveUserFolder(username, account.getId(), folder) ;
      }
    }
  }
  protected void getMail(String accountId) throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class) ;
    String username = uiPortlet.getCurrentUser() ;
    Account account = mailSvr.getAccountById(username, accountId) ;
    mailSvr.checkNewMessage(username, account) ;
  }

  protected void resetForm() {}

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void updateValue(String fieldId, String value) {
    // TODO Auto-generated method stub

  }
  public static class ViewStep1ActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.viewStep(1) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  public static class ViewStep2ActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.viewStep(2) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  public static class ViewStep3ActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.viewStep(3) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  public static class ViewStep4ActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.viewStep(4) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  public static class ViewStep5ActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.viewStep(5) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
  public static class FinishActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP1) ;
      UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP2) ;
      UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP3) ;
      UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP4) ;
      UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP5) ;
      String accname, description, displayName, email, replyMail, signature, protocol, popHost, 
      popPort, smtpHost, smtpPort, storeFolder, userName, password ;
      boolean isSSL ;
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
      userName = uiAccWs4.getUserName() ;
      
      password = null ;
      Account acc = new Account() ;
      if(uiAccWs4.getIsSavePass()) password = uiAccWs4.getPassword() ;
      acc.setLabel(accname) ;
      acc.setDescription(description) ;
      acc.setUserDisplayName(displayName) ;
      acc.setEmailAddress(email) ;
      acc.setEmailReplyAddress(replyMail) ;
      acc.setSignature(signature) ;
      acc.setServerProperty(Utils.SVR_USERNAME, userName); 
      acc.setServerProperty(Utils.SVR_PASSWORD, password);
      acc.setServerProperty(Utils.SVR_POP_HOST, popHost);
      acc.setServerProperty(Utils.SVR_POP_PORT, popPort);  
      acc.setServerProperty(Utils.SVR_PROTOCOL, protocol);  
      acc.setServerProperty(Utils.SVR_SSL, String.valueOf(isSSL));
      acc.setServerProperty(Utils.SVR_FOLDER, storeFolder) ;
      acc.setServerProperty(Utils.SVR_SMTP_USER, userName);
      acc.setServerProperty(Utils.SVR_SMTP_HOST, smtpHost);
      acc.setServerProperty(Utils.SVR_SMTP_PORT, smtpPort);
      
      UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
      UIMailPortlet uiPortlet = uiAccCreation.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      try {
         uiAccCreation.saveForm(uiPortlet.getCurrentUser(), acc) ;
        uiNavigation.getChild(UISelectAccount.class).refreshItems() ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.create-acc-successfully", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiAccCreation.getAncestorOfType(UIPopupAction.class).deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getAncestorOfType(UIPopupAction.class)) ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.create-acc-unsuccessfully", null, ApplicationMessage.ERROR)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      } 
      if(uiAccWs5.isGetmail()) {
        try {
          uiAccCreation.getMail(acc.getId()) ;
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
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiNavigation) ;
    }
  }

  public static class NextActionListener extends EventListener<UIAccountCreation>{
    public void execute(Event<UIAccountCreation> event) throws Exception {
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep1 uiAccWs1 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP1) ;
      UIAccountWizardStep2 uiAccWs2 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP2) ;
      UIAccountWizardStep3 uiAccWs3 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP3) ;
      UIAccountWizardStep4 uiAccWs4 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP4) ;
      UIAccountWizardStep5 uiAccWs5 = uiAccCreation.getChildById(UIAccountCreation.FIELD_STEP5) ;
      WizardStep wss = (WizardStep)uiAccCreation.getChildById(uiAccCreation.getCurrentChild()) ;
      if(wss.isFieldsValid()) { 
        uiAccCreation.nextStep() ;
        if(uiAccWs4.isRendered()) {
          String accname = uiAccWs1.getAccName() ;
          String accOutgoingName = uiAccWs2.getOutgoingName() ;
          String email = uiAccWs2.getEmailAddress() ;
          String serverName = uiAccWs3.getIncomingServer();
          String serverType = uiAccWs3.getServerType(); 
          String storeFolder = uiAccWs3.getStoreFolder() ;
          uiAccWs5.fillFields(accname, accOutgoingName, email, serverName, serverType, storeFolder) ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAccCreation.getParent()) ;
      } else {
        UIApplication uiApp = uiAccCreation.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.fields-requirement", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
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
      UIPopupAction uiPopup = uiAccCreation.getAncestorOfType(UIPopupAction.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  public static class SelectFolderActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception {
      System.out.println("\n\n SelectFolderActionListener");
    } 
  }
  public static class ChangeServerTypeActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception {
      System.out.println("\n\n ChangeServerTypeActionListener");
      UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep3 uiWs3 = uiAccCreation.getChildById(FIELD_STEP3) ;
      uiWs3.setDefaultValue(uiWs3.getServerType(), uiWs3.getIsSSL()) ;
    } 
  }
  public static class ChangeCheckedActionListener extends EventListener<UIAccountCreation> {
    public void execute(Event<UIAccountCreation> event) throws Exception {
      System.out.println("\n\n ChangeCheckedActionListener"); UIAccountCreation uiAccCreation = event.getSource() ;
      UIAccountWizardStep3 uiWs3 = uiAccCreation.getChildById(FIELD_STEP3) ;
      uiWs3.setDefaultValue(uiWs3.getServerType(), uiWs3.getIsSSL()) ;
    } 
  }
}
