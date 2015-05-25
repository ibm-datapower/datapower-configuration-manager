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

/**
 * This simple class adds forXPath to DCM, allowing the body of the loop 
 * to be executed multiple times based on the XML and XPath, and the optional 
 * xpathtype="...". 
 *  
 * <forxpath (inprop="..." | infile="...") xpath="..."> 
 *   <sequential>
 *     ... tasks to execute ...
 *   </sequential>
 * </forxpath> 
 *  
 * Optional arguments: 
 *   xpathtype = (text|node)
 *   debug = true
 *   propname = name of property within loop, defaults to "forxpath"
 *  
 * Note3: 
 *   1. inprop and infile are mutually exclusive and one or the other is required.
 *   2. The <sequential> task is required.
 *   3. xpathtype=text takes the string() of the current node while
 *      xpathtype=node serializes the current node.
 *   4. propname is the macrodef attribute to use within the <sequential>.
 *  
 * Examples: 
 *  
 *   Given ${in} is <a><b>hello world</b><c><d>live to work</d></c></a>
 *  
 *   <forxpath inprop="in" xpath="/a/*">
 *     <sequential>
 *       <echo>@{forxpath}</echo>
 *     </sequential>
 *   </forxpath>
 *  
 *   prints:
 *  
 *     [echo] hello world          (from <a><b>)
 *     [echo] live to work         (from <a><c>)
 *  
 *   <forxpath inprop="in" xpath="/a/*" xpathtype="node" propname="testme">
 *     <sequential>
 *       <echo>@{testme}</echo>
 *     </sequential>
 *   </forxpath>
 *  
 *   prints:
 *  
 *     [echo] <b>hello world</b>
 *     [echo] <c><d>live to work</d></c>
 *  
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class taskForXpath extends Task {

  private String inProp = "";
  private String inFile = "";
  private String xpath = "";
  private String xpathType = "text";
  private String propname = "forxpath";
  private boolean debug = false;
  private MacroDef baseMacroDef;
  
  
  public taskForXpath() {
    
    // Create the macrodef template that will form the basis for this task.
    baseMacroDef = new MacroDef();
    baseMacroDef.setProject(getProject());

  }

  /**
   * The Task base class requires that the user provide a <sequential> object. 
   * This is where all the child tasks are recorded. 
   * 
   * @return Object 
   */
  public Object createSequential() {
    return baseMacroDef.createSequential();
  }

  
  public void setInprop(String propname) {
    inProp = propname;
  }

  public void setInfile(String filename) {
    inFile = filename;
  }

  public void setXpath(String text) {
    xpath = text;
  }

  public void setXpathtype(String text) {
    xpathType = text;
  }

  public void setPropname(String text) {
    propname = text;
  }

  public void setDebug(String text) {
    String lc = text.toLowerCase();
    if (lc.equals("true") || lc.equals("on") || 
        lc.equals("enable") || lc.equals("enabled") || 
        lc.equals("1")) {
      debug = true;
    }
  }
  

  public void execute() throws BuildException {
    
    // Define the desired property name for the loop variable (e.g. forxpath)    
    MacroDef.Attribute attrForxpath = new MacroDef.Attribute(); 
    attrForxpath.setName(propname);
    baseMacroDef.addConfiguredAttribute(attrForxpath);

    Project proj = getProject();
    try {

      // Do basic validation of the inputs.
      if ((inProp+inFile).length() == 0 || (inProp != "" && inFile != ""))
        throw new BuildException("The <forxpath> task requires XML either from an ANT property (inprop) or a file/url (infile), which are mutually exclusive.");
      if (xpath.length() == 0)
        throw new BuildException("The <forxpath> task requires an xpath=\"...\" attribute.");
      if (!(xpathType.equals("text") || xpathType.equals("node"))) {
        throw new BuildException("xpathtype=\"(text|node)\" where 'text' is the string() value of the node returned by the xpath and 'node' is the serialized form of the node.");
      }
      
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

      if (debug) {
        System.out.println("### xml=" + xml); 
      }

      // Parse the XML and run the xpath against it.
      Node doc = SomaUtils.getDOM(xml);
      NodeList nl = SomaUtils.nodelistXpathFor(doc, xpath);
      
      // For each node returned by the xpath, create a <sequential> element,
      // populate it with the children of this <forXPath> element, then execute
      // it.
      for (int i = 0; i < nl.getLength(); i += 1) {
        String forxpath = nl.item(i).getTextContent();
        if (xpathType.equals("node")) {
          // We'll need to pass this XML to the tasks within the loop as text.
          forxpath = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nl.item(i)));
        }
        if (debug) {
          System.out.println("### " + i + " : " + xpathType + " : " + forxpath); 
        }
        
        try {
          
          // Create an instance of the base <sequential> element (using the 
          // macroDef mechanism) and run it, setting to loop property to the 
          // content of the current node.
          MacroInstance runme = new MacroInstance();
          runme.setProject(getProject());
          runme.setOwningTarget(getOwningTarget());
          runme.setMacroDef(baseMacroDef);
          runme.setDynamicAttribute(propname, forxpath);
          runme.execute();
          
        } catch (BuildException e) {
          throw e;
        }
      }
    } catch (Exception e) {
      // Checked exceptions were some language designer's irritating idea that
      // didn't work out, but now we're stuck with them. 
      throw new BuildException (e);
    }
  }
}

