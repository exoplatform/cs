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
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ChangeNickBean {
  
  /**
   * 
   */
  private String participant;

  /**
   * 
   */
  private String newNickname;

  /**
   * 
   */
  public ChangeNickBean() {
  }

  /**
   * @param newNickname the nickname
   * @param participant the participant id
   */
  public ChangeNickBean(String newNickname, String participant) {
    this.newNickname = newNickname;
    this.participant = participant;
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

  /**
   * @return the newNickname
   */
  public String getNewNickname() {
    return newNickname;
  }

  /**
   * @param newNickname the newNickname to set
   */
  public void setNewNickname(String newNickname) {
    this.newNickname = newNickname;
  }

}
