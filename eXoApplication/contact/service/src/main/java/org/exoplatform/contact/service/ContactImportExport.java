/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Author : Huu-Dung Kieu
 *          huu-dung.kieu@bull.be
 * 16 oct. 07  
 */
public interface ContactImportExport {
  public void importContact(String username, InputStream input, String groupId) throws Exception ;
  public OutputStream exportContact(String username, List<String> contactIds) throws Exception ;
}
