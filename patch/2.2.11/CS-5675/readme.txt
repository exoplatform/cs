CS-5675: [Mail] No mail account is listed in list when edit delegated account

Problem description
* What is the problem to fix?
No mail account is listed in list when edit delegated account

Fix description
* Problem analysis
The bug is caused by not take into account delegated accounts in UIAccountSetting#getAccounts()

* How is the problem fixed?
Add delegated accounts to account list

Patch file: https://github.com/exoplatform/cs/pull/26

Tests to perform
* Reproduction test
- Login as Mary
- Go to Mail application
- Create new mail account: exomailtest@gmail.com
- Click on Setting, delegate this account to John with full permission
- Login as John
- Go to Mail application
- From mail account list, click Edit
> Result: Edit mail form is displayed without account mail in list
> Expected result: Delegated mail is displayed in list

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
