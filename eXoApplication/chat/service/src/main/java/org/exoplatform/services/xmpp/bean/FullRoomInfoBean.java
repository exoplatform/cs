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

import java.util.Collection;

import org.jivesoftware.smackx.muc.RoomInfo;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class FullRoomInfoBean {

  /**
   * 
   */
  private RoomInfo                 roomInfo;

  /**
   * 
   */
  private Collection<OccupantBean> occupants;

  /**
   * 
   */
  private Boolean                  isPasswordProtected;

  /**
   * 
   */
  public FullRoomInfoBean() {
  }

  /**
   * @param occupants the occupants
   * @param roomInfo the roominfo
   */
  public FullRoomInfoBean(Collection<OccupantBean> occupants, RoomInfo roomInfo) {
    super();
    this.occupants = occupants;
    this.roomInfo = roomInfo;
    this.isPasswordProtected = roomInfo.isPasswordProtected();
  }

  /**
   * @return the roomInfo
   */
  public RoomInfo getRoomInfo() {
    return roomInfo;
  }

  /**
   * @param roomInfo the roomInfo to set
   */
  public void setRoomInfo(RoomInfo roomInfo) {
    this.roomInfo = roomInfo;
  }

  /**
   * @return the occupants
   */
  public Collection<OccupantBean> getOccupants() {
    return occupants;
  }

  /**
   * @param occupants the occupants to set
   */
  public void setOccupants(Collection<OccupantBean> occupants) {
    this.occupants = occupants;
  }

  public Boolean getIsPasswordProtected() {
    return isPasswordProtected;
  }

  public void setIsPasswordProtected(Boolean isPasswordProtected) {
    this.isPasswordProtected = isPasswordProtected;
  }

}
