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

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.resources.PropertyResource;


public class taskDpupload extends MatchingTask {

  private Vector<Arg> args = new Vector<Arg>();
  private Vector<PropertySet> propertysets = new Vector<PropertySet>();
  private Vector<String> uploadedFiles = new Vector<String>();
  private String successProperty = null;
  private String uploadedFilesProperty = null;
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

  public taskDpupload () {
  }


  public void setDir (File directory) {
    fileset.setDir(directory);
  }

  public void setFile (File file) {
    fileset.setFile(file);
  }

  public void setDomain (String domain) {
    args.add(new Arg("domain", domain));
  }

  public void setHost (String host) {
    args.add(new Arg("host", host));
  }

  public void setPort (String port) {
    args.add(new Arg("port", port));
  }

  public void setPwd (String pwd) {
    args.add(new Arg("pwd", pwd));
  }

  public void setTarget (String target) {
    args.add(new Arg("target", target));
  }

  public void setUid (String uid) {
    args.add(new Arg("uid", uid));
  }

  public void setUrl (String url) {
    args.add(new Arg("url", url));
  }

  public void setSuccessprop(String propname) {
    successProperty = propname;
  }

  public void setUploadedfilesprop(String propname) {
    uploadedFilesProperty = propname;
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

      upload();

      if (successProperty != null)
        proj.setProperty(successProperty, "true");

      if (uploadedFilesProperty != null) {
        proj.setProperty(uploadedFilesProperty, fileListInXML(uploadedFiles));
      }

    } catch (Exception e) {

      if (successProperty != null)
        proj.setProperty(successProperty, "false");
      else
        throw new BuildException(e);
    }

  }


  //
  // Permit <propertyset> elements to be children of <soma>, using ANT's class for the actual implementation.
  //
  public void addConfiguredPropertyset(PropertySet ps) {
    propertysets.add(ps);

    // Copy the contents of the propertyset into the argument-list.
    for (Iterator<?> prop=ps.iterator(); prop.hasNext(); ) {
      PropertyResource res = (PropertyResource)prop.next();
      args.add(new Arg(res.getName(), res.getValue()));
    }

  }


  //
  // These createXyxx methods permit ANT to process attributes under <dpupload> such as 
  // uid="zzzzz" and msg="xxxx"
  //

  public Arg createDomain() {
    Arg arg = new Arg("domain");
    args.add(arg);
    return arg;
  }

  public Arg createHost() {
    Arg arg = new Arg("host");
    args.add(arg);
    return arg;
  }

  public Arg createPort() {
    Arg arg = new Arg("port");
    args.add(arg);
    return arg;
  }

  public Arg createPwd() {
    Arg arg = new Arg("pwd");
    args.add(arg);
    return arg;
  }

  public Arg createTarget() {
    Arg arg = new Arg("target");
    args.add(arg);
    return arg;
  }

  public Arg createUid() {
    Arg arg = new Arg("uid");
    args.add(arg);
    return arg;
  }

  public Arg createUrl() {
    Arg arg = new Arg("url");
    args.add(arg);
    return arg;
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
    return findArg(name, "");
  }

  private String findArg (String name, String defaultValue) {
    String value = defaultValue;

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
   * Upload a set of files/directories described by the current FileSet to a directory
   * on DP specified by the <target>, which is something like local:/// or local:///abc.
   */
  public void upload () throws Exception {

    HashSet<String> createdDirs = new HashSet<String>();

//    System.out.println("### fileset=" + getImplicitFileSet().toString());

    DirectoryScanner dirScanner = getImplicitFileSet().getDirectoryScanner(); 

    String remoteRoot = findArg("target");
    if (remoteRoot.length() > 1 && remoteRoot.charAt(remoteRoot.length() - 1) != '/')
      remoteRoot += "/";

    // Ensure the remote root directory (if any) exists.
//    System.out.println ("creating directory " + remoteRoot + " in domain " + findArg("domain"));
    createDirectory(remoteRoot, createdDirs, true);

    // Ensure that any directories specified in the FileSet are created.
    String[] dirs = dirScanner.getIncludedDirectories();
    for (int i = 0; i < dirs.length; i += 1) {

      String remote = remoteRoot + dirs[i];

//      System.out.println ("creating directory " + remote + " in domain " + findArg("domain"));
      createDirectory(remote, createdDirs, true);
    }

//    System.out.println();

    // Upload the files in the FileSet.
    String[] files = dirScanner.getIncludedFiles();
    String path = dirScanner.getBasedir().getAbsolutePath();
    for (int i = 0; i < files.length; i += 1) {
      String remote = remoteRoot + files[i];
      String local = path + File.separator + files[i];

      System.out.println ("uploading file \"" + local + "\" to \"" + remote + "\" in domain " + findArg("domain"));

      // We don't worry about firmware-specific variations in this SOMA SOAP message because
      // the message hasn't changed between 3.5.1.13 through 3.8.1.7.
      String msg =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                  "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                    "<env:Body>" +
                      "<soma:request domain=\"" + findArg("domain")+ "\" xmlns:soma=\"http://www.datapower.com/schemas/management\">" +
                        "<soma:set-file name=\"" + remote + "\">" + Base64.base64FromBinaryFile(local) + "</soma:set-file>" +
                      "</soma:request>" +
                    "</env:Body>" +
                  "</env:Envelope>";

      String url = findArg("url");
      if (findArg("host").length() > 0) {
        url = "https://" + findArg("host") + ":" + findArg("port", "5550") + "/service/mgmt/current";
      }

      String result = getConnection().sendAndReceive(url, findArg("uid"), findArg("pwd"), msg, dumpInput, dumpOutput, capturesoma);
//      System.out.println("### " + result);
      SomaUtils.ensureOK(result, "Failed uploading file " + remote);

      uploadedFiles.add(remote); // Record the name of this uploaded file, in case the caller wants it.
      uploadedFiles.add(files[i]); // and just the filename, too.
    }
  }

  /**
   * Given a DP name (e.g. local:///orchestra/violins) ensure that the directory
   * exists on DP.  The createdDirectories set is used to avoid futile efforts to 
   * create a directory that already exists.  Note that it is necessary to create 
   * directories from the root out to the leaf nodes.
   * 
   * When isDirectoryName=true then the whole name is treated as a directory (e.g.
   * local:///orchestra/violins denotes a directory and both local:///orchestra and
   * local:///orchestra/violins are created).  When it is false then only local:///orchestra
   * is created and local:///orchestra/violins is assumed to specify a file rather than
   * a directory.
   */
  private void createDirectory (String remote, HashSet<String> createdDirectories, boolean isDirectoryName) throws Exception {

    // Isolate the portion of the directory name following the filestore:/// piece.
    int i = remote.indexOf(":///");
    if (i < 3)
      throw new RuntimeException ("The parameter \"remote\"=\"" + remote + "\" doesn't begin with a filestore (e.g. local:///).");
    String base = remote.substring (0, i + ":///".length());
    String relativeDirs = remote.substring(i + ".///".length());

    // Split the string on the file separators and ensure that each directory exists.
    String[] dirnames = relativeDirs.split("[/\\\\]");
    if (!isDirectoryName) {
      // A filename, so remove the last portion.
      String[] names = new String[dirnames.length - 1];
      for (int n = 0; n < names.length; n += 1)
        names[n] = dirnames[n];
      dirnames = names;
    }
    for (int k = 0; k < dirnames.length; k += 1) {

      // Build the name for this directory.
      String dirname = new String(base);
      for (int n = 0; n < k; n += 1) {
        dirname += dirnames[n] + "/";
      }
      dirname += dirnames[k];

      // Ensure that the directory exists, invoking SOMA if necessary.
      if (!createdDirectories.contains(dirname)) {

        // This directory hasn't been created before (as far as records in createdDirectories show),
        // so create the directory, unless it is a root reference (filestore:///).
        if (!dirname.endsWith(":///")) {

          // We don't worry about firmware-specific variations in this SOMA SOAP message because
          // the message hasn't changed since it was introduced in 3.6.0.0 (Now 3.8.2.x).
          String msg =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
          "<env:Body>" +
          "<soma:request domain=\"" + findArg("domain")+ "\" xmlns:soma=\"http://www.datapower.com/schemas/management\">" +
          "<soma:do-action>" +
          "<CreateDir>" +
          "<Dir>"+ dirname + "</Dir>" + 
          "</CreateDir>" + 
          "</soma:do-action>" +
          "</soma:request>" +
          "</env:Body>" +
          "</env:Envelope>";

//                  System.out.println();
//                  System.out.println ("url=" + findArg("url"));
//                  System.out.println ("uid=" + findArg("uid"));
//                  System.out.println ("pwd=" + findArg("pwd"));
//                  System.out.println ("msg=" + msg);
//                  System.out.println();

          String url = findArg("url");
          if (findArg("host").length() > 0) {
            url = "https://" + findArg("host") + ":" + findArg("port", "5550") + "/service/mgmt/current";
          }

          getConnection().sendAndReceive(url, findArg("uid"), findArg("pwd"), msg, dumpInput, dumpOutput, capturesoma);
          //
          // We don't check for "OK" here because it will fail if you try to "create" the directory that is for the
          // hard drive. Since we can't conveniently tell which directory is the mount point for the hard drive,
          // we just ignore checking for errors creating directories.
          //
        }
        createdDirectories.add(dirname);
      }
    }
  }


  private String fileListInXML (Vector<String> list) {
    StringBuffer sb = new StringBuffer();

    if (list.size() > 0) {

      sb.append("<files>");

      for (int i = 0; i < list.size(); i += 2) {
        String fullname = list.get(i);
        String filename = list.get(i + 1);
        sb.append("<file filenameonly=\"" + filename + "\">" + fullname + "</file>");
      }

      sb.append("</files>");

    } else {

      // No files, so return an empty XML list.
      sb.append("<files/>");

    }

    return sb.toString();
  }


}
