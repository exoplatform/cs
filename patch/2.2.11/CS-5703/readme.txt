CS-5703: [Calendar] IE7: Cannot change time of event/task

Problem description
* What is the problem to fix?
Cannot change time of event/task on IE7.

Fix description
* Problem analysis
OnBlur event of javascript doesn't work well in IE7. So we should find another way to let user click on calendar.
* How is the problem fixed?
Use onChange and onKeyDown javascript event to listen user mouse click when choosing date & time.

Patch file: https://github.com/exoplatform/cs/pull/12

Tests to perform
* Reproduction test
- Login
- Go to Calendar application
- Click to add new event or task
- Click on Time box on Create event form ==> Display a pop up to select time
- Select a time from pop up
Result:
Time is selected but the default time of event is not changed
On select time pop up, click on scroll bar ==> scroll bar doesn't work.

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
	PM validated
Support Comment
...
QA Feedbacks
...
