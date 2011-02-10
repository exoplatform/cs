/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.mail.service.AccountDelegation;
import org.exoplatform.mail.webui.UIFormInputWithActions;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SAS
 * Author : nguyen van hoang
 *          hoang.nguyen@exoplatform.com
 * Jan 20, 2011  
 */

@ComponentConfig(
                 template = "app:/templates/mail/webui/popup/UIDelegationInputSet.gtmpl"

)

public class UIDelegationInputSet extends UIFormInputWithActions {

  private List<AccountDelegation> ad;

  public UIDelegationInputSet(String id){
    super(id);
    setComponentConfig(getClass(), null) ;
    try {
      addChild(UIDelegationAccountGrid.class, null, null).setRendered(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void init() throws Exception{
    addChild(UIDelegationAccountGrid.class, null, null);
  }

  public List<AccountDelegation> getAccountsDelegation(){
    return ad;
  }

  public void setAccountsDelegation(List<AccountDelegation> ad){
    this.ad = ad;
  }



  @Override
  public String event(String name) throws Exception {
    return ((UIComponent)getParent()).event(name) ;
  }

}
