/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import javax.jcr.Node;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Contact;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestMailService extends BaseMailTestCase{

  public void testMailService() throws Exception {
    assertNotNull(rootNode_) ;
  }
  
  public void testAccount() throws Exception {
    
    assertNotNull(mailHomeNode_) ;
    //Add new account
    Account myaccount = new Account() ;
    myaccount.setId("myId") ;
    myaccount.setLabel("My Google Mail") ;
    myaccount.setUserDisplayName("Hung Nguyen") ;
    myaccount.setEmailAddress("nguyenkequanghung@gmail.com") ;
    myaccount.setEmailReplyAddress("hung.nguyen@exoplatform.com") ;
    myaccount.setSignature("my sign") ;
    myaccount.setDescription("No description ...") ;
    mailService_.createAccount("hungnguyen", myaccount) ;
    
    //assert added account
    assertNull(mailService_.getAccountById("myName", "myId")) ;
    
    Node account = rootNode_.addNode("account1", "exo:account") ;
    rootNode_.save() ;
  }
  
  public void testContact() throws Exception {
    Node mailHomeNode = mailService_.getMailHomeNode("vuduytu") ;
    Node accout = mailHomeNode.addNode("account1", "exo:account") ;
    
    // call addContact(...)
    // prepare Contact object
    mailService_.addContact("vuduytu", new Contact(), true) ;
    // assert(some thing)
    // call getContact(...)
    //assert (some thing)
    
  }
}