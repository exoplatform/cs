/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.organization.rest.json;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class UserBean {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger("ws.UserBean");
  private Date createdDate;
  private String email;
  private String firstName;
  private String fullName;
  private Date lastLoginTime;
  private String lastName;
  private String organizationId;
  private String userName;
  
  public UserBean(User user) {
    this.createdDate = user.getCreatedDate();
    this.email = user.getEmail();
    this.firstName = user.getFirstName();
    this.fullName = user.getFullName();
    this.lastLoginTime = user.getLastLoginTime();
    this.lastName = user.getLastName();
    this.organizationId = user.getOrganizationId();
    this.userName = user.getUserName();
  }

  /**
   * @return the createdDate
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * @param createdDate the createdDate to set
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param email the email to set
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return the firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName the firstName to set
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return the fullName
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * @param fullName the fullName to set
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * @return the lastLoginTime
   */
  public Date getLastLoginTime() {
    return lastLoginTime;
  }

  /**
   * @param lastLoginTime the lastLoginTime to set
   */
  public void setLastLoginTime(Date lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  /**
   * @return the lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param lastName the lastName to set
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * @return the organizationId
   */
  public String getOrganizationId() {
    return organizationId;
  }

  /**
   * @param organizationId the organizationId to set
   */
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return userName;
  }

  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }
  
}
