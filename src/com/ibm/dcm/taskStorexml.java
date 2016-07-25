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

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.XMLFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class taskStorexml extends XMLFragment {

  private String outProp = "";
  private String outFile = "";
  private StringBuffer text = new StringBuffer();

  public taskStorexml () {
  }


  public void setOutprop(String propname) {
    outProp = propname;
  }

  public void setOutfile(String filename) {
    outFile = filename;
  }

  public void addText(String newText) {
    text.append(newText);
  }

  public void execute()
  {
    boolean debug = false;

    Project proj = getProject();
    try {

      // Do basic validation of the inputs.
      if ((outProp+outFile).length() == 0 || (outProp != "" && outFile != ""))
        throw new BuildException("The <storexml> task requires a place to write the result, either an ANT property (outprop) or a file (outfile), or both.");

      NodeList kids = getFragment().getChildNodes();
      StringBuffer result = new StringBuffer();
      if (kids != null) {
        if (kids.getLength() > 0) {
          if (debug) {
            System.out.println("^^^ storexml : kids.getLength() is " + kids.getLength());
          }
          for (int i = 0; i < kids.getLength(); i += 1) {
            Node kid = kids.item(i);
            String value = "";
            if (kid.getNodeType() == Node.ELEMENT_NODE) {
              value = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(kid));
            } else if (kid.getNodeType() == Node.TEXT_NODE) {
              value = kid.getNodeValue();
            }
            result.append(value);
            if (debug) {
              System.out.println("^^^ storexml : kid " + i + " : " + value);
            }
          }
        } else {
          // No XML fragments, so just use any accumulated text.
          String input = text.toString();
          if (input.startsWith("${") && input.endsWith("}")) {
            // Appears to be a reference to an Ant property - go fetch it.
            String value = proj.getProperty(input.substring(2, input.length() - 1));
            if (value != null) {
              input = value;
              if (debug) {
                System.out.println("^^^ storexml : text was a reference to an Ant property, which we fetched.");
              }
            } else {
              if (debug) {
                System.out.println("^^^ storexml : text was not a reference to an Ant property so just using it as it is.");
              }
            }
          }

          try {

            Node root = SomaUtils.getDOM(input);
            if (root != null) {

              // Well, it appears to be XML, since it parsed, so capture it.
              String value = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(root));
              result.append(value);
              if (debug) {
                System.out.println("^^^ storexml : found XML as text : " + value);
              }
            }

          } catch (Exception e) {

            // Well, failed to parse so perhaps it isn't XML.  Treat it as plain text.
            result.append(text);
            if (debug) {
              System.out.println("^^^ storexml : no XML, just text : " + text);
            }

          }
        }
      } else {
        if (debug) {
          System.out.println("^^^ storexml : kids is null");
        }
      }

      // Deliver the result.
      if (outProp != "") {

        proj.setProperty(outProp, result.toString());

      }
      if (outFile != "") {

        OutputStream out = new FileOutputStream (outFile);
        out.write(result.toString().getBytes());
        out.close();

      }

    } catch (Exception e) {

      throw new BuildException (e);
    }

  }

}
