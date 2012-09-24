CS-5755: [Calendar] Wrong display all day event, repeat monthly in month view

Problem description
* What is the problem to fix?
Steps:
- Server time zone is GMT+7, client time zone is GMT+1: Europe/Brussels
- Login
- Go to Calendar app
- Create event, check on check box All day
- Click More detail, select repeat monthly
- Save new event
- Switch to month view

Result: The event is displayed on 2 days (current day and previous day)
Expected result: The event is displayed on 1 day

Fix description
* Problem analysis
The event created at time that is affected by Day Light Saving will be incorrectly displayed.
* How is the problem fixed?
Modify API JCRDataStorage#getOccurrenceEvents to check if event is created at the day that applies Day Light Saving Time and reduce event time appropriately.

Patch file: PROD-ID.patch

Tests to perform
* Reproduction test
- Server time zone is GMT+7, client time zone is GMT+1: Europe/Brussels
- Login
- Go to Calendar app
- Create event, check on check box All day
- Click More detail, select repeat monthly
- Save new event
- Switch to month view
Result: The event is displayed on 2 days (current day and previous day)
Expected result: The event is displayed on 1 day

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
