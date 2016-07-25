/**
 * Copyright 2014 IBM Corp.
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
 **/


package com.ibm.dcm;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.w3c.dom.ls.*;


/**
 * This class provides useful utility functions for the classes that implement 
 * all the various SOMA calls.
 * 
 */
public class SomaUtils {

  private static XPathFactory xpathFactory = XPathFactory.newInstance();
  private static MyNamespaces myNamespaces = new MyNamespaces();



  /**
   * Returns a SOAP envelope that embeds the supplied body of a <soma:do-action>.
   * For example, this call:
   * 
   * getDoActionEnvelope("xyzzy", actionBody);
   * 
   * returns this:
   * 
   * 
   */
  public static String getDoActionEnvelope(String domain, String actionBody) {
    String env =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
    "<env:Body>" +
    "<soma:request domain=\"" + domain + "\" xmlns:soma=\"http://www.datapower.com/schemas/management\">" +
    "<soma:do-action>" +
    actionBody + 
    "</soma:do-action>" +
    "</soma:request>" +
    "</env:Body>" +
    "</env:Envelope>";
    return env;
  }



  /**
   * Returns a SOAP envelope that embeds the supplied body of a <soma:do-action>.
   * For example, this call:
   * 
   * String actionBody = "<soma:get-status class="MemoryStatus"/>"
   * getDoActionEnvelope("xyzzy", actionBody);
   * 
   * returns this:
   * 
   *  <?xml version="1.0" encoding="UTF-8"?>
   *  <env:Envelope xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">
   *      <env:Body>
   *          <soma:request domain='xyzzy' xmlns:soma="http://www.datapower.com/schemas/management">
   *              <soma:get-status class="MemoryStatus"/>
   *          </soma:request>
   *      </env:Body>
   *  </env:Envelope>
   * 
   */
  public static String getGeneralEnvelope(String domain, String requestBody) {
    String env =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
    "<env:Body>" +
    "<soma:request domain=\"" + domain + "\" xmlns:soma=\"http://www.datapower.com/schemas/management\">" +
    requestBody + 
    "</soma:request>" +
    "</env:Body>" +
    "</env:Envelope>";
    return env;
  }


  /**
   * This method converts the supplied XML (UTF-8 assumed!) to a DOM.  
   * It returns a root node or throws an exception.
   */
  public static Node getDOM (String xml) throws Exception {

    // Get the XML into a DOM.
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = domFactory.newDocumentBuilder();
    Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

    return doc.getDocumentElement();
  }


  /**
   * This method searches the supplied XML tree for the specified XPath expression.
   * Returns a node or throws an exception for any errors.
   * 
   * These namespaces are defined for the XPath:
   * 
   */
  public static Node nodeXpathFor (Node root, String xpathDesired) throws Exception {

    XPath xpath = xpathFactory.newXPath();
    xpath.setNamespaceContext(myNamespaces);
    Node nodeRet = (Node)(xpath.evaluate(xpathDesired, root, XPathConstants.NODE));

    return nodeRet;
  }


  /**
   * This method searches the supplied XML tree for the specified XPath expression.
   * Returns a node list or throws an exception for any errors.
   * 
   * These namespaces are defined for the XPath:
   * 
   */
  public static NodeList nodelistXpathFor (Node root, String xpathDesired) throws Exception {

    XPath xpath = xpathFactory.newXPath();
    xpath.setNamespaceContext(myNamespaces);
    NodeList listRet = (NodeList)(xpath.evaluate(xpathDesired, root, XPathConstants.NODESET));

    return listRet;
  }


  /**
   * This method searches the supplied XML tree for the specified XPath expression.
   * Returns a string or throws an exception for any errors.
   * 
   * These namespaces are defined for the XPath:
   * 
   */
  public static String stringXpathFor (Node root, String xpathDesired) throws Exception {

    XPath xpath = xpathFactory.newXPath();
    xpath.setNamespaceContext(myNamespaces);
    String strRet = (String)(xpath.evaluate(xpathDesired, root, XPathConstants.STRING));

    return strRet;
  }


  /**
   * This method expects serialized XML and it returns the XML without any
   * leading <?...?> bits.
   * 
   * @param xml
   * @return clean XML
   */
  public static String xmlNoHeader (String xml) {
    if (xml.startsWith("<?")) {
      // Strip any leading <?...?> bits, plus "\n".
      StringBuffer buf = new StringBuffer (xml);
      while (buf.substring(0, 2).equals("<?")) {
        int end = buf.indexOf("?>");
        buf.delete(0, end + 2);
        while ((buf.charAt(0) == '\n') || (buf.charAt(0) == '\r')) {
          buf.deleteCharAt(0);
        }
      }
      return buf.toString();
    } else {
      return xml;     // Nothing to do.
    }
  }


  /**
   * This method serializes the supplied XML.  It returns something like this:
   * 
   * <?xml version="1.0" encoding="UTF-16"?>
   * <soma:response xmlns:soma="http://www.datapower.com/schemas/management">
   *     <soma:timestamp>2008-11-26T19:18:01-05:00</soma:timestamp>
   *     <soma:result>OK</soma:result>
   * </soma:response>
   * 
   * Yes, it encodes with UTF-16 rather than the preferred UTF-8.  Someone ought to
   * invest the time to correct this behavior.
   */
  public static String serializeXML (Node root) {
    if (root == null)
      return "<null/>";
    if (root.getFeature("Core", "3.0") == null)
      throw new UnsupportedOperationException("JVM doesn't support DOM Core 3.0");
    if (root.getFeature("LS", "3.0") == null)
      throw new UnsupportedOperationException("JVM doesn't support DOM LS 3.0");

    DOMImplementationLS ls = (DOMImplementationLS)(root.getOwnerDocument().getImplementation()).getFeature("LS", "3.0");
    LSSerializer lss = ls.createLSSerializer();
    String result = lss.writeToString(root);

    return result;
  }

  /**
   * Ensure that the supplied DataPower response contains <dp:response><dp:result>OK</dp:result></dp:response> or 
   * throw an exception that contains the explanation plus the contents of the <dp:result>, which hopefully will 
   * shed some additional light on the problem. 
   * 
   * @param dpresponse - XML containing the complete SOAP response message from DataPower's XML Management Interface.
   * @param explanation - an explanation to include in the exception, if one is thrown. 
   * @throw RuntimeException when the DataPower response is not "OK". 
   */
  public static void ensureOK (String dpresponse, String explanation) throws Exception {

    Node dom = getDOM(dpresponse);
    Node nodeResponse = nodeXpathFor(dom, "/env:Envelope/env:Body/soma:response");
    if (nodeResponse != null) {
      // Test whether the request succeeded by checking for <soma:result>OK</soma:result>.
      String okay = SomaUtils.stringXpathFor (nodeResponse, "soma:result").trim();
      if (!okay.equals("OK")) {
        throw new RuntimeException (explanation + " : " + xmlNoHeader(serializeXML(nodeXpathFor(dom, "/env:Envelope/env:Body/soma:response/soma:result"))));
      }
    }
  }
} 
