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
 */
public class JCRAttachment extends Attachment{

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
