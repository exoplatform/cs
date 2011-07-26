Summary

    * Status: In Calendar, Only identifier or email of participants of an event is displayed instead of name, surname and mail
    * CCP Issue: CCP-965, Product Jira Issue: CS-5011.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In Calendar, Only identifier or email of participants of an event is displayed. It should display name, surname and mail of a participant.

Fix description

How is the problem fixed?

    * add participant information column, this column contains full name and email of participants.

Patch files:CS-5011.patch

Tests to perform

Reproduction test

   1. Go to Calendar
   2. Add an event/task to a calendar
   3. Click more details > Choose Tab Participants > Add some participants by picking a contact from address book or pick a portal user
   4. User name of user in case of portal user or email in case of contact from address book is shown instead of their name, surname and mail.

Tests performed at DevLevel
*No

Tests performed at QA/Support Level
*No
Documentation changes

Documentation changes:
* In CS user guide: to update Illustration 25: The Participants tab of the Add/Edit events form in section 3.5.1.2 Add detailed event page 73 with the new UI.
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
*Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change : No

Is there a performance risk/cost?
*No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

