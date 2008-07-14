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
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;

import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import sun.util.calendar.CalendarSystem;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestMailService extends BaseMailTestCase{
  private MailService mailService_ ;
  private SessionProvider sProvider ;
  private  String username = "root" ;


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

  public void testAccount() throws Exception {


    System.out.println("\n\n Test POP Account" );
    Account  account = createAccountObj(Utils.POP3) ;
    String accId = account.getId() ;
    mailService_.createAccount(sProvider, username, account) ;
    System.out.println("\n\n getAccount id " +  accId + " has been saved!" );
    Account getAccount =  mailService_.getAccountById(sProvider, username, accId) ;
    assertNotNull(getAccount) ;

    System.out.println("\n\n Test IMAP Account" );
    Account  account2= createAccountObj(Utils.IMAP) ;
    String accId2 = account2.getId() ;
    mailService_.createAccount(sProvider, username, account2) ;
    System.out.println("\n\n getAccount id " +  accId2 + " has been saved!" );
    Account getAccount2 =  mailService_.getAccountById(sProvider, username, accId2) ;
    assertNotNull(getAccount2) ;

    System.out.println("\n\n Test get default Account" );
    Account defaultAcc = mailService_.getDefaultAccount(sProvider, username) ;
    assertNotNull(defaultAcc) ;
    String defaultId = defaultAcc.getId() ;
    System.out.println("\n\n defaltAcc id " + defaultId);

    System.out.println("\n\n check  message from POP acc");
    List<Message> message =  mailService_.checkNewMessage(sProvider, username, accId) ;
    
    
    assertEquals(message.isEmpty(), true) ;
    System.out.println("\n\n unread messages " + message.size());
    System.out.println("\n\n get message from default acc");
    assertNull(gettMessage(accId)) ;

  }
  public Account createAccountObj(String protocol) {
    Account account = new Account() ;
    Folder folder = new Folder() ;
    folder.setName("inbox") ;
    List<Folder> folders = new ArrayList<Folder>() ;
    folders.add(folder) ;
    account.setCheckedAuto(false) ;
    account.setDefaultFolder(folders) ;
    account.setDescription("Create account") ;
    account.setEmailAddress("exomailtest@gmail.com") ;
    account.setEmailReplyAddress("exomailtest@gmail.com")  ;
    account.setEmptyTrashWhenExit(false) ;
    account.setIncomingFolder("inbox") ;
    if(Utils.POP3.equals(protocol)) account.setIncomingHost("pop.gmail.com") ;
    if(Utils.IMAP.equals(protocol)) account.setIncomingHost("imap.gmail.com") ;
    account.setIncomingPassword("exoadmin") ;
    account.setIncomingPort("995") ;
    account.setIncomingSsl(true) ;
    account.setIncomingUser("exomailtest@gmail.com") ;
    account.setIsSavePassword(true) ;
    account.setLabel("exomail test account") ;
    account.setOutgoingHost("smtp.gmail.com") ;
    account.setOutgoingPort("465") ;
    account.setPlaceSignature("exo mail") ;
    account.setProtocol(protocol) ;
    account.setUserDisplayName("mail") ;
    return  account ;
  } 
  public List<Message> checkMailFormServer(String protocol, String accountId) {
    List<Message> message = null ;

    if(Utils.POP3.equals(protocol)) {
      
    } else if (Utils.IMAP.equals(protocol)) {

    }
    return message ;
  }
  public List<Message> gettMessage(String accountId) throws Exception {
    System.out.println("\n\n getMessage ");
    MessageFilter filter = new MessageFilter("filter1") ;
    filter.setAccountId(accountId) ;
    return mailService_.getMessages(sProvider, username, filter) ;
  }
}