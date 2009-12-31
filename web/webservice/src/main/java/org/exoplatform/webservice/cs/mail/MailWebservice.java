/**
 * 
 */
package org.exoplatform.webservice.cs.mail;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.rest.resource.ResourceContainer;
/**
 * @author Uoc Nguyen
 * Modified by : Phung Nam (phunghainam@gmail.com) 
 * 
 */
@Path("/cs/mail")
public class MailWebservice implements ResourceContainer {

  public static final int MIN_SLEEP_TIMEOUT = 100;
  public static final int MAX_TIMEOUT  = 16;

  public MailWebservice() {  }

  @GET
  @Path("/checkmail/{username}/{accountId}/{folderId}/")
  //@OutputTransformer(StringOutputTransformer.class)
  public Response checkMail(@PathParam("username")
  String userName, @PathParam("accountId")
  String accountId, @PathParam("folderId")
  String folderId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    // try to start if no checking info available
    if (checkingInfo == null) {
      mailService.checkMail(userName, accountId, folderId);
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

    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }

  @GET
  @Path("/synchfolders/{username}/{accountId}/")
  //@OutputTransformer(StringOutputTransformer.class)
  public Response synchFolders(@PathParam("username")
  String userName, @PathParam("accountId")
  String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    if (checkingInfo == null) {
      mailService.synchImapFolders(userName, accountId);
      checkingInfo = mailService.getCheckingInfo(userName, accountId);
    } 
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("<info>");
    buffer.append("  <checkingmail>");
    if (checkingInfo != null) {
      buffer.append("    <status>" + checkingInfo.getSyncFolderStatus() + "</status>");
    }
    buffer.append("  </checkingmail>");
    buffer.append("</info>");
    
    //mailService.removeCheckingInfo(userName, accountId);
    
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }
  
  @GET
  @Path("/stopcheckmail/{username}/{accountId}/")
  //@OutputTransformer(StringOutputTransformer.class)
  public Response stopCheckMail(@PathParam("username")
  String userName, @PathParam("accountId")
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
      Response.status(HTTPStatus.INTERNAL_ERROR) ;
      return Response.ok().build();
    }
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }


  
  @GET
  @Path("/checkmailjobinfo/{username}/{accountId}/")
  //@OutputTransformer(StringOutputTransformer.class)
  public Response getCheckMailJobInfo(@PathParam("username")
  String userName, @PathParam("accountId")
  String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    StringBuffer buffer = new StringBuffer();
    if (checkingInfo == null) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      checkingInfo = mailService.getCheckingInfo(userName, accountId);
      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>1000</status>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
      return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
    }
    
    if (!checkingInfo.hasChanged()) {
      Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      for (int i = 1; i < MailWebservice.MIN_SLEEP_TIMEOUT * MailWebservice.MAX_TIMEOUT; i++) {
        if (checkingInfo.hasChanged()) break;        
        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
      }
    }
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
        //mailService.removeCheckingInfo(userName, accountId);
        return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
      } else if (checkingInfo.hasChanged()) {
        buffer.append("<info>");
        buffer.append("  <checkingmail>");
        buffer.append("    <status>" + CheckingInfo.DOWNLOADING_MAIL_STATUS + "</status>");
        buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
        buffer.append("    <total>" + checkingInfo.getTotalMsg() + "</total>");
        buffer.append("    <syncFolderStatus>" + checkingInfo.getSyncFolderStatus() + "</syncFolderStatus>");
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
    
    return Response.ok(buffer.toString(), "text/xml").cacheControl(cacheControl).build();
  }
}
