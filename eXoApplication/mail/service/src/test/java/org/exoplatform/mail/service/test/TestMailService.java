/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.Tag;

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
   
    Account myaccount = createNewAccount() ;
    
    //assertNotNull(mailHomeNode_) ;
    //Add new account
    
    mailService_.createAccount("hungnguyen", myaccount) ;
    //assert added account
    assertNotNull(mailService_.getAccountById("hungnguyen", myaccount.getId())) ;
    assertEquals("my sign", mailService_.getAccountById("hungnguyen", myaccount.getId()).getSignature());
    List<Account> accounts = mailService_.getAccounts("hungnguyen") ;
    assertEquals(accounts.size(), 1) ;
    

    //update account
    myaccount.setLabel("new gmail");
    mailService_.updateAccount("hungnguyen", myaccount);
    //assert account updated
    assertEquals("new gmail", mailService_.getAccountById("hungnguyen", myaccount.getId()).getLabel());
    
    //delete account
    //mailService_.removeAccount("hungnguyen", myaccount);
    //assert account deleted
    //assertNull(mailService_.getAccountById("hungnguyen", "myId"));
    
    
    //create folder
    Folder folder = new Folder();
    folder.setId("home");
    folder.setLabel("homefolder");
    folder.setName("INBOX");
    folder.setNumberOfUnreadMessage(0);
    mailService_.saveUserFolder("hungnguyen", myaccount.getId(), folder);
    // assert folder created
    assertNotNull(mailService_.getFolder("hungnguyen", myaccount.getId(), "INBOX"));

    // update folder
    folder.setLabel("Inbox folder");
    mailService_.saveUserFolder("hungnguyen", myaccount.getId(), folder);
    // assert folder modified
    assertEquals("Inbox folder", mailService_.getFolder("hungnguyen",myaccount.getId(), "INBOX").getLabel());
    
    // delete folder
    //mailService_.removeUserFolder("hungnguyen", myaccount, folder);
    // assert folder is deleted
    //assertNull(mailService_.getFolder("hungnguyen", "myId", "INBOX"));
    
    //  create mail server config
//    MailServerConfiguration conf = new MailServerConfiguration();
    
