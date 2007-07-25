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
  private String contactName ;
  private String emailAddress ;
  private String homePhone ;
  private String workPhone ;
  private String city ;
  private String country ;
  private String postalCode ;
  private String personalSite ;
  private String organization ;
  private String jobTitle ;
  private String[] groups ;
  
  /**
   * The contactName_ of the contact for ex: Hung Nguyen, Nguyen Ke Quang Hung
   * @return the contactName_ of the contact
   */
  public String getContactName()  { return contactName ; }
  public void   setContactName(String s) { contactName = s ; }
  
  /**
   * The display email address of the contact for ex:  hung.nguyen@exoplatform.com
   * @return The email address of the contact
   */
  public String getEmailAddress() { return emailAddress ; }
  public void   setEmailAddress(String s) { emailAddress = s ; }
  
  public String getHomePhone() { return homePhone ; }
  public void   setHomePhone(String s) { homePhone = s ; }
  
  public String getWorkPhone() { return workPhone ; }
  public void   setWorkPhone(String s) { workPhone = s ; }
  
  public String getCity() { return city ; }
  public void   setCity(String s) { city = s ; }
  
  public String getCountry() { return country ; }
  public void   setCountry(String s) { country = s ; }
  
  public String getPostalCode() { return postalCode ; }
  public void   setPostalCode(String s) { postalCode = s ; }
  
  public String getPersonalSite() { return personalSite ; }
  public void   setPersonalSite(String s) { personalSite = s ; }
  
  public String getOrganization() { return organization ; }
  public void   setOrganization(String s) { organization = s ; }
  
  public String getJobTitle() { return jobTitle ; }
  public void   setJobTitle(String s) { jobTitle = s ; }
  
  public String[] getGroups() { return groups ; }
  public void   setGroups(String[] s) { groups = s ; }
  
}
