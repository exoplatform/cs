/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
abstract public class Attachment {
  private String id ;
  private String name ;
  
  abstract InputStream getInputStream() throws Exception  ;

}
