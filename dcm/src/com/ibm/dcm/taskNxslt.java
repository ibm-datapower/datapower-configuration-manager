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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.XMLFragment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class taskNxslt extends XMLFragment {

  private String inProp = "";
  private String outProp = "";
  private String inFile = "";
  private String outFile = "";
  private String xslFile = "";

  public taskNxslt () {
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

  public void setXsl(String text) {
    xslFile = text;
  }



  public void execute()
  {

    Project proj = getProject();
    try {

      // Do basic validation of the inputs.
      if ((outProp+outFile).length() == 0 || (outProp != "" && outFile != ""))
        throw new BuildException("The <nxslt> task requires a place to write the result, either an ANT property (outprop) or a file (outfile), or both.");
      if (xslFile.length() == 0)
        throw new BuildException("The <nxslt> task requires an XSL filename.");

      // Pull in the XML.
      String xml = "<null/>";
      if (inProp != "") {

        xml = proj.getProperty(inProp);

      } else if (inFile != "") {

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

      // Parse the input XML.
      DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
      domFactory.setNamespaceAware(true);
      DocumentBuilder builder = domFactory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

      // Set up the stylesheet.
      Transformer xform = XSLCache.getTransformer(getProject(), xslFile);
      StringWriter outstring = new StringWriter();

      // Check for any <param ant="..." xsl="..."/> child elements.
      //
      // Since this class is derived from XMLFragment, ANT stores any child elements under the
      // <xsl> element as individual elements in this fragment.  (The kids don't have a parent.)
      xform.clearParameters();
      NodeList kids = getFragment().getChildNodes();
      for (int i = 0; i < kids.getLength(); i += 1) {
        Node node = kids.item(i);
        if ((node.getNodeType() == Node.ELEMENT_NODE) && node.getNodeName().equals("param")) {
          // Found a <param> element.  See if it has both @ant and @xsl attributes, non-empty of course.
          String ant = ((Element)node).getAttribute("ant");
          String xsl = ((Element)node).getAttribute("xsl");
          if (ant.length() > 0 && xsl.length() > 0) {
            // Pass the contents of the specified ANT property as the specified stylesheet parameter.
            // Or just pass the name of the "ANT property" when it isn't an actual ANT property name.
            Object obj = proj.getProperty(ant);
            if (obj == null)
              obj = ant;
            xform.setParameter(xsl, obj);
          }
        }
      }

      String source = inFile;
      if (inProp != "") {
        source = inProp;
      }
      String destination = outFile;
      if (outProp != "" && outFile != "") {
        destination = outProp + " and " + outFile;
      } else if (outProp != "") {
        destination = outProp;
      }
      System.out.println("nxslt running " + xslFile + " on " + source + " producing " + destination);

      // Run the stylesheet.
      xform.transform(new DOMSource(doc), new StreamResult(outstring));

      // Deliver the result.
      if (outProp != "") {

        proj.setProperty(outProp, SomaUtils.xmlNoHeader(outstring.toString()));

      }
      if (outFile != "") {

        OutputStream out = new FileOutputStream (outFile);
        out.write(outstring.toString().getBytes());
        out.close();

      }

    } catch (Exception e) {

      throw new BuildException (e);
    }

  }
}
