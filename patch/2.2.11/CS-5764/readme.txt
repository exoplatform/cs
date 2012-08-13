CS-5764: Must start/end life cycle of the OrganizationUserInfoServiceImpl called in smack listeners

Problem description
* What is the problem to fix?
In chat service, there are Smack listeners calling OrganizationUserInfoServiceImpl in its code. Events are emitted from Openfire server, so life cycle of OrganizationUserInfoServiceImpl is not started.
Problem: Many exceptions flood the server's log file.

Fix description
* Problem analysis
- getUserInfo is called in both Rest services and smack listener, and we need start/end life cycle only in Smack listeners. 
- starting/ending lifecycle in rest services will cause some exceptions. 

How is the problem fixed?
- distinguish two methods getUserInfo to be called in Rest and Smack listener.

Patch file: PROD-ID.patch

Tests to perform
Reproduction test
steps ...
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
