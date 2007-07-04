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
  private String contactName_ ;
  private String emailAddress_ ;
  private String homePhone_ ;
  private String workPhone_ ;
  private String city_ ;
  private String country_ ;
  private String postalCode_ ;
  private String personalSite_ ;
  private String organization_ ;
  private String jobTitle_ ;
  private String[] groups_ ;
  
  /**
   * The contactName_ of the contact for ex: Hung Nguyen, Nguyen Ke Quang Hung
   * @return the contactName_ of the contact
   */
  public String getContactName()  { return contactName_ ; }
  public void   setContactName(String s) { contactName_ = s ; }
  
  /**
   * The display email address of the contact for ex:  hung.nguyen@exoplatform.com
   * @return The email address of the contact
   */
  public String getEmailAddress() { return emailAddress_ ; }
  public void   setEmailAddress(String s) { emailAddress_ = s ; }
  
  public String getHomePhone() { return homePhone_ ; }
  public void   setHomePhone(String s) { homePhone_ = s ; }
  
  public String getWorkPhone() { return workPhone_ ; }
  public void   setWorkPhone(String s) { workPhone_ = s ; }
  
  public String getCity() { return city_ ; }
  public void   setCity(String s) { city_ = s ; }
  
  public String getCountry() { return country_ ; }
  public void   setCountry(String s) { country_ = s ; }
  
  public String getPostalCode() { return postalCode_ ; }
  public void   setPostalCode(String s) { postalCode_ = s ; }
  
  public String getPersonalSite() { return personalSite_ ; }
  public void   setPersonalSite(String s) { personalSite_ = s ; }
  
  public String getOrganization() { return organization_ ; }
  public void   setOrganization(String s) { organization_ = s ; }
  
  public String getJobTitle() { return jobTitle_ ; }
  public void   setJobTitle(String s) { jobTitle_ = s ; }
  
  public String[] getGroups() { return groups_ ; }
  public void   setGroups(String[] s) { groups_ = s ; }
  
}
