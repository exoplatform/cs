Summary

    * Status: Optimize LDAP requests for Calendar
    * CCP Issue: CCP-1032, Product Jira Issue: CS-5226.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * There are a very important numbers of LDAP requests made by our products, more than 17 requests/sec are made for only one user.

Fix description

How is the problem fixed?

    * Using ConversationState.getCurrent().getIdentity() to get groups and memberships of current user instead of using OrganizationService.
          o Using ConversationState.getCurrent().getIdentity().getGroups() instead of using OrganizationService.getGroupHandler().findGroupsOfUser(currentuser).
          o Using ConversationState.getCurrent().getIdentity().getMemberships() instead of using OrganizationService.getMembershipHandler().findMembershipsByUserAndGroup(currentuser, groupId)

Patch file: CS-5226.patch

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

    * No side effects

Is there a performance risk/cost?
* No risk

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
*

QA Feedbacks
*
