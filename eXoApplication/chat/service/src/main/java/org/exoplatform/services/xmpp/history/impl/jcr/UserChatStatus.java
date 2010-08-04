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

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

@JcrNode(nodeType = "lr:userchatstatus")
public class UserChatStatus {

  /**
   * assigned by userId***/
  @JcrName
  private String                  ucs_name;                                            

  @JcrPath
  private String                  path;
  
  /**
   * set/get [lr:status] node property**/
  @JcrProperty(name = "lr:status")
  private String                  status;

  public UserChatStatus(){
    //nothing
  }
  
  public UserChatStatus(String userId){
    ucs_name = userId;
  }
  
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
  

  public String getUs_name() {
    return ucs_name;
  }

  public void setUs_name(String usName) {
    ucs_name = usName;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

}
