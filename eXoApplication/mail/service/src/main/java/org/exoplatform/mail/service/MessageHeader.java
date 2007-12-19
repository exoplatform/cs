/*
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
package org.exoplatform.mail.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MessageHeader {
  private String id ;
  private String accountId;
  private long priority;
  
  public MessageHeader() {
    setId("MessageHeader" + IdGenerator.generate()) ;
  }
  public String getId() { return id ; }
  public void setId(String id) { this.id = id; }
  
  public String getAccountId() { return accountId ; }
  public void setAccountId(String accountId) { this.accountId = accountId ; }

  public long getPriority() { return priority; }
  public void setPriority(long priority) { this.priority = priority; }
}
