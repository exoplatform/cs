Summary

    * Status: Backport CS Data Injectors to 2.1.x
    * CCP Issue: N/A, Product Jira Issue: CS-5219.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Create injectors for CS.
      These injectors will help QA Team to use their data sets and process performance tests.

Fix description

How is the problem fixed?

    * Implement the injector from API injector provided in COMMONS-88
    * Create new artifact org.exoplatform.cs:exo.cs.component.injector to package all injectors in CS: Contact, Calendar, Mail injectors.
    * Each data injector is implemented as plugin attached to org.exoplatform.services.bench.DataInjectorService service and handled via RESTful requests.

Patch file: CS-5219.patch

Tests to perform

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* Following the CS injector guide.

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* New configuration for injectors is added:
<component>
    <type>org.exoplatform.services.bench.DataInjectorService</type>
  </component>
 
  <external-component-plugins>
    <target-component>org.exoplatform.services.bench.DataInjectorService</target-component>
    <component-plugin>
      <name>ForumDataInjector</name>
      <set-method>addInjector</set-method>
      <type>org.exoplatform.ks.bench.ForumDataInjector</type>
      <description>inject data for Forum</description>
    </component-plugin>
    <component-plugin>
      <name>AnswerDataInjector</name>
      <set-method>addInjector</set-method>
      <type>org.exoplatform.ks.bench.AnswerDataInjector</type>
      <description>inject data for Answer</description>
    </component-plugin>
  </external-component-plugins>

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated.

Support Comment
*

QA Feedbacks
*
