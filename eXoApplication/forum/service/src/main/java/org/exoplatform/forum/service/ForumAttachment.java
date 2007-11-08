/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class ForumAttachment {
  private String id ;
  private String fileName ;
  private String mimeType ;
  private String workspace ;
  private InputStream inputStream ;
  
  public String getId() { return id ; }
  public void   setId(String s) { id = s ; }
  
  public String getWorkspace() { return workspace ; }
  public void setWorkspace(String ws) { workspace = ws ; }
  
  public String getFileName()  { return fileName ; }
  public void   setFileName(String s) { fileName = s ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String s) { mimeType = s ;}
  
  public void setInputStream(InputStream input) throws Exception {
    inputStream = input ;
  }
  public InputStream getInputStream() throws Exception {
    if(inputStream != null) return inputStream ;
    Node attachment ;
    try{
      attachment = (Node)getSesison().getItem(getId()) ;      
    }catch (ItemNotFoundException e) {
      return null ;
    }
    return attachment.getNode("jcr:content").getProperty("jcr:data").getStream() ;
  }
  
  private Session getSesison()throws Exception {
    RepositoryService repoService = (RepositoryService)PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class) ;
    return repoService.getDefaultRepository().getSystemSession(workspace) ;
  }
  
}
