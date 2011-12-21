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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UITagContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.ext.UIFormColorPicker;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/mail/webui/UITagForm.gtmpl", events = {
    @EventConfig(listeners = UITagForm.AddActionListener.class),
    @EventConfig(listeners = UITagForm.RemoveActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE) })
public class UITagForm extends UIForm implements UIPopupComponent {

  public static final String   SELECT_AVAIABLE_TAG = "tag-name";

  public static final String   TAG_COLOR           = "choose-color";

  public static final String   TAG_MESSAGE         = "TagMessage";

  private Map<String, Message> messageMap          = new HashMap<String, Message>();

  private Map<String, Tag>     tagMap              = new HashMap<String, Tag>();

  private List<String>         checkedTagList      = null;

  public UITagForm() {
  }

  public void setTagList(List<Tag> tagList) throws Exception {
    tagMap.clear();
    addUIFormInput(new UIFormStringInput(SELECT_AVAIABLE_TAG, SELECT_AVAIABLE_TAG, null).addValidator(SpecialCharacterValidator.class));
    addUIFormInput(new UIFormColorPicker(TAG_COLOR, TAG_COLOR));
    for (Tag tag : tagList) {
      UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(tag.getName(),
                                                                                 tag.getName(),
                                                                                 null);
      if(checkedTagList != null && checkedTagList.size() > 0 &&  checkedTagList.contains(tag.getName()))
        uiCheckBox.setChecked(true);
      addUIFormInput(uiCheckBox);
      tagMap.put(tag.getName(), tag);
    }
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id);
    } catch (Exception e) {
      return id;
    }
  }

  public String[] getActions() {
    return new String[] { "Add", "Remove", "Cancel" };
  }

  public List<Tag> getTagList() {
    return new ArrayList<Tag>(tagMap.values());
  }

  public void setMessageList(List<Message> messageList) throws Exception {
    messageMap.clear();
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    String username = MailUtils.getCurrentUser();
    String accountId = dataCache.getSelectedAccountId();
    MailService mailSrv = getApplicationComponent(MailService.class);
    
    for (Message msg : messageList) {
      String subject = (msg.getSubject() != null) ? msg.getSubject() : "";
      String mesSub = getLabel("subject") + " : " + ((subject.length() >= 30) ? (subject.substring(0, 30) + "...") : subject);
      UIFormInputInfo uiTags = new UIFormInputInfo(TAG_MESSAGE, TAG_MESSAGE, null);
      StringBuffer tags = new StringBuffer();
      if (msg.getTags() != null && msg.getTags().length > 0) {
        checkedTagList = new ArrayList<String>();
        for (int i = 0; i < msg.getTags().length; i++) {
          if (i > 0) {
            tags.append(", ");
          }
          Tag tag = mailSrv.getTag(username, accountId, msg.getTags()[i]);
          tags.append("[").append(tag.getName()).append("]");
          checkedTagList.add(tag.getName());
        }
      } else {
        tags.append(getLabel("no-tag"));
      }

      uiTags.setName(mesSub);
      uiTags.setValue(tags.toString());
      addUIFormInput(uiTags);
      messageMap.put(msg.getId(), msg);
    }
  }

  public List<Message> getMessageList() {
    return new ArrayList<Message>(messageMap.values());
  }

  public List<Tag> getCheckedTags() throws Exception {
    List<Tag> tagList = new ArrayList<Tag>();
    for (Tag tag : getTagList()) {
      UIFormCheckBoxInput<Boolean> checkbox = getChildById(tag.getName());
      if (checkbox != null && checkbox.isChecked()) {
        tagList.add(tag);
      }
    }
    return tagList;
  }

  public String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue();
  }

  public void setSelectedColor(String value) {
    getChild(UIFormColorPicker.class).setValue(value);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class AddActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource();
      String newTagName = uiTagForm.getUIStringInput(SELECT_AVAIABLE_TAG).getValue();
      // CS-3009
      newTagName = MailUtils.reduceSpace(newTagName);
      String tagColor = uiTagForm.getSelectedColor();
      UIMailPortlet uiPortlet = uiTagForm.getAncestorOfType(UIMailPortlet.class);
      UITagContainer uiTagContainer = uiPortlet.findFirstComponentOfType(UITagContainer.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class)
                                  .getSelectedValue();
      MailService mailSrv = uiTagForm.getApplicationComponent(MailService.class);
      List<Tag> tagList = new ArrayList<Tag>();

      if (newTagName != null && newTagName.trim().length() > 0) {
        boolean isExist = false;
        newTagName = newTagName.trim();
        for (Tag tag : mailSrv.getTags(username, accountId)) {
          if (tag.getName().equals(newTagName)) {
            isExist = true;
            tagList.add(tag);
          }
        }
        if (!isExist) {
          Tag newTag = new Tag();
          newTag.setName(newTagName);
          newTag.setColor(tagColor);
          newTag.setDescription("Tag's description");
          tagList.add(newTag);
        } else {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UITagForm.msg.tag-already-exists",
                                                                                         null,
                                                                                         ApplicationMessage.INFO));
          return;
        }
      }

      tagList.addAll(uiTagForm.getCheckedTags());
      if (tagList.size() <= 0) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UITagForm.msg.have-to-choose-at-least-a-tag", null, ApplicationMessage.INFO));
        return;
      }
      mailSrv.addTag(username, accountId, uiTagForm.getMessageList(), tagList);

      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      List<String> tagIdList = new ArrayList<String>();
      for (Tag tag : tagList)
        tagIdList.add(tag.getId());
      for (Message msg : uiTagForm.getMessageList()) {
        if (uiMessageList.messageList_.containsKey(msg.getId())) {
          if (msg.getTags() != null && msg.getTags().length > 0) {
            for (int i = 0; i < msg.getTags().length; i++) {
              if (!tagIdList.contains(msg.getTags()[i]))
                tagIdList.add(msg.getTags()[i]);
            }
          }
          msg.setTags(tagIdList.toArray(new String[] {}));
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      uiPortlet.cancelAction();
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTagContainer);
    }
  }

  static public class RemoveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource();
      UIMailPortlet uiPortlet = uiTagForm.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class)
                                  .getSelectedValue();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class);

      List<String> tagList = new ArrayList<String>();
      for (Tag tag : uiTagForm.getCheckedTags())
        tagList.add(tag.getId());

      if (tagList.size() <= 0) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UITagForm.msg.have-to-choose-at-least-a-tag", null, ApplicationMessage.INFO));        
        return;
      }

      mailSrv.removeTagsInMessages(username, accountId, uiTagForm.getMessageList(), tagList);
      for (Message msg : uiTagForm.getMessageList()) {
        List<String> newTags = new ArrayList<String>();
        if (msg.getTags() != null && msg.getTags().length > 0) {
          for (int i = 0; i < msg.getTags().length; i++) {
            if (!tagList.contains(msg.getTags()[i]))
              newTags.add(msg.getTags()[i]);
          }
        }
        msg.setTags(newTags.toArray(new String[] {}));

        String selectedTagId = uiMsgList.getSelectedTagId();
        if (selectedTagId == null || newTags.contains(selectedTagId)) {
          uiMsgList.messageList_.put(msg.getId(), msg);
        } else {
          uiMsgList.messageList_.remove(msg.getId());
        }
      }
      if (uiMsgList.messageList_.size() % uiMsgList.getMessagePageList().getAvailablePage() == 0) {
        uiMsgList.updateList();
      }
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiMsgList.getAncestorOfType(UIMessageArea.class));
      uiPortlet.cancelAction();
    }
  }

  static public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource();
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      uiPortlet.cancelAction();
    }
  }
}
