Summary

    * Status: Contact: Impossible to share contact by dragging & dropping
    * CCP Issue: N/A, Product Jira Issue: CS-4668.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Drag & drop 1 contact to Shared link on left pane but nothing happens.

Fix description

How is the problem fixed?
    * The portlet ID has been change because the portal change to render ID of componenet
    * We make dynamic the ID of portlet ID in java script code

Patch file: CS-4668.patch

Tests to perform

Reproduction test
* Login by user
* Go to contact application 
* Create a contact 
* Drag that contact to Share panel (label) on the left -> click to Share panel could not see the shared contact

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
* Validated

Support Comment
* Validated

QA Feedbacks
*
Labels parameters

