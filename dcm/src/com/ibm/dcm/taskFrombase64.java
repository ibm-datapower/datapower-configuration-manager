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


public class taskFrombase64 extends MatchingTask {

  private String filename = "";
  private String base64text = "";
  private String successProperty = null;
  private String outProp = "";

  public taskFrombase64 () {
  }


  public void setFile(String filename) {
    this.filename = filename;
  }

  public void setBase64text(String base64text) {
    this.base64text = base64text;
  }

  public void setSuccessprop(String propname) {
    successProperty = propname;
  }

  public void setOutprop(String propname) {
    outProp = propname;
  }

  public void execute()
  {
    String result = null;
    Project proj = getProject();
    try {
      if ((outProp+filename).length() == 0 || (outProp != "" && filename != ""))
        throw new BuildException("The <frombase64> element requires either a outprop or filename attribute");
      if (base64text.length() == 0)
        throw new BuildException("The <frombase64> element requires a base64text=\"...\"");

      if (outProp != "") {
        byte[] buffer = new byte[base64text.length()];
        Base64.fromBase64(base64text.getBytes(), buffer);
        result = new String(buffer);
        proj.setProperty(outProp, result);
      } else
        Base64.base64ToBinaryFile(base64text, filename);

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
