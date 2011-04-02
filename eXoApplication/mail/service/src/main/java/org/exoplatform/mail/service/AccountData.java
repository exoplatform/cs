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

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 * 
 */
public class AccountData {
  private Account      account;

  private List<Folder> defaultFolders;

  private List<Folder> userFolders;

  private List<Tag>    tags;

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account acc) {
    this.account = acc;
  }

  /**
   * @return Return a list of the default folder: Inbox, Sent, Draft, Spam and Trash
   */
  public List<Folder> getDefaultFolder() {
    return defaultFolders;
  }

  public void setDefaultFolder(List<Folder> folders) {
    defaultFolders = folders;
  }

  /**
   * @return Return a list of the folder that is created by the user
   */
  public List<Folder> getUserFolder() {
    return userFolders;
  }

  public void setUserFolder(List<Folder> folders) {
    userFolders = folders;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tg) {
    tags = tg;
  }

}
