Summary

    * Status: Calendar Throw exception when create Task/Event incase input invalid Date
    * CCP Issue: N/A, Product Jira Issue: CS-5323.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps
1. Login as root, goto Calendar
2. Create button Task/Event
3. Input invalid date ex: 13/09/0000
4. Click button Save

Actual results

    * Throw exception
      ?
      at org.exoplatform.calendar.webui.popup.UIQuickAddEvent.access$400(UIQuickAddEvent.java:76)
          at org.exoplatform.calendar.webui.popup.UIQuickAddEvent$SaveActionListener.execute(UIQuickAddEvent.java:294)
          at org.exoplatform.webui.event.Event.broadcast(Event.java:89)
          at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:123)
          at org.exoplatform.webui.core.lifecycle.UIFormLifecycle.processAction(UIFormLifecycle.java:40)
          at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:133)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:58)
          at org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle.processAction(UIApplicationLifecycle.java:31)
          at org.exoplatform.webui.core.UIComponent.processAction(UIComponent.java:133)
          at org.exoplatform.webui.core.UIApplication.processAction(UIApplication.java:120)
          at org.exoplatform.webui.application.portlet.PortletApplication.processAction(PortletApplication.java:168)
          at org.exoplatform.webui.application.portlet.PortletApplicationController.processAction(PortletApplicationController.java:80)
          at org.gatein.pc.portlet.impl.jsr168.PortletContainerImpl$Invoker.doFilter(PortletContainerImpl.java:558)
          at org.gatein.pc.portlet.impl.jsr168.api.FilterChainImpl.doFilter(FilterChainImpl.java:109)
          at org.gatein.pc.portlet.impl.jsr168.api.FilterChainImpl.doFilter(FilterChainImpl.java:72)
          at org.gatein.pc.portlet.impl.jsr168.PortletContainerImpl.dispatch(PortletContainerImpl.java:506)
          at org.gatein.pc.portlet.container.ContainerPortletDispatcher.invoke(ContainerPortletDispatcher.java:42)
          at org.gatein.pc.portlet.PortletInvokerInterceptor.invoke(PortletInvokerInterceptor.java:89)
          at org.gatein.pc.portlet.aspects.EventPayloadInterceptor.invoke(EventPayloadInterceptor.java:197)
          at org.gatein.pc.portlet.PortletInvokerInterceptor.invoke(PortletInvokerInterceptor.java:89)
          at org.gatein.pc.portlet.aspects.RequestAttributeConversationInterceptor.invoke(RequestAttributeConversationInterceptor.java:119)
          at org.gatein.pc.portlet.PortletInvokerInterceptor.invoke(PortletInvokerInterceptor.java:89)

Fix description

How is the problem fixed?

    * No throw exception when object equal null, return null in this case

Patch files:CS-5323.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * cf. above

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch approved

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

