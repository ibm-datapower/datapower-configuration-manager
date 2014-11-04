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
 
import com.urbancode.air.AirPluginTool;
import com.urbancode.air.CommandHelper

try 
{
  def apTool = new AirPluginTool(this.args[0], this.args[1]);
  def props = apTool.getStepProperties();

//// Dump the inputs
//if (1) {
//  args.each{ println 'arg ' + it }
//  props.sort().each{ println 'property ' + it }
//  println 'working dir: ' + System.getProperty("user.dir")
//}

  /*
    Set up the DCM properties required/used by deploy.ant.xml
      property dcm.dir (required) - the directory where DCM is installed
      property dcm.jar (optional) - the DCM jar file (default is ${dcm.dir}/dist/dcm.1.jar)
      property work.dir (optional) - the directory where DCM may write temporary files (default is ${dcm.dir}/tmp)
      property schema.dir (optional) - the directory where preprocessed DCM schema files are stored(default is ${dcm.dir}/schemas)
      property host (required) - the IP address or hostname of the DataPower device
      property port (optional) - the XML Management interface port on the device (default is 5550)
      property uid (required) - userid to use in accessing the XML Management interface
      property pwd (required) - password for the userid
      property domain (usually required) - the domain on the device
  */
  def args = [];
  def ch = new CommandHelper(new File('.'));
  def dcmDir = ch.getProcessBuilder().environment().get('PLUGIN_HOME') + '/dcm';
  ch.addEnvironmentVariable('ANT_HOME', dcmDir + '/apache-ant-1.9.4/')
  def isWindows = (System.getProperty('os.name') =~ /(?i)windows/).find()
  def antexe = isWindows ? "ant.bat" : "ant"
  args = [dcmDir + '/apache-ant-1.9.4/bin/' + antexe, 
          '-f', dcmDir + '/deploy.ant.xml', 
          '-Ddcm.dir=' + dcmDir, 
          '-Dhost=' + props['hostname'], 
          '-Dport=' + props['portXMI'], 
          '-Ddomain=' + props['domainName'], 
          '-Duid=' + props['uid'], 
          '-Dpwd=' + props['pwd'], 
          '-Dupload-dir.from=' + props['fromDir'], 
          '-Dupload-dir.to=' + props['toDir'], 
          'upload-dir'];
  ch.runCommand(args.join(' '), args);
} catch (e) {
  println e
  System.exit 1
}


