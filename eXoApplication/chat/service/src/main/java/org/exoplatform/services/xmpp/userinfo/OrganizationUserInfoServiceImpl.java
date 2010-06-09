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
package org.exoplatform.services.xmpp.userinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class OrganizationUserInfoServiceImpl implements UserInfoService {

  /**
   * Logger.
   */
  private final Log                        log                    = LogFactory.getLog(OrganizationUserInfoServiceImpl.class);

  /**
   * 
   */
  private final OrganizationService organizationService;

  protected void start() {
    PortalContainer manager = PortalContainer.getInstance() ;
    ((ComponentRequestLifecycle)organizationService).startRequest(manager);
  }

  protected void stop() {
    PortalContainer manager = PortalContainer.getInstance() ;
    ((ComponentRequestLifecycle)organizationService).endRequest(manager);
  }

  /**
   * @param service the service to set
   */
  public OrganizationUserInfoServiceImpl(OrganizationService service) {
    organizationService = service;
  }

  /**
   * @return the impl of organization service
   */
  public OrganizationService getOrganizationService() {
    return organizationService;
  }

  /**
   * {@inheritDoc}
   */
  public UserInfo getUserInfo(String userID) {
    try {
      start();
      User user = organizationService.getUserHandler().findUserByName(userID);
      stop();
      UserInfo userInfo = new UserInfo();
      userInfo.setUserName(user.getUserName());
      userInfo.setFirstName(user.getFirstName());
      userInfo.setLastName(user.getLastName());
      userInfo.setEMail(user.getEmail());
      userInfo.setOrganization(user.getOrganizationId());
      // userInfo.setUnit("office"); //temporary
      return userInfo;
    } catch (Exception e) {
      if ( log.isDebugEnabled())
        e.printStackTrace();
    }
    return null;
  }

  /**
   *  {@inheritDoc}}
   */
  public String providePassword(String userID) {
    try {
      ConversationState curentState = ConversationState.getCurrent();
      String username = curentState.getIdentity().getUserId();
      if(userID != null && userID.equals(username))
        return (String)curentState.getIdentity().getSubject().getPrivateCredentials().iterator().next();
    } catch (Exception e) {
      if ( log.isDebugEnabled())
        e.printStackTrace();
    }
    return null;
  }

}
