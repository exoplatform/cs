/**
 * 
 */
package org.exoplatform.webservice.cs.chat;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringInputTransformer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webservice.cs.bean.UIStateDataBean;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;
import org.exoplatform.ws.frameworks.json.transformer.Json2BeanInputTransformer;

/**
 * @author Uoc Nguyen
 *
 */
public class UIStateService implements ResourceContainer {
  public final static String JSON_CONTENT_TYPE    = "application/json";
  public final static String APPLICATION_NAME = "eXoChat";
  private static final Log LOG = ExoLogger.getLogger("lr.webservice");
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
  
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/chat/uistateservice/save/{username}/")
  @InputTransformer(Json2BeanInputTransformer.class)
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response saveState(@URIParam("username") String userName, UIStateDataBean stateData) throws Exception {
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
    return Response.Builder.ok(stateDataBean, JSON_CONTENT_TYPE).build();
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/chat/uistateservice/get/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getState(@URIParam("username") String username) throws Exception {
    Node uiStateNode = this.getPrivateNode(username);
    if (uiStateNode.hasProperty(JCR_UI_STATE_NOTE_PROPERTY)) {
      UIStateDataBean stateDataBean = new UIStateDataBean(uiStateNode.getProperty(JCR_UI_STATE_NOTE_PROPERTY).getString());
      return Response.Builder.ok(stateDataBean, JSON_CONTENT_TYPE).build();
    } else {
      UIStateDataBean stateDataBean = new UIStateDataBean();
      return Response.Builder.ok(stateDataBean, JSON_CONTENT_TYPE).build();
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
