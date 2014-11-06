# DataPower Configuration Manager (DCM)

DCM is a package for dealing with IBM DataPower configuration management.
It provides an Ant-based command line tool and a plugin for IBM Rational UrbanCode 
Deploy (UCD).

## Prerequisites

* JDK 1.6 or later
* Apache Ant (1.8.1 or later, 1.9.4 will be packaged with UCD plugin)

## Building

After you obtain the source either via a Git client or by downloading the repository zip file,
you can build the code using Apache Ant. Enter the dcm directory and issue the command below (assumes
Apache Ant is in the path):

    ant -f distro.ant.xml -Ddcm.version=1.0.1

The UCD plugin will be found in dcm-distros/dcm_1.0.1_plugin.zip

## Contributing

If you want to contribute to the project, you will need to fill in the appropriate Contributor 
License agreement which can be found under the CLA directory. Follow the directions inside the
document so that any submissions can be properly accepted into the repository.

## License

The code is licensed under the terms of the Apache License 2.0. See the acompanying Apache-2.0-License.txt
for further details.