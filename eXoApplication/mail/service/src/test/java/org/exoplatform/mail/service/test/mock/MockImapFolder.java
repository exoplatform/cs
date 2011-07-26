package org.exoplatform.mail.service.test.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Flags.Flag;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;

import org.jvnet.mock_javamail.Mailbox;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.Rights;
import com.sun.mail.imap.protocol.IMAPProtocol;

public class MockImapFolder extends IMAPFolder {
  private final Mailbox                mailbox;
  private final Random random = new Random();

  private final HashMap<Long, Message> messageMapByUUID = new HashMap<Long, Message>();

  int                                  i                = 0;

  protected MockImapFolder(ExoMockStore store, Mailbox mailbox) {
    super("INBOX", ';', store, false);
    this.mailbox = mailbox;
  }

  public String getName() {
    return "INBOX";
  }

  public String getFullName() {
    return "INBOX";
  }

  public Folder getParent() throws MessagingException {
    return null;
  }

  public boolean exists() throws MessagingException {
    return true;
  }

  public Folder[] list(String pattern) throws MessagingException {
    return new Folder[0];
  }

  public char getSeparator() throws MessagingException {
    return '/';
  }

  public int getType() throws MessagingException {
    return HOLDS_MESSAGES;
  }

  public boolean create(int type) throws MessagingException {
    return false;
  }

  public boolean hasNewMessages() throws MessagingException {
    return mailbox.getNewMessageCount() > 0;
  }

  public Folder getFolder(String name) throws MessagingException {
    // just use the same folder no matter which folder the caller asks for.
    return this;
  }

  public boolean delete(boolean recurse) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  public boolean renameTo(Folder f) throws MessagingException {
    return true;
  }

  public void open(int mode) throws MessagingException {
    // always succeed
  }

  public void close(boolean expunge) throws MessagingException {
    if (expunge)
      expunge();
  }

  public boolean isOpen() {
    return true;
  }

  public Flags getPermanentFlags() {
    return null;
  }

  public int getMessageCount() throws MessagingException {
    return mailbox.size();
  }

  @Override
  public int getNewMessageCount() throws MessagingException {
    return mailbox.getNewMessageCount();
  }

  public Message getMessage(int msgnum) throws MessagingException {
    return mailbox.get(msgnum - 1); // 1-origin!? please.
  }

  @Override
  public Message[] getMessages(int low, int high) throws MessagingException {
    List<Message> messages = new ArrayList<Message>();
    for (int i = low; i <= high; i++) {
      Message m = mailbox.get(i);
      messages.add(m);
    }
    return messages.toArray(new Message[messages.size()]);
  }

  public void appendMessages(Message[] msgs) throws MessagingException {
    mailbox.addAll(Arrays.asList(msgs));
  }

