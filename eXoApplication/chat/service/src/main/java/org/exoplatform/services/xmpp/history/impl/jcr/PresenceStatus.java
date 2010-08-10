/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.xmpp.history.impl.jcr;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          hoangnv01@gmail.com
 * Jul 26, 2010  
 */

import org.exoplatform.services.xmpp.util.CodingUtils;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

@JcrNode(nodeType = "lr:presencestatus")
public class PresenceStatus {

  /**
   * assigned by userId***/
  @JcrName
  private String                  hexName;                                            

  @JcrPath
  private String                  path;
  
  /**
   * set/get [lr:status] node property**/
  
  @JcrProperty(name = "lr:status")
  private String                  status;

  
  @JcrProperty(name = "lr:userid")
  private String                  userId;
  
  public PresenceStatus(){
    //nothing
  }
  
  /**
   * userId be encode to Hex code by CodingUtils.encode() method***/
  public PresenceStatus(String userId, String status){
    this.userId = userId;
    this.status = status;
    this.hexName = CodingUtils.encodeToHex(userId);
  }
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
  
  public String getHexName() {
    return hexName;
  }

  public void setHexName(String hexName) {
    this.hexName = hexName;
  }

}
