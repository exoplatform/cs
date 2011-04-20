Summary

    * Status: [Mail]: Print message, only the first page is printed
    * CCP Issue: CCP-151, Product Jira Issue: CS-4415
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Login as root
    * Connect to Groups/Mail in the administration toolbar
    * Compose a long email and send it to root@localhost.com
    * Check-in inbox mail
    * Select the last mail and print it
      --> Only the first page is printed
    * Export the mail and open it with another Mail Client and print it
      --> The full mail is printed

Fix description

How is the problem fixed?

    * Fill printed content to an IFrame and print it instead of the whole page.

Patch file: CS-4415.patch

Tests to perform

Reproduction test

    * Cf. above

Tests performed at DevLevel
* Reproduction test & unit test.

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* None

Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No. It just changes UI template and javascript.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
