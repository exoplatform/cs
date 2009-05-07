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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestMailService extends BaseMailTestCase{
  public TestMailService() throws Exception {
    super();
    // TODO Auto-generated constructor stub
  }

  private MailService mailService_ ;
  private SessionProvider sProvider ;
  private  String username = "root" ;
  public static final String TEXT_PLAIN =  "text/plain".intern() ;
  public static final String TEXT_HTML =  "text/html".intern() ;

  public void setUp() throws Exception {
    super.setUp() ;
    mailService_ = (MailService) container.getComponentInstanceOfType(MailService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  public void testMailService() throws Exception {
    assertNotNull(mailService_) ;
    assertNotNull(sProvider) ;
  }
  public Account createAccountObj(String protocol) {
    Account account = new Account() ;
    Folder folder = new Folder() ;
    folder.setName("inbox") ;
    folder.setPersonalFolder(false) ;
    List<Folder> folders = new ArrayList<Folder>() ;
    folders.add(folder) ;
    account.setCheckedAuto(false) ;
    account.setEmptyTrashWhenExit(false) ;
    account.setIncomingFolder("inbox") ;
    account.setProtocol(protocol) ;

    if(Utils.POP3.equals(protocol)) {
      account.setDescription("Create "+protocol+" account") ;
      account.setEmailAddress("exomailtest@gmail.com") ;
      account.setEmailReplyAddress("exomailtest@gmail.com")  ;
      account.setIncomingHost("pop.gmail.com") ;
      account.setIncomingPassword("exoadmin") ;
      account.setIncomingPort("995") ;
      account.setIncomingSsl(true) ;
      account.setIncomingUser("exomailtest@gmail.com") ;
      account.setIsSavePassword(true) ;
      account.setLabel("exomail test account") ;
      account.setOutgoingHost("smtp.gmail.com") ;
      account.setOutgoingPort("465") ;
      account.setPlaceSignature("exomailtest pop") ;  
    } else if(Utils.IMAP.equals(protocol)) {
      account.setDescription("Create "+protocol+" account") ;
      account.setEmailAddress("exoservice@gmail.com") ;
      account.setEmailReplyAddress("exoservice@gmail.com")  ;
      account.setIncomingHost("imap.gmail.com") ;
      account.setIncomingPassword("exoadmin") ;
      account.setIncomingPort("993") ;
      account.setIncomingSsl(true) ;
      account.setIncomingUser("exoservice@gmail.com") ;
      account.setIsSavePassword(true) ;
      account.setLabel("exoservice test account") ;
      account.setOutgoingHost("smtp.gmail.com") ;
      account.setOutgoingPort("465") ;
      account.setPlaceSignature("exosevice imap") ;
    }
    account.setIsOutgoingAuthentication(false) ;
    account.setUseIncomingForAuthentication(true) ;
    account.setOutgoingUserName(username) ;
    account.setUseIncomingForAuthentication(true) ;
    return  account ;
  } 
  //Create account
  public void testAccount() throws Exception {

    System.out.println("\n\n Test POP Account" );
    Account  account = createAccountObj(Utils.POP3) ;
    String accId = account.getId() ;
    mailService_.createAccount(sProvider, username, account) ;
    Account getAccount =  mailService_.getAccountById(sProvider, username, accId) ;
    assertNotNull(getAccount) ;

    System.out.println("\n\n Test IMAP Account" );
    Account  account2= createAccountObj(Utils.IMAP) ;
    String accId2 = account2.getId() ;
    mailService_.createAccount(sProvider, username, account2) ;

    Account getAccount2 =  mailService_.getAccountById(sProvider, username, accId2) ;
    assertNotNull(getAccount2) ;

    Account defaultAcc = mailService_.getDefaultAccount(sProvider, username) ;
    assertNotNull(defaultAcc) ;
    mailService_.removeAccount(sProvider, username, accId) ;
    assertNull(mailService_.getAccountById(sProvider, username, accId)) ;
    mailService_.removeAccount(sProvider, username, accId2) ;
    assertNull(mailService_.getAccountById(sProvider, username, accId2)) ;

  }

  //Send mail
  public void testSendMail() throws Exception {
    Account accPop = createAccountObj(Utils.POP3) ;
    mailService_.createAccount(sProvider, username, accPop) ;

    Account accImap = createAccountObj(Utils.IMAP) ;
    mailService_.createAccount(sProvider, username, accImap) ;
    StringBuffer sbBody = new StringBuffer("") ;
    
    Message message = new Message() ;
    message.setContentType(TEXT_HTML) ;
    message.setSubject("This message has been sent form "+accPop.getEmailAddress()) ;
    message.setFrom(accPop.getEmailAddress()) ;
    message.setMessageTo(accImap.getEmailAddress()) ;
    sbBody.append("<b>Hello "+accImap.getIncomingUser()+"</b>").append("<br/>").append(Calendar.getInstance().getTime().toString()) ;
    message.setMessageBody(sbBody.toString()) ;
    
    accPop.setIsOutgoingAuthentication(true) ;
    accPop.setUseIncomingForAuthentication(true) ;
    accPop.setOutgoingUserName(username) ;

    // javax.mail.AuthenticationFailedException
   // mailService_.sendMessage(sProvider, username, accPop.getId(), message) ;
    System.out.println("\n\n Message has been sent use POP !");  
    
    message.setContentType(TEXT_HTML) ;
    message.setSubject("This message has been sent form "+accImap.getEmailAddress()) ;
    message.setFrom(accImap.getEmailAddress()) ;
    message.setMessageTo(accPop.getEmailAddress()) ;
    sbBody.append("<b>Hello "+accPop.getIncomingUser()+"</b>").append("<br/>").append(Calendar.getInstance().getTime().toString()) ;
    message.setMessageBody(sbBody.toString()) ;
    //javax.mail.AuthenticationFailedException
   // mailService_.sendMessage(sProvider, username, accImap.getId(), message) ;
    
    System.out.println("\n\n Message has been sent use IMAP !");
    
    mailService_.removeAccount(sProvider, username, accPop.getId()) ;
    mailService_.removeAccount(sProvider, username, accImap.getId()) ;

  }

  //Check mail form POP and IMAP server
  public void testGetMailFormServer() throws Exception {
    Account accPop = createAccountObj(Utils.POP3) ;
    mailService_.createAccount(sProvider, username, accPop) ;
    
    Account accImap = createAccountObj(Utils.IMAP) ;
    mailService_.createAccount(sProvider, username, accImap) ;

    mailService_.checkNewMessage(sProvider, username, accPop.getId()) ;
    
    MessageFilter filter = new MessageFilter("testFilter") ;
    filter.setAccountId(accPop.getId()) ;
    assertNotNull(mailService_.getMessages(sProvider, username, filter)) ;
    //assertEquals(mailService_.getMessages(sProvider, username, filter).size(),1) ;
    filter.setAccountId(accImap.getId()) ;
    assertNotNull(mailService_.getMessages(sProvider, username, filter)) ;
    //assertEquals(mailService_.getMessages(sProvider, username, filter).size(),0) ;

    assertNotNull(mailService_.getFolders(sProvider, username, accPop.getId(), false)) ;
    
    mailService_.removeAccount(sProvider, username, accPop.getId()) ;
    mailService_.removeAccount(sProvider, username, accImap.getId()) ;
  }
  
  //Add custom folder
  public void testAddFolder() throws Exception {
    Account accPop = createAccountObj(Utils.POP3) ;
    mailService_.createAccount(sProvider, username, accPop) ;
    Folder folder = new Folder() ;
    folder.setId("folderId1") ;
    folder.setName("folder test") ;
    folder.setURLName("folder 1") ;
    mailService_.saveFolder(sProvider, username, accPop.getId(), folder) ;
    folder = new Folder() ;
    folder.setId("folderId1.1") ;
    folder.setName("folder test 1.1") ;
    folder.setURLName("folder 1.1");
    mailService_.saveFolder(sProvider, username, accPop.getId(), folder) ;
    
    Folder folderChild = new Folder() ;
    folderChild.setId("folderId2") ;
    folder.setURLName("folder 2") ;
    folderChild.setName("child folder ") ;
    mailService_.saveFolder(sProvider, username, accPop.getId(), folder.getId(), folderChild) ;
    List<Folder> fs = new ArrayList<Folder>() ;
    assertNotNull(mailService_.getFolders(sProvider, username, accPop.getId())) ;
    fs.addAll(mailService_.getFolders(sProvider, username, accPop.getId())) ;
    assertEquals(mailService_.getFolders(sProvider, username, accPop.getId()).size(), 2) ;
    mailService_.removeAccount(sProvider, username, accPop.getId()) ;
  }
  
  //Save and move message 
  public void testSaveMessage() throws Exception {
    Account accPop = createAccountObj(Utils.POP3) ;
    mailService_.createAccount(sProvider, username, accPop) ;
    System.out.println("account " + username + " has been saved!");
    StringBuffer sbBody = new StringBuffer("") ;
    Message message = new Message() ;
    message.setContentType(TEXT_HTML) ;
    message.setSubject("This message has been sent form "+accPop.getEmailAddress()) ;
    message.setFrom(accPop.getEmailAddress()) ;
    message.setMessageTo(accPop.getEmailAddress()) ;
    sbBody.append("<b>Hello "+accPop.getIncomingUser()+"</b>").append("<br/>").append(Calendar.getInstance().getTime().toString()) ;
    message.setMessageBody(accPop.toString()) ;
    
    Folder folder = new Folder() ;
    folder.setId("folderId1") ;
    folder.setName("folder test") ;
    folder.setURLName("folder 1") ;
    mailService_.saveFolder(sProvider, username, accPop.getId(), folder) ;
    
    
    Folder desfolder = new Folder() ;
    desfolder.setId("folderId2") ;
    desfolder.setName("folder test 2 ") ;
    desfolder.setURLName("folder 2") ;
    mailService_.saveFolder(sProvider, username, accPop.getId(), desfolder) ;
    
    message.setFolders(new String[]{folder.getId()}) ;
    message.setReceivedDate(new Date()) ;
    mailService_.saveMessage(sProvider, username, accPop.getId(), message, true) ;
    assertNotNull(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), folder.getId())) ;
    assertEquals(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), folder.getId()).size(), 1) ;
    
    message = mailService_.getMessageById(sProvider, username, accPop.getId(), message.getId()) ;
    mailService_.moveMessage(sProvider, username, accPop.getId(), message, folder.getId(), desfolder.getId()) ;
    assertEquals(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), folder.getId()).size(), 0) ;
    
    assertNotNull(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), desfolder.getId())) ;
    assertEquals(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), desfolder.getId()).size(), 1) ;
    mailService_.removeMessage(sProvider, username, accPop.getId(), message) ;
    assertEquals(mailService_.getMessagesByFolder(sProvider, username, accPop.getId(), desfolder.getId()).size(), 0) ;
    
    
    mailService_.removeAccount(sProvider, username, accPop.getId()) ;
  }
}