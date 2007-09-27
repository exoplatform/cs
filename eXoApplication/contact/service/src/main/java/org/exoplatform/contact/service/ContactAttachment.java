/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
abstract public class ContactAttachment {
  private String id ;
  private String fileName ;
  private String mimeType ;
  
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }

  public String getFileName()  { return fileName ; }
  public void   setFileName(String s) { fileName = s ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String s) { mimeType = s ;}

  public abstract InputStream getInputStream() throws Exception ;
  
}
