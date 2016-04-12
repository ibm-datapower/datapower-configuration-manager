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

import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.w3c.dom.Node;


public class taskDpdownload extends MatchingTask {

  private Vector<Arg> args = new Vector<Arg>();
  private String successProperty = null;
  private boolean dumpInput = false;
  private boolean dumpOutput = false;
  private String capturesoma = null;

  private static SSLConnection singletonConnection = null;

  public static SSLConnection getConnection() throws Exception {
    if (singletonConnection == null) {
      singletonConnection = new SSLConnection();
    }
    return singletonConnection;
  }

  public taskDpdownload () {
  }


  public void setDir (String dir) {
    args.add(new Arg("dir", dir));
  }

  public void setDpdir (String dpdir) {
    args.add(new Arg("dpdir", dpdir));
  }

  public void setDomain (String domain) {
    args.add(new Arg("domain", domain));
  }

  public void setFilename (String filename) {
    args.add(new Arg("filename", filename));
  }

  public void setHostname (String hostname) {
    args.add(new Arg("hostname", hostname));
  }

  public void setIgnoreerror (String tf) {
    args.add(new Arg("ignoreerror", tf));
  }

  public void setPort (String port) {
    args.add(new Arg("port", port));
  }

  public void setPwd (String pwd) {
    args.add(new Arg("pwd", pwd));
  }

  public void setUid (String uid) {
    args.add(new Arg("uid", uid));
  }

  public void setSuccessprop(String propname) {
    successProperty = propname;
  }

  public void setDumpinput(boolean flag) {
    dumpInput = flag;
  }

  public void setDumpoutput(boolean flag) {
    dumpOutput = flag;
  }

  public void setCapturesoma(String filename) {
    capturesoma = filename; // filename ending in '.soma'
  }

  public void execute()
  {

    Project proj = getProject();
    try {

      download();

      if (successProperty != null)
        proj.setProperty(successProperty, "true");

    } catch (Exception e) {

      if (successProperty != null)
        proj.setProperty(successProperty, "false");
      else
        throw new BuildException(e);
    }

  }


  // This class simply records a name/value pair.
  public class Arg {

    String name;
    String value;

    public Arg(String name) { 
      this.name = name; 
    }

    public Arg(String name, String value) { 
      this.name = name; 
      this.value = value;
    }

    public void addText(String text) {
      value = text;
    }

    public String getName()
    {
      return name;
    }

    public String getValue()
    {
      return value;
    }
  }

  private String findArg (String name) {
    String value = "";

    for (int i = 0; i < args.size(); i += 1) {
      Arg arg = args.get(i);
      if (arg.getName().equals(name)) {
        value = arg.getValue();
        break;
      }
    }

    return value;
  }


  /**
   * Download the file described by the arguments.
   */
  public void download () throws Exception {

    String remoteDir = findArg("dpdir");
    if (remoteDir.length() > 1 && remoteDir.charAt(remoteDir.length() - 1) != '/')
      remoteDir += "/";
    String localDir = findArg("dir");
    if (localDir.length() > 1 && localDir.charAt(localDir.length() - 1) != '/')
      localDir += "/";
    String remoteName = remoteDir + findArg("filename");
    String localName = localDir + findArg("filename");

    // We don't worry about firmware-specific variations in this SOMA SOAP message because
    // the message hasn't changed between 3.5.1.13 through 4.0.1.0.
    String msg =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
    "<env:Body>" +
    "<soma:request domain=\"" + findArg("domain")+ "\" xmlns:soma=\"http://www.datapower.com/schemas/management\">" +
    "<soma:get-file name=\"" + remoteName + "\"/>" +
    "</soma:request>" +
    "</env:Body>" +
    "</env:Envelope>";

    String port = findArg("port");
    if (port == null || port.length() == 0)
      port = "5550";
    String url = "https://" + findArg("hostname") + ":" + port + "/service/mgmt/current";
//System.out.println("Sending to " + url);
    String result = getConnection().sendAndReceive(url, findArg("uid"), findArg("pwd"), msg, dumpInput, dumpOutput, capturesoma);
//System.out.println(result);

    // Extract, decode, and save the file content.
    Node root = SomaUtils.getDOM (result);
    Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
    if (nodeContent != null) {
      String content = SomaUtils.stringXpathFor(nodeContent, ".");
      if (content != null) {
        // Store the file content, and throw an exception unless the caller doesn't care about success or failure.
        Base64.base64ToBinaryFile(content, localName);
        System.out.println("Downloaded " + remoteName + " to " + localName);
      } else {
        System.out.println("FAILED to download " + remoteName + " to " + localName);
        if (findArg("ignoreerror") == null || findArg("ignoreerror").equals("true") == false) {
          System.out.println("Response from DataPower: " + result);
          throw new RuntimeException("FAILED to download " + remoteName + " to " + localName);
        }
      }
    } else {
      System.out.println("FAILED to download " + remoteName + " to " + localName);
      if (findArg("ignoreerror") == null || findArg("ignoreerror").equals("true") == false) {
        System.out.println("Response from DataPower: " + result);
        throw new RuntimeException("FAILED to download " + remoteName + " to " + localName);
      }
    }
  }


}
