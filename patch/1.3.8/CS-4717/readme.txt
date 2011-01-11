Summary

    * Status: RSS Portlet Reader: missing label internationalization
    * CCP Issue: CCP-727, Product Jira Issue: CS-4717.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The label of Refresh icon in RSS Reader Portlet is hard coded.

Fix description

How is the problem fixed?

    * Add new keys (UIDetailContent.label.refreshbar, UIDetailContent.title.refreshbar) for Refresh icon in property files.
    * Invoke these keys in UIDetailContent template

Patch file: CS-4717.patch

Tests to perform

Reproduction test

   1. Insert RSS Reader Portlet in a page
   2. Add a new category
   3. The text of refresh bar icon is hard coded.

Tests performed at DevLevel
* cf. above

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

    * Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
* 
