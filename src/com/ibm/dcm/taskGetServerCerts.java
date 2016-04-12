/**
 * Copyright 2015 IBM Corp.
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
 * This class implements a highly specialized task - getting the certificate 
 * chain presented by a server during an SSL negotiation. 
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


public class taskGetServerCerts extends Task {

  private String host = null;
  private int    port = 0;
  private String outProperty = null;

  private boolean debug = false;
  private MacroDef baseMacroDef;
  
  private static SSLConnection singletonConnection = null;

  public static SSLConnection getConnection() throws Exception {
    if (singletonConnection == null) {
      singletonConnection = new SSLConnection();
    }
    return singletonConnection;
  }
  
  public taskGetServerCerts() {
    
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

  

  public void setHost(String propname) {
    if (propname.length() > 0) {
      host = propname;
    }
  }

  public void setPort(String propname) {
    if (propname.length() > 0) {
      port = Integer.parseInt(propname, 10);
    }
  }

  public void setOutprop(String propname) {
    if (propname.length() > 0) {
      outProperty = propname;
    }
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
    
    Project proj = getProject();
    try {

      // Do basic validation of the inputs.
      if (host == null || port == 0 || outProperty == null)
        throw new BuildException("The <getservercerts> task requires host, port, and outprop attributes.");
      
      String xml = getConnection().getCertChain(host, port);
      proj.setProperty(outProperty, xml);
      
    } catch (Exception e) {
      // Checked exceptions were some language designer's irritating idea that
      // didn't work out, but now we're stuck with them. 
      throw new BuildException (e);
    }
  }
}

