package org.exoplatform.cs.ext.impl;

import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/cs/social-integration/plugin/space/ContactUIActivity.gtmpl", events = { @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class), @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class), @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class), @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class), @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"), @EventConfig(listeners = ContactUIActivity.MoreContactInfoActionListener.class) })
public class ContactUIActivity extends BaseUIActivity {

  private static final Log log               = ExoLogger.getLogger(ContactUIActivity.class);

  private boolean          isDisplayMoreInfo = false;

  /**
   * @return the isDisplayMoreInfo
   */
  public boolean isDisplayMoreInfo() {
    return isDisplayMoreInfo;
  }

  /**
   * @param isDisplayMoreInfo the isDisplayMoreInfo to set
   */
  public void setDisplayMoreInfo(boolean isDisplayMoreInfo) {
    this.isDisplayMoreInfo = isDisplayMoreInfo;
  }

  public String getJobTitle() {
    return getActivityParamValue(ContactSpaceActivityPublisher.JOB_TITLE_KEY);
  }

  public String getEmail() {
    return getActivityParamValue(ContactSpaceActivityPublisher.EMAIL_KEY);
  }

  public String getPhone() {
    return getActivityParamValue(ContactSpaceActivityPublisher.PHONE_KEY);
  }

  public boolean isContactAdded() {
    String value = getActivityParamValue(ContactSpaceActivityPublisher.ACTIVITY_TYPE);
    if (value == null) {
      return false;
    }
    if (value.equalsIgnoreCase(ContactSpaceActivityPublisher.CONTACT_ADD)) {
      return true;
    }
    return false;
  }

  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }
    if (value == null)
      value = "";
    return value;
  }

  public static class MoreContactInfoActionListener extends EventListener<ContactUIActivity> {

    @Override
    public void execute(Event<ContactUIActivity> event) throws Exception {
      ContactUIActivity uiComponent = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      boolean display = uiComponent.isDisplayMoreInfo();
      uiComponent.setDisplayMoreInfo(!display);
      requestContext.addUIComponentToUpdateByAjax(uiComponent);
    }

  }
}
