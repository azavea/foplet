<!--
   Copyright 2011 Azavea, Inc

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Author: David Zwarg
-->
<html>
  <body>
    <h1>Foplet Test Form</h1>
    <p>Enter a well formed XML document in the first text box to transform into PDF output. Enter a well formed XSLT document in the second text box that will transform the first data document into an FOP XML document.</p>
    <h2>XML Data Document</h2>
	<form method="post" action="Foplet">
		<textarea rows="12" cols="80" name="xml"><?xml version="1.0" encoding="UTF-8"?>
<projectteam>
  <projectname>Documenting Montreal</projectname>
  <image>http://farm6.static.flickr.com/5293/5535462624_f59bf54d03_d.jpg</image>
  <description>This is a photograph taken in Montreal. It was shared on Flickr and represents March accurately.</description>
</projectteam>
</textarea>
	<h2>XSLT Document</h2>	
		<textarea rows="12" cols="80" name="xslt"><?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

  <!-- ========================= -->
  <!-- root element: projectteam -->
  <!-- ========================= -->
  <xsl:template match="projectteam">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="16pt" font-weight="bold" space-after="5mm">Project: <xsl:value-of select="projectname"/>
          </fo:block>
          <fo:block font-size="12pt" space-after="5mm">
            <!--                                               -->
            <!-- image element assigned to an external graphic -->
            <!--                                               -->
            <fo:external-graphic>
              <xsl:attribute name="src">url('<xsl:value-of select="image"/>')</xsl:attribute>
            </fo:external-graphic>
          </fo:block>
          <fo:block>
            <!--                                                   -->
            <!-- description element text assigned to a text block -->
            <!--                                                   -->
            <xsl:value-of select="description"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
</textarea>
      <p>
		<input type="submit" value="Create PDF"/>
        <input type="reset"/>
      </p>
	</form>
  </body>  
</html>
