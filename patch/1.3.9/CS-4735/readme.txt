Summary

    * Status: RSS Reader portlet : problem with the creation of a category (a URL input is required)
    * CCP Issue: CCP-745, Product Jira Issue: CS-4735.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  In RSS Reader Portlet, when creating a category, a URL is required. In fact, if we don't fill in the field, an error message appears "please enter URL!". 
      However,the aim of creating categories is to classify the RSS feeds. So, the URL input must be required when creating RSS feeds, not when creating categories.
      Furthermore, there are icons for creating, editing or removing categories but we don't find icons for managing RSS feeds, as if we have the same interface for the creation of categories and RSS feeds.

Fix description

How is the problem fixed?
    * Add onchange event listener to 'Type' select box, if type is 'RSS', display the URL textbox with mandatory sign ('*'), else, if type is 'DESC', hide the URL textbox
    * Similarly, when editing a content node, depend on the type of content node ('RSS' or 'DESC'), the URL textbox will be visible or invisible.

Patch files: CS-4735.patch, ALL-509.patch

Tests to perform

Reproduction test
In AIO 1.6.8: 
   1. Create new page wizard, in the step 3: Add RSS Reader Portlet in the page.
   2. Save this page
   3. Click to "Add a Category" icon
   4. A URL is required
      Problem: The aim of creating categories is to classify the RSS feeds. So, the URL input must be required when creating RSS feeds, not when creating categories.

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

    * Function or ClassName change: No

Is there a performance risk/cost?
*

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch vailidated

QA Feedbacks
*

