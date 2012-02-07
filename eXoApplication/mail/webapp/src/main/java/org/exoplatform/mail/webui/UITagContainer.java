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
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.action.FullDelegationEventListener;
import org.exoplatform.mail.webui.popup.UIEditTagForm;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/mail/webui/UITagContainer.gtmpl", events = {
    @EventConfig(listeners = UITagContainer.ChangeTagActionListener.class),
    @EventConfig(listeners = UITagContainer.AddTagActionListener.class),
    @EventConfig(listeners = UITagContainer.EditTagActionListener.class),
    @EventConfig(listeners = UITagContainer.RemoveTagActionListener.class, confirm = "UITagContainer.msg.confirm-remove-tag"),
    @EventConfig(listeners = UITagContainer.EmptyTagActionListener.class),
    @EventConfig(listeners = UITagContainer.ChangeColorActionListener.class) })
public class UITagContainer extends UIForm {
  public static final String   OLIVE         = "Olive".intern();

  public static final String   OLIVEDRAB     = "OliveDrab".intern();

  public static final String   ORANGERED     = "OrangeRed".intern();

  public static final String   ORCHID        = "Orchid".intern();

  public static final String   PALEGOLDENROD = "PaleGoldenRod".intern();

  public static final String   PALEGREEN     = "PaleGreen".intern();

  public static final String   PALETURQUOISE = "PaleTurquoise".intern();

  public static final String   PALEVIOLETRED = "PaleVioletRed".intern();

  public static final String   PAPAYAWHIP    = "PapayaWhip".intern();

  public static final String   PEACHPUFF     = "PeachPuff".intern();

  public static final String   PERU          = "Peru".intern();

  public static final String   PINK          = "Pink".intern();

  public static final String   PLUM          = "Plum".intern();

  public static final String   POWDERBLUE    = "PowderBlue".intern();

  public static final String   PURPLE        = "Purple".intern();

  public static final String   RED           = "Red".intern();

  public static final String   ROSYBROWN     = "RosyBrown".intern();

  public static final String   ROYALBLUE     = "RoyalBlue".intern();

  public static final String   SADDLEBROWN   = "SaddleBrown".intern();

  public static final String   SALMON        = "Salmon".intern();

  public static final String   SANDYBROWN    = "SandyBrown".intern();

  public static final String   SEAGREEN      = "SeaGreen".intern();

  public static final String   SEASHELL      = "SeaShell".intern();

  public static final String   SIANNA        = "Sienna".intern();

  public static final String   SILVER        = "Silver".intern();

  public static final String   SKYBLUE       = "SkyBlue".intern();

  public static final String   THISTLE       = "Thistle".intern();

  public static final String   TOMATO        = "Tomato".intern();

  public static final String   TURQUOISE     = "Turquoise".intern();

  public static final String   VIOLET        = "Violet".intern();

  public static final String   WHEAT         = "Wheat".intern();

  public static final String   YELLOW        = "Yellow".intern();

  public static final String[] COLORS        = { POWDERBLUE, ORCHID, PALEGOLDENROD, PALEGREEN,
      OLIVE, OLIVEDRAB, ORANGERED, PALETURQUOISE, PALEVIOLETRED, PAPAYAWHIP, PEACHPUFF, PERU, PINK,
      PLUM, PURPLE, RED, ROSYBROWN, ROYALBLUE, SADDLEBROWN, SALMON, SANDYBROWN, SEAGREEN, SEASHELL,
      SIANNA, SILVER, SKYBLUE, THISTLE, TOMATO, TURQUOISE, VIOLET, WHEAT, YELLOW };

  private String               selectedTagId_;
  
  private Log                  log           = ExoLogger.getLogger(UITagContainer.class);

  public UITagContainer() throws Exception {
  }

  public String getSelectedTagId() {
    return selectedTagId_;
  }

  public void setSelectedTagId(String selectedTagId) {
    selectedTagId_ = selectedTagId;
  }

  public List<Tag> getTags() {
    List<Tag> tagList = new ArrayList<Tag>();
    MailService mailService = MailUtils.getMailService();
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser();
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    try {
      if (!Utils.isEmptyField(accountId)) {
        tagList = mailService.getTags(username, accountId);
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to get tag list of account %s belongs to user %s", accountId, username), e);
      }
    }
    return tagList;
  }

  public String[] getColors() {
    return Calendar.COLORS;
  }

  public String[] getActions() {
    return new String[] { "AddTag" };
  }

  static public class ChangeTagActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTags = event.getSource();
      UIMailPortlet uiPortlet = uiTags.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String accountId = dataCache.getSelectedAccountId();
      String username = MailUtils.getDelegateFrom(accountId, dataCache);
      
      uiMessageList.setMessagePageList(mailSrv.getMessagePagelistByTag(username, accountId, tagId));
      MessageFilter filter = new MessageFilter("Tag");
      filter.setTag(new String[] { tagId });
      filter.setAccountId(accountId);
      uiMessageList.setMessageFilter(filter);
      uiMessageList.setSelectedTagId(tagId);
      uiMessageList.setSelectedFolderId(null);
      uiMessageList.viewing_ = UIMessageList.VIEW_ALL;
      uiMessageList.viewMode = UIMessageList.MODE_LIST;
      uiMessagePreview.setMessage(null);
      UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      uiFolder.setSelectedFolder(null);
      uiTags.setSelectedTagId(tagId);
     
      UISearchForm uiSearchForm = uiPortlet.findFirstComponentOfType(UISearchForm.class);
      if (!MailUtils.isFieldEmpty(uiSearchForm.getTextSearch())) {
        uiSearchForm.setTextSearch("");
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTags.getParent());
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
      }

      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder);
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiMessagePreview.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class AddTagActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTag = event.getSource();
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UIEditTagForm uiTagForm = uiTag.createUIComponent(UIEditTagForm.class, null, null);
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class EditTagActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTag = event.getSource();     
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPopupAction uiPopup = uiTag.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class);      
      UIEditTagForm uiRenameTagForm = uiPopup.activate(UIEditTagForm.class, 450);
      uiRenameTagForm.setTag(tagId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }

  static public class RemoveTagActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTag = event.getSource();
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      uiMessagePreview.setMessage(null);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String accountId = dataCache.getSelectedAccountId();
      String username = MailUtils.getDelegateFrom(accountId, dataCache);

      mailSrv.removeTag(username, accountId, tagId);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
    }
  }

  static public class EmptyTagActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTag = event.getSource();
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      uiMessagePreview.setMessage(null);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = dataCache.getSelectedAccountId();
      List<Message> listMessage = mailSrv.getMessageByTag(username, accountId, tagId);
      List<String> listTag = new ArrayList<String>();
      listTag.add(tagId);
      mailSrv.removeTagsInMessages(username, accountId, listMessage, listTag);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class ChangeColorActionListener extends FullDelegationEventListener<UITagContainer> {
    @Override
    public void processEvent(Event<UITagContainer> event) throws Exception {
      UITagContainer uiTag = event.getSource();
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      String color = event.getRequestContext().getRequestParameter("color");
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String accountId = dataCache.getSelectedAccountId();
      String username = MailUtils.getDelegateFrom(accountId, dataCache);
      
      Tag tag = mailSrv.getTag(username, accountId, tagId);
      tag.setColor(color);
      mailSrv.updateTag(username, accountId, tag);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }
}
