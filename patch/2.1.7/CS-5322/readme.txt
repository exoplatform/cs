Summary

    * Status: Calendar Throw exception when Add Feed on Calendar Settings
    * CCP Issue: N/A, Product Jira Issue: CS-5322.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps
1. Login as root, goto Calendar
2. Click on arrow button, select Calendar Settings
3. Choose tab Feed, Click button Add

Actual results

    * Throw exception
      ?
      14:43:01,048 ERROR [PortletApplicationController] Error while processing action in the porlet
      java.lang.NullPointerException
          at org.exoplatform.calendar.webui.popup.UICalendarSettingForm$AddActionListener.execute(UICalendarSettingForm.java:357)
          at org.exoplatform.webui.event.Event.broadcast(Event.java:89)
          at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processDecode(UIFormLifecycle.java:64)
          at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processDecode(UIFormLifecycle.java:40)
          at org.exoplatform.webui.core.UIComponent.processDecode(UIComponent.java:122)
          at org.exoplatform.webui.form.UIFormTabPane.processDecode(UIFormTabPane.java:80)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processDecode(UIApplicationLifecycle.java:46)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processDecode(UIApplicationLifecycle.java:31)
          at org.exoplatform.webui.core.UIComponent.processDecode(UIComponent.java:122)
          at org.exoplatform.webui.application.portlet.PortletApplication.processAction(PortletApplication.java:165)
          at org.exoplatform.webui.application.portlet.PortletApplicationController.processAction(PortletApplicationController.java:80)
          at org.gatein.pc.portlet.impl.jsr168.PortletContainerImpl$Invoker.doFilter(PortletContainerImpl.java:558)
          at org.gatein.pc.portlet.impl.jsr168.api.FilterChainImpl.doFilter(FilterChainImpl.java:109)
        

Fix description

How is the problem fixed?

    * SetId for UiPopupContainer

Patch files:CS-5322.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: Nome

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

