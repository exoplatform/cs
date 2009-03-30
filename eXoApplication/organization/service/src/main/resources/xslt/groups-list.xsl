<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:ns="http://exoplatform.org/organization/rest"
	exclude-result-prefixes="xlink ns">

	<xsl:output method="html" encoding="UTF-8" />
    <xsl:template match="/ns:groups">
    <xsl:variable name="mainPageLocation">
      <xsl:text>/organization/index.html</xsl:text>
    </xsl:variable>
    <xsl:variable name="createGroupPageLocation">
      <xsl:text>/organization/create_group.html</xsl:text>
    </xsl:variable>
		  <html>
        <head>
          <link rel="stylesheet" type="text/css" href="/organization/css/style.css" />
          <script type="text/javascript" src="/organization/js/script.js"></script>
          <title>Groups Browser</title>
        </head>
        <body>
          <div class="main" style="height: 100%;">
            <div class="inner">
              <p class="table-title">Groups Browser</p>
               <div>
                <img src="/organization/img/SmallGroupIcon.gif" alt="#" />
                <xsl:text> Root</xsl:text>
              </div>
              <xsl:apply-templates />
              <div style="margin-top:20px;">
                <input class="input" type="button" value="Add new"
                  style="float:left;" onclick="top.location='{$createGroupPageLocation}'; return true;" />
                <input class="input" type="button" value="Reload"	style="float:left;"
                onclick="top.location.reload();" />
                <input class="input" type="button" value="Main page"
                  style="float: left;"
                  onclick="top.location='{$mainPageLocation}'; return true;" />
              </div>
            </div>
          </div>
        </body>
      </html>
    </xsl:template>

	<xsl:template match="ns:group">
		<xsl:variable name="groupInfoSuffix">
			<xsl:value-of
				select="concat('info', ./@groupId,'/' )" />
		</xsl:variable>
		<xsl:variable name="childGroupsSuffix">
			<xsl:value-of
				select="concat('view-all/?parentId=', ./@groupId )" />
		</xsl:variable>
    <!--
      select="concat('?output=html&amp;command=view-all&amp;parentId=', ./@groupId, '&amp;schema=groups-list-fragment')" />
    -->
		<div>
		  <!-- create link for view child groups -->
			<img src="/organization/img/SmallGrayPlus.gif" alt="+"
				onclick="groups('{concat(substring-before(./@xlink:href, '?'), $childGroupsSuffix)}', this);" />
			<xsl:text> </xsl:text>
      <!-- create link for view group info -->
			<a href="{concat(substring-before(./@xlink:href, '?'), $groupInfoSuffix)}">
				<img src="/organization/img/SmallGroupIcon.gif" alt="#" />
				<xsl:text> </xsl:text>
				<xsl:value-of select="." />
			</a>
		</div>
	</xsl:template>
</xsl:stylesheet>
