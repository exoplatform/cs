CS-5745: [CS] Unknown error when editing an event from an imported calendar

Problem description
* What is the problem to fix?
Unknown error when editing an event from an imported calendar

Fix description
* Problem analysis
There are two causes in this issue
- Missing data when event is imported. When event is imported into calendar, the RepeatType property is missed before.
- When click on event's tooltip to modify this event, there are multiple requests that sent from browser.

How is the problem fixed?
Fixed for missing data when event is imported: set repeat type to no-repeat.
Fix for multiple requests sent from browser: change the way to call "click" event on tooltip.

Pull request: https://github.com/exoplatform/cs/pull/21

Tests to perform
* Reproduction test
- Perform to export a calendar
- Perform to import calendar
- Right click the event and choose Edit option
- Perform to change information
- Click Save

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
