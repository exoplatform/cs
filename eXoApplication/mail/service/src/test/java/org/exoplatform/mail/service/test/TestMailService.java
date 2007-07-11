/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;

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
    //mailService_.removeAccount("hungnguyen", myaccount);
    //assert account deleted
    //assertNull(mailService_.getAccountById("hungnguyen", "myId"));
    
    
    //create folder
    Folder folder = new Folder();
    folder.setId("home");
    folder.setLabel("home folder");
    folder.setName("INBOX");
    folder.setNumberOfUnreadMessage(0);
    mailService_.saveUserFolder("hungnguyen", "myId", folder);
    // assert folder created
    assertNotNull(mailService_.getFolder("hungnguyen", "myId", "INBOX"));

    // update folder
    folder.setLabel("Inbox folder");
    mailService_.saveUserFolder("hungnguyen", "myId", folder);
    // assert folder modified
    assertEquals("Inbox folder", mailService_.getFolder("hungnguyen", "myId", "INBOX").getLabel());
    
    // delete folder
    //mailService_.removeUserFolder("hungnguyen", myaccount, folder);
    // assert folder is deleted
    //assertNull(mailService_.getFolder("hungnguyen", "myId", "INBOX"));
    
    Message message = new Message();
    message.setSubject("test message");
    message.setMessageTo("philippe@aristote.fr");
    message.setMessageBody("This is a message about to be stored in JCR");
    String[] folders = new String[1];
    folders[0] = folder.getName();
    message.setFolders(folders);
    String[] tags = new String[2];
    tags[0] = "test"; tags[1] = "jcr";
    message.setTags(tags);
    
    //Node account = rootNode_.addNode("account1", "exo:account") ;
    rootNode_.save() ;

  }
}