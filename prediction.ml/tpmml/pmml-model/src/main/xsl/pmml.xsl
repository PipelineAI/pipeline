<?xml version="1.0" ?>
<!--
Copyright (c) 2009 University of Tartu
-->
<xsl:stylesheet version="1.0" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!--
	Simplified Array type definition
	-->
	<xsl:template match="xsd:element[@name='Array']">
	</xsl:template>

	<xsl:template match="xsd:complexType[@name='ArrayType']">
		<xsl:element name="xsd:element">
			<xsl:attribute name="name">Array</xsl:attribute>
			<xsl:element name="xsd:complexType">
				<xsl:element name="xsd:simpleContent">
					<xsl:element name="xsd:extension">
						<xsl:attribute name="base">xsd:string</xsl:attribute>
						<xsl:copy-of select="*"/>
					</xsl:element>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!-- 
	Model types have one Extension list in the beginning and another Extension list in the end, which is too complex for the XJC to handle.
	-->
	<xsl:template match="xsd:element[@ref='Extension'][position() &gt; 1 and position() = last()]">
		<xsl:call-template name="extension-comment"/>
	</xsl:template>

	<xsl:template match="xsd:group[@name='EmbeddedModel']/xsd:sequence/xsd:element[@ref='Extension']">
		<xsl:call-template name="extension-comment"/>
	</xsl:template>

	<xsl:template name="extension-comment">
		<xsl:comment> &lt;xs:element ref=&quot;Extension&quot; minOccurs=&quot;0&quot; maxOccurs=&quot;unbounded&quot;/&gt; </xsl:comment>
	</xsl:template>
</xsl:stylesheet>