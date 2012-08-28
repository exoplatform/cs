CS-5737: [Calendar] Error when adding a remote calendar 

Problem description
* What is the problem to fix?
Get error popup when adding remote calendar.
When click on Cancel button, remote calendar still be added.

Fix description
* Problem analysis
The problem is caused by incorrect order of creating a calendar in RemoteCalendarServiceImpl.java#importRemoteCalendar. 

* How is the problem fixed?
Changing the order of creating remote calendar in storage. 

Pull request: https://github.com/exoplatform/cs/pull/48

Tests to perform
* Reproduction test
- Login as Demo
- Go to My Sites > Intranet > Calendar
- Click on My Group setting icon > Remote Calendar
- Paste this link to URL: https://www.google.com/calendar/render?tab=mc
- Fill in Display Name (e.g: "test3"), and your correct Username/Password.
- Click Save
- Error popup is appeared
- Warning is shown on console.
- Click OK to close popup
- Click Cancel to cancel add remote calendar
Problem: remote calendar is added in remote calendar list ==> KO

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
...
Changes in Selenium scripts 
...

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...

Validation (PM/Support/QA)
PM Comment
...
Support Comment
...
QA Feedbacks
...
