Summary

    * Status: Group Calendar in my Space shows all events
    * CCP Issue: CCP-4862, Product Jira Issue: CS-4862.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Under PLF 3.0.x connect as root.
   2. Create two spaces (AAA & BBB).
   3. Go to AAA space => Calendar and create an event.
   4. Go to BBB space => Calender and switch to the month view=> event in AAA space appears=>KO

Fix description

How is the problem fixed?

    * The calendar made by Social Space is always set as checked while the rest stay at unchecked if the Calendar Portlet is run in Space.

Patch files: CS-4862.patch

Tests to perform

Reproduction test
* cf.above

Tests performed at DevLevel
* unit test & reproduction test.

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* None
Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes.
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * org.exoplatform.calendar.webui.UICalendarPortlet & org.exoplatform.calendar.webui.UICalendars

Is there a performance risk/cost?
* Not detected yet.
Validation (PM/Support/QA)

PM Comment
* PL review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
