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
 * This simple class adds if/then/else to DCM, avoiding the need to include ant-contrib.
 * 
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.apache.tools.ant.taskdefs.Sequential;

public class taskIf extends ConditionBase {

  private Sequential seqThen = null;
  private Sequential seqElse = null;

  public void addThen(Sequential task) {

    if (seqThen != null) {
      throw new BuildException("An 'if' task can have only one 'then'.");
    }
    seqThen = task;
  }

  public void addElse(Sequential task) {

    if (seqElse != null) {
      throw new BuildException("An 'if' task can have only one 'else'.");
    }
    seqElse = task;
  }

  public void execute() throws BuildException {

    // Ensure that exactly one condition was specified.
    int numConditions = countConditions();
    if (numConditions != 1) {
      throw new BuildException("An 'if' must have exactly one condition (e.g. equal, and, etc.) - this 'if' has " + numConditions);
    }

    // Ensure that either a 'then' or an 'else' was specified.  An 'if' without one or the other is useless and probably a mistake.
    if ((seqThen == null) && (seqElse == null)) {
      throw new BuildException("An 'if' must have a 'then' and/or 'else' - this 'if' has neither.");
    }

    // Evaluate the condition and either do the 'then' or the 'else'.
    Condition condition = (Condition) getConditions().nextElement();
    Sequential task = condition.eval() ? seqThen : seqElse;
    if (task != null) {
      task.execute();
    }
  }

}
