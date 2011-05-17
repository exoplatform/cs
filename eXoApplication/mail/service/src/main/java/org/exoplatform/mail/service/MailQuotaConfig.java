/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          hoangnv@exoplatform.com
 * May 12, 2011  
 */
public class MailQuotaConfig{
  
  private final long QUOTA_DEFAULT_VALUE = 200;//measure by MB
  
  private long adminQuota;
  
  public long getAdminQuota() {
    return adminQuota;
  }

  public long getUserQuota() {
    return userQuota;
  }

  private long userQuota;
  
  public MailQuotaConfig(InitParams ip){
    PropertiesParam pparam = ip.getPropertiesParam(Utils.MAIL_QUOTA);
    if(pparam != null){
      adminQuota = (pparam.getProperty(Utils.MAIL_QUOTA_ADMIN) == null) ? QUOTA_DEFAULT_VALUE : Long.parseLong(pparam.getProperty(Utils.MAIL_QUOTA_ADMIN));
      userQuota = (pparam.getProperty(Utils.MAIL_QUOTA_USER) == null) ? QUOTA_DEFAULT_VALUE : Long.parseLong(pparam.getProperty(Utils.MAIL_QUOTA_USER));
    }
  }
}
