Summary
Problem of Priority range when importing ics file in Calendar Portlet 
CCP Issue:  CCP-1244 
Product Jira Issue: CS-5696.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
Cannot import an ics file to calendar.
Get this message when import "Le type du fichier n'est pas correct."
Stack trace :

java.lang.ArrayIndexOutOfBoundsException: 5
 at org.exoplatform.calendar.service.impl.ICalendarImportExport.importToCalendar(ICalendarImportExport.java:906)
 at org.exoplatform.calendar.webui.popup.UIImportForm$SaveActionListener.execute(UIImportForm.java:430)
 at org.exoplatform.webui.event.Event.broadcast(Event.java:89)

Look in code, the corresponding line is :

if(event.getPriority() != null) exoEvent.setPriority(CalendarEvent.PRIORITY[Integer.parseInt(event.getPriority().getValue())] )

In this file, priority is 5, and array CalendarEvent.PRIORITY={PRIORITY_NONE, PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW} ; (only 4 elements).
In fact, the icalendar spec is rfc2445 and say this for priority :

Description: The priority is specified as an integer in the range
zero to nine. A value of zero (US-ASCII decimal 48) specifies an
undefined priority. A value of one (US-ASCII decimal 49) is the
highest priority. A value of two (US-ASCII decimal 50) is the second
highest priority. Subsequent numbers specify a decreasing ordinal
priority. A value of nine (US-ASCII decimal 58) is the lowest
priority.
A CUA with a three-level priority scheme of "HIGH", "MEDIUM" and
"LOW" is mapped into this property such that a property value in the
range of one (US-ASCII decimal 49) to four (US-ASCII decimal 52)
specifies "HIGH" priority. A value of five (US-ASCII decimal 53) is
the normal or "MEDIUM" priority. A value in the range of six (US-
ASCII decimal 54) to nine (US-ASCII decimal 58) is "LOW" priority.

So, we must be able to manage priority from 0 to 9.

Fix description
Problem analysis

 The Exo Calendar only define 4 levels for Priority:
CalendarEvent.PRIORITY = {PRIORITY_NONE, PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW} ; 
{{/code}

* But the icalendar spec is rfc2445  has defined 9 levels for Priority (from 0 to 9). It's the cause when importing an ics file from other Calendar app (not eXo Calendar).
How is the problem fixed?

 Convert  Priority from icalendar (rfc2445) to Priority of Exo Calendar.
Convert by logic:

        priority = Integer.parseInt(value);
       if (1 < priority && priority <= 4) {
          priority = CalendarEvent.PRI_HIGH;
        } else if (priority == 5) {
          priority = CalendarEvent.PRI_MEDIUM;
        } else if (priority > 5) {
          priority = CalendarEvent.PRI_LOW;
        }
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

None
Changes in Selenium scripts 

None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

None
Configuration changes
Configuration changes:

None
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?

None
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
