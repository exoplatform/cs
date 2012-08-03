CS-5647: Wrong date when sending offline messages

Problem description
* What is the problem to fix?
Wrong date when sending offline messages

Fix description
Problem analysis
* How is the problem fixed?
Explicitly set time zone of delayFormatter to GMT - 00:00 to parse correctly the delay time returned by jabber

Patch file: PROD-ID.patch

Tests to perform
Reproduction test
- Connect as root and add John to Contacts
- Keep root connected, and connect with John then accept the request of root
- John disconnects
- Root send a message to John when John is disconnected
- Connect again with John, John finds the message but the time of the message is shifted by one hour.

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
