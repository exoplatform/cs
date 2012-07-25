CS-5768: [Calendar]: Incorrect time of reminder mails when add event/taks

Problem description
* What is the problem to fix?
Incorrect time of reminder mails when add event/taks

Fix description
* Problem analysis
Time period of reminder job is actually 3 minutes, so time to send reminder email can be delayed to 8 minutes, which is not correct with option "Send reminder every 5 minutes". 

* How is the problem fixed?
- Changing value of time period between reminder jobs from 3 mins to 15s
- Fixing incorrect time of popup reminder message and invitation mail body caused by taking incorrect time zone.
- Changing format of popup reminder notification. use event description (exo:description property of reminder node) instead.
- Adding time zone value of reminder creator to reminder messages and invitation mail body after from time and to time.

Patch file: https://github.com/exoplatform/cs/pull/29

Tests to perform
* Reproduction test
Add new event/task
In Reminders tab, check
+ Remind by Email : Send an email before the event starts in -> 5minutes
+ Repeat Every: 5minutes
+ Check Show a notification pop-up
Click Save => Event/Task is added successfully.
Check mail show the time to reminder is not correct. 

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
