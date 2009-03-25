package tool.org.exoplatform.cs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;

import tool.org.exoplatform.cs.CalendarUpdateStoragePlugin;
import tool.org.exoplatform.cs.CsNodeTypeMapping;
import tool.org.exoplatform.cs.CsObjectParam;
import tool.org.exoplatform.cs.CsPropertyMapping;
import tool.org.exoplatform.cs.UpdateStorageEventListener;
import tool.org.exoplatform.cs.MailUpdateStoragePlugin;
import tool.org.exoplatform.cs.MigrationService;

public class MigrationServiceImpl implements MigrationService, Startable {
	/**
	 * Constants
	 */
	public static final String DUMP_DATA = "dump-data";
	public static final String REMOVE_DATA = "remove-data";
	public static final String REMOVE_NT = "remove-nodetypes";
	public static final String REMOVE_NT2 = "remove-nodetypes2";
	public static final String IMPORT_DATA = "import-data";
	public static final String ARRANGE_DATA = "arrange-data";
	public static final String REGISTER_NT = "register-nodetypes";
	public static final String IMPORT_BACKUP = "import-backup";

	private Session _session;
	private RepositoryService _repoService;
	private InitParams _params;
  public CsObjectParam mailCsObj_, calendarCsObj_, contactCsObj_;

	public MigrationServiceImpl(InitParams params, RepositoryService service) {
		_repoService = service;
		_params = params;
	}

public List<UpdateStorageEventListener> listeners_ = new ArrayList<UpdateStorageEventListener>();
  
  public synchronized void addListenerPlugin(ComponentPlugin listener) throws Exception {
    if(listener instanceof UpdateStorageEventListener ) {
      listeners_.add((UpdateStorageEventListener)listener) ;
    }
  }
  
  public void initUpdatedData() {
    for (UpdateStorageEventListener updateListener : listeners_) {
      if (updateListener instanceof MailUpdateStoragePlugin) {
        MailUpdateStoragePlugin mailPlugin = (MailUpdateStoragePlugin) updateListener;
        mailCsObj_ = mailPlugin.getCsObjectParam();
      } else if (updateListener instanceof CalendarUpdateStoragePlugin) { 
        CalendarUpdateStoragePlugin calendarPlugin = (CalendarUpdateStoragePlugin) updateListener;
        calendarCsObj_ = calendarPlugin.getCsObjectParam();
      }  
    }
  }
  
  public List<CsNodeTypeMapping> getMailCsNoteTypeMapping() {
    return mailCsObj_.getNodeTypes();
  }
  
  public List<CsNodeTypeMapping> getCalendarCsNoteTypeMapping() {
    return calendarCsObj_.getNodeTypes();
  }
  
  public List<CsPropertyMapping> getAddedProperty(CsNodeTypeMapping nt) {
    return nt.getAddedProperties();
  }
  
  public List<CsPropertyMapping> getUpdatedProperty(CsNodeTypeMapping nt) {
    return nt.getUpdatedProperties();
  }
  
  public List<CsPropertyMapping> getRemovedProperty(CsNodeTypeMapping nt) {
    return nt.getRemovedProperties();
  }
  
	public void start() {
		System.out.println("######### Start MigrationService #########");
    
    initUpdatedData();    
		try {
			initService();	
				
			if (_params.containsKey(IMPORT_BACKUP))
				importData(_params.getValueParam(IMPORT_BACKUP).getValue().trim());
			if (_params.containsKey(DUMP_DATA))
				dumpData(_params.getValueParam(DUMP_DATA).getValue().trim());
			if (_params.containsKey(REMOVE_DATA))
				removeData();
			if (_params.containsKey(REMOVE_NT))
				removeNT();
			if (_params.containsKey(REGISTER_NT)) {
				registerNodeTypes(_params.getValueParam(REGISTER_NT).getValue()
						.trim());
			}
			if (_params.containsKey(IMPORT_DATA)) {
				importData(_params.getValueParam(IMPORT_DATA).getValue().trim());
			}
			if (_params.containsKey(ARRANGE_DATA)) {
				arrangeData();
			}
			if (_params.containsKey(REMOVE_NT2)) {
				removeNT2();
			}			
		} catch (Exception e) {
			System.out.println("Migration was interrupted due to this unexpected condition:");
			e.printStackTrace();
		} finally {
			if (_session != null) {
				_session.logout();
			}
			System.out.println("######### End MigrationService #########");
		}

	}

