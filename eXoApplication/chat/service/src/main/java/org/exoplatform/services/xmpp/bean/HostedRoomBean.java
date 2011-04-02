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

import org.jivesoftware.smackx.muc.RoomInfo;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class HostedRoomBean {

  /**
   * 
   */
  private String  jid;

  /**
   * 
   */
  private String  name;

  /**
   * 
   */
  private String  room;

  /**
   * 
   */
  private String  description;

  /**
   * 
   */
  private Integer occupantsCount;

  /**
   * 
   */
  private String  subject;

  /**
   * 
   */
  private Boolean isMembersOnly;

  /**
   * 
   */
  private Boolean isModerated;

  /**
   * 
   */
  private Boolean isNonanonymous;

  /**
   * 
   */
  private Boolean isPasswordProtected;

  /**
   * 
   */
  private Boolean isPersistent;

  /**
   * 
   */
  public HostedRoomBean() {
  }

  /**
   * @param roomInfo the roomInfo
   */
  public HostedRoomBean(RoomInfo roomInfo) {
    this.description = roomInfo.getDescription();
    this.isMembersOnly = roomInfo.isMembersOnly();
    this.isModerated = roomInfo.isModerated();
    this.isNonanonymous = roomInfo.isNonanonymous();
    this.isPasswordProtected = roomInfo.isPasswordProtected();
    this.isPersistent = roomInfo.isPersistent();
    this.room = roomInfo.getRoom();
    this.occupantsCount = roomInfo.getOccupantsCount();
    this.subject = roomInfo.getSubject();
  }

  /**
   * @param jid the jid
   * @param name the name
   */
  public HostedRoomBean(String jid, String name) {
    this.jid = jid;
    this.name = name;
  }

  /**
   * @return the jid
   */
  public String getJid() {
    return jid;
  }

  /**
   * @param jid the jid to set
   */
  public void setJid(String jid) {
    this.jid = jid;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
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
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the occupantsCount
   */
  public Integer getOccupantsCount() {
    return occupantsCount;
  }

  /**
   * @param occupantsCount the occupantsCount to set
   */
  public void setOccupantsCount(Integer occupantsCount) {
    this.occupantsCount = occupantsCount;
  }

  /**
   * @return the subject
   */
  public String getSubject() {
    return subject;
  }

  /**
   * @param subject the subject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * @return the isMembersOnly
   */
  public Boolean getIsMembersOnly() {
    return isMembersOnly;
  }

  /**
   * @param isMembersOnly the isMembersOnly to set
   */
  public void setIsMembersOnly(Boolean isMembersOnly) {
    this.isMembersOnly = isMembersOnly;
  }

  /**
   * @return the isModerated
   */
  public Boolean getIsModerated() {
    return isModerated;
  }

  /**
   * @param isModerated the isModerated to set
   */
  public void setIsModerated(Boolean isModerated) {
    this.isModerated = isModerated;
  }

  /**
   * @return the isNonanonymous
   */
  public Boolean getIsNonanonymous() {
    return isNonanonymous;
  }

  /**
   * @param isNonanonymous the isNonanonymous to set
   */
  public void setIsNonanonymous(Boolean isNonanonymous) {
    this.isNonanonymous = isNonanonymous;
  }

  /**
   * @return the isPasswordProtected
   */
  public Boolean getIsPasswordProtected() {
    return isPasswordProtected;
  }

  /**
   * @param isPasswordProtected the isPasswordProtected to set
   */
  public void setIsPasswordProtected(Boolean isPasswordProtected) {
    this.isPasswordProtected = isPasswordProtected;
  }

  /**
   * @return the isPersistent
   */
  public Boolean getIsPersistent() {
    return isPersistent;
  }

  /**
   * @param isPersistent the isPersistent to set
   */
  public void setIsPersistent(Boolean isPersistent) {
    this.isPersistent = isPersistent;
  }

}
