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
package org.exoplatform.mail.service.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.service.impl.JCRDataStorage;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.mortbay.log.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestMailService extends BaseMailTestCase {
  public TestMailService() throws Exception {
    super();
  }

  private MailService        mailService_;

  private SessionProvider    sProvider;

  private String             username   = "root";

  private String             receiver   = "demo";

  public static final String TEXT_PLAIN = "text/plain".intern();

  public static final String TEXT_HTML  = "text/html".intern();
  
  private JCRDataStorage  storage_;

  public void setUp() throws Exception {
    super.setUp();
    mailService_ = (MailService) container.getComponentInstanceOfType(MailService.class);
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    sProvider = sessionProviderService.getSystemSessionProvider(null);
    
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    storage_ = new JCRDataStorage(nodeHierarchyCreator, repositoryService);
  }

  public void testMailService() throws Exception {
    assertNotNull(mailService_);
    assertNotNull(sProvider);
  }

  private Account createAccountObj(String protocol) {
    Account account = new Account();
    Folder folder = new Folder();
    folder.setName("inbox");
    folder.setPersonalFolder(false);
    List<Folder> folders = new ArrayList<Folder>();
    folders.add(folder);
    account.setCheckedAuto(false);
    account.setEmptyTrashWhenExit(false);
    account.setIncomingFolder("inbox");
    account.setProtocol(protocol);

    if (Utils.POP3.equals(protocol)) {
      account.setDescription("Create " + protocol + " account");
      account.setEmailAddress("exomailtest@gmail.com");
      account.setEmailReplyAddress("exomailtest@gmail.com");
      account.setIncomingHost("pop.gmail.com");
      account.setIncomingPassword("exoadmin");
      account.setIncomingPort("995");
      account.setIncomingSsl(true);
      account.setIncomingUser("exomailtest@gmail.com");
      account.setIsSavePassword(true);
      account.setLabel("exomail test account");
      account.setOutgoingHost("smtp.gmail.com");
      account.setOutgoingPort("465");
      account.setOutgoingSsl(true);
      account.setPlaceSignature("exomailtest pop");
    } else if (Utils.IMAP.equals(protocol)) {
      account.setDescription("Create " + protocol + " account");
      account.setEmailAddress("exoservice@gmail.com");
      account.setEmailReplyAddress("exoservice@gmail.com");
      account.setIncomingHost("imap.gmail.com");
      account.setIncomingPassword("exoadmin");
      account.setIncomingPort("993");
      account.setIncomingSsl(true);
      account.setIncomingUser("exoservice@gmail.com");
      account.setIsSavePassword(true);
      account.setLabel("exoservice test account");
      account.setOutgoingHost("smtp.gmail.com");
      account.setOutgoingPort("465");
      account.setOutgoingSsl(true);
      account.setPlaceSignature("exosevice imap");
    }
    account.setSecureAuthsIncoming("XOAUTH");
    account.setAuthMechsIncoming(Utils.PLAIN);
    account.setSecureAuthsOutgoing(Utils.TLS_SSL);
    account.setAuthMechsOutgoing(Utils.PLAIN);

    account.setIsOutgoingAuthentication(true);
    account.setUseIncomingForAuthentication(true);
    account.setOutgoingUserName("exomailtest");
    account.setOutgoingPassword("exoadmin");
    return account;
  }

  // Create account
  public void testAccount() throws Exception {
    Log.info("\n\n Test POP Account");
    Account account = createAccountObj(Utils.POP3);
    String accId = account.getId();
    mailService_.createAccount(username, account);
    Account getAccount = mailService_.getAccountById(username, accId);
    assertNotNull(getAccount);

    Log.info("\n\n Test IMAP Account");
    Account account2 = createAccountObj(Utils.IMAP);
    String accId2 = account2.getId();
    mailService_.createAccount(username, account2);

    Account getAccount2 = mailService_.getAccountById(username, accId2);
    assertNotNull(getAccount2);
    assertEquals("imap.gmail.com", getAccount2.getIncomingHost());

    Account defaultAcc = mailService_.getDefaultAccount(username);
    assertNotNull(defaultAcc);
    mailService_.removeAccount(username, accId);
    assertNull(mailService_.getAccountById(username, accId));
    mailService_.removeAccount(username, accId2);
    assertNull(mailService_.getAccountById(username, accId2));
  }

  // Send mail
  public void testSendMail() throws Exception {
    Account accountPop = createAccountObj(Utils.POP3);
    mailService_.createAccount(username, accountPop);

    Account accImap = createAccountObj(Utils.IMAP);
    mailService_.createAccount(username, accImap);
    StringBuffer sbBody = new StringBuffer("");

    Message message = new Message();
    message.setContentType(TEXT_HTML);
    message.setSubject("This message has been sent form " + accountPop.getEmailAddress());
    message.setFrom(accountPop.getEmailAddress());
    message.setMessageTo(accImap.getEmailAddress());
    sbBody.append("<b>Hello " + accImap.getIncomingUser() + "</b>").append("<br/>").append(Calendar.getInstance().getTime().toString());
    message.setMessageBody(sbBody.toString());

    accountPop.setIsOutgoingAuthentication(true);
    accountPop.setUseIncomingForAuthentication(true);
    accountPop.setOutgoingUserName(username);

    message.setContentType(TEXT_HTML);
    message.setSubject("This message has been sent form " + accImap.getEmailAddress());
    message.setFrom(accImap.getEmailAddress());
    message.setMessageTo(accountPop.getEmailAddress());
    sbBody.append("<b>Hello " + accountPop.getIncomingUser() + "</b>").append("<br/>").append(Calendar.getInstance().getTime().toString());
    message.setMessageBody(sbBody.toString());
    
    try {
      mailService_.sendMessage(username, accImap.getId(), message);
      Log.info("\n\n Message has been sent use IMAP !");
    } catch (AuthenticationFailedException e) {
      fail();
    } catch (UnknownHostException e) {
      fail();
    } catch (Exception e) {
    }
    mailService_.removeAccount(username, accountPop.getId());
    mailService_.removeAccount(username, accImap.getId());
  }

  // Check mail form POP and IMAP server
  public void testGetMailFormServer() throws Exception {
    Account accountPop = createAccountObj(Utils.POP3);
    mailService_.createAccount(username, accountPop);

    Account accImap = createAccountObj(Utils.IMAP);
    mailService_.createAccount(username, accImap);
    try {
      mailService_.checkNewMessage(username, accountPop.getId());

      MessageFilter filter = new MessageFilter("testFilter");
      filter.setAccountId(accountPop.getId());
      assertNotNull(mailService_.getMessages(username, filter));
      filter.setAccountId(accImap.getId());
      assertNotNull(mailService_.getMessages(username, filter));

      assertNotNull(mailService_.getFolders(username, accountPop.getId(), false));
    } catch (Exception e) {
      fail();
    }
    mailService_.removeAccount(username, accountPop.getId());
    mailService_.removeAccount(username, accImap.getId());
  }

  public void testFolder() throws Exception {
    Account accountPop = createAccountObj(Utils.POP3);
    mailService_.createAccount(username, accountPop);
    
    assertNotNull(mailService_.getFolderHomePath(username, accountPop.getId()));
    
    // Create folder
    String folderId = "folderId";
    String folderName = "folderName";
    String folderUrl = "folderUrl";
    Folder folder = createFolder(accountPop.getId(), folderId, folderName, folderUrl);
    
    // Create subFolder
    String subFolderId = "childFolderId";
    String subFolderName = "childFolderName";
    String subFolderUrl = "childFolderUrl";
    Folder subFolder = createSubFolder(accountPop.getId(), folder.getId(), subFolderId, subFolderName, subFolderUrl);
    
    // Test isExistFolder
    assertTrue(mailService_.isExistFolder(username, accountPop.getId(), null, folder.getName()));
    assertFalse(mailService_.isExistFolder(username, accountPop.getId(), null, "Not exist name"));
    assertTrue(mailService_.isExistFolder(username, accountPop.getId(), folder.getId(), subFolder.getName()));
    assertFalse(mailService_.isExistFolder(username, accountPop.getId(), folder.getId(), "Not exist name"));
    assertFalse(mailService_.isExistFolder(username, accountPop.getId(), "Not exist Id", subFolder.getName()));
    
    // Test getFolders
    List<Folder> folders = mailService_.getFolders(username, accountPop.getId());
    assertNotNull(folders);
    assertEquals(1, folders.size());
    Folder expectedFolder = folders.get(0);
    assertEquals(folderId, expectedFolder.getId());
    assertEquals(folderName, expectedFolder.getName());
    assertEquals(folderUrl, expectedFolder.getURLName());
    
    // Test getSubFolders
    List<Folder> subFolders = mailService_.getSubFolders(username, accountPop.getId(), expectedFolder.getPath());
    assertNotNull(subFolders);
    assertEquals(1, subFolders.size());
    Folder expectedSubFolder = subFolders.get(0);
    assertEquals(subFolderId, expectedSubFolder.getId());
    assertEquals(subFolderName, expectedSubFolder.getName());
    assertEquals(subFolderUrl, expectedSubFolder.getURLName());
    
    // Test getFolder
    Folder expectedFolder1 = mailService_.getFolderById(username, accountPop.getId(), subFolder.getId());
    assertEquals(subFolderId, expectedFolder1.getId());
    assertEquals(subFolderName, expectedFolder1.getName());
    assertEquals(subFolderUrl, expectedFolder1.getURLName());
    
    // Test getFolderParentId
    String parentId = mailService_.getFolderParentId(username, accountPop.getId(), subFolder.getId());
    assertEquals(folder.getId(), parentId);
    
    // Test renameFolder
    String newFoldername = "newFoldername";
    mailService_.renameFolder(username, accountPop.getId(), newFoldername, folder.getId());
    Folder expectedFolder2 = mailService_.getFolderById(username, accountPop.getId(), folder.getId());
    assertEquals(folderId, expectedFolder2.getId());
    assertEquals(newFoldername, expectedFolder2.getName());
    assertEquals(folderUrl, expectedFolder2.getURLName());
    
    // Test removeUserFolder
    mailService_.removeUserFolder(username, accountPop.getId(), folder.getId());
    Folder expectedFolder3 = mailService_.getFolderById(username, accountPop.getId(), folder.getId());
    assertNull(expectedFolder3);
    
    mailService_.removeAccount(username, accountPop.getId());
  }

  public void testMessage() throws Exception {
    // Create POP account
    Account accountPop = createAccountObj(Utils.POP3);
    mailService_.createAccount(username, accountPop);
    
    Folder folder = createFolder(accountPop.getId(), "folderId", "folderName", "folderUrl");
    Folder desfolder = createFolder(accountPop.getId(), "desFolderId", "desFolderName", "desFolderUrl");
    
    // Create message data
    StringBuffer sbBody = new StringBuffer();
    sbBody.append("<b>Hello</b>").append("<br/>").append(Calendar.getInstance().getTime().toString());
    String messageBody = sbBody.toString();
    String messageSubject = "Welcome message";
    String messageContentType = TEXT_HTML;
    String[] messageFolderIds = new String[] {folder.getId()};
    Date messageReceivedDate = new Date();
    
    String newMessageBody = "new" + messageBody;
    String newMessageSubject = "new" + messageSubject;
    Date newMessageReceivedDate = new Date();
    
    // Create attachment data
    String attachmentName = "icalendar.ics";
    String attachmentMimeType = "text/calendar";
    
    // Create attachment
    Attachment attachment = new Attachment() {
      @Override
      public InputStream getInputStream() throws Exception {
        return new InputStream() {
          public int read() throws IOException {
            return -1;
          }
        };
      }
    };
    attachment.setName(attachmentName);
    attachment.setMimeType(attachmentMimeType);
    
    // Create and save message
    Message message = new Message();
    message.setContentType(messageContentType);
    message.setSubject(messageSubject);
    message.setFrom(accountPop.getEmailAddress());
    message.setMessageTo(accountPop.getEmailAddress());
    message.setMessageBody(messageBody);
    message.setFolders(messageFolderIds);
    message.setReceivedDate(messageReceivedDate);
    message.setAttachements(Arrays.asList(attachment));
    mailService_.saveMessage(username, accountPop.getId(), folder.getPath(), message, true);
    
    // Test getMessageById
    Message expectedMessage1 = mailService_.getMessageById(username, accountPop.getId(), message.getId());
    assertNotNull(expectedMessage1);
    assertEquals(messageContentType, expectedMessage1.getContentType());
    assertEquals(messageSubject, expectedMessage1.getSubject());
    assertEquals(accountPop.getEmailAddress(), expectedMessage1.getFrom());
    assertEquals(accountPop.getEmailAddress(), expectedMessage1.getMessageTo());
    assertEquals(messageBody, expectedMessage1.getMessageBody());
    assertEquals(messageReceivedDate, expectedMessage1.getReceivedDate());
    assertNotNull(expectedMessage1.getFolders());
    assertEquals(1, expectedMessage1.getFolders().length);
    assertEquals(folder.getId(), expectedMessage1.getFolders()[0]);
    assertNull(expectedMessage1.getAttachments());
    
    // Test edit message
    expectedMessage1.setMessageBody(newMessageBody);
    expectedMessage1.setSubject(newMessageSubject);
    expectedMessage1.setReceivedDate(newMessageReceivedDate);
    mailService_.saveMessage(username, accountPop.getId(), folder.getPath(), expectedMessage1, false);
    Message expectedMessage8 = mailService_.getMessageById(username, accountPop.getId(), message.getId());
    assertEquals(newMessageSubject, expectedMessage8.getSubject());
    assertEquals(newMessageBody, expectedMessage8.getMessageBody());
    assertEquals(newMessageReceivedDate, expectedMessage8.getReceivedDate());
    
    // Test loadTotalMessage
    Message expectedMessage2 = mailService_.loadTotalMessage(username, accountPop.getId(), expectedMessage8);
    assertNotNull(expectedMessage2);
    List<Attachment> attachmentList1 = expectedMessage2.getAttachments();
    assertNull(attachmentList1);
    
    // Test moveMessage
    Message expectedMessage3 = mailService_.moveMessage(username, accountPop.getId(), expectedMessage8, folder.getId(), desfolder.getId());
    assertNotNull(expectedMessage3);
    List<Message> messageList1 = mailService_.getMessagesByFolder(username, accountPop.getId(), desfolder.getId());
    assertNotNull(messageList1);
    assertEquals(1, messageList1.size());
    Message expectedMessage4 = messageList1.get(0);
    assertEquals(message.getId(), expectedMessage4.getId());
    
    // Test move message
    List<Message> messageList3 = mailService_.moveMessages(username, accountPop.getId(), Arrays.asList(expectedMessage3), desfolder.getId(), folder.getId());
    assertNotNull(messageList3);
    assertEquals(1, messageList3.size());
    assertEquals(expectedMessage3.getId(), messageList3.get(0).getId());
    Message expectedMessage6 = mailService_.getMessageById(username, accountPop.getId(), expectedMessage3.getId());
    assertEquals(expectedMessage3.getId(), expectedMessage6.getId());
    
    // Test removeMessage
    mailService_.removeMessage(username, accountPop.getId(), expectedMessage8);
    List<Message> messageList2 = mailService_.getMessagesByFolder(username, accountPop.getId(), desfolder.getId());
    assertEquals(0, messageList2.size());
    
    // Remove account
    mailService_.removeAccount(username, accountPop.getId());
    
    // Create IMAP account
    Account accountImap = createAccountObj(Utils.IMAP);
    mailService_.createAccount(username, accountImap);
    
    Folder desfolder1 = createFolder(accountImap.getId(), "desFolderId", "desFolderName", "desFolderUrl");
    
    // Create attachment
    Attachment attachment1 = new Attachment() {
      @Override
      public InputStream getInputStream() throws Exception {
        return new InputStream() {
          public int read() throws IOException {
            return -1;
          }
        };
      }
    };
    attachment1.setName(attachmentName);
    attachment1.setMimeType(attachmentMimeType);
    
    // Create and save message
    Message message1 = new Message();
    message1.setContentType(messageContentType);
    message1.setSubject(messageSubject);
    message1.setFrom(accountImap.getEmailAddress());
    message1.setMessageTo(accountImap.getEmailAddress());
    message1.setMessageBody(messageBody);
    message1.setFolders(messageFolderIds);
    message1.setReceivedDate(messageReceivedDate);
    message1.setAttachements(Arrays.asList(attachment));
    mailService_.saveMessage(username, accountImap.getId(), desfolder1.getPath(), message1, true);
    
    // Test loadTotalMessage
    Message expectedMessage5 = mailService_.loadTotalMessage(username, accountImap.getId(), message1);
    assertNotNull(expectedMessage5);
    List<Attachment> attachmentList2 = expectedMessage5.getAttachments();
    assertNotNull(attachmentList2);
    Attachment expectedAttachment2 = attachmentList2.get(0);
    assertEquals(attachmentName, expectedAttachment2.getName());
    assertEquals(attachmentMimeType, expectedAttachment2.getMimeType());
    
    // Test remove message
    mailService_.removeMessages(username, accountImap.getId(), Arrays.asList(expectedMessage5), true);
    Message expectedMessage7 = mailService_.getMessageById(username, accountImap.getId(), expectedMessage5.getId());
    assertNull(expectedMessage7);
    
    // Remove account
    mailService_.removeAccount(username, accountImap.getId());
  }
  
  public void testDelegateAccount() throws Exception {
    Account accountPop = createAccountObj(Utils.POP3);
    mailService_.createAccount(username, accountPop);
    // root delegate his account for demo with read only permission
    mailService_.delegateAccount(username, receiver, accountPop.getId(), Utils.READ_ONLY);
    assertEquals(1, mailService_.getDelegatedAccounts(receiver).size());

    accountPop = mailService_.getDelegatedAccount(receiver, accountPop.getId());
    assertEquals(Utils.READ_ONLY, accountPop.getPermissions().get(receiver));

    Account accImap = createAccountObj(Utils.IMAP);
    mailService_.createAccount(username, accImap);
    // root delegate his account for demo with send and receive permission
    mailService_.delegateAccount(username, receiver, accImap.getId(), Utils.SEND_RECIEVE);
    assertEquals(2, mailService_.getDelegatedAccounts(receiver).size());

    accImap = mailService_.getDelegatedAccount(receiver, accImap.getId());
    assertEquals(Utils.SEND_RECIEVE, accImap.getPermissions().get(receiver));
    assertEquals(username, accImap.getDelegateFrom());

    mailService_.removeDelegateAccount(username, receiver, accImap.getId());
    accImap = mailService_.getAccountById(username, accImap.getId());
    assertEquals(null, accImap.getPermissions().get(receiver));

    assertEquals(1, mailService_.getDelegatedAccounts(receiver).size());

    // indicate test remove account also remove delegate references
    mailService_.removeAccount(username, accountPop.getId());
    assertEquals(0, mailService_.getDelegatedAccounts(receiver).size());
  }
  
  public void testFilter() {
    try {
      Account accountPop = createAccountObj(Utils.POP3);
      mailService_.createAccount(username, accountPop);
      
      Folder folder = createFolder(accountPop.getId(), "folderId", "folderName", "folderUrl");
      
      // Create filter data
      String filterName = "testFilter";
      String from = "abc@gmail.com";
      int fromCondition = Utils.CONDITION_CONTAIN;
      String to = "xyz@gmail.com";
      int toCondition = Utils.CONDITION_CONTAIN;
      String subject = "mnh";
      int subjectCondition = Utils.CONDITION_CONTAIN;
      String body = "body to filter";
      int bodyCondition = Utils.CONDITION_CONTAIN;
      String applyFolder = folder.getId();
      String applyTag = "";
      
      // Create and save filter
      MessageFilter filter = new MessageFilter(filterName);       
      filter.setAccountId(accountPop.getId());
      filter.setFrom(from);
      filter.setFromCondition(fromCondition);
      filter.setTo(to);
      filter.setToCondition(toCondition);
      filter.setSubject(subject);
      filter.setSubjectCondition(subjectCondition);
      filter.setBody(body);
      filter.setBodyCondition(bodyCondition);
      filter.setApplyFolder(applyFolder);
      filter.setApplyTag(applyTag);
      mailService_.saveFilter(username, accountPop.getId(), filter, true);
      
      // Test getFilterById
      MessageFilter expectedFilter = mailService_.getFilterById(username, accountPop.getId(), filter.getId());
      assertNotNull(expectedFilter);
      assertEquals(filterName, expectedFilter.getName());
      assertEquals(from, expectedFilter.getFrom());
      assertEquals(fromCondition, expectedFilter.getFromCondition());
      assertEquals(to, expectedFilter.getTo());
      assertEquals(toCondition, expectedFilter.getToCondition());
      assertEquals(subject, expectedFilter.getSubject());
      assertEquals(subjectCondition, expectedFilter.getSubjectCondition());
      assertEquals(body, expectedFilter.getBody());
      assertEquals(bodyCondition, expectedFilter.getBodyCondition());
      assertEquals(applyFolder, expectedFilter.getApplyFolder());
      assertEquals(applyTag, expectedFilter.getApplyTag());
      
      // Test getFilters
      List<MessageFilter> filterList = mailService_.getFilters(username, accountPop.getId());
      assertNotNull(filterList);
      assertEquals(1, filterList.size());
      
      // Test getListOfMessageIds
      List<String> messageIdList = mailService_.getListOfMessageIds(username, filter);
      assertNotNull(messageIdList);
      assertEquals(0, messageIdList.size());
      
      // Test execActionFilter
      storage_.execActionFilter(username, accountPop.getId(), Calendar.getInstance());
      
      // Test removeFilter
      mailService_.removeFilter(username, accountPop.getId(), expectedFilter.getId());
      List<MessageFilter> filterList1 = mailService_.getFilters(username, accountPop.getId());
      assertNotNull(filterList1);
      assertEquals(0, filterList1.size());
      
      // Remove account
      mailService_.removeAccount(username, accountPop.getId());
    } catch (Exception ex) {
      fail();
    }
  }
  
  public void testSpamFilter() {
    try {
      Account accountPop = createAccountObj(Utils.POP3);
      mailService_.createAccount(username, accountPop);
      
      String sender = "abc@gmail.com";
      
      SpamFilter filter = new SpamFilter();
      filter.setSenders(new String[] {sender});
      mailService_.saveSpamFilter(username, accountPop.getId(), filter);
      
      SpamFilter expectedFilter = mailService_.getSpamFilter(username, accountPop.getId());
      assertNotNull(expectedFilter);
      String[] senders =  expectedFilter.getSenders();
      assertNotNull(senders);
      assertEquals(1, senders.length);
      assertEquals(sender, senders[0]);
    } catch (Exception ex) {
      fail();
    }
  }
  
  public void testTag() {
    try {
      Account accountPop = createAccountObj(Utils.POP3);
      mailService_.createAccount(username, accountPop);
      
      Folder folder = createFolder(accountPop.getId(), "folderId", "folderName", "folderUrl");
      
      StringBuffer sbBody = new StringBuffer();
      sbBody.append("<b>Hello</b>").append("<br/>").append(Calendar.getInstance().getTime().toString());
      String messageBody = sbBody.toString();
      String messageSubject = "Welcome message";
      String messageContentType = TEXT_HTML;
      String[] messageFolderIds = new String[] {folder.getId()};
      Date messageReceivedDate = new Date();
      
      // Create and save message
      Message message = new Message();
      message.setContentType(messageContentType);
      message.setSubject(messageSubject);
      message.setFrom(accountPop.getEmailAddress());
      message.setMessageTo(accountPop.getEmailAddress());
      message.setMessageBody(messageBody);
      message.setFolders(messageFolderIds);
      message.setReceivedDate(messageReceivedDate);
      mailService_.saveMessage(username, accountPop.getId(), folder.getPath(), message, true);
      
      Message expectedMessage1 = mailService_.getMessageById(username, accountPop.getId(), message.getId());
      
      // Create tag data
      String tagId = "tagId";
      String tagName = "tagName";
      String tagDescription = "tagDescription";
      String tagColor = "WHITE";
      
      String newTagName = "newTagName";
      String newTagDesciption = "newTagDesciption";
      String newTagColor = "BLACK";
      
      // Create and save tag
      Tag tag = new Tag();
      tag.setId(tagId);
      tag.setName(tagName);
      tag.setDescription(tagDescription);
      tag.setColor(tagColor);
      mailService_.addTag(username, accountPop.getId(), tag);
      
      // Test getTag
      Tag expectedTag = mailService_.getTag(username, accountPop.getId(), tag.getId());
      assertNotNull(expectedTag);
      assertEquals(tagName, expectedTag.getName());
      assertEquals(tagDescription, expectedTag.getDescription());
      assertEquals(tagColor, expectedTag.getColor());
      
      // Test update tag
      expectedTag.setName(newTagName);
      expectedTag.setDescription(newTagDesciption);
      expectedTag.setColor(newTagColor);
      mailService_.updateTag(username, accountPop.getId(), expectedTag);
      
      // Test gettags
      List<Tag> tagList = mailService_.getTags(username, accountPop.getId());
      assertNotNull(tagList);
      assertEquals(1, tagList.size());
      Tag expectedTag1 = tagList.get(0);
      assertEquals(tagId, expectedTag1.getId());
      assertEquals(newTagName, expectedTag1.getName());
      assertEquals(newTagDesciption, expectedTag1.getDescription());
      assertEquals(newTagColor, expectedTag1.getColor());
      
      // Test addTag
      mailService_.addTag(username, accountPop.getId(), Arrays.asList(expectedMessage1), Arrays.asList(expectedTag1));
      
      // Test getMessageByTag
      List<Message> messageList = mailService_.getMessageByTag(username, accountPop.getId(), expectedTag1.getId());
      assertNotNull(messageList);
      assertEquals(1, messageList.size());
      assertEquals(expectedMessage1.getId(), messageList.get(0).getId());
      
      // Test removeTagsInMessages
      mailService_.removeTagsInMessages(username, accountPop.getId(), Arrays.asList(expectedMessage1),Arrays.asList(expectedTag1.getId()));
      List<Message> messageList1 = mailService_.getMessageByTag(username, accountPop.getId(), expectedTag1.getId());
      assertNotNull(messageList1);
      assertEquals(0, messageList1.size());
      
      // Test remove tag
      mailService_.removeTag(username, accountPop.getId(), expectedTag1.getId());
      List<Tag> tagList1 = mailService_.getTags(username, accountPop.getId());
      assertNotNull(tagList1);
      assertEquals(0, tagList1.size());
      
    } catch (Exception ex) {
      fail();
    }
  }
  
  public void testMailSetting() {
    try {
      Account accountPop = createAccountObj(Utils.POP3);
      mailService_.createAccount(username, accountPop);
      
      long numberMsgPerPage = 50;
      boolean formatAsOriginal = true;
      boolean replyWithAtt = true;
      boolean forwardWithAtt = true;
      String prefixMsgWith = "abc";
      long periodCheckAuto = Utils.PRIORITY_HIGH;
      String defaultAccount = accountPop.getId();
      boolean useWysiwyg = true;
      boolean saveMsgInSent = true;
      long layout = MailSetting.HORIZONTAL_LAYOUT;
      long sendReceipt = MailSetting.SEND_RECEIPT_ASKSME;
      
      MailSetting setting = new MailSetting();
      setting.setNumberMsgPerPage(numberMsgPerPage);
      setting.setFormatAsOriginal(formatAsOriginal);
      setting.setReplyWithAttach(replyWithAtt);
      setting.setForwardWithAtt(forwardWithAtt);
      setting.setPrefixMessageWith(prefixMsgWith);
      setting.setPeriodCheckAuto(periodCheckAuto);
      setting.setDefaultAccount(defaultAccount);
      setting.setUseWysiwyg(useWysiwyg);
      setting.setSaveMessageInSent(saveMsgInSent);
      setting.setLayout(layout);
      setting.setSendReturnReceipt(sendReceipt);
      mailService_.saveMailSetting(username, setting);
      
      MailSetting expectedSetting = mailService_.getMailSetting(username);
      assertNotNull(expectedSetting);
      assertEquals(numberMsgPerPage, expectedSetting.getNumberMsgPerPage());
      assertEquals(formatAsOriginal, expectedSetting.formatAsOriginal());
      assertEquals(replyWithAtt, expectedSetting.replyWithAttach());
      assertEquals(forwardWithAtt, expectedSetting.forwardWithAtt());
      assertEquals(prefixMsgWith, expectedSetting.getPrefixMessageWith());
      assertEquals(periodCheckAuto, expectedSetting.getPeriodCheckAuto());
      assertEquals(defaultAccount, expectedSetting.getDefaultAccount());
      assertEquals(useWysiwyg, expectedSetting.useWysiwyg());
      assertEquals(saveMsgInSent, expectedSetting.saveMessageInSent());
      assertEquals(layout, expectedSetting.getLayout());
      assertEquals(sendReceipt, expectedSetting.getSendReturnReceipt());
    } catch (Exception ex) {
      fail();
    }
  }
  
  private Folder createFolder(String accountId, String id, String name, String urlName) {
    Folder folder = new Folder();
    folder.setId(id);
    folder.setName(name);
    folder.setURLName(urlName);
    try {
      mailService_.saveFolder(username, accountId, folder);
    } catch (Exception e) {
      fail();
    }
    return folder;
  }
  
  private Folder createSubFolder(String accountId, String parentId, String id, String name, String urlName) {
    Folder subFolder = new Folder();
    subFolder.setId(id);
    subFolder.setName(name);
    subFolder.setURLName(urlName);
    try {
      mailService_.saveFolder(username, accountId, parentId, subFolder);
    } catch (Exception e) {
      fail();
    }
    return subFolder;
  }
}
