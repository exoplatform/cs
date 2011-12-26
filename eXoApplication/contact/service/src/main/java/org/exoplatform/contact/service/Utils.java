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
package org.exoplatform.contact.service;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.mail.internet.InternetAddress;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */

public class Utils {
  private static final Log log = ExoLogger.getExoLogger(Utils.class);
  
  final public static String SPLIT         = "::".intern();

  final public static String SEMI_COLON    = ";".intern();

  final public static String COMMA         = ",".intern();

  final public static String contactTempId = "ContacttempId";

  public static int          limitExport   = 150;

  public static final String COLON         = ":".intern();

  public static final String MEMBERSHIP    = "*.".intern();

  final static public String MANAGER       = "manager";
  
  public static final String ADDRESSBOOK_ID_PREFIX = "ContactGroupInSpace".intern();

  public static String formatDate(String format, Date date) {
    Format formatter = new SimpleDateFormat(format);
    return formatter.format(date);
  }

  public static boolean isEmpty(String s) {
    if (s == null || s.trim().length() == 0)
      return true;
    return false;
  }

  /**
   * check string array is whether empty or not
   * @param array
   * @return false if at least one element of array is not empty, true in the opposite case.
   */
  public static boolean isEmpty(String[] array) {
    if (array != null && array.length > 0) {
      for (String s : array) {
        if (s != null && s.trim().length() > 0)
          return false;
      }
    }
    return true;
  }

