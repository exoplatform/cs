/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.service;

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
public class BufferAttachment extends Attachment{
  
  private InputStream inputStream;
  
  public InputStream getInputStream()throws Exception{
    /*DownloadService downloadService = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
    DownloadResource downloadResource = downloadService.getDownloadResource(getId()) ;
    downloadResource.getInputStream() ;*/
    return inputStream ; 
  }
  public void setInputStream(InputStream is){ inputStream = is ; }
  
}
