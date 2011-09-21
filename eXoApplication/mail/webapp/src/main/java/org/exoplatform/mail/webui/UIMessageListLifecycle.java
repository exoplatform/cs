/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.portlet.PortletRequest;

import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jul 18, 2011  
 */
public class UIMessageListLifecycle extends UIFormLifecycle {

  @Override
  public void processDecode(UIForm uicomponent, WebuiRequestContext context) throws Exception {
    uicomponent.setSubmitAction(null);

    processNormalRequest(uicomponent, context);

    List<UIComponent> children = uicomponent.getChildren();
    for (UIComponent uiChild : children) {
      uiChild.processDecode(context);
    }
    String action = uicomponent.getSubmitAction();
    String subComponentId = context.getRequestParameter(UIForm.SUBCOMPONENT_ID);
    if (subComponentId == null || subComponentId.trim().length() < 1) {
      Event<UIComponent> event = uicomponent.createEvent(action, Event.Phase.DECODE, context);
      if (event != null) {
        event.broadcast();
      }
      return;
    }
    UIComponent uiSubComponent = uicomponent.findComponentById(subComponentId);
    Event<UIComponent> event = uiSubComponent.createEvent(action, Event.Phase.DECODE, context);
    if (event == null) {
      event = uicomponent.createEvent(action, Event.Phase.DECODE, context);
    }
    if (event != null) {
      event.broadcast();
    }
  }

  private void processNormalRequest(UIForm uiForm, WebuiRequestContext context) throws Exception {
    uiForm.setSubmitAction(context.getRequestParameter(UIForm.ACTION));
    PortletRequest request = context.getRequest();
    Iterator<Entry<String, String[]>> paramsIter = request.getParameterMap().entrySet().iterator();
    while (paramsIter.hasNext()) {
      Entry<String, String[]> entry = paramsIter.next();
      String paramName = entry.getKey();
      String[] paramValue = entry.getValue();
      
      if (paramName.contains("@")) {
        String msgId = Utils.decodeMailId(paramName);
        UIFormCheckBoxInput<Boolean> uiCheckBox = uiForm.getChildById(paramName);
        if (uiCheckBox == null) {
          MailService mailSrv = MailUtils.getMailService();
          String username = MailUtils.getCurrentUser();
          DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
          String accountId = dataCache.getSelectedAccountId();
          Message message = mailSrv.getMessageById(username, accountId, msgId);
          if (message != null) {
            UIMessageList uiMsgList = UIMessageList.class.cast(uiForm);
            boolean value = Boolean.valueOf(paramValue[0]);
            uiForm.addUIFormInput(new UIFormCheckBoxInput<Boolean>(paramName, paramName, false).setChecked(value));
            uiMsgList.messageList_.put(msgId, message);
          }
        }
      }
    }
    
    List<UIFormInputBase> inputs = new ArrayList<UIFormInputBase>();
    uiForm.findComponentOfType(inputs, UIFormInputBase.class);
    for (UIFormInputBase input : inputs) {
      if (!input.isValid()) {
        continue;
      }
      String inputValue = context.getRequestParameter(input.getId());
      if (inputValue == null || inputValue.trim().length() == 0) {
        inputValue = context.getRequestParameter(input.getName());
      }
      input.decode(inputValue, context);
    }
  }
}
