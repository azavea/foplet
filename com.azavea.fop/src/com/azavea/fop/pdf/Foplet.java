/*********************************************************************
 *
 * Copyright 2011 Azavea, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ********************************************************************/

package com.azavea.fop.pdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import java.nio.ByteBuffer;
import java.net.URL;
import java.net.URLConnection;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.servlet.ServletContextURIResolver;

/**
 * A servlet class that generates PDFs when provided an xml and an xslt.
 * <br/>
 * Servlet params are:
 * <ul>
 *   <li>xml: an XML file to render</li>
 *   <li>xslt: an XSLT file that can transform the above XML to XSL-FO</li>
 * </ul>
 * <br/>
 * For this to work with Internet Explorer, you might need to append "&ext=.pdf"
 * to the URL.
 *
 * @author <a href="mailto:dzwarg@azavea.com">Azavea Incorporated</a>
 * @version 1.0.0.0
 */
public class Foplet extends HttpServlet {

    /** Name of the parameter used for the XML file */
    protected static final String XML_REQUEST_PARAM = "xml";

    /** Name of the parameter used for the XSLT file */
    protected static final String XSLT_REQUEST_PARAM = "xslt";

    /** Logger to give to FOP */
    protected SimpleLog log = null;

    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;

    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory = null;

    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver;
	
    /**
     * Initialize the foplet servlet. This method configures a log,
     * transformer factory and resolvers necessary for pulling in
     * external graphics from URLs.
     *
     * @see javax.servlet.GenericServlet#init()
     * @throws ServletException
     */
    public void init() throws ServletException {
        this.log = new SimpleLog("foplet");
        log.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        this.uriResolver = new ServletContextURIResolver(getServletContext());
        this.transFactory = TransformerFactory.newInstance();
        this.transFactory.setURIResolver(this.uriResolver);

        // Configure FopFactory as desired
        this.fopFactory = FopFactory.newInstance();
        this.fopFactory.setURIResolver(this.uriResolver);
    }
    
    /**
     * Accept POST requests with XML and XSLT documents as the POST request body,
     * and return a PDF document.
     * <p>
     * If the xml or xslt parameters are missing, this method responds with an error 
     * message in text/html.
     *
     * @param request An HttpServletRequest
     * @param response An HttpServletResponse
     * @throws ServletException When something goes wrong.
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {
            //Get parameters
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xsltParam = request.getParameter(XSLT_REQUEST_PARAM);

            //Analyze parameters and decide which method to use
            if ((xmlParam != null) && (xsltParam != null)) {
                renderXML(xmlParam, xsltParam, response);
            } else {
                response.setContentType("text/html");
                response.getWriter().println("<html><head>" +
                    "<title>Error</title></head><body>" +
                    "<h1>Could Not Create Document</h1>" +
                    "<h3>No request parameters provided.</h3>" + 
                    "<p>Both 'xml' and 'xslt' parameters are required " +
                    "to generate a PDF.</p></body></html>");
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * Convert a String parameter to a JAXP Source object.
     *
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        Source src;
		if ( param.startsWith("<?xml") )
		{
			src = new StreamSource( new ByteArrayInputStream( param.getBytes() ) );
		}
		else
		{
			src = new StreamSource(new File(param));
		}
        return src;
    }

    /**
     * Send the PDF content in the response.
     *
     * @param content A byte array of PDF content.
     * @param response An HttpServletResponse.
     * @throws IOException
     */
    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    /**
     * Renders an XML file into a PDF file by applying a stylesheet
     * that converts the XML to XSL-FO. The PDF is written to a byte array
     * that is returned as the method's result.
     *
     * @param xml the XML file
     * @param xslt the XSLT file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the XSL-FO
     * @throws TransformerException If an error occurs during XSL transformation
     * @throws IOException In case of an I/O problem
     */
    protected void renderXML(String xml, String xslt, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup sources
        Source xmlSrc = convertString2Source(xml);
        Source xsltSrc = convertString2Source(xslt);

        //Setup the XSL transformation
        Transformer transformer = this.transFactory.newTransformer(xsltSrc);

        //Start transformation and rendering process
        render(xmlSrc, transformer, response);
    }

    /**
     * Renders an input file (XML or XSL-FO) into a PDF file. It uses the JAXP
     * transformer given to optionally transform the input document to XSL-FO.
     * The transformer may be an identity transformer in which case the input
     * must already be XSL-FO. The PDF is written to a byte array that is
     * returned as the method's result.
     *
     * @param src Input XML or XSL-FO
     * @param transformer Transformer to use for optional transformation
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the XSL-FO
     * @throws TransformerException If an error occurs during XSL transformation
     * @throws IOException In case of an I/O problem
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        //Setup output
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Setup FOP
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        sendPDF(out.toByteArray(), response);
	}
}
