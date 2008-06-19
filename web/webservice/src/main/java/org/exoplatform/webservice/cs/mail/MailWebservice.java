/**
 * 
 */
package org.exoplatform.webservice.cs.mail;

import java.util.List;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobDetail;


/**
 * @author Uoc Nguyen
 * 
 */
public class MailWebservice implements ResourceContainer {

  public static final int MIN_SLEEP_TIMEOUT = 100;
  public static final int MAX_TIMEOUT  = 16;

  public MailWebservice() {
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/cs/mail/checkmail/{username}/{accountId}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response checkMail(@URIParam("username")
  String userName, @URIParam("accountId")
  String accountId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    MailService mailService = (MailService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(MailService.class);
    boolean isExists = false;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    JobSchedulerService schedulerService = (JobSchedulerService) container
        .getComponentInstanceOfType(JobSchedulerService.class);
    List allJobs = schedulerService.getAllJobs();
    for (Object obj : allJobs) {
      if (((JobDetail) obj).getName().equals(userName + ":" + accountId)) {
        isExists = true;
      }
    }

    if (!isExists) mailService.checkMail(userName, accountId);
    
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
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
    CheckingInfo checkingInfo = mailService.getCheckingInfo(userName, accountId);
    
    if (checkingInfo != null) {
      checkingInfo.setRequestStop(true);
      while (checkingInfo.getStatusCode() != CheckingInfo.FINISHED_CHECKMAIL_STATUS) {
        Thread.sleep(MailWebservice.MIN_SLEEP_TIMEOUT);
        continue;
      }

      buffer.append("<info>");
      buffer.append("  <checkingmail>");
      buffer.append("    <status>" + checkingInfo.getStatusCode() + "</status>");
      buffer.append("    <statusmsg>" + checkingInfo.getStatusMsg() + "</statusmsg>");
      buffer.append("  </checkingmail>");
      buffer.append("</info>");
      mailService.removeCheckingInfo(userName, accountId);
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

  public String getUserName() throws Exception {
    AuthenticationService authService = (AuthenticationService) ExoContainerContext
        .getCurrentContainer().getComponentInstanceOfType(AuthenticationService.class);
    return authService.getCurrentIdentity().getUsername();
  }
}
