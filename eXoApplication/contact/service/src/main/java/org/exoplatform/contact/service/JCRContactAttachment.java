package org.exoplatform.contact.service;

import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 9, 2007  
 * 
 * TODO: wrong location, rename to JCRMessageAttachment
 */
public class JCRContactAttachment extends ContactAttachment{
  private String workspace ;
  
  public String getWorkspace() { return workspace ; }
  public void setWorkspace(String ws) { workspace = ws ; }
  
  @Override
  public InputStream getInputStream() throws Exception {
    Node attachment ;
    try {
      attachment = (Node)getSesison().getItem(getId()) ;      
    } catch (ItemNotFoundException e) {
      return null ;
    }
    return attachment.getNode("jcr:content").getProperty("jcr:data").getStream() ;
  }
  
  private Session getSesison()throws Exception {
    RepositoryService repoService = (RepositoryService)PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class) ;
    return repoService.getDefaultRepository().getSystemSession(workspace) ;
  }
}
