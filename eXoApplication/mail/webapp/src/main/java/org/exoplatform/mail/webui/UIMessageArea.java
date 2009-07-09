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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.webui.UIFetchingBar ;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UIMessageArea.gtmpl"
)
public class UIMessageArea extends UIContainer  {
  
  private MailSetting mailSetting_;
  
  public UIMessageArea() throws Exception {}
  
  public void init(String accId) throws Exception {
	  addChild(UIFetchingBar.class, null, null);
    UIMessageList uiMessageList = createUIComponent(UIMessageList.class, null, null);
    uiMessageList.init(accId);
    addChild(uiMessageList);
    UIMessagePreview messagePreview = addChild(UIMessagePreview.class, null, null);
    messagePreview.setRendered(false) ;
  }
  
  public void reloadMailSetting() throws Exception {
    MailService mailSrv = getApplicationComponent(MailService.class) ;
    mailSetting_ = mailSrv.getMailSetting(MailUtils.getCurrentUser()) ;    
  }
  
  public UIMessageList getUIMessageList() { return findFirstComponentOfType(UIMessageList.class); }
  
  public UIMessagePreview getUIMessagePreview() { return findFirstComponentOfType(UIMessagePreview.class); }
  
  public MailSetting getMailSetting() { return mailSetting_; }
  public void setMailSetting(MailSetting ms) { mailSetting_ = ms; } 
  
  public boolean isVerticalLayout() { 
    return (mailSetting_.getLayout() == MailSetting.VERTICAL_LAYOUT); 
  }
  
  public boolean isHorizontalLayout() {
    return (mailSetting_.getLayout() == MailSetting.HORIZONTAL_LAYOUT);
  }
  
  public boolean isNoSplitLayout() {
    return (mailSetting_.getLayout() == MailSetting.NO_SPLIT_LAYOUT);
  }
}
