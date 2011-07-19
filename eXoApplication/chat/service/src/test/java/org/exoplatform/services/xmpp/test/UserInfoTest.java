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

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/org/exoplatform/services/organization/TestOrganization-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.cs.eXoApplication.chat.service.test-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration2.xml") })
public class UserInfoTest extends AbstractKernelTest {
  private Log log = ExoLogger.getLogger(this.getClass());
  private OrganizationUserInfoServiceImpl infoServiceImpl;

  protected void setUp() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/test/java/conf/login.conf");
    OrganizationService service = (OrganizationService) portalContainer.getComponentInstanceOfType(OrganizationService.class);
    infoServiceImpl = new OrganizationUserInfoServiceImpl(service);
    begin();
  }

  public void testGetUserInfo() {
    log.info("==========================================================");
    log.info("Testing geting userinfo from OrgService");
    log.info("==========================================================");
    UserInfo info = infoServiceImpl.getUserInfo("root");
    log.info("Username: " + info.getUserName());
    log.info("FirstName: " + info.getFirstName());
    log.info("LastName: " + info.getLastName());
    log.info("Organization: " + info.getOrganization());
    // log.info("Unit: " + info.getUnit());
    log.info("eMail: " + info.getEMail());
  }

  protected void tearDown() throws Exception {
    end();
    super.tearDown();
  }

}
