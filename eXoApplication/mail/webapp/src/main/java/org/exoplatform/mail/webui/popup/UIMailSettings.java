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
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Aug 10, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIMailSettings.gtmpl",
    events = {
        @EventConfig(listeners = UIMailSettings.SaveActionListener.class),
        @EventConfig(listeners = UIMailSettings.CancelActionListener.class)
    }
)
public class UIMailSettings extends UIForm implements UIPopupComponent {
  public static final String NUMBER_OF_CONVERSATION = "number-of-conversation".intern();
  public static final String DEFAULT_ACCOUNT = "default-account".intern();
  public static final String PERIOD_CHECK_MAIL = "period-check-mail".intern();
  public static final String COMPOSE_MESSAGE_IN = "compose-message-in".intern();
  public static final String REPLY_FORWARD_AS = "reply-forward-as".intern();
  public static final String REPLY_MESSAGE_WITH = "reply-message-with".intern();
  public static final String FORWARD_MESSAGE_WITH = "forward-message-with".intern();
  public static final String PREFIX_MESSAGE_WITH = "prefix-message-with".intern();
  public static final String SAVE_SENT_MESSAGE = "save-sent-message".intern();
  
  public UIMailSettings() {
    UIFormInputWithActions  setting = new UIFormInputWithActions("setting").setRendered(true);
    
    List<SelectItemOption<String>> numberConversation = new ArrayList<SelectItemOption<String>>();
    for (int i = 1; i <= 10; i++) {
      numberConversation.add(new SelectItemOption<String>(String.valueOf(10*i)));
    }
    setting.addUIFormInput(new UIFormSelectBox(NUMBER_OF_CONVERSATION, NUMBER_OF_CONVERSATION, numberConversation));
    
    List<SelectItemOption<String>> autoCheckmail = new ArrayList<SelectItemOption<String>>();
    autoCheckmail.add(new SelectItemOption<String>("Never", String.valueOf(MailSetting.NEVER_CHECK_AUTO)));
    autoCheckmail.add(new SelectItemOption<String>("10 minutes", String.valueOf(MailSetting.TEN_MINS)));
    autoCheckmail.add(new SelectItemOption<String>("20 minutes", String.valueOf(MailSetting.TWENTY_MINS)));
    autoCheckmail.add(new SelectItemOption<String>("30 minutes", String.valueOf(MailSetting.THIRTY_MINS)));
    autoCheckmail.add(new SelectItemOption<String>("1 hour", String.valueOf(MailSetting.ONE_HOUR)));
    setting.addUIFormInput(new UIFormSelectBox(PERIOD_CHECK_MAIL, PERIOD_CHECK_MAIL, autoCheckmail));
    
    List<SelectItemOption<String>> defaultAccount = new ArrayList<SelectItemOption<String>>();
    setting.addUIFormInput(new UIFormSelectBox(DEFAULT_ACCOUNT, DEFAULT_ACCOUNT, defaultAccount));
    
    List<SelectItemOption<String>> composeMessageIn = new ArrayList<SelectItemOption<String>>();
    composeMessageIn.add(new SelectItemOption<String>("Wysiwyg", MailSetting.WYSIWYG));
    composeMessageIn.add(new SelectItemOption<String>("Plain text", MailSetting.PLAIN_TEXT));
    setting.addUIFormInput(new UIFormSelectBox(COMPOSE_MESSAGE_IN, COMPOSE_MESSAGE_IN, composeMessageIn));
    
    List<SelectItemOption<String>> replyForwardAs = new ArrayList<SelectItemOption<String>>();
    replyForwardAs.add(new SelectItemOption<String>("Format of the original message", MailSetting.FORMAT_AS_ORIGINAL));
    replyForwardAs.add(new SelectItemOption<String>("Text Only", MailSetting.FORMAT_AS_TEXTONLY));
    setting.addUIFormInput(new UIFormSelectBox(REPLY_FORWARD_AS, REPLY_FORWARD_AS, replyForwardAs));
    
    List<SelectItemOption<String>> replyMessageWith = new ArrayList<SelectItemOption<String>>();
    replyMessageWith.add(new SelectItemOption<String>("Original message", MailSetting.REPLY_AS_ORIGINAL));
    replyMessageWith.add(new SelectItemOption<String>("Original message included attachment", MailSetting.REPLY_WITH_ATTACH));
    setting.addUIFormInput(new UIFormSelectBox(REPLY_MESSAGE_WITH, REPLY_MESSAGE_WITH, replyMessageWith));
    
    List<SelectItemOption<String>> forwardMessageWith = new ArrayList<SelectItemOption<String>>();
    forwardMessageWith.add(new SelectItemOption<String>("Original message", MailSetting.FORWARD_AS_ORIGINAL));
    forwardMessageWith.add(new SelectItemOption<String>("Original message included attachment", MailSetting.FORWARD_WITH_ATTACH));
    setting.addUIFormInput(new UIFormSelectBox(FORWARD_MESSAGE_WITH, FORWARD_MESSAGE_WITH, forwardMessageWith));
    
    List<SelectItemOption<String>> prefixMessageWith = new ArrayList<SelectItemOption<String>>();
    prefixMessageWith.add(new SelectItemOption<String>("---------------------", MailSetting.PREFIX_WITH_MINUS));
    prefixMessageWith.add(new SelectItemOption<String>("*********************", MailSetting.PREFIX_WITH_STAR));
    prefixMessageWith.add(new SelectItemOption<String>("=====================", MailSetting.PREFIX_WITH_EQUAL));
    prefixMessageWith.add(new SelectItemOption<String>("'''''''''''''''''''''", MailSetting.PREFIX_WITH_QUOTE));
    setting.addUIFormInput(new UIFormSelectBox(PREFIX_MESSAGE_WITH, PREFIX_MESSAGE_WITH, prefixMessageWith));
    
    setting.addUIFormInput(new UIFormCheckBoxInput<Boolean>(SAVE_SENT_MESSAGE, SAVE_SENT_MESSAGE, false));
    addChild(setting);
  }
  
