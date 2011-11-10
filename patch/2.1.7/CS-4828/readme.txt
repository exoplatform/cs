Summary

    * Status: [PLF:Calendar] Parsing date time error on starting of Daylight saving time in week view
    * CCP Issue: CCP-895, Product Jira Issue: CS-4828.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Go to Calendar application (the default value of time zone is GMT +01:00)
   2. Keep in week view.
   3. Go to the last week of March see the UI error on the screen
   4. Exception on server:
      ?
      SEVERE: Error while rendering the porlet
      org.exoplatform.groovyscript.TemplateRuntimeException: Groovy template exception at DataText[pos=Position[col=8,line=86],data=uicomponent.renderChild(UICalendarWorkingContainer.class)] for template app:/templates/calendar/webui/UICalendarPortlet.gtmpl
          at org.exoplatform.groovyscript.GroovyScript.buildRuntimeException(GroovyScript.java:178)
          at org.exoplatform.groovyscript.GroovyScript.render(GroovyScript.java:121)
          at org.exoplatform.groovyscript.GroovyTemplate.render(GroovyTemplate.java:118)
          at org.exoplatform.groovyscript.text.TemplateService.merge(TemplateService.java:117)
          at org.exoplatform.webui.core.lifecycle.Lifecycle.renderTemplate(Lifecycle.java:128)
      ...
      Caused by: org.exoplatform.groovyscript.TemplateRuntimeException: Groovy template exception at DataText[pos=Position[col=1,line=252],data=               cl.setTime(fullDateFormat.parse(startTime)) ;] for template app:/templates/calendar/webui/UIWeekView.gtmpl
          at org.exoplatform.groovyscript.GroovyScript.buildRuntimeException(GroovyScript.java:178)
          at org.exoplatform.groovyscript.GroovyScript.render(GroovyScript.java:121)
          at org.exoplatform.groovyscript.GroovyTemplate.render(GroovyTemplate.java:118)
          at org.exoplatform.groovyscript.text.TemplateService.merge(TemplateService.java:117)
      ...
      Caused by: java.text.ParseException: Unparseable date: "03/27/2011 02:00"
          at java.text.DateFormat.parse(DateFormat.java:337)
          at sun.reflect.GeneratedMethodAccessor717.invoke(Unknown Source)
          at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
          at java.lang.reflect.Method.invoke(Method.java:597)
          at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:86)
          at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:226)
          at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:899)
          at groovy.lang.MetaClassImpl.invokeMethod(MetaClassImpl.java:740)
          at org.codehaus.groovy.runtime.InvokerHelper.invokePojoMethod(InvokerHelper.java:765)
          at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:753)
          at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:167)
          at UIWeekView.run(UIWeekView.gtmpl:252)
          at org.exoplatform.groovyscript.GroovyScript.render(GroovyScript.java:111)
      ...

      This issue occurs with any Day Light Saving Time Transition starting (i.e. the last Sunday of March in Central European, the second Sunday of March in Canada/US) when calendar's time zone is set at such regions (GMT+1 for Central European, GTM-7 for Canada/US).

Fix description

How is the problem fixed?

    * '03/27/2011 02:00' is the start time of Daylight Saving Time in Central Europe, at this time, the clock will skip to 03:00 AM, so the time 02:00AM does not exist.
    * To avoid error at this time stamp, we add try..catch block to catch this exception and temporarily set the 'lenient' property of date-time format object to 'true'.
    * Update calendar setting for UIMiniCalendar when users change it.

Patch files:CS-4828.patch

Tests to perform

Reproduction test

    * Cf. above.

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

    * org.exoplatform.calendar.CalendarUtils
    * org.exoplatform.calendar.webui.UICalendarView
    * org.exoplatform.calendar.webui.UICalendarContainer
    * org.exoplatform.calendar.webui.popup.UICalendarSettingForm

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment

    * Patch approved

Support Comment

    * Patch validated

QA Feedbacks
*

