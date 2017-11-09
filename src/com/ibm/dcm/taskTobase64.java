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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;


public class taskTobase64 extends MatchingTask {

  private String filename = "";
  private String propname = "";
  private String successProperty = null;

  public taskTobase64 () {
  }


  public void setFile(String filename) {
    this.filename = filename;
  }

  public void setProp(String propname) {
    this.propname = propname;
  }

  public void setSuccessprop(String propname) {
    successProperty = propname;
  }

  public void execute()
  {

    Project proj = getProject();
    try {

      if (filename.length() == 0 || propname.length() == 0) {
        throw new BuildException("The <tobase64> element requires two attributes: file=\"...\" is the name of a file to read and prop=\"...\" is the name of an ANT property in which the base-64 form of the file's content is stored.");
      } else {
        proj.setProperty(propname, Base64.base64FromBinaryFile(filename));
      }

      if (successProperty != null)
        proj.setProperty(successProperty, "true");

    } catch (Exception e) {

      if (successProperty != null)
        proj.setProperty(successProperty, "false");
      else
        throw new BuildException(e);
    }

  }

}
