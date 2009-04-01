/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam 
 *          phunghainam@gmail.com
 * Feb 25, 2009  
 */
public class AuthenticationLogoutListener extends Listener<ConversationRegistry, ConversationState> {

  public AuthenticationLogoutListener() throws Exception { }

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
   ExoContainer container = ExoContainerContext.getCurrentContainer();
     if (container instanceof RootContainer) {
       container = RootContainer.getInstance().getPortalContainer("portal");
     }
    MailService mService = (MailService)container.getComponentInstanceOfType(MailService.class) ;
    String username = event.getData().getIdentity().getUserId();
    List<Account> accList = mService.getAccounts(SessionProvider.createSystemProvider(), username);
    System.out.println("\n\n goes here when log out");
    for (Account acc : accList) {
      mService.stopCheckMail(username, acc.getId());
    }    
  } 
 }