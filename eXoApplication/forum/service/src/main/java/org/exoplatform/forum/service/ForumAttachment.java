/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 10, 2007  
 */
abstract public class ForumAttachment {
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
  //TODO: should not have  Session in the api
  public abstract InputStream getInputStream() throws Exception ;
}
