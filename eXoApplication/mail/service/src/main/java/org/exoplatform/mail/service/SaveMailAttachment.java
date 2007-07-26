/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 9, 2007  
 * 
 * wrong location , SaveMailAttachment should be SavedMailAttachment
 */
public class SaveMailAttachment extends Attachment{
  
  private InputStream inputStream;
  
  public InputStream getInputStream() { return inputStream ; }
  public void setInputStream(InputStream is) { inputStream = is ; }
  
  @Override
  public InputStream getInputStream(Session session) throws Exception {
    Node attachment ;
    try{
      attachment = (Node)session.getItem(getId()) ;      
    }catch (ItemNotFoundException e) {
      return null ;
    }
    return attachment.getNode("jcr:content").getProperty("jcr:data").getStream() ;
  }
}
