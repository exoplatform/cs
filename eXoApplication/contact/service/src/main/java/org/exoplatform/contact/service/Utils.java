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
package org.exoplatform.contact.service ;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.internet.InternetAddress;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */

public class Utils {
  final public static String SPLIT = "::".intern();
  final public static String SEMI_COLON = ";".intern();
  final public static String COMMA = ",".intern() ;
  final public static String contactTempId = "ContacttempId" ;
  public static int limitExport = 150 ;
  
  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }
  public static boolean isEmpty(String s) {
    if (s == null || s.trim().length() == 0) return true ;
    return false ;    
  }
  

  public static List<String> parseEmails(String emails) throws Exception {
    List<String> emailList = new ArrayList<String>() ;
    if (isEmpty(emails)) return emailList ;
    for (String email : emails.replaceAll(SEMI_COLON, COMMA).split(COMMA))
    {
      try {
        if (isEmpty(email)) continue;        
        email =  InternetAddress.parse(email)[0].getAddress() ;
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (isValidEmailAddresses(email)) emailList.add(email) ;
    }
    return emailList ;
  }
  
  public static boolean isValidEmailAddresses(String email) {
    if (isEmpty(email)) return false ;
    String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
    if(!email.trim().matches(emailRegex)) return false ;
    return true ;
  }
  
  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0) return ""; 
    StringBuilder builder = new StringBuilder();
    for (String str : list) {
      if (builder.length() > 0) builder.append("; " + str);
      else builder.append(str);
    }
    return builder.toString();
  }
  
}