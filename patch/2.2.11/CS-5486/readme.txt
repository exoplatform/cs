CS-5486: [Mail] Can't close print form, null pointer exception is shown on terminal

Problem description
* What is the problem to fix?
Can't close print form of an email, null pointer exception is shown on terminal.

Fix description
* Problem analysis
After call the method UIMailPortlet.cancelAction(), the popup will be remove from it's parent, so don't need to call method addUIComponentToUpdateByAjax

* How is the problem fixed?
Remove the code line that call method addUIComponentToUpdateByAjax

Patch file: https://github.com/exoplatform/cs/pull/36

Tests to perform
* Reproduction test
- Open Mail application
- Select a mail from Inbox and click print
- A print form is displayed
- Click on "Close" button

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
	No

Configuration changes
Configuration changes:
	No
Will previous configuration continue to work?
	Yes

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
	Support validated
QA Feedbacks
...
