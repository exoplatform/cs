Summary

    * Status: Job starts before loading some nodetypes
    * CCP Issue: CCP-759, Product Jira Issue: CS-4748.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* The following exception is displayed on the second start of AllInOne:

      [ERROR] NodeTypeManagerImpl - Error obtaining node type javax.jcr.nodetype.NoSuchNodeTypeException: NodeTypeManager.getNodeType(): NodeType 'http://www.exoplatform.com/jcr/exo/1.0datetime' not found

Fix description

Problem analysis
* This exception is due to PopupReminderJob that is triggered before NodeTypeManager loads all node types. 
PopupReminderJob tries to use the "datetime" node type before it is loaded by NodeTypeManager.

How is the problem fixed?

    * Change configuration to make CS's jobs postponing until the components are initialized. They will start 60' later than the time when they are scheduled.
    * The patch also increases the default period between two moments that PopupReminderJob executes from 6' to 15' because 6' is too fast and not necessary for updating Reminder.

Patch file: CS-4415.patch

Tests to perform

Reproduction test
* Start AIO server. Reading logs printed on server console.
  The case is not persistent appearance, and maybe must edit logs-configuration.xml to set "job.PopupRecordsJob" as "debug" for better chasing. 
  Maybe also need to run in development mode.

Tests performed at DevLevel
* Reproduction test

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No.

Configuration changes

Configuration changes:
* Configuration of CS's jobs in cs-plugins-configuration.xml.
  In AIO, need ALL-506 for CS job configuration.

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
* None

Validation (PM/Support/QA)

PM Comment
* Patch approved. The fix is more a workaround, but there is no better way in AIO.

Support Comment
* Patch validated.

QA Feedbacks
*

