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
package org.exoplatform.contact.service.impl;

import org.exoplatform.contact.service.ContactService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Nov 23, 2007 3:09:21 PM
 */
public class NewMembershipListener extends MembershipEventListener {
  private ContactService cservice_ ;
  public NewMembershipListener(ContactService cservice) throws Exception {
  	cservice_ = cservice ;
  }
  
  public void postSave(Membership m, boolean isNew) throws Exception {
  	cservice_.addGroupToPersonalContact(m.getUserName(), m.getGroupId()) ;
  }
  
  public void preDelete(Membership m) throws Exception {
    
  }
}