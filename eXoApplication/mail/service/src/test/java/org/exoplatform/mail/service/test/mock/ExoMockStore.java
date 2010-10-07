package org.exoplatform.mail.service.test.mock;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;

import org.jvnet.mock_javamail.Aliases;
import org.jvnet.mock_javamail.Mailbox;

import com.sun.mail.imap.IMAPStore;

public class ExoMockStore extends IMAPStore {
  private MockImapFolder folder;

  public ExoMockStore(Session session, URLName url) {
    super(session, url);
  }

  protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
    String address = user + '@' + host;
    Mailbox mailbox = Mailbox.get(Aliases.getInstance().resolve(address));
    folder = new MockImapFolder(this, mailbox);
    if (mailbox.isError())
      throw new MessagingException("Simulated error connecting to " + address);
    return true;
  }

  public Folder getDefaultFolder() throws MessagingException {
    return folder;
  }

  public Folder getFolder(String name) throws MessagingException {
    return folder;
  }

  public Folder getFolder(URLName url) throws MessagingException {
    return folder;
  }

}
