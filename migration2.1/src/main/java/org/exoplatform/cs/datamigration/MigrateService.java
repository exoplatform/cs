package org.exoplatform.cs.datamigration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.calendar.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class MigrateService implements Startable{
  
  private ConfigurationManager configManager_ ;
  private ExtendedNodeTypeManager ntManager_ ;
  private NodeHierarchyCreator nodeHierarchy_ ;
  private InitParams params_ ;
  private RepositoryService  repoService_ ;
  private final String CALENDAR_TEMP = "CALENDAR_TEMP";
  private final String CONTACT_TEMP = "CONTACT_TEMP";
  private final String MAIL_TEMP = "MAIL_TEMP";
  private static final String EXO_PUBLIC_URL = "exo:publicUrl".intern();
  private static final String EXO_PRIVATE_URL = "exo:privateUrl".intern();
  private static final String UNDERSCORE = "_".intern();
  private static final String PUBLIC = "Public".intern();
  private static String CALENDAR_CATEGORIES = "categories".intern() ;
  private static String CALENDAR_SETTING = "calendarSetting".intern() ;
  private static String EVENT_CATEGORIES = "eventCategories".intern() ;
  private static String CALENDARS = "calendars".intern() ;
  final private static String CONTACT_APP = "ContactApplication".intern() ;
  final private static String PERSONAL_ADDRESS_BOOKS = "contactGroup".intern() ;
  final private static String SHARED_HOME = "Shared".intern() ;
  final private static String TAGS = "tags".intern() ;
  final private static String CONTACTS = "contacts".intern() ;
  final private static String USERS_PATH = "usersPath".intern();
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String SHARED_CONTACT = "SharedContact".intern() ;
  final private static String SHARED_ADDRESSBOOK = "SharedAddressBook".intern() ;
  private static final String  MAIL_SERVICE = "MailApplication";
  private static final String KEY_MAIL_SETTING = "MailSetting".intern();
  
  public MigrateService(ConfigurationManager configManager, NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
    configManager_ = configManager ;
    nodeHierarchy_ = nodeHierarchy ;
    params_ = params ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    repoService_ = (RepositoryService)container.getComponentInstance(RepositoryService.class) ;
  }
  
  public void start() {
    try{
      ntManager_ = repoService_.getCurrentRepository().getNodeTypeManager() ;
      NodeTypeIterator ntIter = ntManager_.getAllNodeTypes() ;
      boolean is1_3 = true ;
      while(ntIter.hasNext()) {
        NodeType nt = ntIter.nextNodeType() ;
        if (nt.getName().equals(Utils.EXO_CALENDAR)) {
          for (PropertyDefinition propertyDefinition : nt.getPropertyDefinitions())
            if (propertyDefinition.getName().equals(EXO_PUBLIC_URL) || propertyDefinition.getName().equals(EXO_PRIVATE_URL)) {
              is1_3 = false;
              break;
            }
        }
        if (!is1_3) break;
      }
      if (is1_3) {
        exportCalendarData1_3() ;
        exportContactData1_3() ;
        exportMailData1_3() ;
      } else {
        registerCalendarNodetypeForMigration() ;
        calendarMigration() ;
        removeCalendarMigragionNodetypes() ;
        
        registerContactNodetypeForMigration() ;
        contactMigration() ;
        removeContactMigragionNodetypes() ;
        
        mailMigration();
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public void stop() {}
  
  private void removeCalendarMigragionNodetypes () throws Exception{
    try{
      String nt_migrate_File = params_.getValueParam("migrationCalendarNodetypes").getValue() ;
      InputStream in_migrate = configManager_.getInputStream(nt_migrate_File) ;
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(in_migrate);
      NodeList list = doc.getElementsByTagName("nodeType") ;
      System.out.println("\nRemoving Calendar migrate data nodetypes");

      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node calendarHome = getUserCalendarAppHomeNode(username);
        Node nodeTypes = (Node)calendarHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
        for(int i = 0; i < list.getLength(); i ++) {
          String name = list.item(i).getAttributes().getNamedItem("name").getNodeValue();
          if (nodeTypes.hasNode(name)) {
            Node NT = nodeTypes.getNode(name) ;
            NT.remove() ;
          }
        }
        nodeTypes.getSession().save() ;
      }
      System.out.println("Calendar Nodetypes Migragion removed");
    }catch (Exception e) {
      e.printStackTrace();
    }    
  }
  
  private void removeContactMigragionNodetypes () throws Exception{
    try{
      String nt_migrate_File = params_.getValueParam("migrationContactNodetypes").getValue() ;
      InputStream in_migrate = configManager_.getInputStream(nt_migrate_File) ;
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(in_migrate);
      NodeList list = doc.getElementsByTagName("nodeType") ;
      System.out.println("Removing Contact migrate data nodetypes");
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node contactHome = getUserContactAppHomeNode(username); 
        Node nodeTypes = (Node)contactHome.getSession().getItem("/jcr:system/jcr:nodetypes") ;
        for(int i = 0; i < list.getLength(); i ++) {
          String name = list.item(i).getAttributes().getNamedItem("name").getNodeValue();
          if (nodeTypes.hasNode(name)) {
            Node NT = nodeTypes.getNode(name) ;     
            NT.remove() ;
          }
        }
        nodeTypes.getSession().save() ;
      }
      System.out.println("Contact Nodetypes Migragion removed");
    }catch (Exception e) {
      e.printStackTrace();
    }    
  }
  
  private void registerCalendarNodetypeForMigration() throws Exception{
    try{
      //Register migrate data nodetypes
      System.out.println("Register Calendar nodetypes for migrate data") ;
      String ntFile = params_.getValueParam("migrationCalendarNodetypes").getValue() ;
      InputStream in = configManager_.getInputStream(ntFile) ;
      ntManager_.registerNodeTypes(in, ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ;
    }catch(Exception e){
      e.printStackTrace() ;
    }
  }
  
  private void registerContactNodetypeForMigration() throws Exception{
    try{
      //Register migrate data nodetypes
      System.out.println("Register Contact nodetypes for migrate data") ;
      String ntFile = params_.getValueParam("migrationContactNodetypes").getValue() ;
      InputStream in = configManager_.getInputStream(ntFile) ;
      ntManager_.registerNodeTypes(in, ExtendedNodeTypeManager.IGNORE_IF_EXISTS) ;
    }catch(Exception e){
      e.printStackTrace() ;
    }
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
  
  private void calendarMigration() {
    try{     
      String calendarData13 = params_.getValueParam("calendarData1.3").getValue() ;
      String calendarData20 = params_.getValueParam("calendarData2.1").getValue() ;
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        //renameOldData(username);
        Node tempNode = getCalendarTemp(username) ;
        if (tempNode.hasNodes()) return ;
        try {
          InputStream in = configManager_.getInputStream(getFilenameByUser(calendarData13, username)) ;
          tempNode.getSession().importXML(tempNode.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          tempNode.getSession().save() ;
          migrateCalendarData(username) ;
          Node userCalendarHome = getUserCalendarAppHomeNode(username);
          ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
          userCalendarHome.getSession().exportSystemView(userCalendarHome.getPath(), bos, false, false) ;
          String newCalendarData20 = getFilenameByUser(calendarData20, username);
          saveToFile(bos.toByteArray(), newCalendarData20) ;
          tempNode.remove();
          userCalendarHome.getSession().save() ;
          System.out.println(">>>>>> Calendar data version 1.3 has exported to file " + newCalendarData20);
        } catch (FileNotFoundException e) { }        
      }
      Node publicTempNode = getPublicCalendarTemp();
      InputStream inReminder = configManager_.getInputStream(getPublicCalendarFilename(calendarData13)) ;
      publicTempNode.getSession().importXML(publicTempNode.getPath(), inReminder, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
      publicTempNode.getSession().save() ;
      migratePublicCalendarData() ;
      Node publicCalendarHome = getPublicCalendarAppHome();
      ByteArrayOutputStream bosReminder = new ByteArrayOutputStream() ;

      publicCalendarHome.getSession().exportSystemView(publicCalendarHome.getPath(), bosReminder, false, false) ;
      String newReminderData20 = getPublicCalendarFilename(calendarData20);
      saveToFile(bosReminder.toByteArray(), newReminderData20) ;
      publicTempNode.remove();
      publicCalendarHome.getSession().save() ;       
      System.out.println("Colaboration Suite: Migrate Calendar data 1.3 to 2.1 is finished ..");
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void contactMigration() {
    try{     
      String contactData13 = params_.getValueParam("contactData1.3").getValue() ;
      String contactData20 = params_.getValueParam("contactData2.1").getValue() ;
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node tempNode = getContactTemp(username);
        if (tempNode.hasNodes()) return ;
        try {
          InputStream in = configManager_.getInputStream(getFilenameByUser(contactData13, username)) ;
          tempNode.getSession().importXML(tempNode.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          tempNode.getSession().save() ;          
          migrateContactData(username) ;
          Node userContactHome = getUserContactAppHomeNode(username);
          ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
          userContactHome.getSession().exportSystemView(userContactHome.getPath(), bos, false, false) ;
          String newContactData20 = getFilenameByUser(contactData20, username);
          saveToFile(bos.toByteArray(), newContactData20) ;
          userContactHome.getSession().save() ;
          System.out.println(" >>>>>> Contact data version 1.3 has exported to file " + newContactData20);          
        } catch (FileNotFoundException e) {}
      }
      System.out.println("Colaboration Suite: Migrate Contact data 1.3 to 2.1 is finished ");
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void mailMigration() {
    try{     
      String mailData13 = params_.getValueParam("mailData1.3").getValue() ;
      String mailData20 = params_.getValueParam("mailData2.1").getValue() ;
      NodeIterator iteratorUsers = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iteratorUsers.hasNext()) {
        String userPath = iteratorUsers.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node tempNode = getMailTemp(username);
        if (tempNode.hasNodes()) return ;
        try {
          InputStream in = configManager_.getInputStream(getFilenameByUser(mailData13, username)) ;
          tempNode.getSession().importXML(tempNode.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
          tempNode.getSession().save() ;
          migrateMailData(username) ;
          Node userMailHome = getUserMailAppHomeNode(username);
          ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
          userMailHome.getSession().exportSystemView(userMailHome.getPath(), bos, false, false) ;
          String newMailData20 = getFilenameByUser(mailData20, username);
          saveToFile(bos.toByteArray(), newMailData20) ;
          System.out.println("Mail data version 1.3 has exported to file " + newMailData20);          
        } catch (FileNotFoundException e) {}
      }
      System.out.println("Colaboration Suite: Migrate Mail data 1.3 to 2.1 is finished ....");
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }

  public Node getPersonalAddressBooksHome(String username) throws Exception {
    Node userDataHome = getUserContactAppHomeNode(username);
    try {
      return userDataHome.getNode(PERSONAL_ADDRESS_BOOKS) ;
    } catch (PathNotFoundException ex) {
      Node padHome = userDataHome.addNode(PERSONAL_ADDRESS_BOOKS, Utils.NT_UNSTRUCTURED) ;
      userDataHome.save() ;
      return padHome ;
    }
  }
  
  private void migrateCalendarData(String username) {
    try{   
      Node tempData = getCalendarTemp(username) ;
      if(!tempData.hasNodes()) {
        System.out.println("\n >>>>>> There is no Calendar's data for migrating! \n") ;
        return ;
      }
      Node calendarTempHome = tempData.getNode(Utils.CALENDAR_APP) ;
      if(calendarTempHome.hasNode(CALENDAR_SETTING)) {
        Node appNode = getUserCalendarAppHomeNode(username);
        if (appNode.hasNode(CALENDAR_SETTING)) {
          appNode.getNode(CALENDAR_SETTING).remove();
          appNode.getSession().save();
        }
        appNode.getSession().getWorkspace().move(
          calendarTempHome.getNode(CALENDAR_SETTING).getPath(), appNode.getPath() + "/" + CALENDAR_SETTING);
        appNode.getSession().save(); 
      }
      if(calendarTempHome.hasNode(EVENT_CATEGORIES)) {
        migrateEventCategories(username, calendarTempHome.getNode(EVENT_CATEGORIES)) ;
      }
      if(calendarTempHome.hasNode(CALENDAR_CATEGORIES)) {
        migrateCalendarCategories(username, calendarTempHome.getNode(CALENDAR_CATEGORIES)) ;
      }
      if(calendarTempHome.hasNode(CALENDARS)) {
        migrateCalendars(username, calendarTempHome.getNode(CALENDARS)) ;
      } 
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void migrateMailData(String username) {
    try{     
      Node tempData = getMailTemp(username);
      if(!tempData.hasNodes()) {
        System.out.println("\n >>>>>> There is no Mail's data for migrating! \n") ;
        return ;
      }
      Node mailTempHome = tempData.getNode(MAIL_SERVICE) ;
      Node appNode = getUserMailAppHomeNode(username);
      if (appNode.hasNode(KEY_MAIL_SETTING)) appNode.getNode(KEY_MAIL_SETTING).remove();
      NodeIterator it = mailTempHome.getNodes();
      while (it.hasNext()) {
        Node node = it.nextNode();
        if (node.isNodeType("exo:account"))
          appNode.getSession().move(node.getPath(), appNode.getPath() + "/" + node.getName());
      }
      appNode.getSession().save();
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private Node getSharedHome(String userId) throws Exception {
    Node contactHome = getUserContactAppHomeNode(userId);
    Node sharedHome ;
    try {
      sharedHome = contactHome.getNode(SHARED_HOME) ;
    } catch (PathNotFoundException ex) {
      sharedHome = contactHome.addNode(SHARED_HOME, NT_UNSTRUCTURED) ;
      contactHome.save() ;
    }    
    try{
      return sharedHome.getNode(SHARED_CONTACT) ;
    }catch(PathNotFoundException ex) {
      Node sharedContact = sharedHome.addNode(SHARED_CONTACT, NT_UNSTRUCTURED) ;
      if(sharedContact.canAddMixin("mix:referenceable")) {
        sharedContact.addMixin("mix:referenceable") ;
      }
      sharedHome.save() ;
      return sharedContact ;
    }
  }  
  
  private Node getSharedAddressBooksHome(String userId) throws Exception {
      Node contactHome = getUserContactAppHomeNode(userId);
      Node sharedHome;
      try {
        sharedHome = contactHome.getNode(SHARED_HOME);
      } catch (PathNotFoundException ex) {
        sharedHome = contactHome.addNode(SHARED_HOME, NT_UNSTRUCTURED);
        contactHome.save();
      }
      try {
        return sharedHome.getNode(SHARED_ADDRESSBOOK);
      } catch (PathNotFoundException ex) {
        Node sharedAddressBooksHome = sharedHome.addNode(SHARED_ADDRESSBOOK, NT_UNSTRUCTURED);
        if (sharedAddressBooksHome.canAddMixin("mix:referenceable")) {
          sharedAddressBooksHome.addMixin("mix:referenceable");
        }
        sharedHome.save();
        return sharedAddressBooksHome;
      }
  }
  
  private void migrateContactData(String username) {
    try{
      Node tempData = getContactTemp(username);
      if(!tempData.hasNodes()) {
        System.out.println("\n >>>>>> There is no Contact's data for migrating! \n") ;
        return ;
      }
      Node contactTempHome = tempData.getNode(CONTACT_APP) ;
      if(contactTempHome.hasNode(PERSONAL_ADDRESS_BOOKS)) {
        Node groupHome = getPersonalAddressBooksHome(username);
        NodeIterator iteratorGroup = contactTempHome.getNode(PERSONAL_ADDRESS_BOOKS).getNodes();
        while (iteratorGroup.hasNext()) {
          Node group = iteratorGroup.nextNode();
          if (!groupHome.hasNode(group.getName()))
            groupHome.getSession().getWorkspace().move(
              group.getPath(), groupHome.getPath() + "/" + group.getName());          
        }
        groupHome.getSession().save();
      }
      if(contactTempHome.hasNode(SHARED_HOME)) {
        Node shareHome = contactTempHome.getNode(SHARED_HOME);
        if (shareHome.hasNode(SHARED_ADDRESSBOOK)) {          
          Node groupHome = getSharedAddressBooksHome(username);
          NodeIterator iteratorGroup = shareHome.getNode(SHARED_ADDRESSBOOK).getNodes();
          while (iteratorGroup.hasNext()) {
            Node group = iteratorGroup.nextNode();
            if (!groupHome.hasNode(group.getName()))
              groupHome.getSession().getWorkspace().move(
                group.getPath(), groupHome.getPath() + "/" + group.getName());          
          }
          groupHome.getSession().save();
        }
        if (shareHome.hasNode(SHARED_ADDRESSBOOK)) {          
          Node groupHome = getSharedHome(username);
          NodeIterator iteratorGroup = shareHome.getNode(SHARED_CONTACT).getNodes();
          while (iteratorGroup.hasNext()) {
            Node group = iteratorGroup.nextNode();
            if (!groupHome.hasNode(group.getName()))
              groupHome.getSession().getWorkspace().move(
                group.getPath(), groupHome.getPath() + "/" + group.getName());          
          }
          groupHome.getSession().save();
        }
      }
      if(contactTempHome.hasNode(TAGS)) {
        Node groupHome = getTagsHome(username);
        NodeIterator iteratorGroup = contactTempHome.getNode(TAGS).getNodes();
        while (iteratorGroup.hasNext()) {
          Node group = iteratorGroup.nextNode();
          if (!groupHome.hasNode(group.getName()))
            groupHome.getSession().getWorkspace().move(
              group.getPath(), groupHome.getPath() + "/" + group.getName());          
        }
        groupHome.getSession().save();
      }
      if(contactTempHome.hasNode(CONTACTS)) {
        migrateContacts(username, contactTempHome.getNode(CONTACTS)) ;
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  public Node getTagsHome(String username) throws Exception {
    Node contactServiceHome = getUserContactAppHomeNode(username);
    try {
      return contactServiceHome.getNode(TAGS) ;
    } catch (PathNotFoundException ex) {
      Node tagHome = contactServiceHome.addNode(TAGS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return tagHome ;
    } 
  }
  
  private void migratePublicCalendarData() {
    try {
      Node publicCalendarHome = getPublicCalendarAppHome();
      Node tempPublicCalendarApp = getPublicCalendarTemp().getNode(Utils.CALENDAR_APP);
      NodeIterator iterator = tempPublicCalendarApp.getNodes();
      while (iterator.hasNext()) {
        Node node = iterator.nextNode();
        String nodeName = node.getName();
        if (nodeName.contains(CALENDARS)) {
          migratePublicCalendars(node);
        } else {
          publicCalendarHome.getSession().getWorkspace().move(node.getPath(), publicCalendarHome.getPath() + "/" + nodeName);
          publicCalendarHome.getSession().save();
        }
      }
      Node parent = tempPublicCalendarApp.getParent() ;
      tempPublicCalendarApp.remove() ;     
      parent.save() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void repairContacts(Node tempNode) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try{
      Session session = tempNode.getSession();
      if (!session.isLive()) {
        session = sessionProvider.getSession(tempNode.getSession()
          .getWorkspace().getName(), (ManageableRepository)tempNode.getSession().getRepository());        
      }
      QueryManager qm = session.getWorkspace().getQueryManager();
      StringBuffer queryBuffer = new StringBuffer();
      queryBuffer.append("/jcr:root").append(tempNode.getPath())
        .append("//element(*,exo:contact)") ;
      Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while(iter.hasNext()) {
        Node contact = iter.nextNode() ;
        try {
          if (contact.hasProperty("exo:emailAddress")) {
            try {
              Value[] values = contact.getProperty("exo:emailAddress").getValues();
              List<String> newEmails = new ArrayList<String>();
              for (Value value : values) {
                String[] emails = value.getString().split(";");
                for (String email : emails) {
                  newEmails.add(email.trim());
                }
              }
              contact.setProperty("exo:emailAddress", newEmails.toArray(new String[newEmails.size()]));          
            } catch (Exception e) {
              e.printStackTrace();
            }
          }          
        } catch (Exception e) {
          e.printStackTrace();
        }        
        contact.save() ;
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    } finally {
      sessionProvider.close();
    }
  }
  
  private void repairCalendars(Node tempNode) {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try{
      Session session = tempNode.getSession();
      if (!session.isLive()) {
        session = sessionProvider.getSession(tempNode.getSession()
          .getWorkspace().getName(), (ManageableRepository)tempNode.getSession().getRepository());        
      }      
      QueryManager qm = session.getWorkspace().getQueryManager();
      StringBuffer queryBuffer = new StringBuffer();
      queryBuffer.append("/jcr:root").append(tempNode.getPath())
        .append("//element(*,exo:calendar)[@exo:publicUrl='null' and @exo:privateUrl='null']") ;
      Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while(iter.hasNext()) {
        Node calendar = iter.nextNode() ;
        calendar.setProperty("exo:publicUrl", " migrate public url ") ;
        calendar.setProperty("exo:privateUrl", " migrate private url ") ;
        calendar.save() ;
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    } finally {
      sessionProvider.close();
    }
  }
  
  private void migratePublicCalendars(Node tempCalendarHome) {
    try{
      repairCalendars(tempCalendarHome);
      Node calendarHome ;
      Node publicApp = getPublicCalendarAppHome();
      try {
        calendarHome = publicApp.getNode(CALENDARS);
      } catch ( Exception e) {
        calendarHome = publicApp.addNode(CALENDARS);
        publicApp.getSession().save();
      }
      NodeIterator iter = tempCalendarHome.getNodes() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        calendarHome.getSession().getWorkspace().move(node.getPath(), calendarHome.getPath() + "/" + node.getName()) ;
      }
      calendarHome.getSession().save() ; 
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void migrateCalendars(String username, Node tempCalendarHome) {
    try{
      repairCalendars(tempCalendarHome);
      Node calendarHome = getUserCalendarHome(username);
      NodeIterator iter = tempCalendarHome.getNodes() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        if (!calendarHome.hasNode(node.getName()))
          calendarHome.getSession().getWorkspace().move(node.getPath(), calendarHome.getPath() + "/" + node.getName()) ;
      }
      calendarHome.getSession().save() ; 
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void migrateContacts(String username, Node tempContactHome) {
    try{
      repairContacts(tempContactHome);
      Node contactHome = getUserContactAppHomeNode(username).getNode(CONTACTS);
      NodeIterator iter = tempContactHome.getNodes() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        if (!contactHome.hasNode(node.getName()))
          contactHome.getSession().getWorkspace().move(node.getPath(), contactHome.getPath() + "/" + node.getName()) ;
      }
      contactHome.getSession().save() ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private Node getUserCalendarHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarAppHomeNode(username);
    try {
      return calendarServiceHome.getNode(CALENDARS) ;
    } catch (Exception e) {
      Node calendars = calendarServiceHome.addNode(CALENDARS, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calendars ;
    }
  }
  
  private void migrateCalendarCategories(String username, Node tempCalendarCategoriesHome) {
    try{
      Node calendarCategoryHome = getCalendarCategoryHome(username);
      NodeIterator iter = tempCalendarCategoriesHome.getNodes() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        if (!calendarCategoryHome.hasNode(node.getName()))
          calendarCategoryHome.getSession().getWorkspace().move(node.getPath(), calendarCategoryHome.getPath() + "/" + node.getName()) ;
      }
      calendarCategoryHome.getSession().save() ;      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  protected Node getCalendarCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarAppHomeNode(username);
    try {
      return calendarServiceHome.getNode(CALENDAR_CATEGORIES) ;
    } catch (Exception e) {
      Node calCat = calendarServiceHome.addNode(CALENDAR_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return calCat;
    }
  }
  
  private void migrateEventCategories(String username, Node tempEventCategoriesHome) {
    try{
      Node eventCategoryHome = getEventCategoryHome(username);
      NodeIterator iter = tempEventCategoriesHome.getNodes() ;
      while(iter.hasNext()) {
        Node node = iter.nextNode() ;
        if (!eventCategoryHome.hasNode(node.getName()))
          eventCategoryHome.getSession().getWorkspace().move(node.getPath(), eventCategoryHome.getPath() + "/" + node.getName()) ;
      }
      eventCategoryHome.getSession().save() ;      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private Node getEventCategoryHome(String username) throws Exception {
    Node calendarServiceHome = getUserCalendarAppHomeNode(username);
    try {
      return calendarServiceHome.getNode(EVENT_CATEGORIES) ;
    } catch (Exception e) {
      Node eventCat = calendarServiceHome.addNode(EVENT_CATEGORIES, Utils.NT_UNSTRUCTURED) ;
      calendarServiceHome.getSession().save() ;
      return eventCat ; 
    }
  }
  
  private Node getCalendarSettingHome(String username) throws Exception {
    try {
      return getUserCalendarAppHomeNode(username).getNode(CALENDAR_SETTING) ; 
    } catch (Exception e) {
      return getUserCalendarAppHomeNode(username).addNode(CALENDAR_SETTING, Utils.EXO_CALENDAR_SETTING);
    }
  }
  private Node getCalendarTemp(String username) throws Exception {
    Node userApp = getUserCalendarAppHomeNode(username);
    try {
      return  userApp.getNode(CALENDAR_TEMP) ;
    } catch (PathNotFoundException ex) {
      userApp.addNode(CALENDAR_TEMP, "nt:unstructured") ;
      userApp.getSession().save() ;
      return userApp.getNode(CALENDAR_TEMP) ;
    }
  }
  
  private Node getContactTemp(String username) throws Exception {
    Node userApp = getUserContactAppHomeNode(username);
    try {
      return  userApp.getNode(CONTACT_TEMP) ;
    } catch (PathNotFoundException ex) {
      userApp.addNode(CONTACT_TEMP, "nt:unstructured") ;
      userApp.getSession().save() ;
      return userApp.getNode(CONTACT_TEMP) ;
    }
  }
  
  private Node getMailTemp(String username) throws Exception {
    Node userApp = getUserMailAppHomeNode(username);
    try {
      return  userApp.getNode(MAIL_TEMP) ;
    } catch (PathNotFoundException ex) {
      userApp.addNode(MAIL_TEMP, "nt:unstructured") ;
      userApp.getSession().save() ;
      return userApp.getNode(MAIL_TEMP) ;
    }
  }
  
  private Node getPublicCalendarTemp() throws Exception {
    Node publicApp = getPublicCalendarAppHome();
    try {
      return  publicApp.getNode(CALENDAR_TEMP) ;
    } catch (PathNotFoundException ex) {
      publicApp.addNode(CALENDAR_TEMP, "nt:unstructured") ;
      publicApp.getSession().save() ;
      return publicApp.getNode(CALENDAR_TEMP) ;
    }   
  }
  
  private void exportCalendarData1_3() {
    try{
      String file = params_.getValueParam("calendarData1.3").getValue() ;
      NodeIterator iterator = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iterator.hasNext()) {
        String userPath = iterator.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node userCalendarHome = getUserCalendarAppHomeNode(username);
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        userCalendarHome.getSession().exportSystemView(userCalendarHome.getPath(), bos, false, false) ;
        String fileName = getFilenameByUser(file, username);
        saveToFile(bos.toByteArray(), fileName) ;
        System.out.println("Calendar data has exported to " + fileName) ;
      }
      Node publicHome = getPublicCalendarAppHome();      
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      publicHome.getSession().exportSystemView(publicHome.getPath(), bos, false, false) ;
      saveToFile(bos.toByteArray(), getPublicCalendarFilename(file)) ;      
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void exportContactData1_3() {
    try{      
      String file = params_.getValueParam("contactData1.3").getValue() ;
      NodeIterator iterator = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iterator.hasNext()) {
        String userPath = iterator.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node userContactHome = getUserContactAppHomeNode(username);        
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        userContactHome.getSession().exportSystemView(userContactHome.getPath(), bos, false, false) ;
        String fileName = getFilenameByUser(file, username);
        saveToFile(bos.toByteArray(), fileName) ;
        System.out.println("Contact data has exported to " + fileName ) ;
      }     
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void exportMailData1_3() {
    try{      
      String file = params_.getValueParam("mailData1.3").getValue() ;
      NodeIterator iterator = getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider()).getNodes();
      while (iterator.hasNext()) {
        String userPath = iterator.nextNode().getPath(); 
        String username = userPath.substring(userPath.lastIndexOf("/") + 1);
        Node userMailHome = getUserMailAppHomeNode(username);        
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        userMailHome.getSession().exportSystemView(userMailHome.getPath(), bos, false, false) ;
        String fileName = getFilenameByUser(file, username);
        saveToFile(bos.toByteArray(), fileName) ;
        System.out.println("\nMail data has exported to " + fileName + "\n") ;
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void saveToFile(byte[] data, String path) throws Exception {
    FileOutputStream file = new FileOutputStream(path.replaceAll("file:", "")) ;   
    file.write(data) ;
    file.flush() ;
    file.close() ;
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
