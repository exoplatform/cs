Summary

    * Status: Customer specific fixChat is not working for users having space in their username
    * CCP Issue: CCP-1179, Product Jira Issue: CS-5607.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * 1)Edit /webapps/platform-extension/WEB-INF/conf/organization/organization-configuration.xml and add a new user having a space within hsi username "Exochat TEST".

?
<value>
<object type="org.exoplatform.services.organization.OrganizationConfig$User">
<field  name="userName"><string>Exochat TEST</string></field>
<field  name="password"><string>gtn</string></field>
<field  name="firstName"><string>auser20</string></field>
<field  name="lastName"><string>user20</string></field>
 <field  name="email"><string>user20@localhost</string></field>
<field  name="groups">
<string>member:/platform/users</string>
</field>
</object>
</value>

This step is mandatory because this(adding a user having space within his user-name) cannot be done through UI.
2)Start both PLF and Open Fire
3)Login as "Exochat TEST"
4)Attempt to change his chat status==>KO
You'll get the following Exception in Open Fire console.
Fix description

How is the problem fixed?

    * Encoding the space character

Tests to perform

Reproduction test

    * cf. above

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

    * Function or ClassName change: none

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

      Support Comment
    * Support review: Patch validated

      QA FeedbacksS
