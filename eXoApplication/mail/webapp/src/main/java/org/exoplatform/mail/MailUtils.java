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
package org.exoplatform.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.portlet.PortletPreferences;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAddContactForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.event.Event;

import com.sun.mail.smtp.SMTPSendFailedException;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class MailUtils { 
  final public static String SEMICOLON = ";".intern() ;
  final public static String COMMA = ",".intern() ;
  final public static String SLASH = "/".intern() ;
  final public static String BACKSLASH = "\\".intern() ;
  public static final String GREATER_THAN = ">".intern() ;
  public static final String SMALLER_THAN = "<".intern() ;
  public static final String MINUS = "-".intern();
  final public static String SPECIALCHARACTER[] = {"?", "[", "(", "|", ")", "*", "\\", "+", "}", "{", "^", "$", "\""
    ,"!", "#", "%", "&", ":", ">", "<", "~", "`", "]", "'", "/", "-"} ; ;
  final public static String SUPPORTED_VIEW_TYPE_ATTACH[] = {"gif", "png", "jpg", "jpec", "bmp"} ;
  final public static String SIMPLECHARACTER[] = {GREATER_THAN, SMALLER_THAN, "'", "\""};
  final public static int MAX_POPUP_WIDTH = 900;
  public static final int DEFAULT_VALUE_UPLOAD_PORTAL = -1;

    static public MailService getMailService() throws Exception {
      return (MailService)PortalContainer.getComponent(MailService.class) ;
    }

    static public String getCurrentUser() throws Exception { 
      return Util.getPortalRequestContext().getRemoteUser() ; 
    }
    /*
  public static boolean isNameValid(String name, String[] regex) {
    for(String c : regex){ if(name.contains(c)) return false ;}
    return true ;
  }
     */
    public static String getImageSource(Contact contact, DownloadService dservice) throws Exception {    
      ContactAttachment contactAttachment = contact.getAttachment();
      if (contactAttachment != null) {
        InputStream input = contactAttachment.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
          dresource.setDownloadName(contactAttachment.getFileName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        }
      }
      return null ;
    }

    static public OrganizationService getOrganizationService() throws Exception {
      return (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    }

    public static String encodeJCRText(String str) {
      if(isFieldEmpty(str)) return "";
      return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").
      replaceAll("'", "&apos;").replaceAll("\"", "&quot;") ;
    }

    public static String encodeURL(String urlPart) {
      try {
        return URLEncoder.encode(urlPart, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        return urlPart;
      }
    }

    public static String decodeURL(String encodedPart) {
      try {
        return URLDecoder.decode(encodedPart, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        return encodedPart;
      }
    }


    public static String encodeJCRPath2URLPath(String jcrPath) {
      if (jcrPath == null) return "";
      String[] arr = jcrPath.split("/");
      StringBuffer sb = new StringBuffer();
      for (String s : arr) {
        sb.append(encodeURL(s)).append("/");
      }

      sb.delete(sb.length() - 1, sb.length());
      return sb.toString();
    }

    public static String convertSize(long size) throws Exception {
      return Utils.convertSize(size);
    }

    public static String getImageSource(Attachment attach, DownloadService dservice) throws Exception {      
      if (attach != null) {
        InputStream input = attach.getInputStream() ;
        byte[] imageBytes = null ;
        if (input != null) {
          imageBytes = new byte[input.available()] ;
          input.read(imageBytes) ;
          ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
          InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
          dresource.setDownloadName(attach.getName()) ;
          return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
        }
      }
      return null ;
    }

    public static String fillImage(String body, Map<String, String> imageLocationMap) throws Exception {
      String attId = "", src = "";
      if (imageLocationMap.size() > 0) {
        while (body.indexOf("\"cid:") > -1) {        
          attId = body.substring(body.indexOf("\"cid:") + 5, body.length());
          attId = attId.substring(0, attId.indexOf("\""));
          src = "\"" + imageLocationMap.get(attId);
          body = body.replaceFirst("\"cid:(.*?)", src);
        }
      }
      return body;
    }

    public static boolean isFieldEmpty(String s) {
      return (s == null || s.trim().length() == 0);    
    }

    //  public static boolean isChecking(String username, String accountId) throws Exception {
    //    try {
    //      ExoContainer container = ExoContainerContext.getCurrentContainer();
    //      JobSchedulerService schedulerService = 
    //        (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    //      List allJobs = schedulerService.getAllJobs() ;
    //      for(Object obj : allJobs) {
    //        if(((JobDetail)obj).getName().equals(username + ":" + accountId)) return true ; 
    //      }
    //    } catch(Exception e) { }
    //    
    //    return false ;
    //  }

    public static String formatDate(String format, Date date, Locale locale) {
      Format formatter = new SimpleDateFormat(format, locale);
      return formatter.format(date);
    }

    public static boolean isDate(Calendar objCal){
      if (objCal == null) return false;
      try{
        return MailUtils.isDate(objCal.getTime().toString(),"MM/dd/yyyy");
      }catch (Exception e) {
        return false;
      }
    }

    public static boolean isDate(String isDate, String format){
      if(isFieldEmpty(isDate)) return false;
      SimpleDateFormat fomatter = new SimpleDateFormat(format);
      if(isDate.length() != fomatter.toPattern().length()) return false;
      fomatter.setLenient(false);
      try {
        fomatter.parse(isDate);
        return true;
      } catch (java.text.ParseException e) {
        return false;
      }
    }

    public static String formatDate(Date date, Locale locale) {
      Calendar systemDate =  new GregorianCalendar() ;
      Calendar cal =  new GregorianCalendar() ;
      cal.setTime(date);
      boolean isSameYear = (systemDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) ;
      boolean isSameMonth = (systemDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) ;
      boolean isSameWeek = (isSameMonth && (systemDate.get(Calendar.WEEK_OF_MONTH) == cal.get(Calendar.WEEK_OF_MONTH)));
      boolean isSameDate = (isSameWeek && (systemDate.get(Calendar.DAY_OF_WEEK) == cal.get(Calendar.DAY_OF_WEEK)));
      if (isSameYear) 
        if (isSameDate) 
          return (new SimpleDateFormat("hh:mm aaa", locale)).format(date);
        else if (isSameWeek)
          return (new SimpleDateFormat("EEEE", locale)).format(date);
        else if (isSameMonth)
          return (new SimpleDateFormat("EEEE, dd", locale)).format(date);
        else 
          return (new SimpleDateFormat("MMM dd", locale)).format(date);
      else 
        return (new SimpleDateFormat("MMM dd, yyyy", locale)).format(date);
    }

    public static String getAttachmentLink(Attachment att) throws RepositoryException {
      RepositoryService rService = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
      String repository = rService.getCurrentRepository().getConfiguration().getName();
      return "/" + PortalContainer.getInstance().getRestContextName() + "/private/jcr/" + repository + encodeJCRPath2URLPath(att.getPath());
    }

    public static String encodeHTML(String htmlContent) throws Exception {
      return (!isFieldEmpty(htmlContent)) ? htmlContent.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
                                          .replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&#39;") : "" ;
    }

    public static String decodeHTML(String htmlContent) throws Exception {
      return (!isFieldEmpty(htmlContent)) ? htmlContent.replaceAll( "&quot;", "\"").replaceAll("&#39;", "'")
                                          .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&") : "" ;
    }

    public static boolean isInvitation(Message msg) throws Exception {
      return (msg.getHeader("X-Exo-Invitation") != null) ;
    }

    public static String getEventFrom(Message msg) throws Exception {
      String from = null;
      if (isInvitation(msg)) {
        from = msg.getHeader("X-Exo-Invitation").split(";")[0].trim() ;
      }
      return from ;
    }

    public static String getEventTo(Message msg) throws Exception {
      String to = null;
      if (isInvitation(msg)) {
        to = msg.getHeader("X-Exo-Invitation").split(";")[1].trim() ;
      }
      return to ;
    }

    public static String getAddressTo(Message msg) throws Exception {
      String to = null;
      if (isInvitation(msg)) {
        to = msg.getHeader("To").trim() ;
      }
      return to ;
    }

    public static String getEventType(Message msg) throws Exception {
      String eventType = null;
      if (isInvitation(msg)) {
        eventType = msg.getHeader("X-Exo-Invitation").split(";")[2].trim() ;
      }
      return eventType ;
    }

    public static String getCalendarId(Message msg) throws Exception {
      String calId = null;
      if (isInvitation(msg)) {
        calId = msg.getHeader("X-Exo-Invitation").split(";")[3].trim() ;
      }
      return calId ;
    }

    public static String getCalendarEventId(Message msg) throws Exception {
      String calEvenId = null;
      if (isInvitation(msg)) {
        calEvenId = msg.getHeader("X-Exo-Invitation").split(";")[4].trim() ;
      }
      return calEvenId ;
    }

    public static boolean isValidEmailAddresses(String addressList) throws Exception {
      if (isFieldEmpty(addressList))  return true ;
      boolean isInvalid = true ;
      try {
        InternetAddress[] iAdds = InternetAddress.parse(addressList, true);

        String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
        for (int i = 0 ; i < iAdds.length; i ++) {
          if(!iAdds[i].getAddress().toString().matches(emailRegex)) isInvalid = false;
        }
      } catch(AddressException e) {
        return false ;
      }
      return isInvalid ;
    }

    public static String html2string(String str) throws Exception {
      if(isFieldEmpty(str)) return "";
      return str.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", "").replaceAll("&quot;", "\"").replaceAll("\n", "");
    }

    //TODO : need to improve later
    public static String html2text(String str) throws Exception {
      if(isFieldEmpty(str)) return "";
      return str.replaceAll("<br*/?>", "\n").replaceAll("<[^>]*>", "").replaceAll("&nbsp;", "").replaceAll("&quot;", "\"");

    }

    //TODO : need to improve later
    public static String text2html(String str) throws Exception {
      if(isFieldEmpty(str)) return "";
      return str.replaceAll("\n", "<br />");
    }

    public static String camovylageLessGreateTag(String s){
      if(isFieldEmpty(s)) return "";
      return s.replaceAll("&lt;", "&lt;;").replaceAll("&gt;", ";&gt;");
    }

    public static String convertTextToHtmlLink(String s) throws Exception {
      if (isFieldEmpty(s)) return "" ;
      s = decodeHTML(s);
      // for email 
      s = s.replaceAll("(\\s)([_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5})", "$1<a target=\"_blank\" href=\"mailto:$2\"> $2 </a>") ;
      // for external link with form http:// , https://, ftp://
      String strPattern = "([^((href|src)=\")])(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]?";
      Pattern pattern = Pattern.compile(strPattern);
      Matcher matcher = pattern.matcher(s);
      String tem = "";
      while(matcher.find()){
        String link = matcher.group();//.substring(1);
        String preffix = s.substring(0, s.indexOf(link) + link.length());
        tem += preffix.replace(link.substring(1),  "<a target=\"_blank\" href=\"" + link.substring(1) + "\">"+link.substring(1)+"</a>");
        s = s.substring(s.indexOf(link) + link.length());
        matcher = pattern.matcher(s);
      }
      return tem + s;
    }

    public static String insertTargetToHtmlLink(String s) throws Exception {
      if (isFieldEmpty(s)) return "" ;
      s = decodeHTML(s);
      // for a tag we insert "target=_blank" to open in other window or tab  
      s = s.replaceAll("<;(A|a)(\\s)(.*?);>(.*?)<;/(A|a);>", "<a $2 target=\"_blank\"> $3 </a>");
      return s ;
    }

    public static String getDisplayAdddressShared(String sharedUserId, String addressName) {
      return sharedUserId + " - " + addressName ;
    }

    public static List<String> getUserGroups() throws Exception {
      OrganizationService organizationService = 
        (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(getCurrentUser()).toArray() ;
      List<String> groupIds = new ArrayList<String>() ;
      for (Object object : objGroupIds) {
        groupIds.add(((Group)object).getId()) ;
      }
      return groupIds ;
    }

    public static String reduceSpace(String s) {
      if (isFieldEmpty(s)) return "" ;
      String[] words = s.split(" ") ;
      StringBuilder builder = new StringBuilder() ;
      for (String word : words) {
        if (builder.length() > 0 && word.trim().length() > 0) builder.append(" ") ;
        builder.append(word.trim()) ;
      }
      return builder.toString() ;
    }

    public static boolean isSearchValid(String name, String[] regex) {
      if (isFieldEmpty(name)) return true ;
      for(String c : regex){ if(name.contains(c)) return false ;}
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

    /**
     * this function check whether UIDocumentSelector existed.
     * @return
     */
    public static boolean hasDMSSelector() {    
      try {
      Class.forName("org.exoplatform.webui.commons.UIDocumentSelector");
      return true;
      } catch (ClassNotFoundException e) {
        return false;
      }
    }

    public static String getGroupCalendarName(String groupName, String calendarName) {
      return groupName + MINUS + calendarName;
    }

    public static boolean isDelegatedAccount(Account acc, String recieve) {
      return (acc != null && acc.getDelegateFrom() != null && recieve != null && !recieve.equalsIgnoreCase(acc.getDelegateFrom()));
    }

    public static boolean isFull (String user, String perms) {
      return (user != null && perms != null) && Utils.SEND_RECIEVE.equalsIgnoreCase(perms) ;
    }

    public static boolean isFull(String accountId) {
      try {
        MailService mService = getMailService();
        String uid = getCurrentUser();
        if(mService.getAccountById(uid, accountId) == null) {
          Account dAccount = mService.getDelegatedAccount(uid, accountId);
          return (isDelegatedAccount(dAccount, uid) && isFull(uid, dAccount.getPermissions().get(uid)));
        }
        return true;
      }catch (Exception e) {
        //e.printStackTrace();
        return false;
      }
    }

    public static boolean isDelegated(String id) {
      try {
        String uid = getCurrentUser();
        return getMailService().getDelegatedAccount(uid, id) != null;
      }catch (Exception e) {
        return false;
      }
    }

    public static String decodeAttachName(String name) throws Exception {
      return MimeUtility.decodeText(name);
    }
    
    static public ContactService getcontactService() throws Exception {
      return (ContactService)PortalContainer.getComponent(ContactService.class) ;
    }
    public static boolean havePermission(String groupId) throws Exception {
      String currentUser = MailUtils.getCurrentUser();
      AddressBook sharedGroup = getcontactService().getSharedAddressBook(currentUser, groupId);
      if (sharedGroup == null)
        return false;
      if (sharedGroup.getEditPermissionUsers() != null
          && Arrays.asList(sharedGroup.getEditPermissionUsers()).contains(currentUser
              + DataStorage.HYPHEN)) {
        return true;
      }
      String[] editPerGroups = sharedGroup.getEditPermissionGroups();
      if (editPerGroups != null)
        for (String editPer : editPerGroups)
          if (MailUtils.getUserGroups().contains(editPer))
            return true;
      return false;
    }

    public static void sendReturnReceipt(UIApplication uiApp, Event event, String username, String accid, String msgId, ResourceBundle res) throws Exception{
      try {
        getMailService().sendReturnReceipt(username, accid, msgId, res);
      } catch (AddressException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-error-parsing-the-addresses-sending-failed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (AuthenticationFailedException e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.please-check-configuration-for-smtp-server", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (SMTPSendFailedException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.sorry-there-was-an-error-sending-the-message-sending-failed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (MessagingException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-unexpected-error-sending-falied", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
    }
    
    public static void createContactForm(Event event, UIPopupAction uiPopup, Message msg, String componentId) throws Exception{
      UIPopupActionContainer uiPopupContainer = uiPopup.createUIComponent(UIPopupActionContainer.class, null, componentId) ;
      uiPopup.activate(uiPopupContainer, 730, 0, true);
      UIAddContactForm uiAddContactForm = uiPopupContainer.createUIComponent(UIAddContactForm.class, null, null);
      uiPopupContainer.addChild(uiAddContactForm);
      InternetAddress[] addresses  = Utils.getInternetAddress(msg.getFrom());
      String personal = (addresses[0] != null) ? Utils.getPersonal(addresses[0]) : "";
      String firstName = personal;
      String email = (addresses[0] != null) ? addresses[0].getAddress() : "";
      String lastName = "";
      if (personal.indexOf(" ") > 0) {
        firstName = personal.substring(0, personal.indexOf(" "));
        lastName = personal.substring(personal.indexOf(" ") + 1, personal.length());
      }
      uiAddContactForm.setFirstNameField(firstName);
      uiAddContactForm.setLastNameField(lastName);
      uiAddContactForm.setEmailField(email);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
    
    public static int getLimitUploadSize() {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      int limitMB = DEFAULT_VALUE_UPLOAD_PORTAL;
      try {
        limitMB = Integer.parseInt(portletPref.getValue("uploadFileSizeLimitMB", "").trim());
      } catch (Exception e) {}
      return limitMB;
    }
}

