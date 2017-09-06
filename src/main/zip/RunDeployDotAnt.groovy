/**
 * Copyright 2015, 2017 IBM Corp.
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

import com.urbancode.air.AirPluginTool
import com.urbancode.air.CommandHelper

try
{
  def apTool = new AirPluginTool(args[0], args[1])
  def props = apTool.getStepProperties()

  def debug = false

  // Dump the inputs
  if (debug) {
    args.each{ println 'Argument: ' + it }
    props.sort().each{ println 'Property: ' + it }
  }

  // An odd bug surfaced in 6.1.0.3 (uncertain about other releases).  Arguments
  // that should be a single argument, e.g. -Dxxx=yyy, are being delivered as
  // two arguments, e.g. -D and xxx=yyy.  This bit of code patches around that
  // by the simple expedient of checking for a -D argument and, when found,
  // rewriting the arguments array.
  if (args.any{it == '-D'}) {
    println '### rewriting args'
    def tmpargs = []
    for (int i = 0; i < args.size(); i += 1) {
      if (args[i] == '-D') {
        tmpargs.add(args[i] + args[i+1])
        i += 1
      } else {
        tmpargs.add(args[i])
      }
    }
    args = tmpargs
    if (debug) {
      args.each{ println 'rewritten arg ' + it }
    }
  }

  def ch = new CommandHelper(new File('.'))
  def dcmDir = ch.getProcessBuilder().environment().get('PLUGIN_HOME') + '/dcm'
  def anthome = dcmDir + '/apache-ant-1.9.9/'
  ch.addEnvironmentVariable('ANT_HOME', anthome)

  // Get ANT_OPTS environment variable
  def envVars = System.getenv()
  def antOpts = envVars['ANT_OPTS']?:""

  // Add -Xmx###m if specified
  def memorySize = props['memorySize']
  if (memorySize != "default") {
      antOpts = antOpts.trim() ? antOpts.trim() + " " : ""
      antOpts += memorySize
      println "[Ok] Setting Java Max Memory Size as '${memorySize}'."
  }

  // On Windows, surround password in quotes
  // Solves all instances except escaping double quotes
  if (apTool.isWindows) {
      println "[Ok] Windows OS identified. Surrounding password in double quotes to help escape special characters."
      props['pwd'] = '"' + props['pwd'] + '"'
  }

  ch.addEnvironmentVariable('ANT_OPTS', antOpts)

  // Construct the initial set of arguments for the ant command.
  def isWindows = (System.getProperty('os.name') =~ /(?i)windows/).find()
  def antexe = isWindows ? "ant.bat" : "ant"
  def antargs = [anthome + "bin/" + antexe,
                 '-f', dcmDir + '/deploy.ant.xml',
                 '-Ddcm.dir=' + dcmDir,
                 '-Dhost=' + props['hostname'],
                 '-Dport=' + props['portXMI'],
                 '-Duid=' + props['uid'],
                 '-Dpwd=' + props['pwd'],
                 '-Dwork.dir=' + ch.getProcessBuilder().directory().getAbsolutePath() + '/tmp']

  // Add -Dignore.error if specified
  def ignoreError = props['ignoreError']
  if (ignoreError) {
      antargs << "-Dignore.error=" + ignoreError
  }

  // When addlProperties != '' then we pick out one or more mappings from UCD properties
  // to additional ant properties.  For example, crypto.dir=${p:resource/work.dir}${p:component.name}/ucdDemo/crypto
  // will pass -Dcrypto.dir=... to deploy.ant.xml. Multiple properties may be defined, separated by '~' characters.
  def addlProps = props['addlProperties']
  if (addlProps.size() > 0) {
    def definitions = addlProps.tokenize('~')
    definitions.each{
      if (debug) {
        println '### additional property : ' + it
      }
      def property = it.toString().tokenize('=')*.trim()
      if (property.size() == 2 && property[0].size() > 0) {
        if (debug) {
          println '###   property ' + property[0] + '=' + property[1]
        }
        antargs += '-D' + property[0] + '=' + property[1]
      } else {
        println '!!! Ignoring malformed additional property: ' + it
      }
    }
  }

  // Arguments 2-N on the command line are passed to deploy.ant.xml after replacing any @xxx@ values,
  // where xxx is the name of some property (e.g. domainName or environment).
  for (int i = 2; i < args.size(); i += 1) {
    def arg = args[i]
    if (debug) {
      println '### arg[' + i + ']=' + arg
    }
    // Check the argument for occurrences of every property.
    props.each{
      it.toString().find('([^=]+)=(.*)') { match, propname, propvalue ->
        // The property=value has been split into propname and propvalue.
        if (debug) {
          println '     ' + propname + '=' + propvalue
        }
        // Replace every occurrence of the @xxx@ property name with the property value.
        arg = arg.replaceAll('@(' + propname + ')@', { Object[] pieces -> props[pieces[1].toString()] })
        if (debug) {
          println '  ->' + arg
        }
      }
    }
    if (arg.matches('-D[^=]+=.+') || arg.contains('=') == false) {
          // Properly formed -Dxxx=yyy or simply an ant target (e.g. idcred-from-def) which doesn't contain an equal sign.
      if (debug) {
        println '%%% arg[' + i + ']=' + arg
      }
      antargs += arg
    } else {
      if (debug) {
        println '%%% ignoring arg[' + i + ']=' + arg
      }
    }
  }

  ch.runCommand(antargs.join(' '), antargs)
} catch (e) {
  println e
  System.exit 1
}
