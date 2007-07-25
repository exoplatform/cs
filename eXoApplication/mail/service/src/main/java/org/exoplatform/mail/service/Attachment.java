/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.io.InputStream;

import javax.jcr.Session;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
abstract public class Attachment {
  private String id ;
  private String name ;
  private String mimeType ;
  private long size ;
   
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String mimeType_) { this.mimeType = mimeType_ ; }
  
  public long getSize() { return size ; }
  public void setSize(long size_) { this.size = size_ ; }
  
  public String getName() { return name ; }
  public void setName(String name_) { this.name = name_ ; }
  
  public abstract InputStream getInputStream(Session session) throws Exception ;
}
