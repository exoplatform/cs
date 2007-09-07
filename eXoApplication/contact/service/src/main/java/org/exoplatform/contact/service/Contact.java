/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import org.exoplatform.services.jcr.util.IdGenerator;

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
  
  private String fullName ;
  private String firstName ;
  private String middleName ;
  private String lastName ;
  private String nickName ;
  private String gender ;
  private String birthday ;
  private String jobTitle ;
  private String emailAddress ;
  
  private String workAddress ;
  private String workCity ;
  private String workState_province ;
  private String workPostalCode ;
  private String workCountry ;
  private String workPhone1 ;
  private String workPhone2 ;
  private String workFax ;
  private String mobilePhone ;
  private String webPage ;
  
  private String exoId ;
  private String googleId ;
  private String msnId ;
  private String aolId ;
  private String yahooId ;
  private String icrId ;
  private String skypeId ;
  private String icqId ;
  
  private String homeAddress ;
  private String homeCity ;
  private String homeState_province ;
  private String homePostalCode ;
  private String homeCountry ;
  private String homePhone1 ;
  private String homePhone2 ;
  private String homeFax ;
  private String personalSite ;
  
  private String note ;
  
  private String[] categories ;
  private String[] tags ;
  private String[] editPermission ;
  
  public Contact() {
    id = "Contact" + IdGenerator.generate() ;
  }
  
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  public String getPath()  { return path ; }
  public void   setPath(String p) { path = p ; }
  
  public String getFullName()  { return fullName ; }
  public void   setFullName(String s) { fullName = s ; }
  
  public String getFirstName()  { return firstName ; }
  public void   setFirstName(String s) { firstName = s ; }
  
  public String getMiddleName()  { return middleName ; }
  public void   setMiddleName(String s) { middleName = s ; }
  
  public String getLastName()  { return lastName ; }
  public void   setLastName(String s) { lastName = s ; }
  
  public String getNickName()  { return nickName ; }
  public void   setNickName(String s) { nickName = s ; }
  
  public String getGender() { return gender ; }
  public void setGender(String s) { gender = s ; }

  public String getBirthday() { return birthday ; }
  public void setBirthday(String s) { birthday = s ; }
  
  public String getJobTitle() { return jobTitle ; }
  public void   setJobTitle(String s) { jobTitle = s ; }
  
  public String getEmailAddress() { return emailAddress ; }
  public void   setEmailAddress(String s) { emailAddress = s ; }
  
  public String getExoId()  { return exoId ; }
  public void   setExoId(String s) { exoId = s ; }
  
  public String getGoogleId()  { return googleId ; }
  public void   setGoogleId(String s) { googleId = s ; }
  
  public String getMsnId()  { return msnId ; }
  public void   setMsnId(String s) { msnId = s ; }
  
  public String getAolId()  { return aolId ; }
  public void   setAolId(String s) { aolId = s ; }
  
  public String getYahooId()  { return yahooId ; }
  public void   setYahooId(String s) { yahooId = s ; }
  
  public String getIcrId()  { return icrId ; }
  public void   setIcrId(String s) { icrId = s ; }
  
  public String getSkypeId()  { return skypeId ; }
  public void   setSkypeId(String s) { skypeId = s ; }
  
  public String getIcqId()  { return icqId ; }
  public void   setIcqId(String s) { icqId = s ; }
  
  public String getHomeAddress() { return homeAddress ; }
  public void   setHomeAddress(String s) { homeAddress = s ; }
  
  public String getHomeCity()  { return homeCity ; }
  public void   setHomeCity(String s) { homeCity = s ; }
  
  public String getHomeState_province()  { return homeState_province ; }
  public void   setHomeState_province(String s) { homeState_province = s ; }
  
  public String getHomePostalCode()  { return homePostalCode ; }
  public void   setHomePostalCode(String s) { homePostalCode = s ; }
  
  public String getHomeCountry()  { return homeCountry ; }
  public void   setHomeCountry(String s) { homeCountry = s ; }
  
  public String getHomePhone1()  { return homePhone1 ; }
  public void   setHomePhone1(String s) { homePhone1 = s ; }
  
  public String getHomePhone2()  { return homePhone2 ; }
  public void   setHomePhone2(String s) { homePhone2 = s ; }
  
  public String getHomeFax()  { return homeFax ; }
  public void   setHomeFax(String s) { homeFax = s ; }
  
  public String getPersonalSite()  { return personalSite ; }
  public void   setPersonalSite(String s) { personalSite = s ; }
  
  public String getWorkAddress()  { return workAddress ; }
  public void   setWorkAddress(String s) { workAddress = s ; }
  
  public String getWorkCity()  { return workCity ; }
  public void   setWorkCity(String s) { workCity = s ; }
  
  public String getWorkStateProvince()  { return workState_province ; }
  public void   setWorkStateProvince(String s) { workState_province = s ; }
  
  public String getWorkPostalCode()  { return workPostalCode ; }
  public void   setWorkPostalCode(String s) { workPostalCode = s ; }
  
  public String getWorkCountry()  { return workCountry ; }
  public void   setWorkCountry(String s) { workCountry = s ; }
  
  public String getWorkPhone1()  { return workPhone1 ; }
  public void   setWorkPhone1(String s) { workPhone1 = s ; }
  
  public String getWorkPhone2()  { return workPhone2 ; }
  public void   setWorkPhone2(String s) { workPhone2 = s ; }
  
  public String getWorkFax()  { return workFax ; }
  public void   setWorkFax(String s) { workFax = s ; }
  
  public String getMobilePhone()  { return mobilePhone ; }
  public void   setMobilePhone(String s) { mobilePhone = s ; }
  
  public String getWebPage()  { return webPage ; }
  public void   setWebPage(String s) { webPage = s ; }
  
  public String getNote()  { return note ; }
  public void   setNote(String s) { note = s ; }
  
  public String[] getCategories() { return categories ; }
  public void   setCategories(String[] s) { categories = s ; }
  
  public String[] getTags() { return tags ; }
  public void   setTags(String[] s) { tags = s ; }

  public String[] getEditPermission() { return editPermission ; }

  public void setEditPermission(String[] s) { editPermission = s ; }
}
