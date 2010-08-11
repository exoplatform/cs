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
package org.exoplatform.webservice.cs.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Tag;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 14, 2010  
 */
public class MessageData {
  private Collection<Message> info ;
  private List<Account> accounts = new ArrayList<Account>();  
  private List<Folder> folders = new ArrayList<Folder>();
  private List<Tag> tags = new ArrayList<Tag>();
  
  public Collection<Message> getInfo() { return info; }
  public void setInfo(Collection<Message> info) { this.info = info; }
 
  public List<Folder> getFolders() { return folders; }
  public void setFolders(List<Folder> f) { this.folders = f; }
  
  public List<Tag> getTags() { return tags; }
  public void setTags(List<Tag> t) { this.tags = t; }
  
  public List<Account> getAccounts() { return accounts; }
  public void setAccounts(List<Account> a) { this.accounts = a; }
}