	/**
	 * importing data from /exo:applications and /Users
	 */
	private void importData(String exportPath) throws Exception {
		System.out.println("######### Begin import data #########");
		Node rootNode = _session.getRootNode();
		importNode(exportPath, rootNode, "exo:applications");
		importNode(exportPath, rootNode, "Users");
		System.out.println("######### End import data #########");
	}

	public void stop() {
		if (_session.isLive())
			_session.logout();
		System.out.println("JCR session logged out.");
	}

	public void initService() throws Exception {
		System.out.println("Initializing service...");
		// Gets jcr session on repository : repository (default); workspace :
		// collaboration
		String workspace = "collaboration";
		SessionProvider sessionProvider = SessionProvider
				.createSystemProvider();
		_session = sessionProvider.getSession(workspace, _repoService
				.getDefaultRepository());
		System.out.println("JCR session initialized on workspace " + workspace);
	}

	/**
	 * Steps for migration
	 */

	/**
	 * Dumping data in /exo:applications and /Users
	 */
	public void dumpData(String exportPath) throws Exception {

		System.out.println("######### Begin dump data #########");
		Node rootNode = _session.getRootNode();
		if (rootNode.hasNode("exo:applications")) {
			exportNode(exportPath, rootNode.getNode("exo:applications"));
		}
		if (rootNode.hasNode("Users")) {
			exportNode(exportPath, rootNode.getNode("Users"));
		}
		System.out.println("######### End dump data #########");
	}

	/**
	 * Deleting /exo:applications and /Users
	 */
	public void removeData() throws Exception {
		System.out.println("######### Begin remove data #########");
		removeNode("/exo:applications");
		removeNode("/Users");
		System.out.println("######### End remove data #########");
	}

	/**
	 * remove exo:reminder and exo:userProfile nodetypes
	 * 
	 * @throws Exception
	 */
	public void removeNT() throws Exception {
		System.out.println("######### Begin remove nodetypes #########");
		for (CsNodeTypeMapping nt : getMailCsNoteTypeMapping()) {
      removeNodeType(nt.getNodeTypeName());
    }
    for (CsNodeTypeMapping nt : getCalendarCsNoteTypeMapping()) {
      removeNodeType(nt.getNodeTypeName());
    }
		System.out.println("######### End remove nodetypes #########");

	}
	
	public void removeNT2() throws Exception {
		System.out.println("######### Begin remove modified nodetypes #########");
		removeNodeType("exo:forumAttachment");
		removeNodeType("exo:post");
		removeNodeType("exo:topic");
		removeNodeType("exo:forum");
		removeNodeType("exo:forumCategory");
		removeNodeType("exo:userProfile");
		removeNodeType("exo:forumStatistic");
		removeNodeType("exo:administration");
		removeNodeType("exo:forumWatching");
		System.out.println("######### End remove modified nodetypes #########");

	}	

	/**
	 * migrate reminders and profiles
	 * 
	 * @throws Exception
	 */
	public void arrangeData() throws Exception {
		System.out.println("######### Begin arrange nodes #########");
    for (CsNodeTypeMapping nt : getMailCsNoteTypeMapping()) {
      fixNodeType(nt);
    }
    for (CsNodeTypeMapping nt : getCalendarCsNoteTypeMapping()) {
      fixNodeType(nt);
    }
		System.out.println("######### End arrange nodes #########");
	}

