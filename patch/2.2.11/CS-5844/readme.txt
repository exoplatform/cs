CS-5844: [UXP-ERGO] Weird display when calendar name is too long 

Problem description
* What is the problem to fix?
There are 2 problems with calendar name.
- Firstly with personal calendar :
Add a calendar with a long name, containing spaces : "Test calendar with long name"
When it is displayed we don't see all the calendar name.
- Secondly with shared calendar : if there is a shared calendar with having name larger than the witdh of the column, you will see only to owner of the calendar, and not the whole calendar name.
We must find a solution to correctly display long calendar names.

Fix description
* Problem analysis
If calendar name is too long (more than 23 characters), it will be hidden by CSS code. There is no sign to inform that it's just a part of calendar name. Calendar name is fully shown in tooltip.   

* How is the problem fixed?
If calendar name is longer than 23 characters, then it will be cut to shorter name with 23 characters. "..." is used to show that it's just a part of calendar name. Calendar name is fully shown in tooltip.  
Patch file: https://github.com/exoplatform/cs/pull/51

Tests to perform
* Reproduction test
- Login as John
- Go to Calendar
- Add new calendar: "Test calendar with very long name"
- Expected result: in My Group, see "Test calendar with v..."
- Share this calendar with Mary.
- Login as Mary
- Go to Calendar
Expected result: in shared calendar, see "john- Test calendar ..."

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
