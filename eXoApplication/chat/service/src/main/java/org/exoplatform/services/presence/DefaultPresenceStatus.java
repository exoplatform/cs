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

import java.util.HashMap;

import java.util.Map;

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
    
  public final static String DEFAULT_STATUS              =     "Available";
  public final static String OFFLINE_STATUS              =     "Unavailable";
  public final static String FREE_TO_CHAT_STATUS         =     "Free to chat";
  public final static String DO_NOT_DISTURB_STATUS       =     "Do not disturb";
  public final static String AWAY_STATUS                 =     "Away";
  public final static String EXTEND_AWAY_STATUS          =     "Extend away";

  public final static String ONLINEICON                  =    "OnlineIcon";
  public final static String AWAYICON                    =    "AwayIcon";
  public final static String EXTENDAWAYICON              =    "ExtendAwayIcon";
  public final static String FREETOICON                  =    "FreeToChat";
  public final static String OFFLINEICON                 =    "OfflineIcon";
 
  private String status_ = DEFAULT_STATUS;
  
  public String getStatus_() {
    return status_;
  }
  public void setStatus_(String status) {
    status_ = status;
  }
  
  public DefaultPresenceStatus() {}
  
  private String statusIcon_ = ONLINEICON;
  
  private Map<String, String> statusmap = new HashMap<String, String>();//statusText|statusIcon
  
  public DefaultPresenceStatus(InitParams param){
    PropertiesParam pparam = param.getPropertiesParam("chat-status");
    if(pparam != null){
      status_ = (pparam.getProperty("mode") == null)?DEFAULT_STATUS:pparam.getProperty("mode");
    }
    
    if(!getStatusIcon_(status_).equals(""))
      statusIcon_ = getStatusIcon_(status_);
    statusmap.clear();
    
    statusmap.put(status_,  statusIcon_);
  }
  
  private String getStatusIcon_(String status_){
    String stIcon_ = "";
    if(status_.equalsIgnoreCase(DefaultPresenceStatus.DEFAULT_STATUS)) stIcon_ = ONLINEICON;
    if(status_.equalsIgnoreCase(DefaultPresenceStatus.AWAY_STATUS)) stIcon_ = AWAYICON;
    if(status_.equalsIgnoreCase(DefaultPresenceStatus.EXTEND_AWAY_STATUS)) stIcon_ = EXTENDAWAYICON;
    if(status_.equalsIgnoreCase(DefaultPresenceStatus.FREE_TO_CHAT_STATUS)) stIcon_ = FREETOICON;
    if(status_.equalsIgnoreCase(DefaultPresenceStatus.OFFLINE_STATUS))stIcon_ = OFFLINEICON;
    return stIcon_;
  }
  
  /**
   * Getting user chat status**/
  public Map<String, String> getPreviousStatus(String userId){
    //get status from jcr here
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    HistoryImpl history = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
    SessionProvider provider = SessionProvider.createSystemProvider();
    String status = history.getPresenceStatusHistory(provider, userId);
    if(status != null) {
      setStatus_(status);
      if(!getStatusIcon_(status).equals(""))
        statusIcon_ = getStatusIcon_(status);
      statusmap.clear();
      statusmap.put(status, statusIcon_);
    }
    if(provider != null) provider.close();
    return statusmap;
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
