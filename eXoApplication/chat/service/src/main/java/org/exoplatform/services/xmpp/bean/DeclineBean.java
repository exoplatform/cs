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
public class DeclineBean {

  /**
   * 
   */
  private String invitee;

  /**
   * 
   */
  private String reason;

  /**
   * 
   */
  public DeclineBean() {
  }

  /**
   * @param invitee the invitee 
   * @param reason the reason
   */
  public DeclineBean(String invitee, String reason) {
    this.invitee = invitee;
    this.reason = reason;
  }

  /**
   * @return the invitee
   */
  public String getInvitee() {
    return invitee;
  }

  /**
   * @param invitte the invitee to set
   */
  public void setInvitee(String invitte) {
    this.invitee = invitte;
  }

  /**
   * @return the reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * @param reason the reason to set
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

}
