CS-5350: ChatBar portlet raises javascript error under IE-7, IE-8

Problem description
* What is the problem to fix?
ChatBar portlet raises javascript error under IE-7, IE-8

Fix description
* Problem analysis
Problem comes from a js syntax error (COMMONS-87) in cometd.js and a wrong flow control of if condition in UIMainChatWindow.js 

* How is the problem fixed?
- First, apply COMMONS-87 patch. 
- Second, correct condition isConnected and flow control.

Patch file: https://github.com/exoplatform/cs/pull/34

Tests to perform
Reproduction test
* Login as John in PLF 3.0.10, using IE8
* See that there is no warning message
Message: Object doesn't support this action

Tests performed at DevLevel
*

Tests performed at Support Level
*

Tests performed at QA
*

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
* No

Will previous configuration continue to work?
* Yes

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
	Support validated
QA Feedbacks

