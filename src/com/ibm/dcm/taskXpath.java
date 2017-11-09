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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class taskXpath extends MatchingTask {

  private String inProp = "";
  private String outProp = "";
  private String inFile = "";
  private String outFile = "";
  private String xpath = "";
  private String xpathType = "";

  public taskXpath () {
  }


  public void setInprop(String propname) {
    inProp = propname;
  }

  public void setOutprop(String propname) {
    outProp = propname;
  }

  public void setInfile(String filename) {
    inFile = filename;
  }

  public void setOutfile(String filename) {
    outFile = filename;
  }

  public void setXpath(String text) {
    xpath = text;
  }

  public void setXpathtype(String text) {
    xpathType = text;
  }

  public void execute()
  {

    Project proj = getProject();
    try {

      // Do basic validation of the inputs.
      if ((inProp+inFile).length() == 0 || (inProp != "" && inFile != ""))
        throw new BuildException("The <xpath> task requires XML either from an ANT property (inprop) or a file/url (infile), which are mutually exclusive.");
      if ((outProp+outFile).length() == 0 || (outProp != "" && outFile != ""))
        throw new BuildException("The <xpath> task requires a place to write the result, either an ANT property (outprop) or a file (outfile), or both.");
      if (xpath.length() == 0)
        throw new BuildException("The <xpath> task requires an XPath expression as its content.");

      // Pull in the XML.
      String xml = "";
      if (inProp != "") {

        xml = proj.getProperty(inProp);

      } else {

        File file = new File (inFile);
        byte[] buffer = new byte[(int)file.length()];
        BufferedInputStream input = new BufferedInputStream (new FileInputStream (inFile));
        try {
            input.read (buffer);
        }
        finally {
            input.close();
        }
        xml = new String(buffer);
      }

      // Gather the result of the XPath against the XML.
      Node node = SomaUtils.getDOM(xml);
      String xpathResult = "";
      if (xpathType != null && xpathType.equals("node")) {

        // Return XML for one node
        xpathResult = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(SomaUtils.nodeXpathFor(node, xpath)));

      } else if ("nodelist".equals(xpathType)) {

        // Return XML for a node list
        NodeList nl = SomaUtils.nodelistXpathFor(node, xpath);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
          sb.append(SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nl.item(i))));
        }
        xpathResult = sb.toString();

      } else if ("list".equals(xpathType)) {

        // Return a comma separated list of the text value of each node in the node list
        NodeList nl = SomaUtils.nodelistXpathFor(node, xpath);
        if (nl.getLength() < 1)
          xpathResult = "";
        else {
          StringBuffer sb = new StringBuffer();
          sb.append(nl.item(0).getTextContent());
          for (int i = 1; i < nl.getLength(); i++) {
            sb.append("," + nl.item(i).getTextContent());
          }
          xpathResult = sb.toString();
        }

      } else {

        // Return the text value of the node
        xpathResult = SomaUtils.stringXpathFor(node, xpath);

      }

      // Deliver the result.
      if (outProp != "") {

        proj.setProperty(outProp, xpathResult);

      }
      if (outFile != "") {

        OutputStream out = new FileOutputStream (outFile);
        out.write(xpathResult.getBytes());
        out.close();

      }

    } catch (Exception e) {

      throw new BuildException (e);
    }

  }

}
