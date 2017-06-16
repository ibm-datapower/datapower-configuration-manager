# DataPower Configuration Manager (DCM) [![Build Status](https://travis-ci.org/ibm-datapower/datapower-configuration-manager.svg?branch=master)](https://travis-ci.org/ibm-datapower/datapower-configuration-manager)

DCM is a package for dealing with IBM DataPower configuration management.
It provides an Ant-based command line tool and a plugin for IBM UrbanCode
Deploy (UCD).

## Prerequisites

* JDK 1.6 or later is required to build. An equivalent JRE is supported if using a prebuilt plugin.
* Apache Ant (1.8.1 or later, 1.9.9 will be packaged with UCD plugin)

## Building

After you obtain the source either via a Git client or by downloading the repository zip file,
you can build the code using Apache Ant. Enter the dcm directory and issue the command below (assumes
Apache Ant is in the path):

    ant

The UCD plugin will be found in `dist/datapower-vdev.zip`

See Quick Start in the wiki for more details on installation.

## Download
The latest plugin distributable and command line interface can be found under the [Releases Tab](https://github.com/ibm-datapower/datapower-configuration-manager/releases).
Download the `datapower-v*.zip` plugin and follow these [installation directions](https://developer.ibm.com/urbancode/docs/installing-plugins-ucd/#ucd) to use the Datapower plugin in IBM UrbanCode Deploy.
Download the `dcm.jar` file and place it in an accessible location to use the dcm command line interface.
Follow the wiki guide for detailed directions on how to use the plugin and CLI.

## Contributing

If you want to contribute to the project, you will need to fill in the appropriate Contributor
License agreement which can be found under the CLA directory. Follow the directions inside the
document so that any submissions can be properly accepted into the repository.

## License

The code is licensed under the terms of the Apache License 2.0. See the acompanying Apache-2.0-License.txt
for further details.
