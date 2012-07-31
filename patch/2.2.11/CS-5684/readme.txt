CS-5684: [Contact] Cannot copy shared contact when login as user who has not edit right

Problem description
* What is the problem to fix?
	Cannot copy shared contact when login as user who has not edit right.

Fix description
* Problem analysis
 Both 'move' and 'copy' action use same function with same permission checking, that causes the problem.

* How is the problem fixed?
- Modify API ContactServiceImpl#pasteContacts
- add  param 'isMove, type boolean to distinguish 'copy' or 'move' action
- correct permission checking with value of isMove

Patch file: https://github.com/exoplatform/cs/pull/33

Tests to perform
* Reproduction test
- Login user A
- Go to Contacts app
- Create new contact: test
- Shared this contact to user B without edit right
- Login user B
- Go to Contacts app
- Click on Share on the left pane
- Right click on shared contact and select copy
- Paste this contact to personal address book
Result: Display message "You do not have the edit permission. Please check again.
Expected result: User can copy/paste a shared contact without edit right

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
