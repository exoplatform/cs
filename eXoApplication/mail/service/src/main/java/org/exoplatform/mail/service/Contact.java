/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class Contact {
  private String contactDisplayName_ ;
  private String emailAddress_ ;
  
  /**
   * The contactDisplayName_ of the contact for ex: Hung Nguyen, Nguyen Ke Quang Hung
   * @return the contactDisplayName_ of the contact
   */
  public String getContactDisplayName()  { return contactDisplayName_ ; }
  public void   setContactDisplayName(String s) { contactDisplayName_ = s ; }
  
  /**
   * The display email address of the contact for ex:  hung.nguyen@exoplatform.com
   * @return The email address of the contact
   */
  public String getEmailAddress() { return emailAddress_ ; }
  public void   setEmailAddress(String s) { emailAddress_ = s ; }
  
}
