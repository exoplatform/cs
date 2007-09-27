/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.io.InputStream;
//import org.exoplatform.download.DownloadResource;
//import org.exoplatform.download.DownloadService;
//import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 9, 2007
 */
public class BufferAttachment extends ContactAttachment {
  
  private InputStream inputStream;
  
  public InputStream getInputStream()throws Exception{
    /*DownloadService downloadService = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
    DownloadResource downloadResource = downloadService.getDownloadResource(getId()) ;
    downloadResource.getInputStream() ;*/
    return inputStream ; 
  }
  public void setInputStream(InputStream is){ inputStream = is ; }
  
}
