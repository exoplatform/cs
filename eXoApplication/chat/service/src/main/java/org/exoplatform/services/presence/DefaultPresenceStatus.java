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
package org.exoplatform.services.presence;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          hoangnv01@gmail.com
 * Jul 2, 2010  
 */

public class DefaultPresenceStatus {
    
  public final static String DEFAULT_STATUS              =     "default_presence_status";
  
  private String status_ = DEFAULT_STATUS;
  
  public String getStatus_() {
    return status_;
  }
  public void setStatus_(String status) {
    status_ = status;
  }
  
  public DefaultPresenceStatus() {}
  
  public DefaultPresenceStatus(InitParams param){
    PropertiesParam pparam = param.getPropertiesParam("presence-status");
    if(pparam != null){
      status_ = (pparam.getProperty("mode") == null)?DEFAULT_STATUS:pparam.getProperty("mode");
    }
  }

  /**
   * Getting user chat status**/
  public String getPreviousStatus(String userId){
    //get status from jcr here
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    HistoryImpl history = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
    SessionProvider provider = SessionProvider.createSystemProvider();
    String ps = history.getPresenceStatusHistory(provider, userId);
    if (ps == null) ps = getStatus_();//set default presence status
    if(provider != null) provider.close();
    return ps;
  }
  
  /**
   * Saving user chat status**/
  public void savePresenceStatus(String userId, String status){
    //if can not get status form jcr, then set status default
    try {
      SessionProvider provider = SessionProvider.createSystemProvider();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      HistoryImpl history = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
      history.savePresenceStatus(provider, userId, status);  
      if(provider != null) provider.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
}
