/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.test;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.test.BasicTestCase;


/**
 * Created by The eXo Platform SAS
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 					
 * july 3, 2007  
 */
public class BaseMailTestCase extends BasicTestCase {
  
  final protected static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final protected static String NT_FOLDER = "nt:folder".intern() ;
  final protected static String NT_FILE = "nt:file".intern() ;    
  final protected static String ADMIN = "admin".intern() ;
    
  final protected static String DEFAULT_WS = "production".intern() ;
  final protected static String MAIL_HOME = "mailHome".intern() ;
  
  protected Node rootNode_;
  protected Node mailHomeNode_;
  protected Node systemNode_;
  protected ManageableRepository repository_;
  protected SimpleCredentials credentials_;
  protected PortalContainer manager_;  
  protected OrganizationService orgService_;  
  protected Session sysSessionOnDefault_ ;  
  protected MailService mailService_ ;
  public void setUp() throws Exception{
    
    LogService logService = 
      (LogService) RootContainer.getInstance().getComponentInstanceOfType(LogService.class); 

    logService.setLogLevel("org.exoplatform.services.jcr", LogService.DEBUG, true);     
    
    manager_ = PortalContainer.getInstance() ;
    if(System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/resource/login.conf" );

    credentials_ = new SimpleCredentials(ADMIN, ADMIN.toCharArray());

    RepositoryService repositoryService = 
      (RepositoryService) manager_.getComponentInstanceOfType(RepositoryService.class);
        
    repository_ = repositoryService.getDefaultRepository();
    mailService_ = (MailService)manager_.getComponentInstanceOfType(MailService.class) ;
    
    orgService_ = (OrganizationService)manager_.getComponentInstanceOfType(OrganizationService.class);            
    sysSessionOnDefault_ = repository_.getSystemSession(DEFAULT_WS) ;   
    rootNode_ = sysSessionOnDefault_.getRootNode(); 
    if(rootNode_.hasNode(MAIL_HOME)) mailHomeNode_ = rootNode_.getNode(MAIL_HOME) ;
    else{ mailHomeNode_ = rootNode_.addNode(MAIL_HOME, NT_UNSTRUCTURED) ; }
  }
}