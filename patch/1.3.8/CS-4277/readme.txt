Summary

    * Status: UI error when select user in French language
    * CCP Issue: N/A, Product Jira Issue: CS-4277
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  UI error when select user in French language when select user form 

Fix description

How is the problem fixed?

    *  Add stylesheet in eXoApplication/calendar/webapp/src/main/webapp/skin/DefaultSkin/webui/Stylesheet.css

Patch file: CS-4277.patch

Tests to perform

Reproduction test
   1. Login
   2. Agenda
   3. Add new event
   4. Add/Edit event
   5. Paticipants
   6. Add paticipants
   7. Pick a user
In French, UI eror occurs.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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
    * Function or ClassName change : None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

