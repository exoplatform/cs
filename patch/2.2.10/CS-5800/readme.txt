 CS-5800: CLONE - Problem when creating an event in "week" view in Calendar using IE9

Summary
CLONE - Problem when creating an event in "week" view in Calendar using IE9 
CCP Issue:  CCP-1406 
Product Jira Issue: CS-5800.
Complexity: N/A

 
Problem description
What is the problem to fix?
	In IE9, on week view mode of calendar, when creating an event by selecting a date, the event is not created on the right date. 

Fix description
Problem analysis
	IE9 cannot get offset height value in week view. 

How is the problem fixed?
	Add a try - catch when calling function eXo.calendar.UICalendarMan.initWeek() in function UICalendarPortlet.prototype.resortEvents.

Patch file: https://github.com/exoplatform/cs/pull/15

Tests to perform
Reproduction test
	Using IE9, go to Calendar, choose "week" view and create an event by selecting a date. The event is not created in the right date.
Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

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
Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
No

Validation (PM/Support/QA)
PM Comment
	PM validated.
Support Comment
	Support validated.
QA Feedbacks
...
