/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.contact.webui.popup;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
 * Mar 21, 2011  
 */

public class PermissionData {
  private static final Log log = ExoLogger.getExoLogger(PermissionData.class);
  
  String viewPermission = null ;
  String editPermission = null ;

  public  String getViewPermission() {return viewPermission ;}
  public  String getEditPermission() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
    try {
      if (editPermission != null && editPermission.equalsIgnoreCase("true")) {
        return  res.getString("UIAddEditPermission.label.true");
      } else {
        return res.getString("UIAddEditPermission.label.false");
      }
    } catch (MissingResourceException e) {      
      if (log.isDebugEnabled()) {
        log.debug("MissingResourceException in method getEditPermission", e);
      }
      return editPermission ;
    }
  }
    
  public PermissionData(String username, boolean canEdit) throws Exception {
    viewPermission = username.replaceFirst(DataStorage.HYPHEN, "") ;
    String edit = String.valueOf(canEdit) ;
    editPermission = edit.replaceFirst(DataStorage.HYPHEN, "") ;
  }
}