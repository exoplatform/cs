/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 18, 2009  
 */
public class Info {
  private String id_; 
  private String accountId_; 
  private String from_;
  private String to_;
  private String subject_;
  private String date_;
  private String size_;
  private String folders_;
  private String isRead_;
  
  public String getMsgId() { return id_; }
  public void setMsgId(String id) { id_ = id; }
  
  public String getAccountId() { return accountId_; }
  public void setAccountId(String accountId) { accountId_ = accountId; }
  
  public String getFrom() { return from_; }
  public void setFrom(String from) { from_ = from; }
  
  public String getTo() { return to_; }
  public void setTo(String to) { to_ = to; }
  
  public String getSubject() { return subject_; }
  public void setSubject(String subject) { subject_ = subject; }
  
  public String getDate() {return date_; }
  public void setDate(String date) { date_ = date; }
  
  public String getSize() { return size_; }
  public void setSize(String size) { size_ = size; }
  
  public String getIsRead() { return isRead_; }
  public void setIsRead(boolean isRead) { isRead_ = String.valueOf(isRead); }
  
  public String getFolders() { return folders_; }
  public void setFolders(String folders) { folders_ = folders; }
}
