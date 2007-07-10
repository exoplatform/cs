/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import javax.jcr.Node;

import org.exoplatform.mail.service.Account;

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

    //assertNotNull(mailHomeNode_) ;
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
    assertNotNull(mailService_.getAccountById("hungnguyen", "myId")) ;
    assertEquals("my sign", mailService_.getAccountById("hungnguyen", "myId").getSignature());
    

    //update account
    myaccount.setLabel("new gmail");
    mailService_.updateAccount("hungnguyen", myaccount);
    //assert account updated
    assertEquals("new gmail", mailService_.getAccountById("hungnguyen", "myId").getLabel());
    
    //delete account
    mailService_.removeAccount("hungnguyen", myaccount);
    //assert account deleted
    assertNull(mailService_.getAccountById("hungnguyen", "myId"));
    

    Node account = rootNode_.addNode("account1", "exo:account") ;
    rootNode_.save() ;

  }
}