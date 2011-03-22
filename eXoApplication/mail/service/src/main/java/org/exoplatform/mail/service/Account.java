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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam <phunghainam@gmail.com>
 *          Tuan Nguyen <tuan.nguyen@exoplatform.com>
 * Jun 23, 2007  
 * 
 */
public class Account {
  private String id ;
  private String label ;
  private String userDisplayName ;
  private String emailAddress ;
  private String emailReplyAddress ;
  private String signature ;  
  private String description ;
  private boolean checkedAuto_ ;
  private boolean emptyTrashWhenExit ;
  private boolean isSavePassword_ ;
  private String placeSignature;
  private Date lastCheckedDate_ ;
  private Date lastStartCheckingTime_;
  private boolean checkAll_ = false;
  private Date checkFromDate_;
  
  private Map<String, String> serverProperties ;  
  private Map<String, String> smtpServerProperties ;
  
  private String secureAuthsIncoming = Utils.STARTTLS;
  private String secureAuthsOutgoing  = Utils.STARTTLS;
  private String authMechsIncoming   = Utils.PLAIN;
  private String authMechsOutgoing    = Utils.PLAIN;
  
  private Map<String,String> permissions = null ;
  private String delegateFrom_ = null;
  
  
  public String getSecureAuthsIncoming() {
    if(secureAuthsIncoming == Utils.TLS_SSL) return "SSL";
    return secureAuthsIncoming;
  }

  public void setSecureAuthsIncoming(String secureAuthsIncoming) {
    this.secureAuthsIncoming = secureAuthsIncoming;
  }

  public String getSecureAuthsOutgoing() {
    if(secureAuthsOutgoing == Utils.TLS_SSL) return "SSL";
    return secureAuthsOutgoing;
  }

  public void setSecureAuthsOutgoing(String secureAuthsOutgoing) {
    this.secureAuthsOutgoing = secureAuthsOutgoing;
  }

  public String getAuthMechsIncoming() {
    if(authMechsIncoming.equalsIgnoreCase(Utils.KERBEROS_GSSAPI))
      return "GSSAPI";
    return authMechsIncoming;
  }

  public void setAuthMechsIncoming(String authMechsIncoming) {
    this.authMechsIncoming = authMechsIncoming;
  }

  public String getAuthMechsOutgoing() {
    if(authMechsIncoming.equalsIgnoreCase(Utils.KERBEROS_GSSAPI))
      return "GSSAPI";
    return authMechsOutgoing;
  }

  public void setAuthMechsOutgoing(String authMechsOutgoing) {
    this.authMechsOutgoing = authMechsOutgoing;
  }

  public Account() {
    id = Utils.KEY_ACCOUNT + IdGenerator.generate() ;
    MailService mService = (MailService)PortalContainer.getInstance().getComponentInstanceOfType(MailService.class) ;
    setServerProperty(Utils.SVR_LEAVE_ON_SERVER, mService.getSettingConfig().get(Utils.LEAVE_ON_SEVER).getMailSettingConfig().getDefaultValue()) ;
  }
  
  /**
   * The id of the account for ex: GmailAccount, YahooAccount
   * @return the id of the account
   */
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  /**
   * The display label of the account for ex:  Google Mail, Yahoo Mail
   * @return The label of the account
   */
  public String getLabel() { return label ; }
  public void   setLabel(String s) { label = s ; }
  
  /**
   * @return Return a description_ of account
   */
  public String getDescription() { return description ; }
  public void setDescription(String s) { description = s ; }
  
  /**
   * @return Return a signature of account
   */
  public String getSignature() { return signature ; }
  public void setSignature(String s) { signature = s ; }
  
  /**
   * @return Return a reply email address name of account
   */
  public String getEmailReplyAddress() { return emailReplyAddress ; }
  public void setEmailReplyAddress(String s) { emailReplyAddress = s ; }
  
  /**
   * @return Return a email address name of account
   */
  public String getEmailAddress() { return emailAddress ; }
  public void setEmailAddress(String s) { emailAddress = s ; }
  
  /**
   * @return Return a display name of account
   */
  public String getUserDisplayName() { return userDisplayName ; }
  public void setUserDisplayName(String s) { userDisplayName = s ; }
  
  /**
   * @return Return a boolean value that will set check mail automatically
   */
  public boolean checkedAuto() { return checkedAuto_; }
  public void setCheckedAuto(boolean checkedAuto) { checkedAuto_ = checkedAuto; }
  
  /**
   * @return Return a boolean value that will set whether save password
   */  
  public boolean isSavePassword() { return isSavePassword_; }
  public void setIsSavePassword(boolean isSavePassword) { isSavePassword_ = isSavePassword ; }
  
  /**
   * @return Return a boolean value that will set to empty trash folder when exit
   */
  public boolean isEmptyTrashWhenExit() { return emptyTrashWhenExit; }
  public void setEmptyTrashWhenExit(boolean bool) { emptyTrashWhenExit = bool; }
  
  /**
   * @return Return a string display place to include email signature (head , foot ...)
   */
  public String getPlaceSignature() { return placeSignature; }
  public void setPlaceSignature(String placeSig) { placeSignature = placeSig; }
  
  
  public Date getLastCheckedDate() { return lastCheckedDate_; }
  public void setLastCheckedDate(Date date) { lastCheckedDate_ = date ; }
  
  public Date getLastStartCheckingTime() { return lastStartCheckingTime_; }
  public void setLastStartCheckingTime(Date date) { lastStartCheckingTime_ = date ; }
  
