Summary

    * Status: 'Repository error' when change name of initial document
    * CCP Issue: CCP-901, Product Jira Issue: CS-4923.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. In FF browsed connect as root and go to agenda.
   2. Edit the timezone (eg choose GMT-3).
   3. Add a new event and choose holiday as event category (absent from 11:00 to 12:30).
   4. In chrome browser connect as john and go to agenda.
   5. Edit timezone (choose the same as root session).
   6. Add a new event and add root as participant (the time in which root is not available will be included) (eg meeting from 11:00 to 11:30) and save.
   7. Go to schedule tab =>Wrong date availability

Fix description

How is the problem fixed?

    * In Javascript: Re-calculate the timezone offset between local browser and user setting timezone.
    * The free/busy time to display will be based on user setting timezone
    * Free/busy information will be saved based on GMT timezone, when retrieving free/busy information, it needs to query on all date folders related to user's day with user's timezone

Patch files:CS-4923.patch

Tests to perform

Reproduction test
cd. above
Tests performed at DevLevel
*

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

    * Function or ClassName change: None

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

