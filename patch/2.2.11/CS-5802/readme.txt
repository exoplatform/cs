CS-5802: Calendar: No notification for participant adding

Problem description
* What is the problem to fix?
When adding a participant, there is no message or reaction that indicates that the user was added.

Fix description
* Problem analysis
UserSelect popup still show when adding new participant(s). It may make end user feel confused.

* How is the problem fixed?
Close UserSelect popup right after adding participant.

Patch file: https://github.com/exoplatform/cs/pull/38

Tests to perform
* Reproduction test
Using chrome or IE8 :
When adding an event to Calendar, click "more details" then "participants".
- Add a participant by clicking the "plus" button 
- Select "pick a contact" or "pick a user". 
- Select a user then click the "add" button. 
There is no message or reaction that indicates that the user was added. If you click close, you will see that the user was added in the window "invitations". But this wasn't clear because windows were superposed.

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
	Support validated
QA Feedbacks
...
