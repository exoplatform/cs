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
package org.exoplatform.mail.connection;

import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 18, 2009  
 */
public interface Connector {

  public void openStore(Account account) throws Exception;
  
  public javax.mail.Folder createFolder(Folder folder) throws Exception;
  
  public javax.mail.Folder createFolder(Folder parentFolder, Folder folder) throws Exception;
  
  public Folder renameFolder(String newName, Folder folder) throws Exception;
  
  public boolean deleteFolder(Folder folder) throws Exception;
  
  public int emptyFolder(Folder folder) throws Exception;
  
  public List<Message> createMessage(List<Message> msgs, Folder folder) throws Exception;
  
  public boolean deleteMessage(List<Message> msgs, Folder folder) throws Exception;
  
  /**
   * Move message(s) between folders.
   * Return a list of deleted/moved messages
   * */
  public List<Message> moveMessage(List<Message> msgs, Folder currentFolder, Folder desFolder) throws Exception;
  
  public boolean markAsRead(List<Message> msgList, Folder folder) throws Exception;
  
  public boolean markAsUnread(List<Message> msgList, Folder folder) throws Exception;
  
  public boolean setIsStared(List<Message> msgList, boolean isStared, Folder folder) throws Exception;  
}
