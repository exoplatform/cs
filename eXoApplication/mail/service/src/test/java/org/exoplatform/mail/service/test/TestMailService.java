/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import java.util.Calendar;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;

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
    
    // create message
    Message message = new Message();
    message.setReceivedDate(Calendar.getInstance().getTime());
    message.setId("msg0001");
    message.setSubject("test message");
    message.setMessageTo("philippe@aristote.fr");
    message.setMessageBody("This is a message about to be stored in JCR");
    message.setAccountId("myId");
    String[] folders = {folder.getName()};
    message.setFolders(folders);
    String[] tags = {"test", "jcr", "philippe"};
    message.setTags(tags);
    // save message
    mailService_.saveMessage("hungnguyen", "myId", message, true);
    // assert message created
    assertNotNull(mailService_.getMessageById("hungnguyen", "msg0001", "myId"));
    // assert message searched by tag
    MessageFilter tagFilter = new MessageFilter("tagFilter");
    tagFilter.setTag(tags);
    tagFilter.setAccountId("myId");
    List<MessageHeader> msgs = mailService_.getMessageByFilter("hungnguyen", tagFilter);
    assertTrue(msgs.size() > 0);
    // get messages by folder
    msgs = null;
    msgs = mailService_.getMessageByFolder("hungnguyen", folder, "myId");
    assertTrue(msgs.size() > 0);
    
    // add a tag
    mailService_.addTag("hungnguyen", message, "message");
    String[] newtag = {"message"};
    //assert tag is added
    tagFilter.setTag(newtag);
    msgs = null;
    msgs = mailService_.getMessageByFilter("hungnguyen", tagFilter);
    assertTrue(msgs.size() > 0);
    // remove a tag
    mailService_.removeTag("hungnguyen", myaccount, "message");
    msgs = null;
    msgs = mailService_.getMessageByFilter("hungnguyen", tagFilter);
    assertFalse(msgs.size() > 0);
    
    //Node account = rootNode_.addNode("account1", "exo:account") ;
    rootNode_.save() ;

  }
}