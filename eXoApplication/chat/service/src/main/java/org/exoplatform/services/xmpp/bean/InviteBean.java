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
public class InviteBean {

  /**
   * 
   */
  private String      room;

  /**
   * 
   */
  private String      inviter;
  
  /**
   * 
   */
  private String inviterName;

  /**
   * 
   */
  private String      reason;

  /**
   * 
   */
  private String      password;

  /**
   * 
   */
  private MessageBean message;

  /**
   * 
   */
  public InviteBean() {
  }

  /**
   * @param inviter the inviter
   * @param message the message
   * @param password the password
   * @param reason the reason
   * @param room the room
   */
  public InviteBean(String inviter, MessageBean message, String password, String reason, String room) {
    super();
    this.inviter = inviter;
    this.message = message;
    this.password = password;
    this.reason = reason;
    this.room = room;
  }
  
  public InviteBean(String inviter, String inviterName, MessageBean message, String password, String reason, String room) {
    super();
    this.inviter = inviter;
    this.inviterName = inviterName;
    this.message = message;
    this.password = password;
    this.reason = reason;
    this.room = room;
  }

  /**
   * @return the room
   */
  public String getRoom() {
    return room;
  }

  /**
   * @param room the room to set
   */
  public void setRoom(String room) {
    this.room = room;
  }

  /**
   * @return the inviter
   */
  public String getInviter() {
    return inviter;
  }

  /**
   * @param inviter the inviter to set
   */
  public void setInviter(String inviter) {
    this.inviter = inviter;
  }
  
  public String getInviterName() {
    return inviterName;
  }
  
  public void setInviterName(String inviterName) {
    this.inviterName = inviterName;
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

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }
  // /**
  // * @return the message
  // */
  // public MessageBean getMessage() {
  // return message;
  // }
  // /**
  // * @param message the message to set
  // */
  // public void setMessage(MessageBean message) {
  // this.message = message;
  // }
  //  

}