//    myaccount.setConfiguration(conf);
    myaccount.setServerProperty("folder",folder.getName());
    mailService_.updateAccount("hungnguyen", myaccount);
    
    // get mail
    int nbOfNewMail = mailService_.checkNewMessage("hungnguyen", myaccount);
    assertTrue(nbOfNewMail > -1);
    MessageFilter filter = new MessageFilter("filter by folder "+folder);
    String[] folders = {folder.getName()};
    filter.setFolder(folders);
    filter.setAccountId(myaccount.getId());
    List<MessageHeader> newMsg = mailService_.getMessages("hungnguyen", filter);
    System.out.println("[Total] : " + newMsg.size() + " message(s)") ;
    Iterator<MessageHeader> it = newMsg.iterator();
    while (it.hasNext()) {
      Message msg = (Message)it.next();
      System.out.println("---------------START--------------------------");
      System.out.println("[Subject]  : " + msg.getSubject());
      System.out.println("[Content]  : " + msg.getMessageBody());
      List<Attachment> filesAttached = msg.getAttachments();
      System.out.println("[Attachments] : "+filesAttached.size()+" file(s)");
      Iterator<Attachment> itFiles = filesAttached.iterator();
      while (itFiles.hasNext()) {
        System.out.println("\t________START FILE___________");
        BufferAttachment file = (BufferAttachment)itFiles.next();
        System.out.println("\t[Attached] : " + file.getName());
        System.out.println("\t[Attached Content]");
        if (file.getMimeType().equals("text/plain") || file.getMimeType().equals("text/html")) {
          String line = null;
          BufferedReader in
            = new BufferedReader(new InputStreamReader(file.getInputStream()));
          while ((line = in.readLine()) != null) {
            System.out.println("\t"+line);
          }
        }
        System.out.println("\t__________END FILE________");
      }
      System.out.println("----------------END-------------------------");
    } 
     
    // create message
    
    System.out.println("----------------SENDING MAIL-------------------");
     Message message = createNewMessage("Test Message" , myaccount.getId()) ;
     mailService_.sendMessage("hungnguyen", message) ;
     System.out.println("[Subject]  : " + message.getSubject());
     System.out.println("[Content]  : " + message.getMessageBody());
     System.out.println("---------------- MAIL SENT-------------------");
  }
  
  private Account createNewAccount() {
    Account myaccount = new Account();
    myaccount.setLabel("My Google Mail");
    myaccount.setUserDisplayName("Exo Admin");
    myaccount.setEmailAddress("exomailtest@gmail.com");
    myaccount.setEmailReplyAddress("exomailtest@gmail.com");
    myaccount.setSignature("my sign");
    myaccount.setDescription("No description ...");
    myaccount.setServerProperty("username", "exomailtest@gmail.com");
    myaccount.setServerProperty("password", "exoadmin");
    myaccount.setServerProperty("host", "pop.gmail.com");
    myaccount.setServerProperty("port", "995"); // POP3 : 110, POP3 (SSL) : 995,
    // IMAP : 143, IMAP (SSL) : 993
    myaccount.setServerProperty("protocol", "pop3"); // pop3 or imap
    myaccount.setServerProperty("ssl", "true");

    myaccount.setServerProperty("mail.smtp.user", "exomailtest@gmail.com");
    myaccount.setServerProperty("mail.smtp.host", "smtp.gmail.com");
    myaccount.setServerProperty("mail.smtp.port", "465");
    myaccount.setServerProperty("ssl", "true");
    myaccount.setServerProperty("mail.smtp.debug", "true");
    myaccount.setServerProperty("mail.debug", "true");
    myaccount.setServerProperty("mail.smtp.starttls.enable", "true");
    myaccount.setServerProperty("mail.smtp.auth", "true");
    myaccount.setServerProperty("mail.smtp.socketFactory.port", "465");
    myaccount.setServerProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    myaccount.setServerProperty("mail.smtp.socketFactory.fallback", "false");

    return myaccount;
  }
  
  private Message createNewMessage(String subject, String accountId) {
    Message myMsg = new Message();
    myMsg.setFrom("nam.phung@exoplatform.com");
    myMsg.setSubject(subject);
    myMsg.setMessageTo("nam.phung@exoplatform.com");
    myMsg.setMessageCc("nam.phung@exoplatform.com");
    myMsg.setMessageBcc("nam.phung@exoplatform.com");
    myMsg.setMessageBody("This is a message for test !");
    myMsg.setAttachements(new ArrayList<Attachment>());
    myMsg.setAccountId(accountId);
    myMsg.setSendDate(new Date());
    return myMsg;
  }

  public void testTag() throws Exception{
    Account myaccount = createNewAccount();
    mailService_.createAccount("namphung", myaccount) ;
    assertNotNull(mailService_.getAccountById("namphung", myaccount.getId()));
    
    Message myMessage = createNewMessage( "Test Message 1", myaccount.getId());
    mailService_.saveMessage("namphung", myaccount.getId(), myMessage, true);
    Message myMessage2 = createNewMessage( "Test Message 2", myaccount.getId());
    mailService_.saveMessage("namphung", myaccount.getId(), myMessage2, true);
    
    List<String> listMessage = new ArrayList<String>();
    listMessage.add(myMessage.getId());
    listMessage.add(myMessage2.getId());
    
    Tag tag = new Tag();
    tag.setName("exo");
    mailService_.addTag("namphung", myaccount.getId(), listMessage, tag);
    Tag tag2 = new Tag();
    tag2.setName("xwiki");
    mailService_.addTag("namphung", myaccount.getId(), listMessage, tag2);
    Tag tag3 = new Tag();
    tag3.setName("myproject");
    listMessage.remove(0);
    mailService_.addTag("namphung", myaccount.getId(), listMessage, tag3);
    
    // assert number of tags.
    assertEquals("Number of tag is incorrect !", 3, mailService_.getTags("namphung", myaccount.getId()).size());
    
    // assert tag's message
    assertEquals("Number of messages in tag 'exo' is incorrect !", 2, mailService_.getMessageByTag("namphung", myaccount.getId(), tag.getName()).size());
    assertEquals("Number of messages in tag 'myproject' is incorrect !", 1, mailService_.getMessageByTag("namphung", myaccount.getId(), tag3.getName()).size());
    
    // assert message's tags
    assertEquals("Number of tag in tag 'myMessage' is incorrect !", 2, mailService_.getMessageByTag("namphung", myaccount.getId(), tag.getName()).get(0).getTags().length);
    assertEquals("Number of tag in tag 'myMessage2' is incorrect !", 3, mailService_.getMessageByTag("namphung", myaccount.getId(), tag.getName()).get(1).getTags().length);
    
    // assert remove tag in a message
//    myMessage = mailService_.getMessageByTag("namphung", myaccount.getId(), tag.getName()).get(0);
//    List<String> t = new ArrayList<String>();
//    t.add(tag.getName());  
//    mailService_.removeMessageTag("namphung", myaccount.getId(), listMessage, t);
//    myMessage = mailService_.getMessageByTag("namphung", myaccount.getId(), tag2.getName()).get(0);
//    assertEquals(1, myMessage.getTags().length);
    
    // assert remove tag
//    mailService_.removeTag("namphung", myaccount.getId(), tag2.getName());
//    assertEquals("Number of tags after remove tag2 is invalid !", 2,  mailService_.getTags("namphung", myaccount.getId()).size());
//    assertEquals("Number of tag in tag 'myMessage2' after remove tag2 is incorrect !", 2, mailService_.getMessageByTag("namphung", myaccount.getId(), tag3.getName()).get(0).getTags().length);
       
    // delete account
  }  
}