<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:ns="http://exoplatform.org/organization/rest"
	exclude-result-prefixes="xlink ns">

	<xsl:output method="html" encoding="UTF-8" />

	<xsl:template match="/ns:groups">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="ns:group">
    <xsl:variable name="groupInfoSuffix">
      <xsl:value-of
        select="concat('info', ./@groupId )" />
    </xsl:variable>

    <xsl:variable name="childGroupsSuffix">
      <xsl:value-of
        select="concat('view-all/?parentId=', ./@groupId )" />
    </xsl:variable>
    
    <div>
      <!-- create link for view child groups -->
      <img src="/organization/img/SmallGrayPlus.gif" alt="+"
        onclick="groups('{concat(substring-before(./@xlink:href, '?'), $childGroupsSuffix)}', this);" />
      <xsl:text> </xsl:text>
      <!-- create link for view group info -->
      <a
        href="{concat(substring-before(./@xlink:href, '?'), $groupInfoSuffix)}">
        <img src="/organization/img/SmallGroupIcon.gif" alt="#" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="." />
      </a>
    </div>
	</xsl:template>
</xsl:stylesheet>