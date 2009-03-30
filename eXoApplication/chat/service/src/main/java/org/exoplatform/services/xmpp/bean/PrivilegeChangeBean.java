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
package org.exoplatform.services.xmpp.bean;


/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class PrivilegeChangeBean {

  /**
   * 
   */
  private String status;

  /**
   * 
   */
  private String privilege;

  /**
   * 
   */
  private String participant;

  /**
   * 
   */
  public PrivilegeChangeBean() {
  }

  /**
   * @param privilege the privilege
   * @param status the status
   * @param participant the participant
   */
  public PrivilegeChangeBean(String privilege, String status, String participant) {
    this.privilege = privilege;
    this.status = status;
    this.participant = participant;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * @return the privilege
   */
  public String getPrivilege() {
    return privilege;
  }

  /**
   * @param privilege the privilege to set
   */
  public void setPrivilege(String privilege) {
    this.privilege = privilege;
  }

  /**
   * @return the participant
   */
  public String getParticipant() {
    return participant;
  }

  /**
   * @param participant the participant to set
   */
  public void setParticipant(String participant) {
    this.participant = participant;
  }

}
