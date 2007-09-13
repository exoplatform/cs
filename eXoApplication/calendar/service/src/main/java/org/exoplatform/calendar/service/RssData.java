/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class RssData {
  private String name ;
  private String version ;
  private String title ;
  private String url ;
  private String description ;
  private String copyright ;
  private String link ;
  private Date pubDate ;
  
  public String getTitle() { return title ; }
  public void setTitle(String title) { this.title = title ; }
  
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }
  
  public String getUrl() { return url ; }
  public void setUrl(String url) { this.url = url ; }
  
  public String getDescription() { return description ; }
  public void setDescription(String description) { this.description = description ; }

  public String getCopyright() { return copyright ; }
  public void setCopyright(String copyright) { this.copyright = copyright ; }
  
  public void setLink(String link) { this.link = link ; }
  public String getLink() { return link ; }
  
  public void setPubDate(Date pubDate) {this.pubDate = pubDate ; }
  public Date getPubDate() {return pubDate ; }
  
  public void setVersion(String version) { this.version = version ; }
  public String getVersion() { return version ; }
  
}
