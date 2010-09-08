Summary

    * Status: Update fisheye URL in pom.xml
    * CCP Issue: N/A, Product Jira Issue: CS-4427.
    * Complexity: trivial.

The Proposal
Problem description

What is the problem to fix?

    * Update scm in pom.xml with new url:

      <scm>
      <connection>scm:svn:http://svn.exoplatform.org/projects/cs/trunk</connection>
      <developerConnection>scm:svn:http://svn.exoplatform.org/projects/cs/trunk</developerConnection>
      <url>http://fisheye.exoplatform.org/browse/cs</url>
      </scm>

Fix description

How is the problem fixed?

    * Change this value to right location in svn

      <connection>scm:svn:http://svn.exoplatform.org/projects/cs/trunk</connection>
      <developerConnection>scm:svn:http://svn.exoplatform.org/projects/cs/trunk</developerConnection>

Patch information:
Patch files:
CS-4427.patch

Tests to perform

Reproduction test
* In the phase of commit, the log will be updated in the fisheye.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No


Documentation changes

Documentation changes:
* No


Configuration changes

Configuration changes:
* Yes, the global pom.xml

Will previous configuration continue to work?
* Yes except Fisheye log.


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * Patch approved

Support Comment

    * Support review: patch validated

QA Feedbacks
*

