Summary

Status: The highlight is disappear after adding participant
CCP Issue: N/A, Product Jira Issue: CS-5308.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?
Steps to reproduce:

Login as john
Create new event and save this created event
Edit the created event and go to schedule table
The highlight of busy hours is appeare --> OK
Add some users to this event
--> The highlight is disappear --> NOK

Fix description
How is the problem fixed?

* Fix logic bug in org.exoplatform.calendar.service.impl.JCRDataStorage.checkFreeBusy(): Restore the data of parameter 'eventQuery' after passed through the function.

Patch files: CS-5308.patch

Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel
* reproduction test

Tests performed at QA/Support Level
*

Documentation changes
Documentation changes:
* No

Configuration changes
Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

No
Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*
