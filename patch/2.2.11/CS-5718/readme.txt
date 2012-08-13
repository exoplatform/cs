CS-5718 	[CS-Chat] IE7 Error in Chat status menu

Problem description
* What is the problem to fix?
In Chat portlet, status list is broken UI in IE7.

Fix description
* Problem analysis
In IE7, relative position is not enough to set drop down chat status in a correct place.

* How is the problem fixed?
Update stylesheet to set drop down chat status in a correct place in IE7.

Patch file: https://github.com/exoplatform/cs/pull/40

Tests to perform
* Reproduction test
- Create new page and add Chat Portlet into this page
- Click on User's name to show chat status menu ==> error

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
	None

Configuration changes
Configuration changes:
* None

Will previous configuration continue to work?
* yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
	No

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
...
QA Feedbacks
...