  public Message[] expunge() throws MessagingException {
    List<Message> expunged = new ArrayList<Message>();
    for (Message msg : mailbox) {
      if (msg.getFlags().contains(Flag.DELETED))
        expunged.add(msg);
    }
    mailbox.removeAll(expunged);
    return expunged.toArray(new Message[expunged.size()]);
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#addACL(com.sun.mail.imap.ACL)
   */
  @Override
  public void addACL(ACL acl) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @seecom.sun.mail.imap.IMAPFolder#addMessageCountListener(javax.mail.event. MessageCountListener)
   */
  @Override
  public synchronized void addMessageCountListener(MessageCountListener l) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#addMessages(javax.mail.Message[])
   */
  @Override
  public synchronized Message[] addMessages(Message[] msgs) throws MessagingException {
    mailbox.addAll(Arrays.asList(msgs));
    for (Message msg : msgs) {
      messageMapByUUID.put((long) ++i, msg);
    }
    return msgs;
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#addRights(com.sun.mail.imap.ACL)
   */
  @Override
  public void addRights(ACL acl) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#appendUIDMessages(javax.mail.Message[])
   */
  @Override
  public synchronized AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#copyMessages(javax.mail.Message[], javax.mail.Folder)
   */
  @Override
  public synchronized void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @seecom.sun.mail.imap.IMAPFolder#doCommand(com.sun.mail.imap.IMAPFolder. ProtocolCommand)
   */
  @Override
  public Object doCommand(ProtocolCommand cmd) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @seecom.sun.mail.imap.IMAPFolder#doCommandIgnoreFailure(com.sun.mail.imap. IMAPFolder.ProtocolCommand)
   */
  @Override
  public Object doCommandIgnoreFailure(ProtocolCommand cmd) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#doOptionalCommand(java.lang.String, com.sun.mail.imap.IMAPFolder.ProtocolCommand)
   */
  @Override
  public Object doOptionalCommand(String err, ProtocolCommand cmd) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#doProtocolCommand(com.sun.mail.imap.IMAPFolder .ProtocolCommand)
   */
  @Override
  protected Object doProtocolCommand(ProtocolCommand cmd) throws ProtocolException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#expunge(javax.mail.Message[])
   */
  @Override
  public synchronized Message[] expunge(Message[] msgs) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#fetch(javax.mail.Message[], javax.mail.FetchProfile)
   */
  @Override
  public synchronized void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#forceClose()
   */
  @Override
  public synchronized void forceClose() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getACL()
   */
  @Override
  public ACL[] getACL() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getAttributes()
   */
  @Override
  public synchronized String[] getAttributes() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getDeletedMessageCount()
   */
  @Override
  public synchronized int getDeletedMessageCount() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getMessageByUID(long)
   */
  @Override
  public synchronized Message getMessageByUID(long uid) throws MessagingException {
    return messageMapByUUID.get(uid);
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getMessagesByUID(long, long)
   */
  @Override
  public synchronized Message[] getMessagesByUID(long start, long end) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getMessagesByUID(long[])
   */
  @Override
  public synchronized Message[] getMessagesByUID(long[] uids) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getQuota()
   */
  @Override
  public Quota[] getQuota() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getStoreProtocol()
   */
  @Override
  protected synchronized IMAPProtocol getStoreProtocol() throws ProtocolException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getUID(javax.mail.Message)
   */
  @Override
  public synchronized long getUID(Message message) throws MessagingException {
    Iterator<Entry<Long, Message>> entries = messageMapByUUID.entrySet().iterator();
    while (entries.hasNext()) {
      Entry<Long, Message> entry = entries.next();
      if (entry.getValue().equals(message)) {
        return entry.getKey();
      }
    }
    throw new MessagingException("could not find specified UID!");
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getUIDNext()
   */
  @Override
  public synchronized long getUIDNext() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getUIDValidity()
   */
  @Override
  public synchronized long getUIDValidity() throws MessagingException {
    return random.nextLong();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#getUnreadMessageCount()
   */
  @Override
  public synchronized int getUnreadMessageCount() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#handleResponse(com.sun.mail.iap.Response)
   */
  @Override
  public void handleResponse(Response r) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#idle()
   */
  @Override
  public void idle() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#isSubscribed()
   */
  @Override
  public synchronized boolean isSubscribed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#listRights(java.lang.String)
   */
  @Override
  public Rights[] listRights(String name) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#listSubscribed(java.lang.String)
   */
  @Override
  public Folder[] listSubscribed(String pattern) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#myRights()
   */
  @Override
  public Rights myRights() throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#releaseStoreProtocol(com.sun.mail.imap.protocol .IMAPProtocol)
   */
  @Override
  protected synchronized void releaseStoreProtocol(IMAPProtocol p) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#removeACL(java.lang.String)
   */
  @Override
  public void removeACL(String name) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#removeRights(com.sun.mail.imap.ACL)
   */
  @Override
  public void removeRights(ACL acl) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#search(javax.mail.search.SearchTerm, javax.mail.Message[])
   */
  @Override
  public synchronized Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#search(javax.mail.search.SearchTerm)
   */
  @Override
  public synchronized Message[] search(SearchTerm term) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#setFlags(javax.mail.Message[], javax.mail.Flags, boolean)
   */
  @Override
  public synchronized void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#setQuota(javax.mail.Quota)
   */
  @Override
  public void setQuota(Quota quota) throws MessagingException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see com.sun.mail.imap.IMAPFolder#setSubscribed(boolean)
   */
  @Override
  public synchronized void setSubscribed(boolean subscribe) throws MessagingException {
    throw new UnsupportedOperationException();
  }

}