  public static List<String> parseEmails(String emails) throws Exception {
    List<String> emailList = new ArrayList<String>();
    if (isEmpty(emails))
      return emailList;
    for (String email : emails.replaceAll(SEMI_COLON, COMMA).split(COMMA)) {
      try {
        if (isEmpty(email))
          continue;
        email = InternetAddress.parse(email)[0].getAddress();
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method parseEmails", e);
        }
      }
      if (isValidEmailAddresses(email))
        emailList.add(email);
    }
    return emailList;
  }

  public static boolean isValidEmailAddresses(String email) {
    if (isEmpty(email))
      return false;
    String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}";
    if (!email.trim().matches(emailRegex))
      return false;
    return true;
  }

  public static String listToString(List<String> list) {
    if (list == null || list.size() == 0)
      return "";
    StringBuilder builder = new StringBuilder();
    for (String str : list) {
      if (builder.length() > 0)
        builder.append("; " + str);
      else
        builder.append(str);
    }
    return builder.toString();
  }

  public static String encodeGroupId(String id) {
    return id.replaceAll("/", "&quot;");
  }

  public static String decodeGroupId(String id) {
    return id.replaceAll("&quot;", "/");
  }

  /**
   * {@inheritDoc}
   */
  public static Contact getContact(Node contactNode, String contactType) throws Exception {
    Contact contact = new Contact();
    contact.setContactType(contactType);
    if (contactNode.hasProperty("exo:id"))
      contact.setId(contactNode.getProperty("exo:id").getString());
    if (contactNode.hasProperty("exo:fullName"))
      contact.setFullName(contactNode.getProperty("exo:fullName").getString());
    if (contactNode.hasProperty("exo:firstName"))
      contact.setFirstName(contactNode.getProperty("exo:firstName").getString());
    if (contactNode.hasProperty("exo:lastName"))
      contact.setLastName(contactNode.getProperty("exo:lastName").getString());
    if (contactNode.hasProperty("exo:nickName"))
      contact.setNickName(contactNode.getProperty("exo:nickName").getString());
    if (contactNode.hasProperty("exo:gender"))
      contact.setGender(contactNode.getProperty("exo:gender").getString());
    if (contactNode.hasProperty("exo:birthday"))
      contact.setBirthday(contactNode.getProperty("exo:birthday").getDate().getTime());
    if (contactNode.hasProperty("exo:jobTitle"))
      contact.setJobTitle(contactNode.getProperty("exo:jobTitle").getString());
    if (contactNode.hasProperty("exo:emailAddress"))
      contact.setEmailAddress(valuesToString(contactNode.getProperty("exo:emailAddress").getValues()));

    if (contactNode.hasProperty("exo:exoId"))
      contact.setExoId(contactNode.getProperty("exo:exoId").getString());
    if (contactNode.hasProperty("exo:googleId"))
      contact.setGoogleId(contactNode.getProperty("exo:googleId").getString());
    if (contactNode.hasProperty("exo:msnId"))
      contact.setMsnId(contactNode.getProperty("exo:msnId").getString());
    if (contactNode.hasProperty("exo:aolId"))
      contact.setAolId(contactNode.getProperty("exo:aolId").getString());
    if (contactNode.hasProperty("exo:yahooId"))
      contact.setYahooId(contactNode.getProperty("exo:yahooId").getString());
    if (contactNode.hasProperty("exo:icrId"))
      contact.setIcrId(contactNode.getProperty("exo:icrId").getString());
    if (contactNode.hasProperty("exo:skypeId"))
      contact.setSkypeId(contactNode.getProperty("exo:skypeId").getString());
    if (contactNode.hasProperty("exo:icqId"))
      contact.setIcqId(contactNode.getProperty("exo:icqId").getString());

    if (contactNode.hasProperty("exo:homeAddress"))
      contact.setHomeAddress(contactNode.getProperty("exo:homeAddress").getString());
    if (contactNode.hasProperty("exo:homeCity"))
      contact.setHomeCity(contactNode.getProperty("exo:homeCity").getString());
    if (contactNode.hasProperty("exo:homeState_province"))
      contact.setHomeState_province(contactNode.getProperty("exo:homeState_province").getString());
    if (contactNode.hasProperty("exo:homePostalCode"))
      contact.setHomePostalCode(contactNode.getProperty("exo:homePostalCode").getString());
    if (contactNode.hasProperty("exo:homeCountry"))
      contact.setHomeCountry(contactNode.getProperty("exo:homeCountry").getString());
    if (contactNode.hasProperty("exo:homePhone1"))
      contact.setHomePhone1(contactNode.getProperty("exo:homePhone1").getString());
    if (contactNode.hasProperty("exo:homePhone2"))
      contact.setHomePhone2(contactNode.getProperty("exo:homePhone2").getString());
    if (contactNode.hasProperty("exo:homeFax"))
      contact.setHomeFax(contactNode.getProperty("exo:homeFax").getString());
    if (contactNode.hasProperty("exo:personalSite"))
      contact.setPersonalSite(contactNode.getProperty("exo:personalSite").getString());

    if (contactNode.hasProperty("exo:workAddress"))
      contact.setWorkAddress(contactNode.getProperty("exo:workAddress").getString());
    if (contactNode.hasProperty("exo:workCity"))
      contact.setWorkCity(contactNode.getProperty("exo:workCity").getString());
    if (contactNode.hasProperty("exo:workState_province"))
      contact.setWorkStateProvince(contactNode.getProperty("exo:workState_province").getString());
    if (contactNode.hasProperty("exo:workPostalCode"))
      contact.setWorkPostalCode(contactNode.getProperty("exo:workPostalCode").getString());
    if (contactNode.hasProperty("exo:workCountry"))
      contact.setWorkCountry(contactNode.getProperty("exo:workCountry").getString());
    if (contactNode.hasProperty("exo:workPhone1"))
      contact.setWorkPhone1(contactNode.getProperty("exo:workPhone1").getString());
    if (contactNode.hasProperty("exo:workPhone2"))
      contact.setWorkPhone2(contactNode.getProperty("exo:workPhone2").getString());
    if (contactNode.hasProperty("exo:workFax"))
      contact.setWorkFax(contactNode.getProperty("exo:workFax").getString());
    if (contactNode.hasProperty("exo:mobilePhone"))
      contact.setMobilePhone(contactNode.getProperty("exo:mobilePhone").getString());
    if (contactNode.hasProperty("exo:webPage"))
      contact.setWebPage(contactNode.getProperty("exo:webPage").getString());
    if (contactNode.hasProperty("exo:note"))
      contact.setNote(contactNode.getProperty("exo:note").getString());
    if (contactNode.hasProperty("exo:categories"))
      contact.setAddressBookIds(valuesToStringArray(contactNode.getProperty("exo:categories").getValues()));
    if (contactNode.hasProperty("exo:tags"))
      contact.setTags(valuesToStringArray(contactNode.getProperty("exo:tags").getValues()));
    if (contactNode.hasProperty("exo:editPermissionUsers"))
      contact.setEditPermissionUsers(valuesToStringArray(contactNode.getProperty("exo:editPermissionUsers").getValues()));
    if (contactNode.hasProperty("exo:viewPermissionUsers"))
      contact.setViewPermissionUsers(valuesToStringArray(contactNode.getProperty("exo:viewPermissionUsers").getValues()));

    if (contactNode.hasProperty("exo:editPermissionGroups"))
      contact.setEditPermissionGroups(valuesToStringArray(contactNode.getProperty("exo:editPermissionGroups").getValues()));
    if (contactNode.hasProperty("exo:viewPermissionGroups"))
      contact.setViewPermissionGroups(valuesToStringArray(contactNode.getProperty("exo:viewPermissionGroups").getValues()));

    if (contactNode.hasProperty("exo:lastUpdated"))
      contact.setLastUpdated(contactNode.getProperty("exo:lastUpdated").getDate().getTime());
    contact.setPath(contactNode.getPath());
    if (contactNode.hasNode("image")) {
      Node image = contactNode.getNode("image");
      if (image.isNodeType("nt:file")) {
        ContactAttachment file = new ContactAttachment();
        file.setId(image.getPath());
        file.setMimeType(image.getNode("jcr:content").getProperty("jcr:mimeType").getString());
        file.setFileName(image.getName());
        file.setWorkspace(image.getSession().getWorkspace().getName());
        contact.setAttachment(file);
      }
    }
    if (contactNode.hasProperty("exo:isOwner"))
      contact.setOwner(contactNode.getProperty("exo:isOwner").getBoolean());
    if (contactNode.hasProperty("exo:ownerId"))
      contact.setOwnerId(contactNode.getProperty("exo:ownerId").getString());
    return contact;
  }

  public static String[] valuesToStringArray(Value[] Val) throws Exception {
    if (Val.length == 1)
      return new String[] { Val[0].getString() };
    String[] Str = new String[Val.length];
    for (int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }

  public static String valuesToString(Value[] values) {
    if (values == null)
      return null;
    StringBuilder strs = new StringBuilder();
    try {
      for (Value value : values) {
        if (value.getString().trim().length() > 0) {
          if (strs.length() == 0)
            strs.append(value.getString());
          else
            strs.append(";" + value.getString());
        }
      }
    } catch (Exception e) {
    }
    return strs.toString();
  }
  
  public static SessionProvider createSystemProvider() {
    SessionProviderService sessionProviderService = (SessionProviderService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);
    return sessionProviderService.getSystemSessionProvider(null);
  }

}
