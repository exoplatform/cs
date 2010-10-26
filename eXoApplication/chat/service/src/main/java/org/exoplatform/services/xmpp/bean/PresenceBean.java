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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
/**
 * @author vetal
 *
 */
/**
 * @author vetal
 *
 */
public class PresenceBean {

  /**
   * 
   */
  private String from;

  private String fromName;
  
  /**
   * 
   */
  private String mode;

  /**
   * 
   */
  private String type;

  /**
   * 
   */
  private String status;

  /**
   * 
   */
  public PresenceBean() {
  }

  /**
   * @param from the from
   * @param mode the mode
   * @param type the type
   */
  public PresenceBean(String from, String mode, String type) {
    this.from = from;
    this.mode = mode;
    this.type = type;
  }

  /**
   * @param from the from
   * @param mode the mode
   * @param type the type
   * @param status the satus
   */
  public PresenceBean(String from, String mode, String type, String status) {
    this.from = from;
    this.mode = mode;
    this.type = type;
    this.status = status;
  }
  
  public PresenceBean(String from, String fromName, String mode, String type, String status) {
    this.from = from;
    this.fromName = fromName;
    this.mode = mode;
    this.type = type;
    this.status = status;
  }

  /**
   * @return the from
   */
  public String getFrom() {
    return from;
  }
  
  /**
   * @return the fromName
   */
  public String getFromName() {
	  return fromName;
  }

  /**
   * @return the mode
   */
  public String getMode() {
    return mode;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(String from) {
    this.from = from;
  }
  
  /**
   * 
   */
  public void setFromName(String fromName) {
	  this.fromName = fromName;
  }

  /**
   * @param mode the mode to set
   */
  public void setMode(String mode) {
    this.mode = mode;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
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

}
