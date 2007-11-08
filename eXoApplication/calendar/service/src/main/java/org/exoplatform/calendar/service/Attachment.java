/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.util.Calendar;

import org.exoplatform.services.jcr.util.UUIDGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */

public class Attachment  {
  private String id ;
  private String name ;
  private String mimeType ;
  private long size ;
  private InputStream data ;
  private Calendar lastModified ;
  public Attachment() {
    id =  "Attachment" + UUIDGenerator.generate() ;
  }
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String mimeType_) { this.mimeType = mimeType_ ; }
  
  public long getSize() { return size ; }
  public void setSize(long size_) { this.size = size_ ; }
  
  public String getName() { return name ; }
  public void setName(String name_) { this.name = name_ ; }
  public InputStream getInputStream() {return data ;}
  public void setInputStream(InputStream input) {data = input ;}

  public void setLastModified(Calendar lastModified) {
    this.lastModified = lastModified;
  }

  public Calendar getLastModified() {
    return lastModified;
  }
}