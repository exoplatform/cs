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
public class KickedBannedBean {

  /**
   * 
   */
  private String participant;

  /**
   * 
   */
  private String actor;

  /**
   * 
   */
  private String reason;

  /**
   * @param actor the actor
   * @param participant the participant
   * @param reason the reason
   */
  public KickedBannedBean(String actor, String participant, String reason) {
    super();
    this.actor = actor;
    this.participant = participant;
    this.reason = reason;
  }

  /**
   * 
   */
  public KickedBannedBean() {
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
   * @return the actor
   */
  public String getActor() {
    return actor;
  }

  /**
   * @param actor the actor to set
   */
  public void setActor(String actor) {
    this.actor = actor;
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
