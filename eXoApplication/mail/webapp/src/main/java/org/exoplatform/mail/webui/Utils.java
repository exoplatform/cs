/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */
public class Utils {
  public static final String SVR_PROTOCOL = "protocol".intern() ;
  public static final String SVR_SMTP = "mail.smtp.host".intern() ;
  public static final String SVR_HOST = "host".intern() ;
  public static final String SVR_PORT = "port".intern() ;
  public static final String SVR_FOLDER = "folder".intern() ;
  public static final String SVR_USERNAME = "username".intern() ;
  public static final String SVR_PASSWORD = "password".intern() ; 
  public static final String SVR_SSL =  "ssl".intern() ;

  public static final String POP3 = "pop3".intern() ;
  public static final String IMAP = "imap".intern() ;


  public static boolean isEmptyField(String value) {
    return value == null || value.trim().length() == 0 ;
  }
  public static boolean isNumber(String number) {
    try {
      Long.parseLong(number.trim()) ;
    } catch(NumberFormatException nfe) {
      return false;
    }
    return true ;
  }
}
