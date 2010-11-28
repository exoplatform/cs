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

import java.io.ByteArrayInputStream;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.CalendarUtils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.popup.UIAddressForm.ContactData;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.wysiwyg.FCKEditorConfig;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * <hung.nguyen@exoplatform.com> Phung Nam <phunghainam@gmail.com> Aus 01, 2007
 * 2:48:18 PM
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/mail/webui/popup/UIComposeForm.gtmpl", events = {
    @EventConfig(listeners = UIComposeForm.SendActionListener.class),
    @EventConfig(listeners = UIComposeForm.SaveDraftActionListener.class),
    @EventConfig(phase = Phase.DECODE, listeners = UIComposeForm.DiscardChangeActionListener.class),
    @EventConfig(listeners = UIComposeForm.AttachmentActionListener.class),
    @EventConfig(listeners = UIComposeForm.DownloadActionListener.class),
    @EventConfig(listeners = UIComposeForm.RemoveAttachmentActionListener.class),
    @EventConfig(listeners = UIComposeForm.ToActionListener.class),
    @EventConfig(listeners = UIComposeForm.ToCCActionListener.class),
    @EventConfig(listeners = UIComposeForm.ToBCCActionListener.class),
    @EventConfig(listeners = UIComposeForm.ChangePriorityActionListener.class),
    @EventConfig(listeners = UIComposeForm.UseVisualEdiorActionListener.class),
    @EventConfig(listeners = UIComposeForm.ShowCcActionListener.class),
    @EventConfig(listeners = UIComposeForm.ShowBccActionListener.class),
    @EventConfig(listeners = UIComposeForm.ReturnReceiptActionListener.class),
    @EventConfig(listeners = UIComposeForm.CallDMSSelectorActionListener.class),
    @EventConfig(listeners = UIComposeForm.RemoveGroupActionListener.class) })
public class UIComposeForm extends UIForm implements UIPopupComponent, UISelectable {
  final static public String                   FIELD_TO_SET         = "toSet".intern();

  final static public String                   FIELD_FROM           = "from";

  final static public String                   FIELD_SUBJECT        = "subject";

  final static public String                   FIELD_TO             = "to";

  final static public String                   FIELD_CC             = "cc";

  final static public String                   FIELD_BCC            = "bcc";

  final static public String                   FIELD_ATTACHMENTS    = "attachments";

  final static public String                   FIELD_MESSAGECONTENT = "messageContent";

  final static public String                   ACT_TO               = "To";

  final static public String                   ACT_CC               = "ToCC";

  final static public String                   ACT_BCC              = "ToBCC";

  final static public String                   ACT_REMOVE           = "remove";

  final static public int                      MESSAGE_NEW          = 0;

  final public int                             MESSAGE_IN_DRAFT     = 1;

  final public int                             MESSAGE_REPLY        = 2;

  final public int                             MESSAGE_REPLY_ALL    = 3;

  final public int                             MESSAGE_FOWARD       = 4;

  private Map<String, String>                  addressBooksMap      = null;

  private List<Attachment>                     attachments_         = new ArrayList<Attachment>();

  private Message                              message_             = null;

  private long                                 priority_            = Utils.PRIORITY_NORMAL;

  private Boolean                              isVisualEditor       = true;

  private Boolean                              isReturnReceipt      = false;

  private int                                  composeType_         = MESSAGE_NEW;

  private String                               accountId_;

  private Map<String, HashMap<String, String>> groupData;

  final static public String                   FIELD_TO_GROUP       = "To_Group";

  final static public String                   FIELD_CC_GROUP       = "Cc_Group";

  final static public String                   FIELD_BCC_GROUP      = "Bcc_Group";

  final public String                          ALL                  = "all";

  final public String                          SHARED_CONTACTS_     = "sharedContacts";

  final public static String                   PERSONAL             = "0".intern();

  final public static String                   SHARED               = "1".intern();

  final public static String                   PUBLIC               = "2".intern();

  public String                                parentPath_;

  public List<ContactData>                     toContacts           = new ArrayList<ContactData>();

  public List<ContactData>                     ccContacts           = new ArrayList<ContactData>();

  public List<ContactData>                     bccContacts          = new ArrayList<ContactData>();

  private static final Log                     logger               = ExoLogger.getLogger(UIComposeForm.class);

  public boolean isVisualEditor() {
    return isVisualEditor;
  }

  public void setVisualEditor(boolean b) {
    isVisualEditor = b;
  }

  public UIComposeForm() throws Exception {
  }

