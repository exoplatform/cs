/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class Contact {
  private String id ;
  private String path ;
  private String firstName ;
  private String lastName ;
  private String emailAddress ;
  private String homePhone ;
  private String workPhone ;
  private String homeAddress ;
  private String country ;
  private String postalCode ;
  private String personalSite ;
  private String organization ;
  private String jobTitle ;
  private String companyAddress ;
  private String companySite ;  
  private String[] groups ;
  
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  public String getPath()  { return path ; }
  public void   setPath(String p) { path = p ; }
  
  public String getFirstName()  { return firstName ; }
  public void   setFirstName(String s) { firstName = s ; }
  
  public String getLastName()  { return lastName ; }
  public void   setLastName(String s) { lastName = s ; }
  
  public String getEmailAddress() { return emailAddress ; }
  public void   setEmailAddress(String s) { emailAddress = s ; }
  
  public String getHomePhone() { return homePhone ; }
  public void   setHomePhone(String s) { homePhone = s ; }
  
  public String getWorkPhone() { return workPhone ; }
  public void   setWorkPhone(String s) { workPhone = s ; }
  
  public String getHomeAddress() { return homeAddress ; }
  public void   setHomeAddress(String s) { homeAddress = s ; }
  
  public String getCompanyAddress() { return companyAddress ; }
  public void   setCompanyAddress(String s) { companyAddress = s ; }
  
  public String getCompanySite() { return companySite ; }
  public void   setCompanySite(String s) { companySite = s ; }
  
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
