/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.cs.datamigration;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.picocontainer.Startable;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          exo@exoplatform.com
 * Apr 18, 2011  
 */
public class MigrationService implements Startable{
  protected static Log log = ExoLogger.getLogger(MigrationService.class);
  
  private ExtendedNodeTypeManager ntManager_ ;
  private NodeHierarchyCreator nodeHierarchy_ ;
  private InitParams params_ ;
  private RepositoryService  repoService_ ;
  final private static String USERS_PATH = "usersPath".intern();
  private static String ACC_SHARED = "exo:accountShared";
  
  public MigrationService(NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
    nodeHierarchy_ = nodeHierarchy ;
    params_ = params ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    repoService_ = (RepositoryService)container.getComponentInstance(RepositoryService.class) ;
   }
  
  private void saveToFile(byte[] data, String path) throws Exception {
    FileOutputStream file = new FileOutputStream(path) ;   
    file.write(data) ;
    file.flush() ;
    file.close() ;
  }
  
  private Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }
  
  private Session getSession(SessionProvider sprovider) throws Exception{
    ManageableRepository currentRepo = repoService_.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }
  
  @Override
  public void start() {
    try{
      ntManager_ = repoService_.getCurrentRepository().getNodeTypeManager() ;
      NodeTypeIterator ntIter = ntManager_.getAllNodeTypes() ;
      boolean isVersion21 = true;
      while(ntIter.hasNext()){
        NodeType nt = ntIter.nextNodeType();
        if(nt.isNodeType(ACC_SHARED)) {
          isVersion21 = false;
          break;
        };
      }
      
      //export data to 2.1 format
      if(isVersion21){
        log.info("\n**********Begin exportion CS data***********");
        boolean expo1 = exportUserData();
        boolean expo2 = exportSharedData();
        if(expo1 &&  expo2) {
          log.info("EXO CS DATA EXPORTED SUCCESSFUL");
        } else {
          log.info("EXO CS DATA EXPORTED FAILURE");
        }
        log.info("**********End exportion CS data***********\n");
      }else {
        //import from 2.2 format
        log.info("\n**********Begin importion CS data***********");
        boolean impo1 = importUserData();
        boolean impo2 = importSharedData();
        if(impo1 &&  impo2) {
          log.info("EXO CS DATA IMPORTED SUCCESSFUL");
        } else {
          log.info("EXO CS DATA IMPORTED FAILURE");
        }
        log.info("**********End importion CS data***********\n");
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method start", e);
      }
    }
  }
  
  @Override
  public void stop() {
    log.info("\n\n\n");
  }
  
  public boolean exportUserData(){
    try {
      Node userDataNode = getUserDataRootNode();
      if(userDataNode == null) {
        log.info("Cannot get user data root node");
        return false;
      }else{
        String file = params_.getValueParam("userData2.1").getValue() ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        userDataNode.getSession().exportSystemView(userDataNode.getPath(), bos, false, false) ;
        saveToFile(bos.toByteArray(), file.replaceAll("file:", ""));
        log.info("CS User data 2.1 has exported to file: \"" + file + "\"") ;
      }
    } catch (Exception e) {
      log.error("Cannot export eXo CS data 2.1", e);
      return false;
    }
    return true;
  }
  
  public boolean exportSharedData(){
    try {
      Node sharedDataNode = getSharedDataRootNode();
      if(sharedDataNode == null) {
        log.info("Cannot get shared data root node");
        return false;
      }else{
        String file = params_.getValueParam("sharedData2.1").getValue() ;
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        sharedDataNode.getSession().exportSystemView(sharedDataNode.getPath(), bos, false, false) ;
        saveToFile(bos.toByteArray(), file.replaceAll("file:", ""));
        log.info("CS Shared data 2.1 has exported to file: \"" + file + "\"") ;
      }
    } catch (Exception e) {
      log.error("Cannot export eXo CS data 2.1" + 3, e);
      return false;
    }
    
    return true;
  }
  
  public Node getUserDataRootNode(){
    try {
      return getNodeByPath(nodeHierarchy_.getJcrPath(USERS_PATH), SessionProvider.createSystemProvider());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getUserDataRootNode", e);
      }
      return null;
    }
  } 

  
  public Node getSharedDataRootNode(){
    try {
      return nodeHierarchy_.getPublicApplicationNode(SessionProvider.createSystemProvider());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getSharedDataRootNode", e);
      }
      return null;
    }
  }
  
  public boolean importUserData(){
    try{
      String file = params_.getValueParam("userData2.1").getValue() ;
      log.info("\n CS User data version 2.2 is importing.... ");      
      Node userDataNode = getUserDataRootNode();
      InputStream in = new FileInputStream(file.replace("file:", ""));
      Node root = userDataNode.getParent();
      userDataNode.remove();
      root.getSession().save();
      if(root != null){
        root.getSession().importXML(root.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        root.getSession().save();
      }
      
    }catch (Exception e) {
      log.error("Cannot import CS User data.", e);
      return false;
    }
    
    return true;
  }
  
  public boolean importSharedData(){
    try{
      String file = params_.getValueParam("sharedData2.1").getValue() ;
      log.info("\n CS Shared data version 2.2 is importing.... ");      
      Node sharedDataNode = getSharedDataRootNode();
      InputStream in = new FileInputStream(file.replace("file:", ""));
      Node root = sharedDataNode.getParent();
      sharedDataNode.remove();
      root.getSession().save();
      
      if(root != null){
        root.getSession().importXML(root.getPath(), in, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        root.getSession().save();
      }
    }catch (Exception e) {
      log.error("Cannot import CS Shared data.", e);
      return false;
    }
    
    return true;
  }
}
