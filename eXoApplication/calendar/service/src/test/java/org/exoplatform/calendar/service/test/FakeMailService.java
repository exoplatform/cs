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
package org.exoplatform.calendar.service.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 23, 2009  
 */
public class FakeMailService implements MailService {

  public void addListenerPlugin(ComponentPlugin listener) throws Exception {
    // TODO Auto-generated method stub

  }

  public void addTag(SessionProvider provider, String username, String accountId, Tag tag) throws Exception {
    // TODO Auto-generated method stub

  }

  public void addTag(SessionProvider provider,
                     String username,
                     String accountId,
                     List<Message> messages,
                     List<Tag> tag) throws Exception {
    // TODO Auto-generated method stub

  }

  public void checkMail(String username, String accountId) throws Exception {
    // TODO Auto-generated method stub

  }
  
  public void checkMail(String username, String accountId, String folderId) throws Exception {
    // TODO Auto-generated method stub

  }

  public List<Message> checkNewMessage(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public List<Message> checkNewMessage(SessionProvider provider, String username, String accountId, String folderId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void createAccount(SessionProvider provider, String username, Account account) throws Exception {
    // TODO Auto-generated method stub

  }

  public OutputStream exportMessage(SessionProvider provider,
                                    String username,
                                    String accountId,
                                    Message message) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Account getAccountById(SessionProvider provider, String username, String id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Account> getAccounts(SessionProvider provider, String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public CheckingInfo getCheckingInfo(String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Account getDefaultAccount(SessionProvider provider, String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MessageFilter getFilterById(SessionProvider provider,
                                     String username,
                                     String accountId,
                                     String filterId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<MessageFilter> getFilters(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Folder getFolder(SessionProvider provider,
                          String username,
                          String accountId,
                          String folderId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFolderHomePath(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getFolderParentId(SessionProvider provider,
                                  String username,
                                  String accountId,
                                  String folderId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Folder> getFolders(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Folder> getFolders(SessionProvider provider,
                                 String username,
                                 String accountId,
                                 boolean isPersonal) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public String getMailHierarchyNode(SessionProvider provider) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MailSetting getMailSetting(SessionProvider provider, String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Message getMessageById(SessionProvider provider,
                                String username,
                                String accountId,
                                String nodeName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Message> getMessageByTag(SessionProvider provider,
                                       String username,
                                       String accountId,
                                       String tagId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MessagePageList getMessagePageList(SessionProvider provider,
                                            String username,
                                            MessageFilter filter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MessagePageList getMessagePageListByFolder(SessionProvider provider,
                                                    String username,
                                                    String accountId,
                                                    String folderId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public MessagePageList getMessagePagelistByTag(SessionProvider provider,
                                                 String username,
                                                 String accountId,
                                                 String tagId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Message> getMessages(SessionProvider provider, String username, MessageFilter filter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Message> getMessagesByFolder(SessionProvider provider,
                                           String username,
                                           String accountId,
                                           String folderId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Message> getMessagesByTag(SessionProvider provider,
                                        String username,
                                        String accountId,
                                        String tagId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Message> getReferencedMessages(SessionProvider provider,
                                             String username,
                                             String accountId,
                                             String msgPath) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public SpamFilter getSpamFilter(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Folder> getSubFolders(SessionProvider provider,
                                    String username,
                                    String accountId,
                                    String parentPath) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Tag getTag(SessionProvider provider, String username, String accountId, String tagId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Tag> getTags(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean importMessage(SessionProvider provider,
                               String username,
                               String accountId,
                               String folderId,
                               InputStream inputStream,
                               String type) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isExistFolder(SessionProvider provider,
                               String username,
                               String accountId,
                               String parentId,
                               String folderName) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  public Message loadAttachments(SessionProvider provider,
                                 String username,
                                 String accountId,
                                 Message msg) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void moveMessage(SessionProvider provider,
                          String username,
                          String accountId,
                          Message msg,
                          String currentFolderId,
                          String destFolderId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void moveMessage(SessionProvider provider,
                          String username,
                          String accountId,
                          Message msg,
                          String currentFolderId,
                          String destFolderId,
                          boolean updateReference) throws Exception {
    // TODO Auto-generated method stub

  }

  public void moveMessages(SessionProvider provider,
                           String username,
                           String accountId,
                           List<Message> msgList,
                           String currentFolderId,
                           String destFolderId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void moveMessages(SessionProvider provider,
                           String username,
                           String accountId,
                           List<Message> msgList,
                           String currentFolderId,
                           String destFolderId,
                           boolean updateReference) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeAccount(SessionProvider provider, String username, String accountId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeCheckingInfo(String username, String accountId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeFilter(SessionProvider provider,
                           String username,
                           String accountId,
                           String filterId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeMessage(SessionProvider provider,
                            String username,
                            String accountId,
                            Message message) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeMessages(SessionProvider provider,
                             String username,
                             String accountId,
                             List<Message> messages,
                             boolean moveReference) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeTag(SessionProvider provider, String username, String accountId, String tag) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeTagsInMessages(SessionProvider provider,
                                   String username,
                                   String accountId,
                                   List<Message> messages,
                                   List<String> tags) throws Exception {
    // TODO Auto-generated method stub

  }

  public void removeUserFolder(SessionProvider provider,
                               String username,
                               String accountId,
                               String folderId) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveFilter(SessionProvider provider,
                         String username,
                         String accountId,
                         MessageFilter filter,
                         boolean applyAll) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveFolder(SessionProvider provider, String username, String accountId, Folder folder) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveFolder(SessionProvider provider,
                         String username,
                         String accountId,
                         String parentId,
                         Folder folder) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveMailSetting(SessionProvider provider, String username, MailSetting newSetting) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveMessage(SessionProvider provider,
                          String username,
                          String accountId,
                          String targetMsgPath,
                          Message message,
                          boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveMessage(SessionProvider provider,
                          String username,
                          String accountId,
                          Message message,
                          boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  public void saveSpamFilter(SessionProvider provider,
                             String username,
                             String accountId,
                             SpamFilter spamFilter) throws Exception {
    // TODO Auto-generated method stub

  }

  public Message sendMessage(SessionProvider provider, String username, Account acc, Message message) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Message sendMessage(SessionProvider provider,
                             String username,
                             String accId,
                             Message message) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void sendMessage(Message message) throws Exception {
    // TODO Auto-generated method stub

  }

  public Message sendMessage(SessionProvider provider, String username, Message message) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception {
    // TODO Auto-generated method stub

  }

  public void stopCheckMail(String username, String accountId) {
    // TODO Auto-generated method stub

  }

  public void toggleMessageProperty(SessionProvider provider,
                                    String username,
                                    String accountId,
                                    List<Message> msgList,
                                    String property) throws Exception {
    // TODO Auto-generated method stub

  }

  public void updateAccount(SessionProvider provider, String username, Account account) throws Exception {
    // TODO Auto-generated method stub

  }

  public void updateTag(SessionProvider provider, String username, String accountId, Tag tag) throws Exception {
    // TODO Auto-generated method stub

  }
  public boolean sendReturnReceipt(SessionProvider provider, String username, String accId, String msgId, ResourceBundle res) throws Exception {
   // TODO Auto-generated method stub
    return true;  
  }

}
