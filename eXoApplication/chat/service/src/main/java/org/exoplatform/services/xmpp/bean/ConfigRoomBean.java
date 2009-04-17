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

import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ConfigRoomBean {

  /**
   * 
   */
  private String       roomname;

  /**
   * 
   */
  private String       roomdesc;

  /**
   * 
   */
  private Boolean      changesubject;

  /**
   * 
   */
  private Boolean      enablelogging;

  /**
   * 
   */
  private List<String> maxusers;

  /**
   * 
   */
  private List<String> presencebroadcast;

  /**
   * 
   */
  private Boolean      publicroom;

  /**
   * 
   */
  private Boolean      persistentroom;

  /**
   * 
   */
  private Boolean      moderatedroom;

  /**
   * 
   */
  private Boolean      membersonly;

  /**
   * 
   */
  private Boolean      allowinvites;

  /**
   * 
   */
  private Boolean      passwordprotectedroom;

  /**
   * 
   */
  private String       roomsecret;

  /**
   * 
   */
  private List<String> whois;

  /**
   * 
   */
  private Boolean      reservednick;

  /**
   * 
   */
  private Boolean      canchangenick;

  /**
   * 
   */
  private List<String> roomadmins;

  /**
   * 
   */
  private List<String> roomowners;

  /**
   * @return the roomname
   */
  public String getRoomname() {
    return roomname;
  }

  /**
   * @param roomname the roomname to set
   */
  public void setRoomname(String roomname) {
    this.roomname = roomname;
  }

  /**
   * @return the roomdesc
   */
  public String getRoomdesc() {
    return roomdesc;
  }

  /**
   * @param roomdesc the roomdesc to set
   */
  public void setRoomdesc(String roomdesc) {
    this.roomdesc = roomdesc;
  }

  /**
   * @return the changesubject
   */
  public Boolean getChangesubject() {
    return changesubject;
  }

  /**
   * @param changesubject the changesubject to set
   */
  public void setChangesubject(Boolean changesubject) {
    this.changesubject = changesubject;
  }

  /**
   * @return the maxusers
   */
  public List<String> getMaxusers() {
    return maxusers;
  }

  /**
   * @param maxusers the maxusers to set
   */
  public void setMaxusers(List<String> maxusers) {
    this.maxusers = maxusers;
  }

  /**
   * @return the presencebroadcast
   */
  public List<String> getPresencebroadcast() {
    return presencebroadcast;
  }

  /**
   * @param presencebroadcast the presencebroadcast to set
   */
  public void setPresencebroadcast(List<String> presencebroadcast) {
    this.presencebroadcast = presencebroadcast;
  }

  /**
   * @return the publicroom
   */
  public Boolean getPublicroom() {
    return publicroom;
  }

  /**
   * @param publicroom the publicroom to set
   */
  public void setPublicroom(Boolean publicroom) {
    this.publicroom = publicroom;
  }

  /**
   * @return the persistentroom
   */
  public Boolean getPersistentroom() {
    return persistentroom;
  }

  /**
   * @param persistentroom the persistentroom to set
   */
  public void setPersistentroom(Boolean persistentroom) {
    this.persistentroom = persistentroom;
  }

  /**
   * @return the moderatedroom
   */
  public Boolean getModeratedroom() {
    return moderatedroom;
  }

  /**
   * @param moderatedroom the moderatedroom to set
   */
  public void setModeratedroom(Boolean moderatedroom) {
    this.moderatedroom = moderatedroom;
  }

  /**
   * @return the membersonly
   */
  public Boolean getMembersonly() {
    return membersonly;
  }

  /**
   * @param membersonly the membersonly to set
   */
  public void setMembersonly(Boolean membersonly) {
    this.membersonly = membersonly;
  }

  /**
   * @return the allowinvites
   */
  public Boolean getAllowinvites() {
    return allowinvites;
  }

  /**
   * @param allowinvites the allowinvites to set
   */
  public void setAllowinvites(Boolean allowinvites) {
    this.allowinvites = allowinvites;
  }

  /**
   * @return the passwordprotectedroom
   */
  public Boolean getPasswordprotectedroom() {
    return passwordprotectedroom;
  }

  /**
   * @param passwordprotectedroom the passwordprotectedroom to set
   */
  public void setPasswordprotectedroom(Boolean passwordprotectedroom) {
    this.passwordprotectedroom = passwordprotectedroom;
  }

  /**
   * @return the roomsecret
   */
  public String getRoomsecret() {
    return roomsecret;
  }

  /**
   * @param roomsecret the roomsecret to set
   */
  public void setRoomsecret(String roomsecret) {
    this.roomsecret = roomsecret;
  }

  /**
   * @return the whois
   */
  public List<String> getWhois() {
    return whois;
  }

  /**
   * @param whois the whois to set
   */
  public void setWhois(List<String> whois) {
    this.whois = whois;
  }

  /**
   * @return the reservednick
   */
  public Boolean getReservednick() {
    return reservednick;
  }

  /**
   * @param reservednick the reservednick to set
   */
  public void setReservednick(Boolean reservednick) {
    this.reservednick = reservednick;
  }

  /**
   * @return the canchangenick
   */
  public Boolean getCanchangenick() {
    return canchangenick;
  }

  /**
   * @param canchangenick the canchangenick to set
   */
  public void setCanchangenick(Boolean canchangenick) {
    this.canchangenick = canchangenick;
  }

  /**
   * @return the roomadmins
   */
  public List<String> getRoomadmins() {
    return roomadmins;
  }

  /**
   * @param roomadmins the roomadmins to set
   */
  public void setRoomadmins(List<String> roomadmins) {
    this.roomadmins = roomadmins;
  }

  /**
   * @return the roomowners
   */
  public List<String> getRoomowners() {
    return roomowners;
  }

  /**
   * @param roomowners the roomowners to set
   */
  public void setRoomowners(List<String> roomowners) {
    this.roomowners = roomowners;
  }

  /**
   * @return the enablelogging
   */
  public Boolean getEnablelogging() {
    return enablelogging;
  }

  /**
   * @param enablelogging the enablelogging to set
   */
  public void setEnablelogging(Boolean enablelogging) {
    this.enablelogging = enablelogging;
  }

}