  public String getShowNumberOfConversation() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(NUMBER_OF_CONVERSATION).getValue();
  }
  
  public String getPeriodCheckMailAuto() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(PERIOD_CHECK_MAIL).getValue();
  }
  
  public String getDefaultAccount() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(DEFAULT_ACCOUNT).getValue();
  }
  
  public String getFormatWhenReplyForward() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(REPLY_FORWARD_AS).getValue();
  }
  
  public String getTypeOfEditor() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(COMPOSE_MESSAGE_IN).getValue();
  }
  
  public String getReplyMessageWith() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(REPLY_MESSAGE_WITH).getValue();
  }
  
  public String getForwardMessageWith() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(FORWARD_MESSAGE_WITH).getValue();
  }
  
  public String getPrefixMessageWith() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormSelectBox(PREFIX_MESSAGE_WITH).getValue();
  }
  
  public Boolean saveMessageInSent() throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    return inputSet.getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).isChecked();
  }
  
  public void fillFormAccount(List<SelectItemOption<String>> options) throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    inputSet.getUIFormSelectBox(DEFAULT_ACCOUNT).setOptions(options);
  }
  
  public void fillAllField(MailSetting mailSetting) throws Exception {
    UIFormInputWithActions inputSet = getChildById("setting");
    inputSet.getUIFormSelectBox(NUMBER_OF_CONVERSATION).setValue(String.valueOf(mailSetting.getShowNumberMessage()));
    inputSet.getUIFormSelectBox(PERIOD_CHECK_MAIL).setValue(String.valueOf(mailSetting.getPeriodCheckMailAuto()));
    inputSet.getUIFormSelectBox(DEFAULT_ACCOUNT).setValue(mailSetting.getDefaultAccount());
    inputSet.getUIFormSelectBox(COMPOSE_MESSAGE_IN).setValue(mailSetting.getTypeOfEditor()); 
    inputSet.getUIFormSelectBox(REPLY_FORWARD_AS).setValue(mailSetting.getFormatWhenReplyForward());
    inputSet.getUIFormSelectBox(REPLY_MESSAGE_WITH).setValue(mailSetting.getReplyMessageWith());
    inputSet.getUIFormSelectBox(FORWARD_MESSAGE_WITH).setValue(mailSetting.getForwardMessageWith());
    inputSet.getUIFormSelectBox(PREFIX_MESSAGE_WITH).setValue(mailSetting.getPrefixMessageWith());
    inputSet.getUIFormCheckBoxInput(SAVE_SENT_MESSAGE).setChecked(mailSetting.saveMessageInSent());
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class SaveActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      UIMailSettings uiMailSetting = event.getSource();
      UIMailPortlet uiPortlet = uiMailSetting.getAncestorOfType(UIMailPortlet.class);
      String username = MailUtils.getCurrentUser() ;
      MailService mailSrv = MailUtils.getMailService();
      MailSetting mailSetting = new MailSetting();
      mailSetting.setShowNumberMessage(Long.parseLong(uiMailSetting.getShowNumberOfConversation()));
      mailSetting.setPeriodCheckMailAuto(Long.parseLong(uiMailSetting.getPeriodCheckMailAuto()));
      mailSetting.setDefaultAccount(uiMailSetting.getDefaultAccount());
      mailSetting.setTypeOfEditor(uiMailSetting.getTypeOfEditor());
      mailSetting.setFormatWhenReplyForward(uiMailSetting.getFormatWhenReplyForward());
      mailSetting.setReplyMessageWith(uiMailSetting.getReplyMessageWith());
      mailSetting.setForwardMessageWith(uiMailSetting.getForwardMessageWith());
      mailSetting.setPrefixMessageWith(uiMailSetting.getPrefixMessageWith());
      mailSetting.setSaveMessageInSent(uiMailSetting.saveMessageInSent());
      mailSrv.saveMailSetting(SessionsUtils.getSessionProvider(), username, mailSetting);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.setMessagePageList(mailSrv.getMessagePageList(SessionsUtils.getSessionProvider(), username, uiMessageList.getMessageFilter()));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      uiPortlet.cancelAction();
    }
  }
 
  static  public class CancelActionListener extends EventListener<UIMailSettings> {
    public void execute(Event<UIMailSettings> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