  private void fixNodeType(CsNodeTypeMapping nt) throws Exception {
    System.out.println(">>>>>> Start " + nt.getNodeTypeName() + " upgrade");
    traverseNodes("select * from " + nt.getNodeTypeName() + " ", new NodeVisitor() {
      public void visitNode(Node node) throws Exception {
        List<CsNodeTypeMapping> modifiedNodeType = getMailCsNoteTypeMapping();
        modifiedNodeType.addAll(getCalendarCsNoteTypeMapping());
        for (CsNodeTypeMapping nt : modifiedNodeType) {
          // processing for added properties
          for (CsPropertyMapping prop : getAddedProperty(nt)) {
            try {
              if (prop.getDefaultValue().trim().equalsIgnoreCase("boolean")) {
                setPropertyIfAbsent(node, prop.getPropertyName(), Boolean.valueOf(prop.getDefaultValue()));
              } else if (prop.getDefaultValue().trim().equalsIgnoreCase("string")) {
                setPropertyIfAbsent(node, prop.getPropertyName(), prop.getDefaultValue());
              } else if (prop.getDefaultValue().trim().equalsIgnoreCase("long")) {
                setPropertyIfAbsent(node, prop.getPropertyName(), Long.valueOf(prop.getDefaultValue()));
              }
            } catch (Exception e) {
              System.out.println("Failed to upgrade forum category: "
                  + node.getPath() + ": " + e.getMessage());
            }
            
          }
          
          // processing for removed properties
          for (CsPropertyMapping prop : getRemovedProperty(nt)) {
            try {
              deleteProperty(node, prop.getPropertyName());
            } catch (Exception e) {
              System.out.println("Failed to upgrade forum category: "
                  + node.getPath() + ": " + e.getMessage());
            }
          }
          
          // processing for renamed properties
          for (CsPropertyMapping prop : getUpdatedProperty(nt)) {
            try {
              copyStringProperty(node, prop.getPropertyName(), prop.getReplaceName());
            } catch (Exception e) {
              System.out.println("Failed to upgrade " + nt.getNodeTypeName() + " : "
                  + node.getPath() + ": " + e.getMessage());
            }
          }
        }
      }
    });
    System.out.println("<<<<<< End " + nt.getNodeTypeName() + " upgrade");
  }
  
//	/**
//	 * in forum, remove exo:viewForumRole and exo:replyTopicRole
//	 * @throws Exception
//	 */
//	private void fixForums() throws Exception {
//		System.out.println(">>>>>> start forums upgrade");
//		traverseNodes("select * from exo:forum", new NodeVisitor() {
//			public void visitNode(Node node) throws Exception {
//				try {
//					deleteProperty(node, "exo:viewForumRole");
//					deleteProperty(node, "exo:replyTopicRole");
//					
//				} catch (Exception e) {
//					System.out.println("Failed to upgrade forum : "
//							+ node.getPath() + ": " + e.getMessage());
//				}
//			}
//		});
//		System.out.println("<<<<<< End forums upgrade");
//	}
//	
//	/**
//	 * in exo:forumCategory remove exo:userPrivate
//	 * @throws Exception
//	 */
//	private void fixForumCategories() throws Exception {
//		System.out.println(">>>>>> start forum categories upgrade");
//		traverseNodes("select * from exo:forumCategory", new NodeVisitor() {
//
//			public void visitNode(Node node) throws Exception {
//				try {
//					deleteProperty(node, "exo:userPrivate");
//
//					
//				} catch (Exception e) {
//					System.out.println("Failed to upgrade forum category: "
//							+ node.getPath() + ": " + e.getMessage());
//				}
//			}
//		});
//		System.out.println("<<<<<< End forums categories upgrade");
//	}
//	
//	
//	/**
//	 * in calendar, exo:reminder old property : exo:owner new property :
//	 * exo:creator (rename property)
//	 */
//	private void fixReminders() throws Exception {
//		System.out.println(">>>>>> start calendar reminders upgrade");
//		traverseNodes("select * from exo:reminder", new NodeVisitor() {
//
//			public void visitNode(Node node) throws Exception {
//				try {
//					copyStringProperty(node, "exo:owner", "exo:creator");
//				} catch (Exception e) {
//					System.out.println("Failed to upgrade reminder : "
//							+ node.getPath() + ": " + e.getMessage());
//				}
//			}
//		});
//		System.out.println("<<<<<< End calendar reminders upgrade");
//	}
//
//  
//	/**
//	 * in forum, exo:userProfile remove property exo:moderateTopics remove
//	 * property exo:totalMessage
//	 */
//  private void fixProfiles() throws Exception {
//		System.out.println(">>>>>> start forum user profiles upgrade");
//
//		traverseNodes("select * from exo:userProfile", new NodeVisitor() {
//
//			public void visitNode(Node node) throws Exception {
//				try {
//					deleteProperty(node, "exo:moderateTopics");
//					deleteProperty(node, "exo:totalMessage");
//					setPropertyIfAbsent(node, "exo:newMessage", 0L);
//					setPropertyIfAbsent(node, "exo:userTitle", "User");
//					setPropertyIfAbsent(node, "exo:fullName", " ");
//					setPropertyIfAbsent(node, "exo:firstName", " ");
//					setPropertyIfAbsent(node, "exo:email", " ");
//					setPropertyIfAbsent(node, "exo:lastName", " ");
//					setPropertyIfAbsent(node, "exo:userRole", 2L);
//					setPropertyIfAbsent(node, "exo:signature", " ");
//					setPropertyIfAbsent(node, "exo:totalPost", 0L);
//					setPropertyIfAbsent(node, "exo:totalTopic", 0L);
//					setPropertyIfAbsent(node, "exo:moderateForums", " ");
//					setPropertyIfAbsent(node, "exo:readTopic", " ");
//					setPropertyIfAbsent(node, "exo:isDisplaySignature", false);
//					setPropertyIfAbsent(node, "exo:isDisplayAvatar", false);
//					setPropertyIfAbsent(node, "exo:timeZone", 0.0);
//					setPropertyIfAbsent(node, "exo:timeFormat", "hh:mm a");
//					setPropertyIfAbsent(node, "exo:shortDateformat", "MM/dd/yyyy"); 
//					setPropertyIfAbsent(node, "exo:longDateformat", "DDD,MMM dd,yyyy"); 
//					setPropertyIfAbsent(node, "exo:maxPost", 10L);
//					setPropertyIfAbsent(node, "exo:maxTopic", 10L);
//					setPropertyIfAbsent(node, "exo:isShowForumJump", true);
//					setPropertyIfAbsent(node, "exo:isBanned", false);
//					
//				} catch (Exception e) {
//					System.out.println("Failed to fix userProfile : "
//							+ node.getPath() + ": " + e.getMessage());
//				}
//			}
//	
//
//		});
//
//		System.out.println("<<<<<< End forum user profiles upgrade");
//	}

//	private void setPropertyIfAbsent(Node node, String propertyName, double value) throws Exception {
//		if (!node.hasProperty(propertyName)) {
//			node.setProperty(propertyName, value);
//			System.out.println("Set " + propertyName + "=" + value + " on " + node.getPath());
//		}
//	}
		
	
	private void setPropertyIfAbsent(Node node, String propertyName, long value) throws Exception {
		if (!node.hasProperty(propertyName)) {
			node.setProperty(propertyName, value);
			System.out.println("Set " + propertyName + "=" + value + " on " + node.getPath());
		}
	}
	
