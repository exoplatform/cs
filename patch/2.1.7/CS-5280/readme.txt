Summary

    * Status: Find and fix all JCR Sessions leaks
    * CCP Issue: N/A, Product Jira Issue: CS-5280.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * This issue is link to a code review to track and fix JCR Sessions leaks
      This subject is critical
    * In CS code, SessionProviders are created by calling SessionProvider.createSystemProvider() or new SessionProvider() directly and not closed properly results JCR Sessions leaks

Fix description

How is the problem fixed?

    * Using SessionProviderService to get SessionProvider instead of using SessionProvider.createSystemProvider() directly. SessionProviderService is responsible in managing SessionProviders and closing them when the request is finished automatically.

Patch files:CS-5280.patch

Tests to perform

Reproduction test
*

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

    * No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* N/A

QA Feedbacks
* Validated by QACAP

