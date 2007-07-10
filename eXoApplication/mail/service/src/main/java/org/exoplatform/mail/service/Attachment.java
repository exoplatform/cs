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
  private String id_ ;
  private String name_ ;
  private String mimeType_ ;
  private long size_ ;
   
  public String getId() { return id_ ; }
  public void setId(String id) { id_ = id ; }
  
  public void setMimeType(String mimeType_) { this.mimeType_ = mimeType_ ; }
  public String getMimeType() { return mimeType_ ; }
  
  public void setSize(long size_) { this.size_ = size_ ; }
  public long getSize() { return size_ ; }
  
  public void setName(String name_) { this.name_ = name_ ; }
  public String getName() { return name_ ; }
  
  public abstract InputStream getInputStream(Session session) throws Exception ;
}
