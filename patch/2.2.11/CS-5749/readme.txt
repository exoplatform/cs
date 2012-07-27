CS-5749: [AdressBook] Error pop-up appears before invalid email warning

Problem description
* What is the problem to fix?
Problem 1: if user fills in an invalid email (e.g: test, test@, ...), an error popup appears before invalid email warning ==> NOK.
Expected result: only a warning appears to say that email is invalid.
Problem 2: test@test is a valid email (see this wiki for more infos). So user must add it successfully, without warning.

Fix description
* Problem analysis
- Problem 1 caused by line 
event.getRequestContext().addUIComponentToUpdateByAjax(event.getRequestContext().getUIApplication().getUIPopupMessages());
This line is no longer needed.
- Problem 2 caused by incorrect result of CalendarUtils#isValidEmailAddresses

* How is the problem fixed?
- remove the line that's no longer needed
- rewrite isValidEmailAddresses function

Patch : https://github.com/exoplatform/cs/pull/30

Tests to perform
* Reproduction test
1- Create a personnal adress book
2- Add a contact
3- Input Required fields
4- Input an invalid mail: (test@test)
5- Click "Save"
Problem: 
An error pop up appears: "Cannot complete request as the component to update cannot be found (blockId: _16818836). If the page's contents have changed, a page refresh may be needed."
6- Click "OK"
The Warning message appears to inform that the mail is invalid.

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
	No

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
...
QA Feedbacks
...
