/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 9, 2007
 */
public class BufferAttachment extends Attachment{
  
  private InputStream inputStream;
  
  public InputStream getInputStream(){ return inputStream ; }
  public void setInputStream(InputStream is){ inputStream = is ; }
  
}
