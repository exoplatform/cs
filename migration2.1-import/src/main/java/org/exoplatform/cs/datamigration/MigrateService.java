package org.exoplatform.cs.datamigration;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

public class MigrateService implements Startable{
  
  private ConfigurationManager configManager_ ;
  private NodeHierarchyCreator nodeHierarchy_ ;
  private InitParams params_ ;
  private RepositoryService  repoService_ ;
  private static final String UNDERSCORE = "_".intern();
  private static final String PUBLIC = "Public".intern();
  final private static String CONTACT_APP = "ContactApplication".intern() ;
  final private static String USERS_PATH = "usersPath".intern();
  private static final String  MAIL_SERVICE = "MailApplication";
  
  public MigrateService(ConfigurationManager configManager, NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
    configManager_ = configManager ;
    nodeHierarchy_ = nodeHierarchy ;
    params_ = params ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    repoService_ = (RepositoryService)container.getComponentInstance(RepositoryService.class) ;
  }
  
  public void start() {
    try{
      importNewCalendarData() ;
      importNewContactData() ;
      importNewMailData() ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public void stop() {}
  
  private void importNewCalendarData() {
    try{
      String file = params_.getValueParam("calendarData2.1").getValue() ;
      System.out.println("\n Calendar's data version 2.1 is importing.... ");      
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        try {
          InputStream in = configManager_.getInputStream(getFilenameByUser(file, username)) ;
          Node calendarHome = getUserCalendarAppHomeNode(username);
          Node parent = calendarHome.getParent() ;
          calendarHome.remove() ;
          parent.save() ;
          parent.getSession().importXML(parent.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          parent.getSession().save() ;          
        } catch (FileNotFoundException e) {          
          System.out.println("\n ==> file not found :" + getFilenameByUser(file, username));
        }
      }
      InputStream inReminder = configManager_.getInputStream(getPublicCalendarFilename(file)) ;
      Node publicCalendarHome = getPublicCalendarAppHome();
      Node parentReminder = publicCalendarHome.getParent() ;
      publicCalendarHome.remove() ;
      parentReminder.save() ;
      parentReminder.getSession().importXML(parentReminder.getPath(), inReminder, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
      parentReminder.getSession().save() ;
      System.out.println("\n >>>>>> Calendar's data version 2.1 is imported succesful !\n");      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void importNewContactData() {
    try{
      String file = params_.getValueParam("contactData2.1").getValue() ;
      System.out.println(" Contact's data version 2.1 is importing.... \n");
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        try {          
          InputStream in = configManager_.getInputStream(getFilenameByUser(file, username)) ;
          Node contactHome = getUserContactAppHomeNode(username);
          Node parent = contactHome.getParent() ;
          contactHome.remove() ;
          parent.getSession().save() ;
          parent.getSession().importXML(parent.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          parent.getSession().save() ;        
        } catch (FileNotFoundException e) {
          System.out.println("\n ==> file not found :" + getFilenameByUser(file, username));
        }
      }
      System.out.println("\n >>>>>> Contact's data version 2.1 is imported succesful !\n");      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void importNewMailData() {
    try{
      String file = params_.getValueParam("mailData2.1").getValue() ;
      System.out.println(" Mail's data version 2.1 is importing...\n");
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        try {          
          InputStream in = configManager_.getInputStream(getFilenameByUser(file, username)) ;
          Node mailHome = getUserMailAppHomeNode(username);
          Node parent = mailHome.getParent() ;
          mailHome.remove() ;
          parent.getSession().save() ;
          parent.getSession().importXML(parent.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          parent.getSession().save() ;
        } catch (FileNotFoundException e) {
          System.out.println("\n ==> file not found :" + getFilenameByUser(file, username));
        }
      }
      System.out.println("\n >>>>>> Mail's data version 2.1 is imported succesful !\n");      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private Node getUserMailAppHomeNode(String username) throws Exception {
    Node userApp = nodeHierarchy_.getUserApplicationNode(SessionProvider.createSystemProvider(), username);
    Node mailNode = null;
    try {
      mailNode = userApp.getNode(MAIL_SERVICE);
    } catch (PathNotFoundException e) {
      mailNode = userApp.addNode(MAIL_SERVICE, Utils.NT_UNSTRUCTURED);
      if (userApp.isNew()) userApp.getSession().save();
      else userApp.save();
    }
    return mailNode;
  }
  
  private String getFilenameByUser(String file, String username) {
    if (file.indexOf("\\") > 0) {
      String fileName = username + UNDERSCORE + file.substring(file.lastIndexOf("\\") + 1);
      return (file.substring(0, file.lastIndexOf("\\") + 1) + fileName) ;      
    } else {
      String fileName = username + UNDERSCORE + file.substring(file.lastIndexOf("/") + 1);
      return (file.substring(0, file.lastIndexOf("/") + 1) + fileName) ;
    }
  }
  
  private String getPublicCalendarFilename(String file) {
    if (file.indexOf("\\") > 0) {
      String fileName = PUBLIC + UNDERSCORE + file.substring(file.lastIndexOf("\\") + 1);
      return (file.substring(0, file.lastIndexOf("\\") + 1) + fileName) ;      
    } else {
      String fileName = PUBLIC + UNDERSCORE + file.substring(file.lastIndexOf("/") + 1);
      return (file.substring(0, file.lastIndexOf("/") + 1) + fileName) ;
    }    
  }
  
  private Node getUserCalendarAppHomeNode(String username) throws Exception {
    Node userNode = nodeHierarchy_.getUserApplicationNode(SessionProvider.createSystemProvider(), username);
    try {     
      return userNode.getNode(Utils.CALENDAR_APP);
    } catch (PathNotFoundException e) {
      return  userNode.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
    }   
  }
  
  private Node getUserContactAppHomeNode(String username) throws Exception {
    Node userNode = nodeHierarchy_.getUserApplicationNode(SessionProvider.createSystemProvider(), username);
    try {
      return userNode.getNode(CONTACT_APP);
    } catch (PathNotFoundException e) {
      return  userNode.addNode(CONTACT_APP, Utils.NT_UNSTRUCTURED);
    }   
  }
  
  private Node getPublicCalendarAppHome() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    Node publicApp = getNodeByPath(nodeHierarchy_.getPublicApplicationNode(sProvider).getPath(), sProvider);
    try {
      return publicApp.getNode(Utils.CALENDAR_APP);
    } catch (Exception e) {
      Node calendarApp = publicApp.addNode(Utils.CALENDAR_APP, Utils.NT_UNSTRUCTURED);
      publicApp.getSession().save();
      return calendarApp;
    }
  }
  
  private Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }
  
  private Session getSession(SessionProvider sprovider) throws Exception{
    ManageableRepository currentRepo = repoService_.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }
  
}
