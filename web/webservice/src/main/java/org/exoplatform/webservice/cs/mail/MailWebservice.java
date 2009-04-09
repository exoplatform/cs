/**
 * 
 */
package org.exoplatform.webservice.cs.mail;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

/**
 * @author Uoc Nguyen
 * Modified by : Phung Nam (phunghainam@gmail.com) 
 * 
 */
public class MailWebservice implements ResourceContainer {

  public static final int MIN_SLEEP_TIMEOUT = 100;
  public static final int MAX_TIMEOUT  = 16;

  public MailWebservice() {  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/checkmail/{username}/{accountId}/{folderId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response checkMail(@URIParam("username")
  String userName, @URIParam("accountId")
  String accountId, @URIParam("folderId")
  String folderId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    // try to start if no checking info available
    if (checkingInfo == null) {
      mailService.checkMail(userName, accountId);
    } else if (folderId != null && folderId.trim().length() > 0 && !folderId.equalsIgnoreCase("checkall")){
      checkingInfo.setRequestingForFolder_(folderId);
    }
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("<info>");
    buffer.append("  <checkingmail>");
    buffer.append("    <status>" + CheckingInfo.START_CHECKMAIL_STATUS + "</status>");
    if (checkingInfo != null) {
      buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
    }
    buffer.append("  </checkingmail>");
    buffer.append("</info>");

    return Response.Builder.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/stopcheckmail/{username}/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response stopCheckMail(@URIParam("username")
  String userName, @URIParam("accountId")
  String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    StringBuffer buffer = new StringBuffer();
    
    //mailService.stopCheckMail(userName, accountId);
    
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);   
    if (checkingInfo != null) {
//      while (checkingInfo.getStatusCode() != CheckingInfo.FINISHED_CHECKMAIL_STATUS) {
//        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
//        continue;
//      }

      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>" + checkingInfo.getStatusCode() + "</status>");
      buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
      
      checkingInfo.setRequestStop(true);
    } else {
      return Response.Builder.serverError().build();
    }
    return Response.Builder.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/checkmailjobinfo/{username}/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getCheckMailJobInfo(@URIParam("username")
  String userName, @URIParam("accountId")
  String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    if (checkingInfo == null) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      checkingInfo = mailService.getCheckingInfo(userName, accountId);
      return Response.Builder.serverError().build();
    }
    
    if (!checkingInfo.hasChanged()) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      for (int i = 1; i < MailWebservice.MIN_SLEEP_TIMEOUT * MailWebservice.MAX_TIMEOUT; i++) {
        if (checkingInfo.hasChanged()) break;        
        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      }
    }
    StringBuffer buffer = new StringBuffer();
    if (checkingInfo != null) {
      if (checkingInfo.getStatusCode() == CheckingInfo.FINISHED_CHECKMAIL_STATUS ||
          checkingInfo.getStatusCode() == CheckingInfo.CONNECTION_FAILURE || 
          checkingInfo.getStatusCode() == CheckingInfo.RETRY_PASSWORD) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + checkingInfo.getStatusCode() + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
        mailService.removeCheckingInfo(userName, accountId);
        return Response.Builder.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
      }
      if (checkingInfo.hasChanged()) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.DOWNLOADING_MAIL_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("    <total>" + checkingInfo.getTotalMsg() + "</total>");
        buffer.append("    <completed>" + checkingInfo.getFetching() + "</completed>");
        buffer.append("    <fetchingtofolders>" + checkingInfo.getFetchingToFolders() + "</fetchingtofolders>");
        if (checkingInfo.getMsgId() != null && !checkingInfo.getMsgId().equals("")) {
          buffer.append("    <messageid>" + checkingInfo.getMsgId().replace("<", "&lt;").replace(">", "&gt;") + "</messageid>");
        }
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
        checkingInfo.setHasChanged(false);
      } else {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.NO_UPDATE_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("  </checkingmail>");
        buffer.append("</info>");
      }
    }
    
    return Response.Builder.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }
}
