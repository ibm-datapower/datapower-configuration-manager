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

## DCM Ant Target List

Complete list of current DCM target commands. (* = matching plug-in step)

- __backup-device__*  - Backup all domains on a device (minus key/certificate files)
- __backup-domains__* - Backup one or more domains on a device (minus key/certificate files)

- __certificate-from-def__ - upload a cert (or multiple certs) based on a dcm:definition (with Crypto Certificate objects, of course)

- __check-access__ - check whether the device is accessible and that the userid/password works

- __checkpoint-delete__*  - delete the specified checkpoint
- __checkpoint-restore__* - restore from the specified checkpoint
- __checkpoint-save__*    - create/overwrite the specified checkpoint

- __create-tam-files__ - Create TAM files based on a whole bunch of parameters

- __clean__ - delete any temp files created by this ant script

- __domain-create__*    - ensure the domain exists
- __domain-delete__*    - delete the domain
- __domain-init__       - delete and recreate the domain, then upload the standard files
- __domain-quiesce__*   - quiesce all the services in the domain
- __domain-recreate__   - delete and recreate the domain, then save
- __domain-reset__      - clear all the objects from the domain (but not files)
- __domain-restart__*   - restart the domain
- __domain-unquiesce__* - unquiesce all the services in the domain

- __download-files__* - Download target files or all files from a filestore

- __export-object__* - export an object i.e. service
- __export-objects__ - export objects based on a dcm:definition file

- __firmware-rollback__ - Rollback to the previous firmware (and filesystem contents!)
- __firmware-update__   - Update to a new level of firmware

- __flush-document-cache__*   - Flush a domain's document cache
- __flush-stylesheet-cache__*   - Flush a domain's stylesheet cache

- __host-alias-remove__* - remove a specific host alias
- __host-alias-set__*    - create/overwrite a host alias

- __idcred-from-def__* - create an idcred object based on a dcm:definition
- __idcred-from-key-and-cert__ - create an idcred object after uploading matching key and certificate files
- __idcred-from-p12__  - create an idcred object after uploading a #PKCS12 file containing matching key and certificate

- __import-changed__   - import a specified .zip or .xcfg file into the domain, making changes along the way
- __import-from-def__* - ditto
- __import-dpo__*      - import a specified .zip or .xcfg file into the domain, making changes along the way

- __key-from-def__ - upload a key (or multiple keys) based on a dcm:definition (with Crypto Key objects, of course)
- __key-create__   - generate a private key, a public key and a self-signed certificate

- __load-balancer-group-from-def__* - create/overwrite a load balancer group object

- __ltpa-password__ - prompt the console user for the LTPA shared-secret password

- __main (default target)__ - execute the import-changed and save targets (in that order)

- __objects-from-def__ - create, delete, or modify objects based on dcm:object-* elements

- __object-status__ - check the opstate of objects are as required based on a dcm:definition file

- __password-alias-create__ - Create PasswordAlias object
- __password-alias-update__ - Update password in PasswordAlias object

- __raw-mgmt-call__* - make a raw management (SOMA or AMP etc) call based on raw request file input

- __reboot__ - make an appliance reboot request

- __restore-backup__* - Restore one or more domains from backup file (see Backup Device or Backup Domains)

- __save__* - save the domain

- __secure-backup-device__ - secure backup of device

- __service-quiesce__   - quiesce a service
- __service-unquiesce__ - unquiesce a service
- __service-status__    - get the opstate of a service

- __upload-dir__*      - upload an entire directory
- __upload-from-def__* - upload files specified in dcm:definition/dcm:upload elements

- __valcred-from-def__* - create a valcred object based on a dcm:definition
- __valcred-from-dir__  - create a valcred object from a set of certificates in a directory
