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
package org.exoplatform.services.xmpp.bean;

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ContactBean {

  /**
   * 
   */
  private String       user;

  /**
   * 
   */
  private String       nickname;

  /**
   * 
   */
  private String       subscriptionType;

  /**
   * 
   */
  private String       subscriptionStatus;

  /**
   * 
   */
  private List<String> groups;

  /**
   * 
   */
  private PresenceBean presence;

  
  private String fullName ;
  /**
   * @return the groups
   */
  public List<String> getGroups() {
    return groups;
  }

  /**
   * @param groups the groups to set
   */
  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  /**
   * @return the subscriptionType
   */
  public String getSubscriptionType() {
    return subscriptionType;
  }

  /**
   * @param subscriptionType the subscriptionType to set
   */
  public void setSubscriptionType(String subscriptionType) {
    this.subscriptionType = subscriptionType;
  }

  /**
   * @return the subscriptionStatus
   */
  public String getSubscriptionStatus() {
    return subscriptionStatus;
  }

  /**
   * @param subscriptionStatus the subscriptionStatus to set
   */
  public void setSubscriptionStatus(String subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  /**
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * @return the nickname
   */
  public String getNickname() {
    return nickname;
  }

  /**
   * @return the presence
   */
  public PresenceBean getPresence() {
    return presence;
  }

  /**
   * @param user the user to set
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * @param nickname the nickname to set
   */
  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  /**
   * @param presence the presence to set
   */
  public void setPresence(PresenceBean presence) {
    this.presence = presence;
  }

  
  
  //
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String s) {
    fullName = s;
  }
  
}
