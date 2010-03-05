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
package org.exoplatform.services.organization.rest;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 5, 2010  
 */
public class CSDummyOrganizationService extends DummyOrganizationService {
  
  public CSDummyOrganizationService() {
    super();
    this.userDAO_ = new CSUserHandlerImpl();
  }
  
  static public class CSUserHandlerImpl extends UserHandlerImpl {
    private List<User>       _users;

    public CSUserHandlerImpl() {
      super();
      
      _users = new ArrayList<User>();

      User usr = new UserImpl("exo");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("exo1");
      usr.setPassword("exo1");
      _users.add(usr);

      usr = new UserImpl("exo2");
      usr.setPassword("exo2");
      _users.add(usr);

      usr = new UserImpl("admin");
      usr.setPassword("admin");
      _users.add(usr);

      usr = new UserImpl("weblogic");
      usr.setPassword("11111111");
      _users.add(usr);

      usr = new UserImpl("__anonim");
      _users.add(usr);

      // webos users
      usr = new UserImpl("root");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("john");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("james");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("mary");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("marry");
      usr.setPassword("exo");
      _users.add(usr);

      usr = new UserImpl("demo");
      usr.setPassword("exo");
      _users.add(usr);

    }
    public PageList getUserPageList(int pageSize) throws Exception {
      return new ObjectPageList(_users, pageSize);
    }
    
  }
}