  @SuppressWarnings("deprecation")
  public void init(String accountId, Message msg, int composeType) throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    String username = MailUtils.getCurrentUser();
    accountId_ = accountId;
    MailService mailSrv = getApplicationComponent(MailService.class);
    // img
    if (msg != null) {
      if (msg.getAttachments() != null) {
        try {
          Map<String, String> attachments = new HashMap<String, String>();
          for (Attachment attach : msg.getAttachments()) {
            String attLink = MailUtils.getImageSource(attach, getDownloadService());
            if (attLink != null) {
              attLink = MailUtils.getAttachmentLink(attach);
              String attId = attach.getId();
              attachments.put(attId.substring(attId.lastIndexOf("/") + 1, attId.length()),
                              attLink.substring(0, attLink.lastIndexOf("/") + 1));
            }
          }
          String body = msg.getMessageBody();
          String newBody = MailUtils.fillImage(body, attachments);
          msg.setMessageBody(newBody);
        } catch (Exception e) {
          logger.warn("Error when get attachment link of mail message", e);
        }
      }
    } else if(composeType != UIComposeForm.MESSAGE_NEW)
      logger.info("The message is null or attachment(s) message hasn't damaged");
    for (Account acc : mailSrv.getAccounts(username)) {
      SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName()
          + " &lt;" + acc.getEmailAddress() + "&gt;", acc.getId());
      if (acc.getId().equals(accountId)) {
        itemOption.setSelected(true);
      }
      options.add(itemOption);
    }

    UIComposeInput toSet = new UIComposeInput(FIELD_TO_SET);
    toSet.addUIFormInput(new UIFormSelectBox(FIELD_FROM, FIELD_FROM, options));
    toSet.addUIFormInput(new UIFormStringInput(FIELD_TO, null, null));

    UIFormTextAreaInput textAreaCC = new UIFormTextAreaInput(FIELD_CC, FIELD_CC, null);
    textAreaCC.setId(FIELD_CC);
    textAreaCC.setColumns(2);
    toSet.addUIFormInput(textAreaCC);

    UIFormTextAreaInput textAreaBCC = new UIFormTextAreaInput(FIELD_BCC, FIELD_BCC, null);
    textAreaBCC.setColumns(2);
    textAreaBCC.setId(FIELD_BCC);
    toSet.addUIFormInput(textAreaBCC);
    toSet.addUIFormInput(new UIFormStringInput(FIELD_SUBJECT, FIELD_SUBJECT, null));

    toSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null));
    toSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList());

    addUIFormInput(toSet);

    MailSetting mailSetting = mailSrv.getMailSetting(username);
    isVisualEditor = mailSetting.useWysiwyg();
    if (isVisualEditor) {
      UIFormWYSIWYGInput uiFormWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT,
                                                                     FIELD_MESSAGECONTENT,
                                                                     null);
      FCKEditorConfig fckconfig = new FCKEditorConfig();
      fckconfig.put("CustomConfigurationsPath", "/csResources/javascript/eXo/cs/fckconfig.js");
      uiFormWYSIWYGInput.setFCKConfig(fckconfig);
      addUIFormInput(uiFormWYSIWYGInput);
    } else {
      addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, FIELD_MESSAGECONTENT, null));
    }
    setPriority(Utils.PRIORITY_NORMAL);
    setMessage(msg, composeType);
  }

  public DownloadService getDownloadService() {
    return getApplicationComponent(DownloadService.class);
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }

  public List<ContactData> getToContacts() {
    return toContacts;
  }

  public void setToContacts(List<ContactData> contactList) {
    toContacts = contactList;
  }

  public List<ContactData> getCcContacts() {
    return ccContacts;
  }

  public void setCcContacts(List<ContactData> contactList) {
    ccContacts = contactList;
  }

  public List<ContactData> getBccContacts() {
    return bccContacts;
  }

  public void setBccContacts(List<ContactData> contactList) {
    bccContacts = contactList;
  }

  public int getComposeType() {
    return composeType_;
  }

  public void setComposeType(int t) {
    composeType_ = t;
  }

  public List<ActionData> getUploadFileList() throws Exception {
    List<ActionData> uploadedFiles = new ArrayList<ActionData>();
    for (Attachment attachdata : attachments_) {
      if (!attachdata.isShownInBody()) {
        ActionData fileUpload = new ActionData();
        fileUpload.setActionListener("Download");
        fileUpload.setActionParameter(MailUtils.encodeMailId(attachdata.getId()));
        fileUpload.setActionType(ActionData.TYPE_ICON);
        fileUpload.setCssIconClass("AttachmentIcon");
        fileUpload.setActionName(attachdata.getName() + " ("
            + MailUtils.convertSize(attachdata.getSize()) + ")");
        fileUpload.setShowLabel(true);
        uploadedFiles.add(fileUpload);
        ActionData removeAction = new ActionData();
        removeAction.setActionListener("RemoveAttachment");
        removeAction.setActionName(ACT_REMOVE);
        removeAction.setActionParameter(MailUtils.encodeMailId(attachdata.getId()));
        removeAction.setCssIconClass("LabelLink");
        removeAction.setActionType(ActionData.TYPE_LINK);
        removeAction.setBreakLine(true);
        uploadedFiles.add(removeAction);
      }
    }
    return uploadedFiles;
  }

  public List<String> getCheckedAttach() throws Exception {
    List<String> checkedAttach = new ArrayList<String>();
    for (Attachment att : attachments_) {
      checkedAttach.add(att.getId());
    }
    return checkedAttach;
  }

  public void refreshUploadFileList() throws Exception {
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList());
  }

  public void addToUploadFileList(Attachment attachfile) {
    attachments_.add(attachfile);
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(attachfile.getId(), null, null).setChecked(true));
  }

  public void removeFromUploadFileList(Attachment attachfile) {
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    inputSet.removeChildById(attachfile.getId());
    attachments_.remove(attachfile);
  }

  public void removeUploadFileList() {
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    for (Attachment att : attachments_) {
      inputSet.removeChildById(att.getId());
    }
    attachments_.clear();
  }

  public List<Attachment> getAttachFileList() {
    return attachments_;
  }

  public Message getMessage() {
    return message_;
  }

  public void setMessage(Message message, int composeType) throws Exception {
    setComposeType(composeType);
    this.message_ = message;
    fillFields(message_);
  }

  public void fillFields(Message msg) throws Exception {
    if (msg == null) {
      setFieldContentValue("");
      return;
    }
    MailService mailSrv = MailUtils.getMailService();
    MailSetting mailSetting = mailSrv.getMailSetting(MailUtils.getCurrentUser());
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    String subject = "";
    String replyContent = "";
    String originalAttId = "";
    switch (getComposeType()) {
    case MESSAGE_IN_DRAFT:
      setFieldSubjectValue(msg.getSubject());
      setFieldToValue(msg.getMessageTo());
      setFieldCcValue(msg.getMessageCc());
      setFieldBccValue(msg.getMessageBcc());
      setFieldContentValue(formatContent(msg));
      isReturnReceipt = msg.isReturnReceipt();
      setPriority(msg.getPriority());
      if (msg != null && msg.hasAttachment()) {
        try {
          for (Attachment att : msg.getAttachments()) {
            if (att.isLoadedProperly())
              attachments_.add(att);
          }
        } catch (Exception e) {
          logger.warn("Attachment was broken.");
        }
        if (attachments_.size() > 0) {
          for (ActionData actionData : getUploadFileList()) {
            inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                     null,
                                                                     null).setChecked(true));
          }
          refreshUploadFileList();
        }
      }
      break;
    case MESSAGE_REPLY:
      setFieldToValue(msg.getReplyTo());
      subject = msg.getSubject();
      if (!subject.toLowerCase().startsWith("re:"))
        subject = "Re: " + subject;
      setFieldSubjectValue(subject);
      setPriority(msg.getPriority());
      if (msg != null && msg.hasAttachment()) {
        for (Attachment att : msg.getAttachments()) {
          if (att.isLoadedProperly())
            attachments_.add(att);
        }
      }
      if (mailSetting.replyWithAttach())
        originalAttId = addOriginalMessageAsAttach(msg);
      else {
        replyContent = getReplyContent(msg);
      }
      setFieldContentValue(replyContent);

      if (attachments_.size() > 0) {
        for (ActionData actionData : getUploadFileList()) {
          if (actionData.getActionParameter().equals(originalAttId))
            inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                     null,
                                                                     null).setChecked(true));
          else
            inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                     null,
                                                                     null));
        }
        refreshUploadFileList();
      }
      break;
    case MESSAGE_REPLY_ALL:
      subject = msg.getSubject();
      if (!subject.toLowerCase().startsWith("re:"))
        subject = "Re: " + subject;
      setFieldSubjectValue(subject);
      String replyTo = msg.getReplyTo();
      setFieldToValue(replyTo);
      setPriority(msg.getPriority());

      String replyCc = "";

      String msgTo = (msg.getMessageTo() != null) ? msg.getMessageTo() : "";
      InternetAddress[] msgToAdds = Utils.getInternetAddress(msgTo);

      MailService mailSvr = this.getApplicationComponent(MailService.class);
      Account account = mailSvr.getAccountById(MailUtils.getCurrentUser(), accountId_);
      for (int i = 0; i < msgToAdds.length; i++) {
        if (msgToAdds[i] != null
            && !msgToAdds[i].getAddress().equalsIgnoreCase(account.getEmailAddress())
            && !msgToAdds[i].getAddress().equalsIgnoreCase(account.getIncomingUser())
            && !msgToAdds[i].getAddress().equalsIgnoreCase(replyTo)) {
          if (replyCc.trim().length() > 0)
            replyCc += ", ";
          replyCc += msgToAdds[i].toString();
        }
      }

      String msgCc = (msg.getMessageCc() != null) ? msg.getMessageCc() : "";
      InternetAddress[] msgCcAdds = Utils.getInternetAddress(msgCc);
      for (int i = 0; i < msgCcAdds.length; i++) {
        if (msgCcAdds[i] != null
            && !msgCcAdds[i].getAddress().equalsIgnoreCase(account.getEmailAddress())
            && !msgCcAdds[i].getAddress().equalsIgnoreCase(account.getIncomingUser())) {
          if (replyCc.trim().length() > 0)
            replyCc += ", ";
          replyCc += msgCcAdds[i].toString();
        }
      }

      if (replyCc.trim().length() > 0) {
        setFieldCcValue(replyCc);
        setShowCc(true);
      }

      if (msg != null && msg.hasAttachment()) {
        for (Attachment att : msg.getAttachments()) {
          if (att.isLoadedProperly())
            attachments_.add(att);
        }
      }
      if (mailSetting.replyWithAttach())
        originalAttId = addOriginalMessageAsAttach(msg);
      else {
        replyContent = getReplyContent(msg);
      }
      setFieldContentValue(replyContent);

      if (attachments_.size() > 0) {
        for (ActionData actionData : getUploadFileList()) {
          if (actionData.getActionParameter().equals(originalAttId))
            inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                     null,
                                                                     null).setChecked(true));
          else
            inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                     null,
                                                                     null));
        }
        refreshUploadFileList();
      }
      break;
    case MESSAGE_FOWARD:
      String toAddress = msg.getMessageTo() != null ? msg.getMessageTo() : "";
      subject = msg.getSubject();
      if (!subject.toLowerCase().startsWith("fwd:"))
        subject = "Fwd: " + subject;
      setFieldSubjectValue(subject);
      setPriority(msg.getPriority());

      setFieldToValue("");
      if (msg != null && msg.hasAttachment()) {
        for (Attachment att : msg.getAttachments()) {
          if (att.isLoadedProperly())
            attachments_.add(att);
        }
      }

      StringBuffer forwardTxt = new StringBuffer("");
      ;
      if (!mailSetting.forwardWithAtt()) {
        forwardTxt.append("<br><br>-------- Original Message --------<br>");
        forwardTxt.append("Subject: ")
                  .append(MailUtils.encodeHTML(msg.getSubject()))
                  .append("<br>");
        forwardTxt.append("Date: ").append(msg.getSendDate()).append("<br>");
        forwardTxt.append("From: ");

        InternetAddress[] addresses = Utils.getInternetAddress(msg.getFrom());
        for (int i = 0; i < addresses.length; i++) {
          if (i > 0)
            forwardTxt.append(", ");
          if (addresses[i] != null)
            forwardTxt.append(Utils.getPersonal(addresses[i]))
                      .append(" \"")
                      .append(addresses[i].getAddress())
                      .append("\"");
        }
        forwardTxt.append("<br>To: ");

        InternetAddress[] toAddresses = Utils.getInternetAddress(toAddress);
        for (int i = 0; i < toAddresses.length; i++) {
          if (i > 0)
            forwardTxt.append(", ");
          if (toAddresses[i] != null)
            forwardTxt.append(Utils.getPersonal(toAddresses[i]))
                      .append(" \"")
                      .append(toAddresses[i].getAddress())
                      .append("\"");
        }

        forwardTxt.append("<br><br>").append(formatContent(msg));
      } else {
        addOriginalMessageAsAttach(msg);
      }
      refreshUploadFileList();
      for (ActionData actionData : getUploadFileList()) {
        inputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(actionData.getActionParameter(),
                                                                 null,
                                                                 null).setChecked(true));
      }
      setFieldContentValue(forwardTxt.toString());
      break;
    default:
      break;
    }
  }

  private String getReplyContent(Message msg) throws Exception {
    String msgContent = formatContent(msg);
    String content = msgContent;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    String from = "";
    try {
      from = Utils.getAddresses(msg.getFrom())[0];
    } catch (Exception e) {
    }
    if (isVisualEditor) {
      content = "<br><br><div> On "
          + MailUtils.formatDate("MMM dd, yyyy HH:mm aaa", msg.getSendDate(), locale) + ", " + from
          + " wrote: <br>";
      content += "<blockquote style=\"border-left:1px #cccccc solid ; margin-left: 10px; padding-left: 5px;\">"
          + msgContent + "</blockquote></div>";
    } else {
      content = "\n\n On "
          + MailUtils.formatDate("MMM dd, yyyy HH:mm aaa", msg.getSendDate(), locale) + ", " + from
          + " wrote: \n\n";
      content += msgContent;
    }
    return content;
  }

  private String formatContent(Message msg) throws Exception {
    String msgContent = msg.getMessageBody();
    if (isVisualEditor
        && (msg.getContentType() != null && msg.getContentType().indexOf("text/plain") > -1)) {
      msgContent = MailUtils.encodeHTML(msg.getMessageBody()).replaceAll("\n", "<br />");
    }
    return msgContent;
  }

  private String addOriginalMessageAsAttach(Message msg) throws Exception {
    MailService mailSrv = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser();
    BufferAttachment att = new BufferAttachment();
    ByteArrayOutputStream outputStream = (ByteArrayOutputStream) mailSrv.exportMessage(username,
                                                                                       this.accountId_,
                                                                                       msg);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    att.setSize((long) inputStream.available());
    att.setId("Attachment" + IdGenerator.generate());
    att.setName(msg.getSubject() + ".eml");
    att.setInputStream(inputStream);
    att.setMimeType("message/rfc822");
    attachments_.add(att);
    return att.getId();
  }

  public long getPriority() {
    return priority_;
  }

  public void setPriority(long priority) {
    priority_ = priority;
  }

  public String getFieldFromValue() {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    return input.getUIFormSelectBox(FIELD_FROM).getValue();
  }

  public String getFieldSubjectValue() {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    String subject = input.getUIStringInput(FIELD_SUBJECT).getValue();
    if (subject == null)
      subject = "(no subject)";
    return subject;
  }

  public void setFieldSubjectValue(String value) {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    input.getUIStringInput(FIELD_SUBJECT).setValue(value);
  }

  public String getFieldToValue() {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    return input.getUIStringInput(FIELD_TO).getValue();
  }

  public void setFieldToValue(String value) {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    input.getUIStringInput(FIELD_TO).setValue(value);
  }

  public String getFieldCcValue() {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    return input.getUIFormTextAreaInput(FIELD_CC).getValue();
  }

  public void setFieldCcValue(String value) {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    input.getUIFormTextAreaInput(FIELD_CC).setValue(value);
  }

  public String getFieldBccValue() {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    return input.getUIFormTextAreaInput(FIELD_BCC).getValue();
  }

  public void setFieldBccValue(String value) {
    UIComposeInput input = getChildById(FIELD_TO_SET);
    input.getUIFormTextAreaInput(FIELD_BCC).setValue(value);
  }

  public String getFieldAttachmentsValue() {
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    return inputSet.getUIFormInputInfo(FIELD_ATTACHMENTS).getValue();
  }

  public String getFieldContentValue() {
    String content = "";
    if (isVisualEditor) {
      content = getChild(UIFormWYSIWYGInput.class).getValue();
    } else {
      content = getUIFormTextAreaInput(FIELD_MESSAGECONTENT).getValue();
    }
    if (content == null)
      content = "";
    return content;
  }

  public void setFieldContentValue(String value) throws Exception {
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = getApplicationComponent(MailService.class);
    Account account = mailSrv.getAccountById(username, accountId_);
    if (isVisualEditor) {
      if (!MailUtils.isFieldEmpty(account.getSignature()) && !fromDrafts()) {
        value += "<br><br> -- <br >" + account.getSignature().replace("\n", "<br>") + "";
      }
      getChild(UIFormWYSIWYGInput.class).setValue(value);
    } else {
      if (!MailUtils.isFieldEmpty(account.getSignature())) {
        value = MailUtils.html2text(value).replaceAll("\n", "\n > ") + "\n\n -- \n"
            + account.getSignature();
      }
      getUIFormTextAreaInput(FIELD_MESSAGECONTENT).setValue(value);
    }
  }

  public List<Contact> getContacts() throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = MailUtils.getCurrentUser();
    return contactSrv.getPersonalContacts(username);
  }

  public void resetFields() {
    reset();
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private Message getNewMessage() throws Exception {
    Message message = getMessage();
    if (!fromDrafts()) {
      if (message != null)
        parentPath_ = message.getPath();
      message = new Message();
    }
    String usename = MailUtils.getCurrentUser();
    MailService mailSvr = this.getApplicationComponent(MailService.class);
    Account account = mailSvr.getAccountById(usename, this.getFieldFromValue());
    String from = account.getUserDisplayName() + "<" + account.getEmailAddress() + ">";
    String subject = getFieldSubjectValue();

    String to = getFieldToValue();
    if (to != null && to.indexOf(";") > -1)
      to = to.replace(';', ',');
    to = addAllMailFromGroup(to, FIELD_TO_GROUP);
    String cc = getFieldCcValue();
    if (cc != null && cc.indexOf(";") > -1)
      cc = cc.replace(';', ',');
    cc = addAllMailFromGroup(cc, FIELD_CC_GROUP);
    String bcc = getFieldBccValue();
    if (bcc != null && bcc.indexOf(";") > -1)
      bcc = bcc.replace(';', ',');
    bcc = addAllMailFromGroup(bcc, FIELD_BCC_GROUP);

    String body = getFieldContentValue();
    Long priority = getPriority();
    message.setSendDate(new Date());
    message.setAccountId(accountId_);
    message.setFrom(from);
    String contentType = Utils.MIMETYPE_TEXTHTML;
    if (!isVisualEditor()) {
      contentType = Utils.MIMETYPE_TEXTPLAIN;
    }
    message.setContentType(contentType);
    message.setSubject(subject);
    message.setMessageTo(to);
    message.setMessageCc(cc);
    if (message.getReceivedDate() == null) {
      message.setReceivedDate(new Date());
    }
    message.setMessageBcc(bcc);
    message.setHasStar(false);
    message.setPriority(priority);
    message.setIsReturnReceipt(isReturnReceipt);

    List<Attachment> attachments = new ArrayList<Attachment>();
    long attSize = 0;
    for (Attachment att : this.getAttachFileList()) {
      if (getCheckedAttach().contains(att.getId())) {
        attachments.add(att);
        attSize += att.getSize();
      }
    }

    message.setAttachements(attachments);
    message.setMessageBody(body);
    message.setUnread(false);
    message.setSize(body.getBytes().length + attSize);
    if (!Utils.isEmptyField(account.getEmailReplyAddress())) {
      message.setReplyTo(account.getEmailReplyAddress());
    } else {
      message.setReplyTo(from);
    }
    if (getComposeType() == MESSAGE_REPLY || getComposeType() == MESSAGE_REPLY_ALL
        || getComposeType() == MESSAGE_FOWARD) {
      message.setHeader(Utils.HEADER_IN_REPLY_TO, getMessage().getId());
    }
    return message;
  }

  /**
   * @param to : exited value from to, cc, bcc field
   * @param fieldToGroup : groupId to lookup email
   * @return String of all email
   */
  private String addAllMailFromGroup(String to, String fieldToGroup) throws Exception {
    StringBuffer mailAddresses = new StringBuffer();
    try {
      Collection<String> groupIDList = getGroupDataValues(fieldToGroup).values();
      Map<String, String> otherEmailMap = new HashMap<String, String>();
      Map<String, String> sharedEmailMap = new HashMap<String, String>();
      Map<String, String> allEmailMap = new HashMap<String, String>();
      if (groupIDList != null && groupIDList.size() > 0) {
        ContactService contactSrv = getApplicationComponent(ContactService.class);
        for (String groupId : groupIDList) {
          String value = addressBooksMap.get(groupId);
          String label = groupId;
          if (value != null) {
            String[] arr = value.split("_");
            if (arr != null && arr.length > 1) {
              groupId = arr[arr.length - 1];
            }
          }
          if (ALL.equals(groupId)) {
            ContactFilter filter = new ContactFilter();
            allEmailMap.putAll(contactSrv.searchEmails(MailUtils.getCurrentUser(), filter));
          } else if (SHARED.equals(groupId)) {
            ContactFilter filter = new ContactFilter();
            filter.setType(SHARED);
            filter.setSearchSharedContacts(true);
            sharedEmailMap.putAll(contactSrv.searchEmails(MailUtils.getCurrentUser(), filter));
          } else {
            ContactFilter filter = new ContactFilter();
            if (!MailUtils.isFieldEmpty(label)) {
              filter.setCategories(new String[] { label });
            }
            otherEmailMap.putAll(contactSrv.searchEmails(MailUtils.getCurrentUser(), filter));
          }
        }
        Set<String> mailAddressSet = new TreeSet<String>();
        Collection<String> otherEmailCol = otherEmailMap.values();
        if (otherEmailCol != null && otherEmailCol.size() > 0) {
          for (String email : otherEmailCol) {
            mailAddressSet.add(email);
          }
        }

        Collection<String> sharedEmailCol = sharedEmailMap.values();
        if (sharedEmailCol != null && sharedEmailCol.size() > 0) {
          for (String email : sharedEmailCol) {
            mailAddressSet.add(email);
          }
        }
        Collection<String> allEmailCol = allEmailMap.values();
        if (allEmailCol != null && allEmailCol.size() > 0) {
          for (String email : allEmailCol) {
            mailAddressSet.add(email);
          }
        }
        int i = 0;
        int keySetSize = mailAddressSet.size();
        for (String fullnameAndEmail : mailAddressSet) {
          mailAddresses.append(fullnameAndEmail.split("::")[1]);
          if (keySetSize > 1 && i < keySetSize - 1)
            mailAddresses.append(", ");
          i++;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    InternetAddress[] internetAddresses = InternetAddress.parse(mailAddresses.toString());
    StringBuffer mailList = new StringBuffer();
    for (int i = 0; i < internetAddresses.length; i++) {
      mailList.append(internetAddresses[i].getAddress());
      if (internetAddresses.length > 1 && i < internetAddresses.length - 1)
        mailList.append(", ");
    }
    if (to == null)
      return mailList.toString();
    else
      return to + "," + mailList.toString();
  }

  public boolean fromDrafts() {
    return (getMessage() != null
        && getMessage().getFolders()[0].equals(Utils.generateFID(accountId_, Utils.FD_DRAFTS, false)) || getComposeType() == MESSAGE_IN_DRAFT);
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id);
    } catch (Exception e) {
      return id;
    }
  }

  public void setShowCc(boolean showCc_) {
    UIComposeInput uiInput = getChildById(FIELD_TO_SET);
    uiInput.setShowCc(showCc_);
  }

  public boolean isShowCc() {
    UIComposeInput uiInput = getChildById(FIELD_TO_SET);
    return uiInput.isShowCc();
  }

  public void setShowBcc(boolean showBcc_) {
    UIComposeInput uiInput = getChildById(FIELD_TO_SET);
    uiInput.setShowBcc(showBcc_);
  }

  public boolean isShowBcc() {
    UIComposeInput uiInput = getChildById(FIELD_TO_SET);
    return uiInput.isShowBcc();
  }

  static public class SendActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      UIMailPortlet uiPortlet = uiComposeForm.getAncestorOfType(UIMailPortlet.class);
      MailService mailSvr = uiComposeForm.getApplicationComponent(MailService.class);
      String accountId = uiComposeForm.getFieldFromValue();
      String usename = uiPortlet.getCurrentUser();
      Message message = uiComposeForm.getNewMessage();
      Account acctemp = mailSvr.getAccountById(usename, accountId);
      String emailAddr = acctemp.getIncomingUser();
      if (!uiComposeForm.validateMessage(event, message))
        return;
      if (MailUtils.isFieldEmpty(message.getMessageTo())
          && MailUtils.isFieldEmpty(message.getMessageCc())
          && MailUtils.isFieldEmpty(message.getMessageBcc())) {
        UIApplication uiApp = uiComposeForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.select-at-least-recipient", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      UIApplication uiApp = uiComposeForm.getAncestorOfType(UIApplication.class);
      try {
        mailSvr.sendMessage(usename, message);
        ContactService contactService = (ContactService) PortalContainer.getComponent(ContactService.class);
        contactService.saveAddress(usename, message.getMessageTo());
        contactService.saveAddress(usename, message.getMessageCc());
        contactService.saveAddress(usename, message.getMessageBcc());
      } catch (AddressException e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.there-was-an-error-parsing-the-addresses-sending-failed",
                                                null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (AuthenticationFailedException e) {
        Account acc = mailSvr.getAccountById(usename, accountId);
        if (acc.isOutgoingAuthentication() && acc.useIncomingSettingForOutgoingAuthent()) {
          UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class);
          UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
          UIEnterPasswordDialog enterPasswordDialog = uiChildPopup.createUIComponent(UIEnterPasswordDialog.class,
                                                                                     null,
                                                                                     null);
          enterPasswordDialog.setAccountId(accountId);
          enterPasswordDialog.setSendMessage(message);
          uiChildPopup.activate(enterPasswordDialog, 600, 0);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
        } else {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.please-check-configuration-for-smtp-server",
                                                  null));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }
        return;
      } catch (SMTPSendFailedException e) {
        if (e.getMessage().contains("Authentication Required")) {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.check-authentication-smtp-outgoingServer",
                                                  null));
        } else {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.sorry-there-was-an-error-sending-the-message-sending-failed",
                                                  null));
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (MessagingException e) {
        if(Utils.isGmailAccount(emailAddr))
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.cannot-connect-to-mailserver", null));
        else 
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.there-was-an-unexpected-error-sending-falied", null));
        
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      // save to Sent folder and update the number of total messages in
      // Sent
      // folder
      try {
        Account account = mailSvr.getAccountById(usename, accountId);
        if(!uiComposeForm.saveToSentFolder(usename, account, message)){
          uiApp.addMessage(new ApplicationMessage("UIMoveMessageForm.msg.create-massage-not-successful",
                                                  null, ApplicationMessage.INFO)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
        uiMessageList.updateList();
        if (uiMsgPreview != null && uiMsgPreview.getMessage() != null
            && uiMsgPreview.getMessage().getId().equals(message.getId())) {
          uiMsgPreview.setMessage(null);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview.getParent());
        } else {
          event.getRequestContext()
               .addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
        }
        event.getRequestContext()
             .addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class));
        UIPopupAction uiChildPopup = uiComposeForm.getAncestorOfType(UIPopupAction.class);
        uiChildPopup.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
        for (Attachment a : uiComposeForm.getAttachFileList()) {
          UIAttachFileForm.removeUploadTemp(uiComposeForm.getApplicationComponent(UploadService.class),
                                            a.getResoureId());
        }
      } catch (Exception e) {
        logger.warn("Message is sent, but it was not saved in Sent folder", e);
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-sent-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiComposeForm.getAncestorOfType(UIPopupAction.class).deActivate();
      }

    }
  }

  public boolean saveToSentFolder(String usename, Account account, Message message) throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class);
    MailSetting setting = mailSvr.getMailSetting(usename);
    boolean isSaved = setting.saveMessageInSent();
    boolean fromDrafts = fromDrafts();
    boolean saveMsgSuccess = false;
    if (!fromDrafts && isSaved) {
      message.setReplyTo(message.getMessageTo());
      message.setIsReturnReceipt(false);
      message.setIsLoaded(true);
      message.setFolders(new String[] { Utils.generateFID(account.getId(), Utils.FD_SENT, false) });
      saveMsgSuccess = mailSvr.saveMessage(usename, account, parentPath_, message, true);
    } else if (fromDrafts) {
      Folder drafts = mailSvr.getFolder(usename,
                                        account.getId(),
                                        Utils.generateFID(account.getId(), Utils.FD_DRAFTS, false));
      if (isSaved) {
        message.setFolders(new String[] { Utils.generateFID(account.getId(), Utils.FD_SENT, false) });
        saveMsgSuccess = mailSvr.saveMessage(usename, account, parentPath_, message, false);
      } else {
        mailSvr.removeMessage(usename, account.getId(), message);
      }
      drafts.setTotalMessage(drafts.getTotalMessage() - 1);
      mailSvr.saveFolder(usename, account.getId(), drafts);
    }
    return saveMsgSuccess;
  }

  static public class SaveDraftActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm composeForm = event.getSource();
      UIMailPortlet uiPortlet = composeForm.getAncestorOfType(UIMailPortlet.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      MailService mailSvr = composeForm.getApplicationComponent(MailService.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class)
                                  .getSelectedValue();
      String usename = uiPortlet.getCurrentUser();

      UIPopupAction uiChildPopup = composeForm.getAncestorOfType(UIPopupAction.class);
      Message message = composeForm.getNewMessage();
      // verify message
      if (!composeForm.validateMessage(event, message))
        return;

      message.setReplyTo(message.getMessageTo());
      try {
        String draftFolderId = Utils.generateFID(accountId, Utils.FD_DRAFTS, false);
        message.setFolders(new String[] { draftFolderId });
        message.setIsLoaded(true);
        boolean saveMsgSuccess = false;
        if (!composeForm.fromDrafts()) {
          saveMsgSuccess = mailSvr.saveMessage(usename,
                              mailSvr.getAccountById(usename, accountId),
                              composeForm.parentPath_,
                              message,
                              true);
          Folder drafts = mailSvr.getFolder(usename, accountId, draftFolderId);
          drafts.setTotalMessage(drafts.getTotalMessage() + 1);
          mailSvr.saveFolder(usename, accountId, drafts);
        } else {
          saveMsgSuccess = mailSvr.saveMessage(usename,
                              mailSvr.getAccountById(usename, accountId),
                              composeForm.parentPath_,
                              message,
                              false);
        }
        if(!saveMsgSuccess){
          composeForm.getAncestorOfType(UIApplication.class).addMessage(new ApplicationMessage("UIMoveMessageForm.msg.create-massage-not-successful",
                                                                                               null, ApplicationMessage.INFO)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(composeForm.getAncestorOfType(UIApplication.class)) ;
        }
      }
      // CS-4462
      catch (AuthenticationFailedException e) {
        e.printStackTrace();
        UIApplication uiApp = composeForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.the-username-or-password-may-be-wrong-save-draft-error",
                                                null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } catch (Exception e) {
        e.printStackTrace();
        UIApplication uiApp = composeForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-draft-error", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      }
      // update ui
      String selectedFolder = uiFolderContainer.getSelectedFolder();
      if (selectedFolder != null
          && selectedFolder.equals(Utils.generateFID(accountId, Utils.FD_DRAFTS, false))) {
        UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
        uiMsgList.setMessagePageList(mailSvr.getMessagePageList(usename,
                                                                uiMsgList.getMessageFilter()));
        List<Message> showedMsg = uiMsgPreview.getShowedMessages();
        try {
          if (showedMsg != null && showedMsg.size() > 0) {
            for (Message msg : showedMsg) {
              if (message.getId().equals(msg.getId())) {
                int index = showedMsg.indexOf(msg);
                showedMsg.remove(index);
                message = mailSvr.loadTotalMessage(usename,
                                                   accountId,
                                                   mailSvr.getMessageById(usename,
                                                                          accountId,
                                                                          message.getId()));
                showedMsg.add(index, message);
              }
            }
          }
        } catch (Exception e) {
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList.getParent());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      for (Attachment a : composeForm.getAttachFileList()) {
        UIAttachFileForm.removeUploadTemp(composeForm.getApplicationComponent(UploadService.class),
                                          a.getResoureId());
      }

      uiChildPopup.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class DiscardChangeActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      uiForm.resetFields();
      for (Attachment a : uiForm.getAttachFileList()) {
        UIAttachFileForm.removeUploadTemp(uiForm.getApplicationComponent(UploadService.class),
                                          a.getResoureId());
      }
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class AttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      UIPopupActionContainer uiActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
      uiChildPopup.activate(UIAttachFileForm.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class CallDMSSelectorActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();//
      UIPopupActionContainer actionContainer = uiForm.getParent();
      UIPopupAction uiChildPopup = actionContainer.getChildById("UIPopupActionDMSSelector");
      if (uiChildPopup == null) {
        uiChildPopup = actionContainer.addChild(UIPopupAction.class,
                                                null,
                                                "UIPopupActionDMSSelector");
      }
      UIPopupWindow uiPopup = uiChildPopup.getChild(UIPopupWindow.class);
      uiPopup.setId("UIPopupWindowDMSSelector");
      uiPopup.setWindowSize(600, 600);
      UIOneNodePathSelector uiOneNodePathSelector = uiChildPopup.createUIComponent(UIOneNodePathSelector.class,
                                                                                   null,
                                                                                   null);
      uiOneNodePathSelector.setAcceptedNodeTypesInPathPanel(new String[] { org.exoplatform.ecm.webui.utils.Utils.NT_FILE });
      MailService service = (MailService) uiForm.getApplicationComponent(MailService.class);
      String[] info = service.getDMSDataInfo(CalendarUtils.getCurrentUser());
      uiOneNodePathSelector.setRootNodeLocation(info[0], info[1], info[2]);
      uiOneNodePathSelector.setIsDisable(info[1], true);
      uiOneNodePathSelector.setIsShowSystem(false);
      uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider());
      uiPopup.setUIComponent(uiOneNodePathSelector);
      uiOneNodePathSelector.setSourceComponent(uiForm, null);
      uiPopup.setRendered(true);
      uiPopup.setShow(true);
      uiPopup.setResizable(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(actionContainer);
    }
  }

  static public class DownloadActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      attId = MailUtils.decodeMailId(attId);
      for (Attachment attach : uiComposeForm.getAttachFileList()) {
        if (attach.getId().equals(attId)) {
          DownloadResource dresource = new InputStreamDownloadResource(attach.getInputStream(),
                                                                       attach.getMimeType());
          DownloadService dservice = (DownloadService) PortalContainer.getInstance()
                                                                      .getComponentInstanceOfType(DownloadService.class);
          dresource.setDownloadName(attach.getName());
          String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
          event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('"
              + downloadLink + "');");
          break;
        }
      }
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiComposeForm.getChildById(FIELD_TO_SET));
    }
  }

  static public class RemoveAttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      attFileId = MailUtils.decodeMailId(attFileId);
      Iterator<Attachment> iter = uiComposeForm.attachments_.iterator();
      Attachment att;
      while (iter.hasNext()) {
        att = (Attachment) iter.next();
        if (att.getId().equals(attFileId)) {
          UIAttachFileForm.removeUploadTemp(uiComposeForm.getApplicationComponent(UploadService.class),
                                            att.getResoureId());
          iter.remove();
        }
      }
      uiComposeForm.refreshUploadFileList();
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiComposeForm.getChildById(FIELD_TO_SET));
    }
  }

  static public class ToActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650);
      uiAddress.setRecipientsType(FIELD_TO);
      String toAddressString = uiComposeForm.getFieldToValue();
      InternetAddress[] toAddresses = Utils.getInternetAddress(toAddressString);
      List<String> emailList = new ArrayList<String>();
      for (int i = 0; i < toAddresses.length; i++) {
        if (toAddresses[i] != null)
          emailList.add(toAddresses[i].getAddress());
      }

      List<ContactData> toContact = uiComposeForm.getToContacts();
      if (toContact != null && toContact.size() > 0) {
        List<ContactData> contactList = new ArrayList<ContactData>();
        for (ContactData ct : toContact) {
          if (emailList.contains(ct.getEmail()))
            contactList.add(ct);
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }
      uiAddress.setAvaiAddressStr(toAddressString);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class ToCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650);

      uiAddress.setRecipientsType(FIELD_CC);
      String ccAddressString = uiComposeForm.getFieldCcValue();
      InternetAddress[] ccAddresses = Utils.getInternetAddress(ccAddressString);
      List<String> emailList = new ArrayList<String>();
      for (int i = 0; i < ccAddresses.length; i++) {
        if (ccAddresses[i] != null)
          emailList.add(ccAddresses[i].getAddress());
      }
      List<ContactData> ccContact = uiComposeForm.getCcContacts();
      if (ccContact != null && ccContact.size() > 0) {
        List<ContactData> contactList = new ArrayList<ContactData>();
        for (ContactData ct : ccContact) {
          if (emailList.contains(ct.getEmail()))
            contactList.add(ct);
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }

      uiAddress.setAvaiAddressStr(ccAddressString);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class ToBCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650);

      uiAddress.setRecipientsType(FIELD_BCC);
      String bccAddressString = uiComposeForm.getFieldBccValue();
      InternetAddress[] bccAddresses = Utils.getInternetAddress(bccAddressString);
      List<String> emailList = new ArrayList<String>();
      for (int i = 0; i < bccAddresses.length; i++) {
        if (bccAddresses[i] != null)
          emailList.add(bccAddresses[i].getAddress());
      }
      List<ContactData> bccContact = uiComposeForm.getBccContacts();
      if (bccContact != null && bccContact.size() > 0) {
        List<ContactData> contactList = new ArrayList<ContactData>();
        for (ContactData ct : bccContact) {
          if (emailList.contains(ct.getEmail()))
            contactList.add(ct);
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }

      uiAddress.setAvaiAddressStr(bccAddressString);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class ChangePriorityActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      String priority = event.getRequestContext().getRequestParameter(OBJECTID);
      uiForm.setPriority(Long.valueOf(priority));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class UseVisualEdiorActionListener extends EventListener<UIComposeForm> {
    @SuppressWarnings("deprecation")
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      boolean isVisualEditor = Boolean.valueOf(event.getRequestContext()
                                                    .getRequestParameter(OBJECTID));
      String content = "";
      if (isVisualEditor) {
        try {
          content = uiForm.getUIFormTextAreaInput(FIELD_MESSAGECONTENT).getValue();
          uiForm.removeChildById(FIELD_MESSAGECONTENT);
          UIFormWYSIWYGInput wysiwyg = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT,
                                                              null,
                                                              null,
                                                              true);
          uiForm.addUIFormInput(wysiwyg);
          wysiwyg.setValue(MailUtils.text2html(content));
          uiForm.setVisualEditor(true);
        } catch (Exception e) {
        }
      } else {
        try {
          content = uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
          uiForm.removeChild(UIFormWYSIWYGInput.class);
          UIFormTextAreaInput textArea = new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null);
          textArea.setValue(MailUtils.html2text(content));
          uiForm.addUIFormInput(textArea);
          uiForm.setVisualEditor(false);
        } catch (Exception e) {
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class ShowCcActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      uiForm.setShowCc(!uiForm.isShowCc());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(FIELD_TO_SET));
      event.getRequestContext()
           .getJavascriptManager()
           .addCustomizedOnLoadScript("eXo.mail.AutoComplete.init(['" + FIELD_TO + "', '"
               + FIELD_CC + "', '" + FIELD_BCC + "']);");
    }
  }

  static public class ShowBccActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      uiForm.setShowBcc(!uiForm.isShowBcc());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getChildById(FIELD_TO_SET));
      event.getRequestContext()
           .getJavascriptManager()
           .addCustomizedOnLoadScript("eXo.mail.AutoComplete.init(['" + FIELD_TO + "', '"
               + FIELD_CC + "', '" + FIELD_BCC + "']);");
    }
  }

  static public class ReturnReceiptActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource();
      uiForm.isReturnReceipt = !uiForm.isReturnReceipt;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  private boolean validateMessage(Event<UIComposeForm> event, Message msg) throws Exception {
    String msgWarning = null;
    if (!MailUtils.isValidEmailAddresses(msg.getMessageTo())) {
      msgWarning = "UIComposeForm.msg.invalid-to-field";
    } else if (!MailUtils.isValidEmailAddresses(msg.getMessageCc())) {
      msgWarning = "UIComposeForm.msg.invalid-cc-field";
    } else if (!MailUtils.isValidEmailAddresses(msg.getMessageBcc())) {
      msgWarning = "UIComposeForm.msg.invalid-bcc-field";
    }

    if (msgWarning != null) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      uiApp.addMessage(new ApplicationMessage(msgWarning, null));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      return false;
    }

    return true;
  }

  public void addAddressBooksMap(String key, String value) {
    if (addressBooksMap == null)
      addressBooksMap = new HashMap<String, String>();
    addressBooksMap.put(key, value);
  }

  public Map<String, String> getAddressBooksMap() {
    return addressBooksMap;
  }

  public void addGroupDataValue(String key, String value1, String value2) {
    if (groupData == null)
      groupData = new LinkedHashMap<String, HashMap<String, String>>();
    if (groupData.get(key) == null)
      groupData.put(key, new HashMap<String, String>());
    groupData.get(key).put(value1, value2);
  }

  /**
   * @param key : field add the group
   * @return Map data of group with group id and group name
   */
  public HashMap<String, String> getGroupDataValues(String key) {
    if (groupData == null)
      return new HashMap<String, String>();
    return groupData.get(key);
  }

  public void setGroupData(Map<String, HashMap<String, String>> groupData) {
    this.groupData = groupData;
  }

  public Map<String, String> getAddressBooks() {
    return addressBooksMap;
  }

  public void setAddressBooks(Map<String, String> addressBook) {
    this.addressBooksMap = addressBook;
  }

  public void addAddressBooksValues(String key, String value) {
    if (addressBooksMap == null)
      addressBooksMap = new HashMap<String, String>();
    addressBooksMap.put(key, value);
  }

  public Map<String, HashMap<String, String>> getGroupData() {
    return groupData;
  }

  /**
   * @param key : the field add group
   * @return List of action data has been made
   */
  public List<ActionData> getGroupActionData(String key) {
    List<ActionData> groupAction = new ArrayList<ActionData>();
    for (String groupId : getGroupDataValues(key).keySet()) {
      ActionData removeAction = new ActionData();
      removeAction.setActionListener("RemoveGroup");
      removeAction.setActionName(groupId);
      removeAction.setActionParameter(key + MailUtils.SEMICOLON + groupId);
      removeAction.setCssIconClass("RemoveBtn");
      removeAction.setActionType(ActionData.TYPE_ICON);
      removeAction.setBreakLine(false);
      removeAction.setShowLabel(true);
      groupAction.add(removeAction);
    }
    return groupAction;
  }

  /**
   * update ui ;
   */
  public void refreshGroupFileList(String key) throws Exception {
    UIComposeInput inputSet = getChildById(FIELD_TO_SET);
    inputSet.setActionField(key, getGroupActionData(key));

  }

  static public class RemoveGroupActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      String keyAndId = event.getRequestContext().getRequestParameter(OBJECTID);

      try {
        uiComposeForm.getGroupDataValues(keyAndId.split(MailUtils.SEMICOLON)[0])
                     .remove(keyAndId.split(MailUtils.SEMICOLON)[1]);
        uiComposeForm.refreshGroupFileList(keyAndId.split(MailUtils.SEMICOLON)[0]);
        event.getRequestContext()
             .addUIComponentToUpdateByAjax(uiComposeForm.getChildById(FIELD_TO_SET));

      } catch (Exception e) {
        e.printStackTrace();
        return;
      }

    }
  }

  @Override
  public void doSelect(String selectField, Object value) throws Exception {
    // UIApplication application = getAncestorOfType(UIApplication.class);
    // application.getUIPopupMessages().addMessage(new
    // ApplicationMessage(selectField + value + "", null));

    String valueString = value.toString();
    String relPath = valueString.substring(valueString.indexOf(":/") + 2);

    MailService service = (MailService) this.getApplicationComponent(MailService.class);
    UIApplication uiApp = this.getAncestorOfType(UIApplication.class);
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ApplicationMessage message = null;
    BufferAttachment attachFile = null;
    try {
      attachFile = service.getAttachmentFromDMS(CalendarUtils.getCurrentUser(), relPath);
    } catch (Exception e) {
      message = new ApplicationMessage("UIComposeForm.msg.DMSSelector.load-error",
                                       null,
                                       ApplicationMessage.ERROR);
    }
    if (attachFile == null) {
      message = new ApplicationMessage("UIComposeForm.msg.DMSSelector.selected-path",
                                       null,
                                       ApplicationMessage.WARNING);
    } else {
      this.addToUploadFileList(attachFile);
      this.refreshUploadFileList();

      context.addUIComponentToUpdateByAjax(this);
    }
    if (message != null) {
      uiApp.addMessage(message);
      context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
    // System.out.println(fileNode.getPrimaryNodeType().getName());
    // System.out.println(node.getPath());

  }

  public String getTitle(Node node) throws Exception {
    String title = null;
    if (node.hasNode("jcr:content")) {
      Node content = node.getNode("jcr:content");
      if (content.hasProperty("dc:title")) {
        try {
          title = content.getProperty("dc:title").getValues()[0].getString();
        } catch (Exception ex) {
        }
      }
    } else if (node.hasProperty("exo:title")) {
      title = node.getProperty("exo:title").getValue().getString();
    }
    if ((title == null) || ((title != null) && (title.trim().length() == 0))) {
      title = node.getName();
    }
    return Text.unescapeIllegalJcrChars(title);
  }

}