	private void setPropertyIfAbsent(Node node, String propertyName, String value) throws Exception {
		if (!node.hasProperty(propertyName)) {
			node.setProperty(propertyName, value);
			System.out.println("Set " + propertyName + "=" + value + " on " + node.getPath());
		}
	}		
	
	private void setPropertyIfAbsent(Node node, String propertyName, Boolean value) throws Exception {
		if (!node.hasProperty(propertyName)) {
			node.setProperty(propertyName, value);
			System.out.println("Set " + propertyName + "=" + value + " on " + node.getPath());
		}
	}	
	
	/**
	 * import an xml sysview. if the node exists it is deleted before. Name of
	 * the file i dataFolder/nodeName-sysview.xml
	 * 
	 * @param dataFolderPath
	 *            path for the folder where xml file is read
	 * @param rootNode
	 *            parent node where data is imported
	 * @param nodeName
	 *            name target name of the node to import (should be the same as
	 *            root of xml)
	 * @throws Exception
	 */
	private void importNode(String dataFolderPath, Node rootNode,
			String nodeName) throws Exception {

		if (rootNode.hasNode(nodeName)) {
			System.out.println("Found an existing node " + nodeName
					+ ". Removing before import.");
			rootNode.getNode(nodeName).remove();
			_session.save();
		}
		// now import xml data to rootNode
		String filePath = getDumpFilename(dataFolderPath, nodeName);
		File f = new File(filePath);
		System.out.println("Importing " + nodeName + " from " + filePath);
		if (!f.exists()) {
			throw new IllegalStateException("File to import does not exist : "
					+ filePath);
		}
		InputStream is = new FileInputStream(f);
		String parentPath = rootNode.getPath();
		_session.importXML(parentPath, is,
				ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
		System.out.println(nodeName + " imported successfully.");
	}

	/**
	 * 
	 * @param exportPath
	 * @param node
	 * @throws Exception
	 */
	private void exportNode(String exportPath, Node node) throws Exception {
		if (node == null) {
			throw new IllegalArgumentException("Could node export a null node");
		}
		String nodeName = node.getName();
		System.out.println("Exporting " + nodeName + "...");
		String exportFile = getDumpFilename(exportPath, nodeName);

		File f = new File(exportFile);
		if (!f.createNewFile()) {
			throw new IllegalStateException("File " + exportFile
					+ " already exists.");
		}
		OutputStream os = new FileOutputStream(f);
		_session.exportSystemView(node.getPath(), os, false, false);
		System.out.println("Exported " + nodeName + " to " + exportFile);

	}

	private String getDumpFilename(String exportPath, String nodeName) {
		String normalized = nodeName;
		if (nodeName.lastIndexOf(":") > 0) {
			normalized = nodeName.substring(nodeName.lastIndexOf(":") + 1);
		}
		return exportPath + "/" + normalized + "-sysview.xml";
	}

	private void removeNode(String absNodePath) throws Exception {
		if (!absNodePath.startsWith("/"))
			throw new IllegalArgumentException(absNodePath
					+ " is not an absolute path. Add leading '/'");
		String nodeName = absNodePath.substring(1);
		Node rootNode = _session.getRootNode();
		if (rootNode.hasNode(nodeName)) {
			System.out.println("About to delete" + absNodePath);
			rootNode.getNode(nodeName).remove();
			System.out
					.println("Node " + absNodePath + " deleted.");
			_session.save();
		} else {
			System.out.println("Node " + absNodePath
					+ " does not exist in this workspace.");
		}
	}

	private void removeNodeType(String ntName) throws Exception {
		Session sysSession = null;
		try {
			sysSession = SessionProvider.createSystemProvider().getSession(
					"system", _repoService.getDefaultRepository());

			Node ntNode = sysSession.getRootNode().getNode("jcr:system/jcr:nodetypes");
			Node reminderNT = ntNode.getNode(ntName);
			reminderNT.remove();

			sysSession.save();
			System.out.print(ntName + " nodetype removed");
		} finally {
			if (sysSession != null)
				sysSession.logout();
		}
	}

	private void traverseNodes(String sqlQuery, NodeVisitor visitor)
			throws Exception {
		QueryManager qman = _session.getWorkspace().getQueryManager();
		Query q = qman.createQuery(sqlQuery, Query.SQL);
		QueryResult result = q.execute();
		NodeIterator iter = result.getNodes();

		while (iter.hasNext()) {
			Node node = iter.nextNode();
			visitor.visitNode(node);
		}
		_session.save();
	}

	public interface NodeVisitor {
		public void visitNode(Node n) throws Exception;
	}

	public void renameStringProperty(Node node, String oldName, String newName)
			throws Exception {
		if (node.hasProperty(oldName)) {
			String value = node.getProperty(oldName).getString();
			node.setProperty(newName, value);
			node.getProperty(oldName).remove(); // remove old property
			System.out.println("Renamed property " + oldName + " to " + newName
					+ " on " + node.getPath() + ".");
		}
	}
	
	private void copyStringProperty(Node node, String oldName, String newName)
	throws Exception {
if (node.hasProperty(oldName)) {
	String value = node.getProperty(oldName).getString();
	node.setProperty(newName, value);
	System.out.println("Copied property " + oldName + " to " + newName
			+ " on " + node.getPath() + ".");
}
}

	private void deleteProperty(Node node, String propertyName)
			throws Exception {
		if (node.hasProperty(propertyName)) {
			node.getProperty(propertyName).remove();
			System.out.println("Deleted " + propertyName + " on " + node.getPath());
		}
	}

	private void registerNodeTypes(String nodeTypeFilesName) throws Exception {
		ExtendedNodeTypeManager ntManager = _repoService.getDefaultRepository()
				.getNodeTypeManager();
		InputStream inXml = new FileInputStream(nodeTypeFilesName);
		System.out.println("Trying register node types from xml-file "
				+ nodeTypeFilesName);
		ntManager.registerNodeTypes(inXml,
				ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
		System.out.println("Node types were registered from xml-file "
				+ nodeTypeFilesName);

	}

}
