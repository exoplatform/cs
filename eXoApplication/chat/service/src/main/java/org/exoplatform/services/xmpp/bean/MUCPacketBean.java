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

import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.packet.MUCUser.Destroy;
import org.jivesoftware.smackx.packet.MUCUser.Item;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class MUCPacketBean {

  /**
   * 
   */
  private String              room;

  /**
   * 
   */
  private String              action;

  /**
   * 
   */
  private DeclineBean         decline;

  /**
   * 
   */
  private Destroy             destroy;

  /**
   * 
   */
  private InviteBean          invite;

  /**
   * 
   */
  private Item                item;

  /**
   * 
   */
  private PresenceBean        presence;

  /**
   * 
   */
  private MessageBean         message;

  /**
   * 
   */
  private Boolean             isRoomPasswordProtect;

  /**
   * 
   */
  private PrivilegeChangeBean affiliate;

  /**
   * 
   */
  private PrivilegeChangeBean role;

  /**
   * 
   */
  private KickedBannedBean    kicked;

  /**
   * 
   */
  private KickedBannedBean    banned;

  /**
   * 
   */
  private String              joined;

  /**
   * 
   */
  private String              left;

  /**
   * 
   */
  private ChangeNickBean      changeNick;

  /**
   * 
   */
  private Occupant            occupant;

  /**
   * 
   */
  private SubjectChangeBean   subjectChange;

  /**
   * 
   */
  private FullRoomInfoBean    createdRoom;

  /**
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * @return the affiliate
   */
  public PrivilegeChangeBean getAffiliate() {
    return affiliate;
  }

  /**
   * @return the banned
   */
  public KickedBannedBean getBanned() {
    return banned;
  }

  /**
   * @return the changeNick
   */
  public ChangeNickBean getChangeNick() {
    return changeNick;
  }

  /**
   * @return the decline
   */
  public DeclineBean getDecline() {
    return decline;
  }

  /**
   * @return the destroy
   */
  public Destroy getDestroy() {
    return destroy;
  }

  /**
   * @return the isRoomPasswordProtect
   */
  public Boolean getIsRoomPasswordProtect() {
    return isRoomPasswordProtect;
  }

  /**
   * @return the item
   */
  public Item getItem() {
    return item;
  }

  /**
   * @return the kicked
   */
  public KickedBannedBean getKicked() {
    return kicked;
  }

  /**
   * @return the left
   */
  public String getLeft() {
    return left;
  }

  /**
   * @return the message
   */
  public MessageBean getMessage() {
    return message;
  }

  /**
   * @return the presence
   */
  public PresenceBean getPresence() {
    return presence;
  }

  /**
   * @return the role
   */
  public PrivilegeChangeBean getRole() {
    return role;
  }

  /**
   * @param action the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * @param affiliate the affiliate to set
   */
  public void setAffiliate(PrivilegeChangeBean affiliate) {
    this.affiliate = affiliate;
  }

  /**
   * @param banned the banned to set
   */
  public void setBanned(KickedBannedBean banned) {
    this.banned = banned;
  }

  /**
   * @param changeNick the changeNick to set
   */
  public void setChangeNick(ChangeNickBean changeNick) {
    this.changeNick = changeNick;
  }

  /**
   * @param decline the decline to set
   */
  public void setDecline(DeclineBean decline) {
    this.decline = decline;
  }

  /**
   * @param destroy the destroy to set
   */
  public void setDestroy(Destroy destroy) {
    this.destroy = destroy;
  }

  /**
   * @param isRoomPasswordProtect the isRoomPasswordProtect to set
   */
  public void setIsRoomPasswordProtect(Boolean isRoomPasswordProtect) {
    this.isRoomPasswordProtect = isRoomPasswordProtect;
  }

  /**
   * @param item the item to set
   */
  public void setItem(Item item) {
    this.item = item;
  }

  /**
   * @return the joined
   */
  public String getJoined() {
    return joined;
  }

  /**
   * @param joined the joined to set
   */
  public void setJoined(String joined) {
    this.joined = joined;
  }

  /**
   * @param kicked the kicked to set
   */
  public void setKicked(KickedBannedBean kicked) {
    this.kicked = kicked;
  }

  /**
   * @param left the left to set
   */
  public void setLeft(String left) {
    this.left = left;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(MessageBean message) {
    this.message = message;
  }

  /**
   * @param presence the presence to set
   */
  public void setPresence(PresenceBean presence) {
    this.presence = presence;
  }

  /**
   * @param role the role to set
   */
  public void setRole(PrivilegeChangeBean role) {
    this.role = role;
  }

  /**
   * @return the occupant
   */
  public Occupant getOccupant() {
    return occupant;
  }

  /**
   * @param occupant the occupant to set
   */
  public void setOccupant(Occupant occupant) {
    this.occupant = occupant;
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
   * @return the subjectChange
   */
  public SubjectChangeBean getSubjectChange() {
    return subjectChange;
  }

  /**
   * @param subjectChange the subjectChange to set
   */
  public void setSubjectChange(SubjectChangeBean subjectChange) {
    this.subjectChange = subjectChange;
  }

  /**
   * @return the invite
   */
  public InviteBean getInvite() {
    return invite;
  }

  /**
   * @param invite the invite to set
   */
  public void setInvite(InviteBean invite) {
    this.invite = invite;
  }

  /**
   * @return the createdRoom
   */
  public FullRoomInfoBean getCreatedRoom() {
    return createdRoom;
  }

  /**
   * @param createdRoom the createdRoom to set
   */
  public void setCreatedRoom(FullRoomInfoBean createdRoom) {
    this.createdRoom = createdRoom;
  }

}
