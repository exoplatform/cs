<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:ns="http://exoplatform.org/organization/rest"
	exclude-result-prefixes="xlink ns">

	<xsl:output method="html" encoding="UTF-8" />
	<xsl:template match="/ns:users">
	
    <xsl:variable name="mainPageLocation">
      <xsl:text>/organization/index.html</xsl:text>
    </xsl:variable>

    <xsl:variable name="addUserPageLocation">
      <xsl:text>/organization/create_user.html</xsl:text>
    </xsl:variable>

    <xsl:variable name="searchUserPageLocation">
      <xsl:text>/organization/search_user.html</xsl:text>
    </xsl:variable>

    <xsl:variable name="userInfoSuffix">
      <xsl:text>view-range/</xsl:text>
    </xsl:variable>

		<html>
			<head>
				<link rel="stylesheet" type="text/css" href="/organization/css/style.css" />
        <script type="text/javascript" src="/organization/js/script.js"></script>
        <title>Users Browser</title>
			</head>
			<body>
				<div class="main">
					<div class="inner">

            <p class="table-title">Users Browser</p>
					
						<xsl:if test="ns:prev-range">
							<input class="input" type="button" value="Prev" style="float: left;"><xsl:attribute name="onclick">
                <xsl:text>top.location="</xsl:text>
                <xsl:value-of
								select="concat(substring-before(ns:prev-range/@xlink:href, '?'), $userInfoSuffix)" />
                <xsl:text>"; return true;</xsl:text>
                </xsl:attribute></input>
						</xsl:if>
						
						<xsl:if test="ns:next-range">
							<input class="input" type="button" value="Next"	style="float: left;"><xsl:attribute name="onclick">
                <xsl:text>top.location="</xsl:text>
								<xsl:value-of
								select="concat(substring-before(ns:next-range/@xlink:href, '?'), $userInfoSuffix)" />
                <xsl:text>"; return true;</xsl:text>
								</xsl:attribute></input>
						</xsl:if>
						
						<table class="users-list" style="clear:both;">
							<tr>
								<th>User Name</th>
								<th>First Name</th>
								<th>Last Name</th>
								<th>Email</th>
								<th>Actions</th>
							</tr>
							
							<xsl:choose>
								<xsl:when test="count(./ns:user)=0">
									<tr>
										<td class="action" colspan="5">
											<i>No Items</i>
										</td>
									</tr>
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="ns:user">
									</xsl:apply-templates>
								</xsl:otherwise>
							</xsl:choose>

						</table>
						<input class="input" type="button" value="Add User" style="clear:both; float: left;"
							onclick="top.location='{$addUserPageLocation}'; return true;" />
						<input class="input" type="button" value="Search"
							style="float: left;"
							onclick="top.location='{$searchUserPageLocation}'; return true;" />
            <input class="input" type="button" value="Reload" style="float: left;"
              onclick="window.location.reload();" />
            <input class="input" type="button" value="Main page"
              style="float: left;"
              onclick="top.location='{$mainPageLocation}'; return true;" />

					</div>
				</div>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="ns:user">

    <xsl:variable name="userInfoSuffix">
      <xsl:text>info/</xsl:text>
      <xsl:value-of select="./ns:name" />
    </xsl:variable>

    <xsl:variable name="userDeleteSuffix">
      <xsl:text>delete/</xsl:text>
      <xsl:value-of select="./ns:name" />
    </xsl:variable>

		<tr>
			<td><xsl:value-of select="./ns:name" /></td>
			<td><xsl:value-of select="./ns:first-name"/></td>
			<td><xsl:value-of select="./ns:last-name"/></td>
			<td><xsl:value-of select="./ns:email"/></td>
			
			<td class="action">
        <img src="/organization/img/Edit.gif" alt="Edit" title="Edit" style="cursor:pointer;"><xsl:attribute name="onclick">
          <xsl:text>top.location="</xsl:text>
          <xsl:value-of select="concat(substring-before(./@xlink:href, '?'), $userInfoSuffix)" />
          <xsl:text>"; return true;</xsl:text></xsl:attribute></img>
        <xsl:text>  </xsl:text>
        
				<img src="/organization/img/Delete.gif" alt="Delete" title="Delete" style="cursor:pointer;"><xsl:attribute name="onclick">
          <xsl:text>if(confirm("Do you want delete user '</xsl:text>
          <xsl:value-of select="./ns:name" />
          <xsl:text>' ?")) remove("</xsl:text>
          <xsl:value-of select="concat(substring-before(./@xlink:href, '?'), $userDeleteSuffix)" />
          <xsl:text>");</xsl:text></xsl:attribute></img>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
