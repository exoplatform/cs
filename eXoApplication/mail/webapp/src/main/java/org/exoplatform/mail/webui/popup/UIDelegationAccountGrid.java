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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.mail.service.AccountDelegation;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 28, 2011  
 */
@ComponentConfig(
                 template = "app:/templates/mail/webui/popup/UIDelegationAccountGrid.gtmpl",
                 events = {
                     @EventConfig(listeners = UIDelegationAccountGrid.RemoveActionListener.class, confirm = "UIDelegationAccountGrid.grid.msg.confirm-delete")
                 }
             )
public class UIDelegationAccountGrid extends UIGrid {
  public static final String FULL_PRIVILEGE_FIELD = "fullpri" ;  
  public static final String READONLY_PRIVILEGE_FIELD = "readonlypri" ;
  String fields[] = {"username","delegatedUser",UIDelegationAccountGrid.FULL_PRIVILEGE_FIELD,UIDelegationAccountGrid.READONLY_PRIVILEGE_FIELD};
  String actions[] = {"Remove"};
  public UIDelegationAccountGrid() throws Exception {
    configure("delegateAccount",fields, actions);
    updateGrid();
  }

  public void updateGrid(){
    List<AccountDelegation> delegation = new ArrayList<AccountDelegation>();
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    //test here
    delegation.add(new AccountDelegation("delegation" + IdGenerator.generate(), "root", "demo0", true, false));
    delegation.add(new AccountDelegation("delegation" + IdGenerator.generate(), "root", "demo1", false, true));
    delegation.add(new AccountDelegation("delegation" + IdGenerator.generate(), "root", "demo2", true, false));
    //
    
    LazyPageList<AccountDelegation> pageList = new LazyPageList<AccountDelegation>(
        new ListAccessImpl<AccountDelegation>(AccountDelegation.class, delegation), 1);
    getUIPageIterator().setPageList(pageList) ;
  }
  
  static  public class RemoveActionListener extends EventListener<UIDelegationAccountGrid> {
    public void execute(Event<UIDelegationAccountGrid> event) throws Exception {
      UIDelegationAccountGrid uiDelegate = event.getSource();
      UIMailPortlet uiPortlet = uiDelegate.getAncestorOfType(UIMailPortlet.class) ;
      UIApplication uiApp = uiDelegate.getAncestorOfType(UIApplication.class) ;
      System.out.println(event.getRequestContext().getRequestParameter(OBJECTID));
      uiApp.addMessage(new ApplicationMessage("UIDelegationAccountGrid.msg.remove-delegateion-fail", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }   
}
