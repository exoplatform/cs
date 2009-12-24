


package org.exoplatform.mail.service.impl;

import java.io.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * A special MimeMessage object that contains only message headers,
 * no content.  Used to represent the MIME type text/rfc822-headers.
 */
public class MessageHeaders extends MimeMessage {

  /**
   * Construct a MessageHeaders object.
   */
  public MessageHeaders() throws MessagingException {
    super((Session)null);
    content = new byte[0];
  }

  /**
   * Constructs a MessageHeaders object from the given InputStream.
   *
   * @param	is	InputStream
   */
  public MessageHeaders(InputStream is) throws MessagingException {
    super(null, is);
    content = new byte[0];
  }

  /**
   * Constructs a MessageHeaders object using the given InternetHeaders.
   *
   * @param	headers	InternetHeaders to use
   */
  public MessageHeaders(InternetHeaders headers) throws MessagingException {
    super((Session)null);
    this.headers = headers;
    content = new byte[0];
  }

  /**
   * Return the size of this message.
   * Always returns zero.
   */
  public int getSize() {
    return 0;
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(content);
  }

  protected InputStream getContentStream() {
    return new ByteArrayInputStream(content);
  }

  /**
   * Can't set any content for a MessageHeaders object.
   *
   * @exception	MessagingException	always
   */
  public void setDataHandler(DataHandler dh) throws MessagingException {
    throw new MessagingException("Can't set content for MessageHeaders");
  }

}
