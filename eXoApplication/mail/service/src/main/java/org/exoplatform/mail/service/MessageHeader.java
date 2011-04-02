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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Jun 23, 2007  
 */
public class MessageHeader {
  private String              id_;

  private String              accountId_;

  private long                priority_;

  private String              inReplyToHeader_;

  private Map<String, String> headers_ = new HashMap<String, String>();

  public MessageHeader() {
    setId("MessageHeader" + IdGenerator.generate());
  }

  public String getId() {
    return id_;
  }

  public void setId(String id) {
    this.id_ = id;
  }

  public String getInReplyToHeader() {
    return inReplyToHeader_;
  }

  public void setInReplyToHeader(String inReplyToHeader) {
    this.inReplyToHeader_ = inReplyToHeader;
  }

  public String getAccountId() {
    return accountId_;
  }

  public void setAccountId(String accountId) {
    this.accountId_ = accountId;
  }

  public long getPriority() {
    return priority_;
  }

  public void setPriority(long priority) {
    this.priority_ = priority;
  }

  public Map<String, String> getHeaders() {
    return headers_;
  }

  public void setHeaders(Map<String, String> header) {
    headers_ = header;
  }

  public String getHeader(String key) {
    return headers_.get(key);
  }

  public void setHeader(String key, String value) {
    headers_.put(key, value);
  }
}
