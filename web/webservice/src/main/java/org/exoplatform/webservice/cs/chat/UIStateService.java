/**
 * 
 */
package org.exoplatform.webservice.cs.chat;

import javax.jcr.Node;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.bean.UIStateDataBean;

/**
 * @author Uoc Nguyen
 *
 */
public class UIStateService implements ResourceContainer {
  public final static String JSON_CONTENT_TYPE    = "application/json";
  public final static String APPLICATION_NAME = "eXoChat";
  public final static String JCR_STATE_DATA_NODE_PATH = APPLICATION_NAME + "/uistate"; 
  public final static String JCR_STATE_NOTE_TYPE="lr:state";
  public final static String JCR_UI_STATE_NOTE_PROPERTY="lr:ui";
  private NodeHierarchyCreator nodeHierarchyCreator;
  /**
   * 
   */
  public UIStateService(NodeHierarchyCreator nodeHierarchyCreator) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
  }
  
  @POST 
  @Path("/chat/uistateservice/save/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  //@OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response saveState(@PathParam("username") String userName, UIStateDataBean stateData) throws Exception {
    try {
      Node uiStateNode = this.getPrivateNode(userName); 
      uiStateNode.setProperty(JCR_UI_STATE_NOTE_PROPERTY, stateData.getData());
      if (!uiStateNode.isNew()) {
        uiStateNode.save();
      } else {
        uiStateNode.getSession().save() ;
      }
    } catch (Exception e){
    }
    UIStateDataBean stateDataBean = new UIStateDataBean("null");
    return Response.ok(stateDataBean, JSON_CONTENT_TYPE).build();
  }
  
  @GET
  @Path("/chat/uistateservice/get/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getState(@PathParam("username") String username) throws Exception {
    Node uiStateNode = this.getPrivateNode(username);
    if (uiStateNode.hasProperty(JCR_UI_STATE_NOTE_PROPERTY)) {
      UIStateDataBean stateDataBean = new UIStateDataBean(uiStateNode.getProperty(JCR_UI_STATE_NOTE_PROPERTY).getString());
      return Response.ok(stateDataBean, JSON_CONTENT_TYPE).build();
    } else {
      UIStateDataBean stateDataBean = new UIStateDataBean();
      return Response.ok(stateDataBean, JSON_CONTENT_TYPE).build();
    }
  }
  
  private Node getPrivateNode(String userName) throws Exception {
    Node applicationNode = this.nodeHierarchyCreator.getUserApplicationNode(this.getSessionProvider(), userName);
    if (!applicationNode.hasNode(APPLICATION_NAME)) {
      applicationNode.addNode(APPLICATION_NAME);
    }
    if (!applicationNode.hasNode(JCR_STATE_DATA_NODE_PATH)) {
      applicationNode.addNode(JCR_STATE_DATA_NODE_PATH, JCR_STATE_NOTE_TYPE);
    }
    applicationNode.getSession().save();
    return applicationNode.getNode(JCR_STATE_DATA_NODE_PATH);
  }
  
  private SessionProvider getSessionProvider() {
    return (new SessionProvider(ConversationState.getCurrent()));
  }
}
