# Foplet

Foplet is a Java servlet that generates PDF documents from XML data and an XSL transform. The servlet accepts only POST requests, and returns PDF content directly.

## Usage

Copy the provided .war file into your servlet container's webapp folder. For the default setup of [Apache](http://www.apache.org) [Tomcat](http://tomcat.apache.org) on [Ubuntu](http://www.ubuntu.com), that location is: `/var/lib/tomcat6/webapp`.

A demo form with sample content is provided at "http://\<servlet container\>/foplet". This form may be used to craft sample XML data documents and cooresponding XSL transforms that generate XML-FO content. This form may also be used to test the foplet by pasting in copies of data and transform documents, and examining the output PDF.

# References

 * [XSLT Reference](http://www.w3.org/TR/xslt/)
 * [XSLT Tutorial](http://www.w3schools.com/xsl/xsl_w3celementref.asp)
 * [XSL-FO Reference](http://www.w3.org/TR/xsl/#fo-section)
 * [XSL-FO Tutorial](http://www.w3schools.com/xslfo/xslfo_reference.asp)

