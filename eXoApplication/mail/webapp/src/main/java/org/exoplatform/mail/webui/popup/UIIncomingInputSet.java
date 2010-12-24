/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.mail.webui.UIFormInputWithActions;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          hoang.nguyen@exoplatform.com
 * Dec 15, 2010  
 */

@ComponentConfig(
                 template = "app:/templates/mail/webui/popup/UIIncomingInputSet.gtmpl"
               )
               
public class UIIncomingInputSet extends UIFormInputWithActions {
  
  public final static String CHECK_SUPPORTED_ACT = "CheckSupported";
  
  public UIIncomingInputSet(String id) {
    super(id);
    setComponentConfig(getClass(), null);
  }
  
  public String getUsername(){
    return this.getUIStringInput(UIAccountSetting.FIELD_INCOMING_ACCOUNT).getValue();
  }
  
  public String getProtocol(){
    return this.getUIFormSelectBox(UIAccountSetting.FIELD_SERVER_TYPE).getValue();
  }
  
  public String getHost(){
    return this.getUIStringInput(UIAccountSetting.FIELD_INCOMING_SERVER).getValue();
  }
    
}
