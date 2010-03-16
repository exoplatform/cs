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

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.*;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.xmpp.userinfo.OrganizationUserInfoServiceImpl;
import org.exoplatform.services.xmpp.userinfo.UserInfo;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.cs.eXoApplication.chat.service.test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration1.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration2.xml")
})
public class UserInfoTest extends AbstractKernelTest {

  protected ChromatticManager chromatticManager;
  
  private OrganizationUserInfoServiceImpl infoServiceImpl;

  protected void setUp() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    chromatticManager = (ChromatticManager)portalContainer.getComponentInstanceOfType(ChromatticManager.class);
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config",
                         "src/test/java/conf/login.conf");
    OrganizationService service = (OrganizationService)portalContainer.getComponentInstanceOfType(OrganizationService.class);
    infoServiceImpl = new OrganizationUserInfoServiceImpl(service);
    begin();
  }

  public void testGetUserInfo() {
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
    chromatticManager.getSynchronization().setSaveOnClose(false);
    end();
    super.tearDown();
  }

}