  public boolean isCheckAll() { return checkAll_; }
  public void setCheckAll(boolean b) { checkAll_ = b; }
  
  public Date getCheckFromDate() { return checkFromDate_; }
  public void setCheckFromDate(Date date) { checkFromDate_ = date ; }
  
  /**
   * @return Return a mail server configuration of account
   */
  
  public Folder  getFolderByName(String name) { return null ; }
  
  public boolean isOutgoingAuthentication() { return Boolean.valueOf(smtpServerProperties.get(Utils.SMTP_ISAUTHENTICATION)); }
  public void setIsOutgoingAuthentication(boolean b) {
    setSmtpServerProperty(Utils.SMTP_ISAUTHENTICATION, String.valueOf(b));
  }
  
  public boolean useIncomingSettingForOutgoingAuthent() { return Boolean.valueOf(smtpServerProperties.get(Utils.SMTP_USEINCOMINGSETTING)); }
  public void setUseIncomingForAuthentication(boolean b) {
    setSmtpServerProperty(Utils.SMTP_USEINCOMINGSETTING, String.valueOf(b));
  }
  
  public String getOutgoingUserName() { return smtpServerProperties.get(Utils.SVR_OUTGOING_USERNAME); }
  public void setOutgoingUserName(String username) { 
    setSmtpServerProperty(Utils.SVR_OUTGOING_USERNAME, username); 
  }
  
  public String getOutgoingPassword() { return smtpServerProperties.get(Utils.SVR_OUTGOING_PASSWORD); }
  public void setOutgoingPassword(String password) { 
    setSmtpServerProperty(Utils.SVR_OUTGOING_PASSWORD, password); 
  }
  
  /**
   * Manages the server properties, based on the serverProperties attribute
   */
  public void setServerProperty(String key, String value) {
    if (serverProperties == null) serverProperties = new HashMap<String, String>();
    serverProperties.put(key, value) ;
  }
  
  public Map<String, String> getServerProperties() { return serverProperties ; }
  
  public String getProtocol()  { return serverProperties.get(Utils.SVR_PROTOCOL) ; }
  public void setProtocol(String protocol) { 
    setServerProperty(Utils.SVR_PROTOCOL, protocol) ; 
  }
  
  public String getIncomingHost()  { return serverProperties.get(Utils.SVR_INCOMING_HOST) ; }
  public void setIncomingHost(String host) { 
    setServerProperty(Utils.SVR_INCOMING_HOST, host) ; 
  }
  
  public String getIncomingPort()  { return serverProperties.get(Utils.SVR_INCOMING_PORT) ; }
  public void setIncomingPort(String port) { 
    setServerProperty(Utils.SVR_INCOMING_PORT, port) ; 
  }
  
  public String getOutgoingHost() { return serverProperties.get(Utils.SVR_OUTGOING_HOST) ;}
  public void setOutgoingHost(String host) { 
    setServerProperty(Utils.SVR_OUTGOING_HOST, host) ;
  }
  
  public String getOutgoingPort() { return serverProperties.get(Utils.SVR_OUTGOING_PORT) ;}
  public void setOutgoingPort(String port) { 
    setServerProperty(Utils.SVR_OUTGOING_PORT, port) ;
  }
  
  public boolean isCustomInbox() { return Boolean.valueOf(serverProperties.get(Utils.SVR_IS_CUSTOM_INBOX)) ; }
  public void setIsCustomInbox(boolean b) {
    setServerProperty(Utils.SVR_IS_CUSTOM_INBOX, String.valueOf(b)); 
  }
  
  public String getIncomingFolder() { return serverProperties.get(Utils.SVR_INCOMING_FOLDER) ; }
  public void setIncomingFolder(String folder)  { 
    setServerProperty(Utils.SVR_INCOMING_FOLDER, folder) ; 
  }
  
  public String getIncomingUser()  { return serverProperties.get(Utils.SVR_INCOMING_USERNAME) ; }
  public void setIncomingUser(String user)  { 
    setServerProperty(Utils.SVR_INCOMING_USERNAME, user) ; 
  }
  
  public String getIncomingPassword()  { return serverProperties.get(Utils.SVR_INCOMING_PASSWORD) ; }
  public void setIncomingPassword(String password)  { 
    setServerProperty(Utils.SVR_INCOMING_PASSWORD, password) ; 
  }
  
  public boolean isIncomingSsl()  { return serverProperties.get(Utils.SVR_INCOMING_SSL).equalsIgnoreCase("true");  }
  public void setIncomingSsl(boolean b) { 
    setServerProperty(Utils.SVR_INCOMING_SSL, String.valueOf(b)); 
  }
  
  public boolean isOutgoingSsl()  { return serverProperties.get(Utils.SVR_OUTGOING_SSL).equalsIgnoreCase("true");  }
  public void setOutgoingSsl(boolean b) { 
    setServerProperty(Utils.SVR_OUTGOING_SSL, String.valueOf(b)); 
  }
  
  public void setSmtpServerProperty(String key, String value) {
    if (smtpServerProperties == null) smtpServerProperties = new HashMap<String, String>();
    smtpServerProperties.put(key, value) ;
  }
  
  public Map<String, String> getSmtpServerProperties() { return smtpServerProperties ; }

  public void setPermissions(Map<String, String> permissions) {
    this.permissions = permissions;
  }

  public Map<String, String> getPermissions() {
    return permissions;
  }

  public void setDelegateFrom(String delegateFrom) {
    this.delegateFrom_ = delegateFrom;
  }

  public String getDelegateFrom() {
    return delegateFrom_;
  }
}
