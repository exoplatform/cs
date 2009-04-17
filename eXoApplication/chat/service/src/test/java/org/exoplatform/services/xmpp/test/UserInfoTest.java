/**
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
package org.exoplatform.services.xmpp.test;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;
import org.exoplatform.services.xmpp.userinfo.OrganizationUserInfoServiceImpl;
import org.exoplatform.services.xmpp.userinfo.UserInfo;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class UserInfoTest extends TestCase {

  protected StandaloneContainer           container;

  private OrganizationUserInfoServiceImpl infoServiceImpl;

  protected void setUp() throws Exception {
    super.setUp();

    StandaloneContainer.addConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
    OrganizationService service = (OrganizationService) container.getComponentInstanceOfType(DummyOrganizationService.class);
    infoServiceImpl = new OrganizationUserInfoServiceImpl(service);
  }

  public void testGetUserInfo() {
    assertNotNull(container);
    System.out.println("==========================================================");
    System.out.println("Testing geting userinfo from OrgService");
    System.out.println("==========================================================");
    UserInfo info = infoServiceImpl.getUserInfo("root");
    System.out.println("Username: " + info.getUserName());
    System.out.println("FirstName: " + info.getFirstName());
    System.out.println("LastName: " + info.getLastName());
    System.out.println("Organization: " + info.getOrganization());
    // System.out.println("Unit: " + info.getUnit());
    System.out.println("eMail: " + info.getEMail());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

}
