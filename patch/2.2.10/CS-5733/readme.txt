CS-5733: [Calendar] Problem of Priority range when importing ics file in Calendar Portlet

Summary
[Calendar] Problem of Priority range when importing ics file in Calendar Portlet 
CCP Issue:  CCP-1406 
Product Jira Issue: CS-5733.
Complexity: N/A
 

Problem description
What is the problem to fix?
Cannot import ICS file with priority or having an event without end date to calendar.

Fix description
Problem analysis
	For first problem of priority: 
		Exo Calendar only define 4 levels for Priority: PRIORITY_NONE, PRIORITY_HIGH, PRIORITY_NORMAL, PRIORITY_LOW
		But the icalendar spec is rfc2445  has defined 9 levels for Priority (from 0 to 9). It's the cause when importing an ics file from other Calendar app (not eXo Calendar).

	For second problem of event without end date:
		In case "DTEND" property doesn't existed in imported data, a NullPointerException occurs and breaks the importing process.
	

How is the problem fixed?
	For first problem of priority: 
		Convert  Priority from icalendar (rfc2445) to Priority of Exo Calendar.

	For second problem of having an event without end date:
		For cases where a "VEVENT" calendar component specifies a "DTSTART" property with a DATE-TIME data type but no "DTEND" property, the event ends on the same calendar date and time of day specified by the "DTSTART" property.
		Check whether there is no "DTEND" property in imported data, set "toDate" field  the value of "fromDate" of this event.


Patch file: PROD-ID.patch

Tests to perform
Reproduction test
	For 1st problem of priority:
		Go to Calendar
		On the right of My Groups, click Settings > import ObmCalendar.ics 
		Go to date Feb 28th 2012
		See that Tcheck event is imported successfully.
	For 2nd problem of event without end date:
		Go to Calendar
		On the right of My Groups, click Settings > import ObmCalendar_2.ics 
		See that new events are imported successfully. (e.g: on Jan 13th 2012)
 
Tests performed at DevLevel
cf.above
Tests performed at Support Level
cf.above
Tests performed at QA
cf.above

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
No
Changes in Selenium scripts 
No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
No

Configuration changes
Configuration changes:
No
Will previous configuration continue to work?
Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?
No
Function or ClassName change:  No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?
No

Validation (PM/Support/QA)
PM Comment
	PM validated.
Support Comment
	Support validated.
QA Feedbacks
...
