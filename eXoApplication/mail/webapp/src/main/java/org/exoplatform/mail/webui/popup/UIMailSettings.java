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

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

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
        @EventConfig(listeners = UIMailSettings.CancelActionListener.class)
    }
)
public class UIMailSettings extends UIForm implements UIPopupComponent {
  public static final String DEFAULT_ACCOUNT = "default-account".intern();
  public static final String NUMBER_MSG_PER_PAGE = "number-of-conversation".intern() ;
  public static final String PERIOD_CHECK_AUTO = "period-check-mail".intern() ;
  public static final String COMPOSE_MESSAGE_IN = "compose-message-in".intern();
  public static final String FORMAT_AS_ORIGINAL = "reply-forward-as".intern();
  public static final String REPLY_WITH_ATTACH = "reply-message-with".intern();
  public static final String FORWARD_WITH_ATTACH = "forward-message-with".intern();
  public static final String SAVE_SENT_MESSAGE = "save-sent-message".intern();
  
  public UIMailSettings() { }
  
  public void init() throws Exception {
    addUIFormInput(new UIFormSelectBox(DEFAULT_ACCOUNT, DEFAULT_ACCOUNT, getAccounts()));
        
    List<SelectItemOption<String>> numberPerPage = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i <= 7; i++) {
      numberPerPage.add(new SelectItemOption<String>(String.valueOf(10*i)));
    }
    addUIFormInput(new UIFormSelectBox(NUMBER_MSG_PER_PAGE, NUMBER_MSG_PER_PAGE, numberPerPage));  
    
    List<SelectItemOption<String>> periodCheckAuto = new ArrayList<SelectItemOption<String>>();
    periodCheckAuto.add(new SelectItemOption<String>("Never", String.valueOf(MailSetting.NEVER_CHECK_AUTO)));
    periodCheckAuto.add(new SelectItemOption<String>("5 minutes", String.valueOf(MailSetting.FIVE_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("10 minutes", String.valueOf(MailSetting.TEN_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("20 minutes", String.valueOf(MailSetting.TWENTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("30 minutes", String.valueOf(MailSetting.THIRTY_MINS)));
    periodCheckAuto.add(new SelectItemOption<String>("1 hour", String.valueOf(MailSetting.ONE_HOUR)));
    addUIFormInput(new UIFormSelectBox(PERIOD_CHECK_AUTO, PERIOD_CHECK_AUTO, periodCheckAuto));
    
    List<SelectItemOption<String>> useWysiwyg = new ArrayList<SelectItemOption<String>>();
    useWysiwyg.add(new SelectItemOption<String>("Rich text editor (HTML format)", "true"));
    useWysiwyg.add(new SelectItemOption<String>("Plain text", "false"));
    addUIFormInput(new UIFormSelectBox(COMPOSE_MESSAGE_IN, COMPOSE_MESSAGE_IN, useWysiwyg));
    
    List<SelectItemOption<String>> formatAsOriginal = new ArrayList<SelectItemOption<String>>();
    formatAsOriginal.add(new SelectItemOption<String>("Format of the original message", "true"));
    formatAsOriginal.add(new SelectItemOption<String>("Text only","false" ));
    addUIFormInput(new UIFormSelectBox(FORMAT_AS_ORIGINAL, FORMAT_AS_ORIGINAL, formatAsOriginal));
    

    List<SelectItemOption<String>> replyWithAtt = new ArrayList<SelectItemOption<String>>();
    replyWithAtt.add(new SelectItemOption<String>("Original message included attachment", "true"));
    replyWithAtt.add(new SelectItemOption<String>("Original message", "false"));
    addUIFormInput(new UIFormSelectBox(REPLY_WITH_ATTACH, REPLY_WITH_ATTACH, replyWithAtt));
    
    List<SelectItemOption<String>> forwardWithAtt = new ArrayList<SelectItemOption<String>>();
    forwardWithAtt.add(new SelectItemOption<String>("Original message included attachment", "true"));
    forwardWithAtt.add(new SelectItemOption<String>("Original message", "false"));
    addUIFormInput(new UIFormSelectBox(FORWARD_WITH_ATTACH, FORWARD_WITH_ATTACH, forwardWithAtt));
    
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SAVE_SENT_MESSAGE, SAVE_SENT_MESSAGE, false));
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
      getUIFormSelectBox(DEFAULT_ACCOUNT).setValue(setting.getDefaultAccount()) ;
      getUIFormSelectBox(NUMBER_MSG_PER_PAGE).setValue(String.valueOf(setting.getNumberMsgPerPage()));
      getUIFormSelectBox(PERIOD_CHECK_AUTO).setValue(String.valueOf(setting.getPeriodCheckAuto()));
      getUIFormSelectBox(COMPOSE_MESSAGE_IN).setValue(String.valueOf(setting.useWysiwyg()));
      getUIFormSelectBox(FORMAT_AS_ORIGINAL).setValue(String.valueOf(setting.formatAsOriginal()));
      getUIFormSelectBox(REPLY_WITH_ATTACH).setValue(String.valueOf(setting.replyWithAttach()));
      getUIFormSelectBox(FORWARD_WITH_ATTACH).setValue(String.valueOf(setting.forwardWithAtt()));
      getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).setChecked(setting.saveMessageInSent());
    }
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class SaveActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      UIMailSettings uiSetting = event.getSource();
		  UIMailPortlet uiPortlet = uiSetting.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
      String accountId = uiSelectAccount.getSelectedValue();
		  MailService mailSrv = MailUtils.getMailService();
		  MailSetting setting = new MailSetting();
      String defaultAcc = uiSetting.getUIFormSelectBox(DEFAULT_ACCOUNT).getValue() ;
		  setting.setDefaultAccount(defaultAcc) ;
      setting.setNumberMsgPerPage(Long.valueOf(uiSetting.getUIFormSelectBox(NUMBER_MSG_PER_PAGE).getValue())) ;
		  setting.setPeriodCheckAuto(Long.valueOf(uiSetting.getUIFormSelectBox(PERIOD_CHECK_AUTO).getValue())) ;
      setting.setUseWysiwyg(Boolean.valueOf(uiSetting.getUIFormSelectBox(COMPOSE_MESSAGE_IN).getValue())) ;
      setting.setFormatAsOriginal(Boolean.valueOf(uiSetting.getUIFormSelectBox(FORMAT_AS_ORIGINAL).getValue())) ;
      setting.setReplyWithAttach(Boolean.valueOf(uiSetting.getUIFormSelectBox(REPLY_WITH_ATTACH).getValue()));
      setting.setForwardWithAtt(Boolean.valueOf(uiSetting.getUIFormSelectBox(FORWARD_WITH_ATTACH).getValue()));
      setting.setSaveMessageInSent(uiSetting.getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).isChecked());
      mailSrv.saveMailSetting(SessionProviderFactory.createSystemProvider(), username, setting);
		  UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
		  uiMessageList.setMessagePageList(mailSrv.getMessagePageList(SessionProviderFactory.createSystemProvider(), username, uiMessageList.getMessageFilter()));
      if (defaultAcc != null && !defaultAcc.equals(accountId)) {
        uiSelectAccount.updateAccount() ;
        uiSelectAccount.setSelectedValue(defaultAcc) ;
      }
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
		  uiPortlet.cancelAction();
	  }
  }

  static  public class CancelActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
