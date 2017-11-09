/**
 * Copyright 2014, 2017 IBM Corp.
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

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.w3c.dom.*;


/**
 * This class provides a bunch of methods that wrap various SOMA calls.
 *
 */
public class Soma {

  private SSLConnection conn;


  /**
   * Create a Soma object the relies on the supplied SSLConnection.
   *
   * @param conn
   */
  public Soma (SSLConnection conn) {
    this.conn = conn;
  }

  /**
   * Copy constructor.
   *
   * @param other
   */
  public Soma (Soma other) {
    this.conn = other.conn;
  }

  /**
   * You can't construct a Soma object without the required SSLConnection object.
   */
  @SuppressWarnings("unused")
  private Soma () {}


  /**
   * This method exists because since you supplied the SSLConnection in the first
   * place then providing you access to it doesn't increase the risk, since you
   * could simply have kept a reference to the object.
   *
   * The SSLConnection object belongs to the Soma object, so don't fiddle with it.
   */
  public SSLConnection getConnection () {
    return conn;
  }


  /**
   * This method performs some SOMA operation (specified in params) and returns a
   * SomaParams object containing the results.  The exact results depend on the
   * operation.
   *
   * The params object must contain (at a minimum) this key/value pair:
   *
   * soma= ... some SOMA operation ... (e.g. MemoryStatus, or SetFile)
   *
   * Throws an exception in case of any errors.
   */
  public NamedParams performOperation (NamedParams params) throws Exception {
    NamedParams result = null;

    params.insistOn("soma");  // The SOMA operation to do

    String somaOp = params.get("soma");
    if (somaOp.equals("AddKnownHost")) {
      result = doAddKnownHost(params);
    } else if (somaOp.equals("AddPasswordMap")) {
      result = doAddPasswordMap(params);
    } else if (somaOp.equals("ChangePasswordMap")) {
      result = doChangePasswordMap(params);
    }else if (somaOp.equals("AddTrustedHost")) {
      result = doAddTrustedHost(params);
    } else if (somaOp.equals("Backup")) {
      result = doBackup(params);
    } else if (somaOp.equals("BackupDevice")) {
      result = doBackupDevice(params);
    } else if (somaOp.equals("BootDelete")) {
      result = doBootDelete(params);
    } else if (somaOp.equals("BootUpdate")) {
      result = doBootUpdate(params);
    } else if (somaOp.equals("CacheSchema")) {
      result = doCacheSchema(params);
    } else if (somaOp.equals("CacheStylesheet")) {
      result = doCacheStylesheet(params);
    } else if (somaOp.equals("CacheWSDL")) {
      result = doCacheWSDL(params);
    } else if (somaOp.equals("ChangePassword")) {
      result = doChangePassword(params);
    } else if (somaOp.equals("ClearFilestore")) {
      result = doClearFilestore(params);
    } else if (somaOp.equals("ConvertCertificate")) {
      result = doConvertCertificate(params);
    } else if (somaOp.equals("ConvertKey")) {
      result = doConvertKey(params);
    } else if (somaOp.equals("CpaImport")) {
      result = doCpaImport(params);
    } else if (somaOp.equals("CreateDirectory")) {
      result = doCreateDirectory(params);
    } else if (somaOp.equals("CreateTAMFiles")) {
      result = doCreateTAMFiles(params);
    } else if (somaOp.equals("CryptoExport")) {
      result = doCryptoExport(params);
    } else if (somaOp.equals("CryptoImport")) {
      result = doCryptoImport(params);
    } else if (somaOp.equals("DeleteConfig")) {
      result = doDeleteConfig(params);
    } else if (somaOp.equals("DeleteFile")) {
      result = doDeleteFile(params);
    } else if (somaOp.equals("DeleteKnownHost")) {
      result = doDeleteKnownHost(params);
    } else if (somaOp.equals("DeleteKnownHostTable")) {
      result = doDeleteKnownHostTable(params);
    } else if (somaOp.equals("DeletePasswordMap")) {
      result = doDeletePasswordMap(params);
    } else if (somaOp.equals("DeleteTrustedHost")) {
      result = doDeleteTrustedHost(params);
    } else if (somaOp.equals("DeviceCertificate")) {
      result = doDeviceCertificate(params);
    } else if (somaOp.equals("DisconnectUser")) {
      result = doDisconnectUser(params);
    } else if (somaOp.equals("ExecConfig")) {
      result = doExecConfig(params);
    } else if (somaOp.equals("ExecCLI")) {
      result = doExecCLI(params);
    } else if (somaOp.equals("Export")) {
      result = doExport(params);
    } else if (somaOp.equals("FetchFile")) {
      result = doFetchFile(params);
    } else if (somaOp.equals("FileCapture")) {
      result = doFileCapture(params);
    } else if (somaOp.equals("FirmwareRollback")) {
      result = doFirmwareRollback(params);
    } else if (somaOp.equals("FlushAAACache")) {
      result = doFlushAAACache(params);
    } else if (somaOp.equals("FlushArpCache")) {
      result = doFlushArpCache(params);
    } else if (somaOp.equals("FlushDNSCache")) {
      result = doFlushDNSCache(params);
    } else if (somaOp.equals("FlushDocumentCache")) {
      result = doFlushDocumentCache(params);
    } else if (somaOp.equals("FlushNDCache")) {
      result = doFlushNDCache(params);
    } else if (somaOp.equals("FlushNSSCache")) {
      result = doFlushNSSCache(params);
    } else if (somaOp.equals("FlushPDPCache")) {
      result = doFlushPDPCache(params);
    } else if (somaOp.equals("FlushRBMCache")) {
      result = doFlushRBMCache(params);
    } else if (somaOp.equals("FlushStylesheetCache")) {
      result = doFlushStylesheetCache(params);
    } else if (somaOp.equals("GenerateErrorReport")) {
      result = doGenerateErrorReport(params);
    } else if (somaOp.equals("GetConfig")) {
      result = doGetConfig(params);
    } else if (somaOp.equals("GetConformanceReport")) {
      result = doGetConformanceReport(params);
    } else if (somaOp.equals("GetDiff")) {
      result = doGetDiff(params);
    } else if (somaOp.equals("GetFile")) {
      result = doGetFile(params);
    } else if (somaOp.equals("GetFilestore")) {
      result = doGetFilestore(params);
    } else if (somaOp.equals("GetObjectStatus")) {
      result = doGetObjectStatus(params);
    } else if (somaOp.equals("GetStatuses")) {
      result = doGetStatuses(params);
    } else if (somaOp.equals("GetLog")) {
      result = doGetLog(params);
    } else if (somaOp.equals("ImportConfig")) {
      result = doImportConfig(params);
    } else if (somaOp.equals("ImportExecute")) {
      result = doImportExecute(params);
    } else if (somaOp.equals("IsUp")) {
      result = doIsUp(params);
    } else if (somaOp.equals("Keygen")) {
      result = doKeygen(params);
    } else if (somaOp.equals("LocateDevice")) {
      result = doLocateDevice(params);
    } else if (somaOp.equals("MemoryStatus")) {
      result = doMemoryStatus(params);  // Implement the get-status/MemoryStatus operation.
    } else if (somaOp.equals("ModifyConfig")) {
      result = doModifyConfig(params);
    } else if (somaOp.equals("MoveFile")) {
      result = doMoveFile(params);
    } else if (somaOp.equals("NoPasswordMap")) {
      result = doNoPasswordMap(params);
    } else if (somaOp.equals("PasswordMap")) {
      result = doPasswordMap(params);
    } else if (somaOp.equals("Ping")) {
      result = doPing(params);
    } else if (somaOp.equals("QuiesceDevice")) {
      result = doQuiesceDevice(params);
    } else if (somaOp.equals("QuiesceDomain")) {
      result = doQuiesceDomain(params);
    } else if (somaOp.equals("QuiesceFSH")) {
      result = doQuiesceFSH(params);
    } else if (somaOp.equals("QuiesceService")) {
      result = doQuiesceService(params);
    } else if (somaOp.equals("RawMgmtCall")) {
        result = doRawMgmtCall(params);
    } else if (somaOp.equals("RefreshDocument")) {
      result = doRefreshDocument(params);
    } else if (somaOp.equals("RefreshStylesheet")) {
      result = doRefreshStylesheet(params);
    } else if (somaOp.equals("RefreshTAMCerts")) {
      result = doRefreshTAMCerts(params);
    } else if (somaOp.equals("RemoveCheckpoint")) {
      result = doRemoveCheckpoint(params);
    } else if (somaOp.equals("RemoveDirectory")) {
      result = doRemoveDirectory(params);
    } else if (somaOp.equals("RemoveStylesheet")) {
      result = doRemoveStylesheet(params);
    } else if (somaOp.equals("ResetDomain")) {
      result = doResetDomain(params);
    } else if (somaOp.equals("RestartDomain")) {
      result = doRestartDomain(params);
    } else if (somaOp.equals("Restore")) {
      result = doRestore(params);
    } else if (somaOp.equals("RollbackCheckpoint")) {
      result = doRollbackCheckpoint(params);
    } else if (somaOp.equals("SaveCheckpoint")) {
      result = doSaveCheckpoint(params);
    } else if (somaOp.equals("SaveConfig")) {
      result = doSaveConfig(params);
    } else if (somaOp.equals("SecureBackup")) {
      result = doSecureBackup(params);
    } else if (somaOp.equals("SecureRestore")) {
      result = doSecureRestore(params);
    } else if (somaOp.equals("SelectConfig")) {
      result = doSelectConfig(params);
    } else if (somaOp.equals("SendErrorReport")) {
      result = doSendErrorReport(params);
    } else if (somaOp.equals("SendFile")) {
      result = doSendFile(params);
    } else if (somaOp.equals("SendLogEvent")) {
      result = doSendLogEvent(params);
    } else if (somaOp.equals("SetConfig")) {
      result = doSetConfig(params);
    } else if (somaOp.equals("SetFile")) {
      result = doSetFile(params);
    } else if (somaOp.equals("SetLogLevel")) {
      result = doSetLogLevel(params);
    } else if (somaOp.equals("SetRBMLogLevel")) {
      result = doSetRBMLogLevel(params);
    } else if (somaOp.equals("SetSystemVar")) {
      result = doSetSystemVar(params);
    } else if (somaOp.equals("SetTimeAndDate")) {
      result = doSetTimeAndDate(params);
    } else if (somaOp.equals("Shutdown")) {
      result = doShutdown(params);
    } else if (somaOp.equals("TCPConnectionTest")) {
      result = doTCPConnectionTest(params);
    } else if (somaOp.equals("TestPasswordMap")) {
      result = doTestPasswordMap(params);
    } else if (somaOp.equals("TestURLMap")) {
      result = doTestURLMap(params);
    } else if (somaOp.equals("TestURLRefresh")) {
      result = doTestURLRefresh(params);
    } else if (somaOp.equals("TestURLRewrite")) {
      result = doTestURLRewrite(params);
    } else if (somaOp.equals("TestValidateSchema")) {
      result = doTestValidateSchema(params);
    } else if (somaOp.equals("UndoConfig")) {
      result = doUndoConfig(params);
    } else if (somaOp.equals("UniversalPacketCaptureDebug")) {
      result = doUniversalPacketCaptureDebug(params);
    } else if (somaOp.equals("UniversalStopPacketCapture")) {
      result = doUniversalStopPacketCapture(params);
    } else if (somaOp.equals("UnquiesceDevice")) {
      result = doUnquiesceDevice(params);
    } else if (somaOp.equals("UnquiesceDomain")) {
      result = doUnquiesceDomain(params);
    } else if (somaOp.equals("UnquiesceFSH")) {
      result = doUnquiesceFSH(params);
    } else if (somaOp.equals("UnquiesceService")) {
      result = doUnquiesceService(params);
    } else if (somaOp.equals("Upload")) {
      result = doUpload(params);
    } else if (somaOp.equals("UserForcePasswordChange")) {
      result = doUserForcePasswordChange(params);
    } else if (somaOp.equals("UserResetFailedLogin")) {
      result = doUserResetFailedLogin(params);
    } else if (somaOp.equals("UserResetPassword")) {
      result = doUserResetPassword(params);
    } else if (somaOp.equals("ValCredAddCertsFromDir")) {
      result = doValCredAddCertsFromDir(params);
    } else if (somaOp.equals("WSRRSynchronize")) {
      result = doWSRRSynchronize(params);
    } else {
      throw new UnsupportedOperationException ("soma=" + somaOp + " is not a recognized SOMA operation.");
    }

    return result;
  }

  // TODO: revisit debug logging properly later..
  private static boolean DEBUG = false;

  private static void debug(String msg) {
      if (DEBUG) System.out.println("[debug] Soma: " + msg);
  }

  /**
   * This method implements the SomaCall operation.
   *
   * The params must contain:
   *
   * request= ... the request file name ...
   * response= ... the request file name ...
   *
   * The params may contain:
   *
   * method= ... the management method (default is '/service/mgmt/current' - i.e. SOMA) ...
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
    public NamedParams doRawMgmtCall(NamedParams params) throws Exception {
        params.insistOn(new String[] { "request", "response" });
        File requestFile = new File(params.get("request"));
        debug("requestFile: "+ requestFile.getAbsolutePath());
        String request = readFile(requestFile);
        NamedParams result = params;
        result = conn.sendAndReceive(params, request, params.get("method"));
        String raw = result.get("rawresponse");
        File responseFile = new File(params.get("response"));
        debug("responseFile: "+ responseFile.getAbsolutePath());
        PrintWriter out = new PrintWriter(responseFile);
        out.println(raw);
        out.close();
        return result;
    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

  /**
   * This method implements the AddKnownHost operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * clientname= ... name of SSH Client object ...
   * knownhost= ... hostname or IP addr ...
   * key= ... text of key ...
   *
   * The params may contain:
   *
   * type=ssh-rsa
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doAddKnownHost (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "clientname", "knownhost", "key"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<AddKnownHost>");
    body.append("<ClientName>" + params.get("clientname") + "</ClientName>");
    body.append("<Host>" + params.get("knownhost") + "</Host>");
    if (params.get("type") != null) {
      body.append("<Type>" + params.get("type") + "</Type>");
    } else {
      body.append("<Type>ssh-rsa</Type>");
    }
    body.append("<Key>" + params.get("key") + "</Key>");
    body.append("</AddKnownHost>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the AddPasswordMap operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * aliasname= ... passwordAlias object name to show to the world ...
   * type= ... real, secret password ... Not needed anymore
   * password= ... actual password ...
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doAddPasswordMap (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "aliasname", "password"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<AddPasswordMap>");
    body.append("<AliasName>" + params.get("aliasname") + "</AliasName>");
    body.append("<Password>" + params.get("password") + "</Password>");
    body.append("</AddPasswordMap>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }

   /**
   * This method implements the changePasswordMap operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * aliasname= ... passwordAlias object name to be updated ...
   * password= ... new password ...
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doChangePasswordMap (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "aliasname", "password"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<soma:modify-config>");
    body.append("<PasswordAlias name=\"" + params.get("aliasname") +"\">");
    body.append("<Password>" + params.get("password") + "</Password>");
    body.append("</PasswordAlias>");
     body.append("</soma:modify-config>");

    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the AddTrustedHost operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * trustedhost= ... hostname or IP addr ...
   * key= ... text of key ...
   *
   * The params may contain:
   *
   * type=ssh-rsa
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doAddTrustedHost (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "trustedhost", "key"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<AddTrustedHost>");
    body.append("<Host>" + params.get("trustedhost") + "</Host>");
    if (params.get("type") != null) {
      body.append("<Type>" + params.get("type") + "</Type>");
    } else {
      body.append("<Type>ssh-rsa</Type>");
    }
    body.append("<Key>" + params.get("key") + "</Key>");
    body.append("</AddTrustedHost>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the do-backup operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * local= ... local filename ...
   *
   * The params may contain:
   *
   * format= ZIP or XML (default ZIP)
   * persisted=true or false (default false)
   * deployment-policy-name= ... name of deployment policy object ...
   * user-comment= ...
   * domains= ... blank-separated list of additional domains ...
   * deployment-policy= ... XML config for ConfigConfigDeploymentPolicy object ...
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doBackup (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<soma:do-backup");
    if (params.get("format") != null) {
      body.append(" format=\"" + params.get("format") + "\"");
    } else {
      body.append(" format=\"ZIP\"");
    }
    if (params.get("persisted") != null) {
      body.append(" persisted=\"" + params.get("persisted") + "\"");
    } else {
      body.append(" persisted=\"false\"");
    }
    if (params.get("deployment-policy-name") != null) {
      body.append(" deployment-policy=\"" + params.get("deployment-policy-name") + "\"");
    }
    body.append(">");
    if (params.get("user-comment") != null) {
      body.append("<soma:user-comment>" + params.get("user-comment") + "</soma:user-comment>");
    }
    body.append("<soma:domain name=\"" + params.get("domain") + "\"/>");
    if (params.get("domains") != null) {

      // Parse the list of domain names, which are merely whitespace separated.
      String[] names = params.get("domains").split("[ ,]+");

      // Spin through all the supplied domain names.
      for (int i = 0; i < names.length; i += 1) {
        if (names[i].equals(params.get("domain")) == false) {
          // Not the primary domain so add this to the list.
          body.append("<soma:domain name=\"" + names[i] + "\"/>");
        }
      }
    }
    if (params.get("deployment-policy") != null) {
      body.append("<soma:deployment-policy>" + params.get("deployment-policy") + "</soma:deployment-policy>");
    }
    body.append("</soma:do-backup>");

    String request = SomaUtils.getGeneralEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      String contentBase64 = SomaUtils.stringXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
      if (contentBase64.length() > 0) {
        Base64.base64ToBinaryFile (contentBase64, params.get("local"));
      } else {
        throw new RuntimeException("Failed to backup on " + params.get("hostname"));
      }
    } else {
      // Do the operation, ignoring a failed response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
        Node root = SomaUtils.getDOM (result.get("rawresponse"));
        String contentBase64 = SomaUtils.stringXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
        if (contentBase64.length() > 0) {
          Base64.base64ToBinaryFile (contentBase64, params.get("local"));
        }
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method backs up all domains on the device.
   *
   * The params must contain:
   *
   * local= ... local filename ...
   *
   * The params may contain:
   *
   * format= ZIP or XML
   * persisted=true or false (default false)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doBackupDevice (NamedParams params) throws Exception {
    params.insistOn (new String[] {"local"});

    // Get the list of domains on the device.
    NamedParams getdomains = new NamedParams(params);
    getdomains.set("statuses", "DomainStatus");
    getdomains.set("domain", "default");
    NamedParams domainstatuses = doGetStatuses(getdomains);

    // Build a <domains> element specifying all but the 'default' domain.
    StringBuffer domains = new StringBuffer();
    Node root = SomaUtils.getDOM (domainstatuses.get("status"));
    NodeList listDomains = SomaUtils.nodelistXpathFor(root, "/status/DomainStatus/Domain");
    if (listDomains != null) {
      for (int i = 0; i < listDomains.getLength(); i += 1) {
        String domainname = listDomains.item(i).getTextContent();
        if (domainname.equals("default") == false) {
          if (i > 0) {
            domains.append(" ");
          }
          domains.append(domainname);
        }
      }
    } else if (errorsAreSignificant(params)) {
      throw new RuntimeException("Failed to get the list of domains on " + params.get("hostname") + " for some reason!");
    }

    params.set ("domains", domains.toString());
    params.set ("domain", "default");

    return doBackup(params);
  }


  /**
   * This method implements the BootDelete operation, which deletes the
   * boot image that you could roll back to.
   *
   * The params must contain:
   *
   * nothing (implicitly used "default" domain)
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doBootDelete (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<BootDelete/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the BootUpdate operation, which specifies a
   * new boot image and reload/restart options.  (Implicitly uses the
   * default domain.)
   *
   * The params must contain:
   *
   * name= ... name of a configuration file (implicitly in the config:/// filestore) ...
   *
   * The params may optionally contain:
   *
   * option= (write | append)
   * ignore-errors=
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doBootUpdate (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<BootUpdate>");
    if (params.get("option") != null)
      body.append("<option>" + params.get("option") + "</option>");
    body.append("<File>" + params.get("name") + "</File>");
    body.append("</BootUpdate>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CacheSchema operation, which causes the
   * cache to contain the most up to date version of the specified
   * XML schema.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * objname= ... name of an XML Manager object ...
   * url= ... a URL, which may be on or off the box, for the schema ...
   *
   * The params may optionally contain:
   *
   * mode= (general | stream)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCacheSchema (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "url"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<CacheSchema>");
    body.append("<XMLManager>" + params.get("objname") + "</XMLManager>");
    body.append("<URL>" + params.get("url") + "</URL>");
    if (params.get("mode") != null)
      body.append("<Mode>" + params.get("mode") + "</Mode>");
    body.append("</CacheSchema>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CacheStylesheet operation, which causes the
   * cache to contain the most up to date version of the specified
   * stylesheet.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * objname= ... name of an XML Manager object ...
   * url= ... a URL, which may be on or off the box, for the stylesheet ...
   *
   * Many SOMA functions correspond closely to CLI commands.  This one, oddly,
   * doesn't, which makes me suspicious.
   *
   * The params may optionally contain:
   *
   * mode= ... check this out ...
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCacheStylesheet (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "url"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<CacheStylesheet>");
    body.append("<XMLManager>" + params.get("objname") + "</XMLManager>");
    body.append("<URL>" + params.get("url") + "</URL>");
    if (params.get("mode") != null)
      body.append("<Mode>" + params.get("mode") + "</Mode>");
    body.append("</CacheStylesheet>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CacheWSDL operation, which causes the
   * cache to contain the most up to date version of the specified
   * WSDL.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * objname= ... name of an XML Manager object ...
   * url= ... a URL, which may be on or off the box, for the WSDL ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCacheWSDL (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "url"});

    // Make the request of the XML Management Interface.
    String body = "<CacheWSDL>" +
    "<XMLManager>" + params.get("objname") + "</XMLManager>" +
    "<URL>" + params.get("url") + "</URL>" +
    "</CacheWSDL>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the ChangePassword operation, which replaces
   * password for the uid with the new password, provided you supply the
   * correct old (current) password.
   *
   * The params must contain:
   *
   * pwd= ... the old/current password ...
   * newpwd= ... the new password ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doChangePassword (NamedParams params) throws Exception {
    params.insistOn (new String[] {"pwd", "newpwd"});

    // Make the request of the XML Management Interface.
    String body = "<ChangePassword>" +
    "<OldPassword>" + params.get("pwd") + "</OldPassword>" +
    "<Password>" + params.get("newpwd") + "</Password>" +
    "</ChangePassword>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method clears a filestore, removing all files and directories.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * filestore=(cert: | export: | local: | logstore: | logtemp: | sharedcert: | temporary:)
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return the original params.
   * @throws Exception
   */
  public NamedParams doClearFilestore (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "filestore"});

    if (params.get("domain").equals("default")) {
      if (errorsAreSignificant(params)) {
        throw new RuntimeException("ClearFilestore is not permitted in the default domain since it affects all domains on the device, and as such is considered too dangerous to permit.");
      } else {
        return params;
      }
    }

    String fs = params.get("filestore");
    if (!(fs.equals("cert:")       ||
          fs.equals("export:")     ||
          fs.equals("local:")      ||
          fs.equals("logstore:")   ||
          fs.equals("logtemp:")    ||
          fs.equals("sharedcert:") ||
          fs.equals("temporary:"))) {
      if (errorsAreSignificant(params)) {
        throw new RuntimeException("Filestore \"" + fs + "\" isn't permitted to be cleared.");
      } else {
        return params;
      }
    }

    // Get the filestore listing and parse it.
    NamedParams fsResult = doGetFilestore(params);
    String location = fsResult.get("location");
    if (location != null && location.length() > 0) {
      Node nodeLocation = SomaUtils.getDOM (fsResult.get("location"));
      fsResult = null;
      if (nodeLocation != null) {

        // Gather all the directory names (e.g. local:/zither) and sort them based on the number of file separators in each.
        Vector<String> dirnames = new Vector<String>();
        extractDirectoryNames(nodeLocation, dirnames);

        // Delete all the directories, which are in order from the deepest to the shallowest.
        for (int i = 0; i < dirnames.size(); i += 1) {
          String dirname = dirnames.get(i);
          String remoteDirname = fs + "///" + dirname;
          System.out.println ("Delete directory " + remoteDirname);

          NamedParams p = new NamedParams (params);
          p.set("remote", remoteDirname);
          p.set("ignore-errors", "true");
          doRemoveDirectory(p);
        }

        // Find all the file elements just beneath the location element and delete those too.
        for (Node kid = nodeLocation.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
          if ((kid.getNodeType() == Node.ELEMENT_NODE) && kid.getNodeName().equals("file")) {

            // Delete this file from the filestore.
            String filename = fs + "///" + ((Element)kid).getAttribute("name");
            System.out.println ("Delete file " + filename);
            NamedParams p = new NamedParams (params);
            p.set("remote", filename);
            doDeleteFile(p);
          }
        }
      }
    }

    return params;
  }

  private void extractDirectoryNames (Node node, Vector<String> dirnames) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element el = (Element)node;

      // Process any child nodes, looking for directory elements.
      for (Node kid = el.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
        if (kid.getNodeType() == Node.ELEMENT_NODE) {
          extractDirectoryNames(kid, dirnames);
        }
      }

      // When this is a directory node, record the name of the directory.
      if (el.getNodeName().equals("directory")) {
        String dirname = ((Element)node).getAttribute("name");
        int i = dirname.indexOf(":/");
        if (i > 0)
          dirname = dirname.substring(i + ":/".length());
        dirnames.add(dirname);
      }
    }
  }


  /**
   * This method implements the ConvertCertificate operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * objname= ... name of CryptoCertificate object ...
   * remote= ... URL to write the converted certificate ...
   *
   * The params may contain:
   *
   * format=openssh-pubkey
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doConvertCertificate (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ConvertCertificate>");
    body.append("<ObjectName>" + params.get("objname") + "</ObjectName>");
    body.append("<OutputFilename>" + params.get("remote") + "</OutputFilename>");
    if (params.get("format") != null) {
      body.append("<Format>" + params.get("format") + "</Format>");
    } else {
      body.append("<Format>openssh-pubkey</Format>");
    }
    body.append("</ConvertCertificate>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the ConvertKey operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * objname= ... name of CryptoKey object ...
   * remote= ... URL to write the converted certificate ...
   *
   * The params may contain:
   *
   * format=openssh-pubkey
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doConvertKey (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ConvertKey>");
    body.append("<ObjectName>" + params.get("objname") + "</ObjectName>");
    body.append("<OutputFilename>" + params.get("remote") + "</OutputFilename>");
    if (params.get("format") != null) {
      body.append("<Format>" + params.get("format") + "</Format>");
    } else {
      body.append("<Format>openssh-pubkey</Format>");
    }
    body.append("</ConvertKey>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the do-cpa-import operation.
   *
   * UNTESTED due to a lack of hardware (XB6x)
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * local= ... local filename ...
   * gateway-name= ... name of gateway ...
   * internal-party= ... name of internal party ...
   *
   * The params may contain:
   *
   * overwrite-files=true or false (default true)
   * overwrite-objects=true or false (default true)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCpaImport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local", "gateway-name", "internal-party"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<soma:do-cpa-import>");
    body.append("<input-file>" + params.get("local") + "</input-file>");
    body.append("<gateway-name>" + params.get("gateway-name") + "</gateway-name>");
    body.append("<internal-party>" + params.get("internal-party") + "</internal-party>");
    if (params.get("overwrite-files") != null)
      body.append(" overwrite-files=\"" + params.get("overwrite-files") + "\"");
    else
      body.append(" overwrite-files=\"true\"");
    if (params.get("overwrite-files") != null)
      body.append(" overwrite-objects=\"" + params.get("overwrite-objects") + "\"");
    else
      body.append(" overwrite-objects=\"true\"");
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CreateDirectory operation, which creates
   * a single directory.  Sorry, it won't create multiple levels in one call.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * remote= ... the remote directory to create ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCreateDirectory (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<CreateDir>" +
    "<Dir>" + params.get("remote") + "</Dir>" +
    "</CreateDir>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CreateTAMFiles operation, which creates
   * all the stuff needed to integrate with Tivoli Access Manager.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * outputconfigfile= ...
   * administrator-uid= ...
   * administrator-pwd= ...
   * tamdomain= ...
   * application= ...
   * tamhost= ...
   * tamport ...
   * sslkeyfilelifetime= number of seconds (16 bits)
   * ssltimeout= number of seconds (32 bits)
   * localmode=
   *
   * The params may optionally contain:
   *
   * ldapserver= ...
   * ldapport= ...
   * ldapauthtimeout= number of seconds (32 bits)
   * ldapsearchtimeout= number of seconds (32 bits)
   * createcopy= on or off
   * ldapbindpassword= ...
   * ldapcache= on or off
   * ldapusercachesize= size (16 bits)
   * ldappolicycachesize= size (16 bits)
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCreateTAMFiles (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "outputconfigfile", "administrator-uid", "administrator-pwd",
                       "tamdomain", "application", "tamhost", "tamport", "sslkeyfilelifetime", "ssltimeout",
                       "localmode", "useadregistry"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<CreateTAMFiles>");
    if (params.get("createcopy") != null)
      body.append("<CreateCopy>" + params.get("createcopy") + "</CreateCopy>");
    body.append("<OutputConfigFile>" + params.get("outputconfigfile") + "</OutputConfigFile>");
    body.append("<Administrator>" + params.get("administrator-uid") + "</Administrator>");
    body.append("<Password>" + params.get("administrator-pwd") + "</Password>");
    body.append("<TAMDomain>" + params.get("tamdomain") + "</TAMDomain>");
    body.append("<Application>" + params.get("application") + "</Application>");
    body.append("<Host>" + params.get("tamhost") + "</Host>");
    body.append("<Port>" + params.get("tamport") + "</Port>");
    body.append("<SSLKeyFileLifetime>" + params.get("sslkeyfilelifetime") + "</SSLKeyFileLifetime>");
    body.append("<SSLTimeout>" + params.get("ssltimeout") + "</SSLTimeout>");
    body.append("<LocalMode>" + params.get("localmode") + "</LocalMode>");
    if (params.get("listenmode") != null)
      body.append("<ListenMode>" + params.get("listenmode") + "</ListenMode>");
    if (params.get("localhost") != null)
      body.append("<LocalHost>" + params.get("localhost") + "</LocalHost>");
    if (params.get("localport") != null)
      body.append("<LocalPort>" + params.get("localport") + "</LocalPort>");
    body.append("<UseADRegistry>" + params.get("useadregistry") + "</UseADRegistry>");
    if (params.get("adpprimarydomain") != null)
      body.append("<ADPrimaryDomain>" + params.get("adpprimarydomain") + "</ADPrimaryDomain>");
    if (params.get("adpprimaryhost") != null)
      body.append("<ADPrimaryHost>" + params.get("adpprimaryhost") + "</ADPrimaryHost>");
    if (params.get("adpprimaryreplicas") != null)
      body.append("<ADPrimaryReplicas>" + params.get("adpprimaryreplicas") + "</ADPrimaryReplicas>");
    if (params.get("ldapserver") != null)
      body.append("<LDAPServer>" + params.get("ldapserver") + "</LDAPServer>");
    if (params.get("ldapport") != null)
      body.append("<LDAPPort>" + params.get("ldapport") + "</LDAPPort>");
    if (params.get("ldapbindpassword") != null)
      body.append("<LDAPBindPassword>" + params.get("ldapbindpassword") + "</LDAPBindPassword>");
    if (params.get("ldapauthtimeout") != null)
      body.append("<LDAPAuthenticateTimeout>" + params.get("ldapauthtimeout") + "</LDAPAuthenticateTimeout>");
    if (params.get("ldapsearchtimeout") != null)
      body.append("<LDAPSearchTimeout>" + params.get("ldapsearchtimeout") + "</LDAPSearchTimeout>");
    if (params.get("adpclienttimeout") != null)
      body.append("<ADClientTimeout>" + params.get("adpclienttimeout") + "</ADClientTimeout>");
    if (params.get("enableregistrycache") != null)
      body.append("<EnableRegistryCache>" + params.get("enableregistrycache") + "</EnableRegistryCache>");
    if (params.get("ldapusercachesize") != null)
      body.append("<LDAPUserCacheSize>" + params.get("ldapusercachesize") + "</LDAPUserCacheSize>");
    if (params.get("ldappolicycachesize") != null)
      body.append("<LDAPPolicyCacheSize>" + params.get("ldappolicycachesize") + "</LDAPPolicyCacheSize>");
    if (params.get("adldapcachesize") != null)
      body.append("<ADLdapCacheSize>" + params.get("adldapcachesize") + "</ADLdapCacheSize>");
    if (params.get("adldapcachelife") != null)
      body.append("<ADLdapCacheLife>" + params.get("adldapcachelife") + "</ADLdapCacheLife>");
    if (params.get("addnforpd") != null)
      body.append("<ADDnforpd>" + params.get("addnforpd") + "</ADDnforpd>");
    if (params.get("adusemultidomain") != null)
      body.append("<ADUseMultiDomain>" + params.get("adusemultidomain") + "</ADUseMultiDomain>");
    if (params.get("addomaindomain") != null)
      body.append("<ADDomaindomain>" + params.get("addomaindomain") + "</ADDomaindomain>");
    if (params.get("addomainhost") != null)
      body.append("<ADDomainHost>" + params.get("addomainhost") + "</ADDomainHost>");
    if (params.get("addomainreplicas") != null)
      body.append("<ADDomainReplicas>" + params.get("addomainreplicas") + "</ADDomainReplicas>");
    if (params.get("aduseemailuid") != null)
      body.append("<ADUseEmailUid>" + params.get("aduseemailuid") + "</ADUseEmailUid>");
    if (params.get("adgchost") != null)
      body.append("<ADGcHost>" + params.get("adgchost") + "</ADGcHost>");
    body.append("</CreateTAMFiles>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CryptoExport operation, which exports the
   * specified crypto object to a file.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... object name ...
   * remote= ... name of DP file where the export s/b written ...
   *
   * The params may contain:
   *
   * objtype= key or cert
   * mechanism=hsmkwk
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCryptoExport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<CryptoExport>");
    if (params.get("objtype") != null)
      body.append("<ObjectType>" + params.get("objtype") + "</ObjectType>");
    body.append("<ObjectName>" + params.get("name") + "</ObjectName>");
    body.append("<OutputFilename>" + params.get("remote") + "</OutputFilename>");
    if (params.get("mechanism") != null)
      body.append("<Mechanism>" + params.get("mechanism") + "</Mechanism>");
    body.append("</CryptoExport>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the CryptoImport operation, which imports the
   * specified crypto object from a file (see CryptoExport).
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... object name ...
   * remote= ... name of DP file to inport from ...
   *
   * The params may contain:
   * objtype=key or cert
   * password= ... password for the input file ...
   * passwordalias= ... password alias for the input file ...
   * kwkexportable=on or off
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doCryptoImport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<CryptoImport>");
    if (params.get("objtype") != null)
      body.append("<ObjectType>" + params.get("objtype") + "</ObjectType>");
    body.append("<ObjectName>" + params.get("name") + "</ObjectName>");
    body.append("<InputFilename>" + params.get("remote") + "</InputFilename>");
    if (params.get("password") != null)
      body.append("<ImportPassword>" + params.get("password") + "</ImportPassword>");
    if (params.get("passwordalias") != null)
      body.append("<ImportPasswordAlias>" + params.get("passwordalias") + "</ImportPasswordAlias>");
    if (params.get("kwkexportable") != null)
      body.append("<KwkExportable>" + params.get("kwkexportable") + "</KwkExportable>");
    body.append("</CryptoImport>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeleteConfig operation, which deletes
   * the specified object.  You
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   *   classname= ... name of the objects's class (e.g. Domain or MQQM) ...
   *   objname= ... name of the specific object to delete ...
   * or
   *   config= <config> ... </config>
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeleteConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    StringBuffer body = new StringBuffer("<soma:del-config>");
    if (params.get("classname") != null && params.get("objname") != null) {
      body.append("<" + params.get("classname") + " name=\"" + params.get("objname") + "\"/>");
    } else if (params.get("config") != null) {
      body.append(SomaUtils.xmlNoHeader(params.get("config")));
    } else {
      throw new RuntimeException("Either 'config' must be specified or 'classname' plus 'objname' must be specified.");
    }
    body.append("</soma:del-config>");

    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
    } catch (Exception e) {
    }

    return result;
  }


  /**
   * This method implements the DeleteFile operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * remote= ... the name of some file on the device ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeleteFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<DeleteFile><File>" + params.get("remote") + "</File></DeleteFile>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeleteKnownHost operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * knownhost= ... hostname or IP addr ...
   * clientname= ... name of SSH Client object ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeleteKnownHost (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "knownhost"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DeleteKnownHost>");
    body.append("<ClientName>" + params.get("clientname") + "</ClientName>");
    body.append("<Host>" + params.get("knownhost") + "</Host>");
    body.append("</DeleteKnownHost>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeleteKnownHostTable operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * clientname= ... name of SSH Client object ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeleteKnownHostTable (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "clientname"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DeleteKnownHostTable>");
    body.append("<DomainName>" + params.get("domain") + "</DomainName>");
    body.append("<ClientName>" + params.get("clientname") + "</ClientName>");
    body.append("</DeleteKnownHostTable>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeletePasswordMap operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * aliasname= ... publicly visible "password" ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeletePasswordMap (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "aliasname"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DeletePasswordMap>");
    body.append("<AliasName>" + params.get("aliasname") + "</AliasName>");
    body.append("</DeletePasswordMap>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeleteTrustedHost operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * trustedhost= ... hostname or IP addr ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeleteTrustedHost (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "trustedhost"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DeleteTrustedHost>");
    body.append("<Host>" + params.get("trustedhost") + "</Host>");
    body.append("</DeleteTrustedHost>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the DeviceCertificate operation, which creates
   * and installs a private key/cert pair, signed with the DP internal cert,
   * for use by the web gui for SSL connections.  This avoids all those complaints
   * when connecting to the web gui about the certificate not matching the
   * host.
   *
   * The params must contain:
   *
   * host= ... a dotted decimal host address or hostname ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doDeviceCertificate (NamedParams params) throws Exception {
    params.insistOn (new String[] {"host"});

    // Make the request of the XML Management Interface.
    String body = "<DeviceCertificate>" +
    "<CN>" + params.get("host") + "</CN>" +
    "<SSCert>on</SSCert>" +
    "</DeviceCertificate>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Thie method implemented the DisconnectUser operation, which disconnects a webgui user.
   *
   * The params must contain:
   *
   * sessionid= ... some session id ...
   *
   * The params may contain:
   *
   * sessiontype=(serial-port | telnet | secure-shell | web-gui | saml-artifact | system)
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "rawresponse"
   * @throws Exception
   */
  public NamedParams doDisconnectUser (NamedParams params) throws Exception {
    params.insistOn (new String[] {"sessionid"});

    // Make the request of the XML Management Interface.

    StringBuffer body = new StringBuffer();
    body.append("<Disconnect>");
    body.append("<id>" + params.get("sessionid") + "</id>");
    if (params.get("sessiontype") != null)
      body.append("<connection>" + params.get("sessiontype") + "</connection>");
    body.append("</Disconnect>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the ExecCLI operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   * cli= ... set of CLI commands ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "rawresponse"
   * @throws Exception
   */
  public NamedParams doExecCLI (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "cli"});

    // Fake up a temporary filename to use remotely.  The filename is based on a
    // number between one and two billion, with a prefix and an extension of .cli.
    String filename = new Integer(new Random().nextInt(1000000000) + 1000000000).toString();
    String remoteFilename = "temporary:///ExecCli_" + filename + ".cli";

    // Upload the CLI to the remote file.
    NamedParams myParams = new NamedParams (params);
    myParams.set ("remote", remoteFilename);
    String base64CLI = Base64.bytesToString(Base64.toBase64 (params.get("cli").getBytes(), null));
    NamedParams result = doSetFileImpl (myParams, base64CLI);

    // Execute the CLI.
    // Make the request of the XML Management Interface.
    String body = "<ExecConfig><URL>" + remoteFilename + "</URL></ExecConfig>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    result = conn.sendAndReceive (params, request);

    // Finally, delete the temporary remote file, regardless of the outcome.
    doDeleteFile(myParams);

    // Ensure that we were successful, unless the client specifically ignores that.
    if (errorsAreSignificant(params))
      insistSomaResultIsOkay(result);

    return result;
  }


  /**
   * This method implements the ExecConfig operation, which executes the
   * specified CLI file.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * remote= ... some file on DP ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doExecConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<ExecConfig><URL>" + params.get("remote") + "</URL></ExecConfig>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the do-export operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * local=... filename ...
   *
   * The params may contain:
   *
   * all-files=true or false (defaults to false)
   * deployment-policy-name=name of existing DeploymentPolicy object
   * deployment-policy=raw XML for a deployment policy object
   * format=ZIP or XML (defaults to ZIP)
   * objects=raw XML for <object> elements to include in the request
   * persisted=true or false (defaults to false)
   * user-comment=...
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doExport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<soma:do-export");
    if (params.get("format") != null) {
      body.append(" format=\"" + params.get("format") + "\"");
    } else {
      body.append(" format=\"ZIP\"");
    }
    if (params.get("all-files") != null) {
      body.append(" all-files=\"" + params.get("all-files") + "\"");
    } else {
      body.append(" all-files=\"false\"");
    }
    if (params.get("persisted") != null) {
      body.append(" persisted=\"" + params.get("persisted") + "\"");
    } else {
      body.append(" persisted=\"false\"");
    }
    if (params.get("deployment-policy-name") != null && params.get("deployment-policy-name").length() > 0) {
      body.append(" deployment-policy=\"" + params.get("deployment-policy-name") + "\"");
    }
    body.append(">");
    if (params.get("user-comment") != null && params.get("user-comment").length() > 0)
      body.append("<user-comment>" + params.get("user-comment") + "</user-comment>");
    if (params.get("objects") != null && params.get("objects").length() > 0) {
      body.append(params.get("objects"));
    } else {
      body.append("<soma:object class=\"all-classes\" name=\"all-objects\" ref-objects=\"true\" ref-files=\"true\"/>");
    }
    if (params.get("deployment-policy") != null && params.get("deployment-policy").length() > 0) {
      body.append(params.get("deployment-policy"));
    }
    body.append("</soma:do-export>");
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      String contentBase64 = SomaUtils.stringXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
      if (contentBase64.length() > 0) {
        Base64.base64ToBinaryFile (contentBase64, params.get("local"));
      } else {
        throw new RuntimeException("Failed to export from domain " + params.get("domain") + " on " + params.get("hostname"));
      }
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
        Node root = SomaUtils.getDOM (result.get("rawresponse"));
        String contentBase64 = SomaUtils.stringXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
        if (contentBase64.length() > 0) {
          Base64.base64ToBinaryFile (contentBase64, params.get("local"));
        }
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the FetchFile operation, which fetches the
   * specified URL to a file on the device.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * url= ... URL to download from ...
   * remote= ... name of DP file where the content s/b written ...
   *
   * The params may optionally contain:
   *
   * overwrite=on or off
   * xmlmanager= ... name of XMLManager object ... (not supported in 3.8.2, optional in 4.0 and later)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doFetchFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "url", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<FetchFile>");
    body.append("<URL>" + params.get("url") + "</URL>");
    body.append("<File>" + params.get("remote") + "</File>");
    if (params.get("overwrite") != null)
      body.append("<Overwrite>" + params.get("overwrite") + "</Overwrite>");
    if (params.get("xmlmanager") != null)
      body.append("<XMLManager>" + params.get("xmlmanager") + "</XMLManager>");
    body.append("</FetchFile>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the FileCapture operation, which controls capturing
   * network input in files on the device.
   *
   * The params must contain:
   *
   * tracingmode=(off | always | errors)
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doFileCapture (NamedParams params) throws Exception {
    params.insistOn (new String[] {"tracingmode"});

    // Make the request of the XML Management Interface.
    String body ="<FileCapture><Mode>" + params.get("tracingmode") + "</Mode></FileCapture>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the FirmwareRollback operation, which rolls the
   * firmware back to the previous version.
   *
   * The params must contain:
   *
   * nothing
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doFirmwareRollback (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<BootSwitch/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the AAA cache for the AAA policy object.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * policy= ... some AAA policy object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushAAACache (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "policy"});

    // Make the request of the XML Management Interface.
    String body = "<FlushAAACache><PolicyName>" + params.get("policy") + "</PolicyName></FlushAAACache>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the Arp cache.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushArpCache (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<FlushArpCache/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the DNS cache.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushDNSCache (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<FlushDNSCache/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the document cache for the (optionally specified) XML manager.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * xmlmanager= ... name of an XMLManager object ... (defaults to 'default')
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushDocumentCache (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Determine which XML manager to flush - "default" if caller didn't specify.
    String xmlManager = params.get("xmlmanager");
    if ((xmlManager == null) || (xmlManager.trim().length() == 0)) {
      xmlManager = "default";
    }

    // Make the request of the XML Management Interface.
    String body = "<FlushDocumentCache><XMLManager>" + xmlManager + "</XMLManager></FlushDocumentCache>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the ND cache.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushNDCache (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<FlushNDCache/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the NSS cache.
   *
   * The params must contain:
   *
   * client= ... name of an NSS client ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushNSSCache (NamedParams params) throws Exception {
    params.insistOn (new String[] {"client"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<FlushNSSCache>");
    body.append("<ZosNSSClient>" +  params.get("client")+ "</ZosNSSClient>");
    body.append("</FlushNSSCache>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the PDP cache for the PDP object.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... some PDP object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushPDPCache (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<FlushPDPCache><XACMLPDP>" + params.get("name") + "</XACMLPDP></FlushPDPCache>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the RBM cache.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushRBMCache (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<FlushRBMCache/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the stylesheet cache for the (optionally specified) XML manager.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * xmlmanager= ... name of an XMLManager object ... (defaults to 'default')
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doFlushStylesheetCache (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Determine which XML manager to flush - "default" if caller didn't specify.
    String xmlManager = params.get("xmlmanager");
    if ((xmlManager == null) || (xmlManager.trim().length() == 0)) {
      xmlManager = "default";
    }

    // Make the request of the XML Management Interface.
    String body = "<FlushStylesheetCache><XMLManager>" + xmlManager + "</XMLManager></FlushStylesheetCache>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the GenerateErrorReport operation, which generates
   * an error report based on the previously saved internal state of the device.
   *
   * This generates temporary:///error-report.txt, which you may download (GetFile)
   * or email (SendFile).
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "rawresponse".
   * @throws Exception
   */
  public NamedParams doGenerateErrorReport (NamedParams params) throws Exception {

    // Make the request of the XML Management Interface.
    String body = "<ErrorReport/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the GetConfig operation, which returns the
   * configuration of an object, in XML format.  This XML contains all the
   * default parameters and values, which makes it somewhat verbose.
   *
   * A classname plus object name returns (at most) the information for one object.
   * A classname without an object name returns all the objects of that class.
   * No classname and no object name returns all objects in the domain.
   * It makes no sense to have an object name and no classname.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The param may contain:
   *
   * classname= ... some object class name ... (e.g. Domain or MQQM)
   *                (fully described in xml-mgmt.xsd in name="ConfigEnum")
   * objname= ... name of an existing object of that class
   * recursive=true or false (default false)
   * persisted=true or false (default false)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "config" (the desired XML) and "rawresponse".
   * @throws Exception when the classname is illegal
   */
  public NamedParams doGetConfig (NamedParams params) throws Exception {
    params.insistOn ("domain");

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<soma:get-config");
    if (params.get("classname") != null)
      body.append(" class=\"" + params.get("classname") + "\"");
    if (params.get("objname") != null)
      body.append(" name=\"" + params.get("objname") + "\"");
    if (params.get("recursive") != null) {
      body.append(" recursive=\"" + params.get("recursive") + "\"");
    } else {
      body.append(" recursive=\"false\"");
    }
    if (params.get("persisted") != null) {
      body.append(" persisted=\"" + params.get("persisted") + "\"");
    } else {
      body.append(" persisted=\"false\"");
    }
    body.append("/>");
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = conn.sendAndReceive (params, request);

    // Ensure that we were successful, unless the client specifically ignores that.
    Node root = SomaUtils.getDOM (result.get("rawresponse"));
    Node nodeConfig = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:config");
    boolean throwException = true;
    if (nodeConfig != null) {
      // The request succeeded if the <soma:config> has children.
      NodeList children = SomaUtils.nodelistXpathFor (nodeConfig, "*");
      if (children != null) {
        StringBuffer buf = new StringBuffer();
        buf.append("<objs>");
        for (int i = 0; i < children.getLength(); i += 1) {
          buf.append(SomaUtils.xmlNoHeader(SomaUtils.serializeXML(children.item(i))));
        }
        buf.append("</objs>");
        result.set("config", buf.toString());
        throwException = false;
      }
    }

    if (throwException && errorsAreSignificant(params))
      throw new UnsupportedOperationException (result.get("rawresponse"));

    return result;
  }


  /**
   * This method implements the GetConformanceReport operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * class=WSGateway
   * name= ... name of WSGateway object ...
   * profile= dp-wsi-bp.xsl, dp-wsi-bsp-1.0.xsl, or dp-cfg-bp.xsl
   *
   * The param may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "report" (the desired XML) and "rawresponse".
   * @throws Exception when the classname is illegal or missing parameters
   */
  public NamedParams doGetConformanceReport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "class", "name", "profile"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<soma:get-conformance-report");

    body.append(" class=\"" + params.get("class") + "\"");
    body.append(" name=\"" + params.get("name") + "\"");
    body.append(" profile=\"" + params.get("profile") + "\"");
    body.append("/>");

    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:conformance-report");
      if (nodeContent != null) {
        result.set("report", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeContent)));
      } else {
        throw new RuntimeException("Failed to compute the requested conformance report.");
      }
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
        Node root = SomaUtils.getDOM (result.get("rawresponse"));
        Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:conformance-report");
        if (nodeContent != null) {
          result.set("report", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeContent)));
        }
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the GetDiff operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The param may contain:
   *
   * (class name [recursive] [from-persisted] [to-persisted])
   * OR
   *   this (from-export | from-backup | (from-class from-name [from-recursive] [from-persisted]))
   *   and  (to-export | to-backup | (to-class to-name [to-recursive] [to-persisted]))
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "diff" (the desired XML) and "rawresponse".
   * @throws Exception when the classname is illegal or missing parameters
   */
  public NamedParams doGetDiff (NamedParams params) throws Exception {
    params.insistOn ("domain");

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<soma:get-diff>");

    if (params.get("class") == null || params.get("name") == null) {
      body.append("<soma:from>");
      if (params.get("from-export") != null)
        body.append("<soma:export>" + Base64.base64FromBinaryFile(params.get("from-export")) + "</soma:export>");
      else if (params.get("from-backup") != null)
        body.append("<soma:backup>" + Base64.base64FromBinaryFile(params.get("from-backup")) + "</soma:backup>");
      else if (params.get("from-class") != null) {
        body.append("<soma:object");
        body.append(" class=\"" + params.get("from-class") + "\"");
        if (params.get("from-name") != null) {
          body.append(" name=\"" + params.get("from-name") + "\"");
        } else
          throw new RuntimeException("doGetDiff() - the from-class parameter requires the from-name parameter too.");
        if (params.get("from-recursive") != null) {
          body.append(" recursive=\"" + params.get("from-recursive") + "\"");
        }
        if (params.get("from-persisted") != null) {
          body.append(" persisted=\"" + params.get("from-persisted") + "\"");
        }
        body.append("/>");
      } else
        throw new RuntimeException ("doGetDiff() requires one of from-export, from-backup, or from-class.");
      body.append("</soma:from>");

      body.append("<soma:to>");
      if (params.get("to-export") != null)
        body.append("<soma:export>" + Base64.base64FromBinaryFile(params.get("to-export")) + "</soma:export>");
      else if (params.get("to-backup") != null)
        body.append("<soma:backup>" + Base64.base64FromBinaryFile(params.get("to-backup")) + "</soma:backup>");
      else if (params.get("to-class") != null) {
        body.append("<soma:object");
        body.append(" class=\"" + params.get("to-class") + "\"");
        if (params.get("to-name") != null) {
          body.append(" name=\"" + params.get("to-name") + "\"");
        } else
          throw new RuntimeException("doGetDiff() - the to-class parameter requires the to-name parameter too.");
        if (params.get("to-recursive") != null) {
          body.append(" recursive=\"" + params.get("to-recursive") + "\"");
        }
        if (params.get("to-persisted") != null) {
          body.append(" persisted=\"" + params.get("to-persisted") + "\"");
        }
        body.append("/>");
      } else
        throw new RuntimeException ("doGetDiff() requires one of to-export, to-backup, or to-class.");
      body.append("</soma:to>");
    } else {
      body.append("<soma:object");
      body.append(" class=\"" + params.get("class") + "\"");
      if (params.get("name") != null) {
        body.append(" name=\"" + params.get("name") + "\"");
      } else
        throw new RuntimeException("doGetDiff() - the class parameter requires the name parameter too.");
      if (params.get("recursive") != null) {
        body.append(" recursive=\"" + params.get("recursive") + "\"");
      }
      if (params.get("from-persisted") != null) {
        body.append(" from-persisted=\"" + params.get("from-persisted") + "\"");
      }
      if (params.get("to-persisted") != null) {
        body.append(" to-persisted=\"" + params.get("to-persisted") + "\"");
      }
      body.append("/>");
    }

    body.append("</soma:get-diff>");
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:diff");
      if (nodeContent != null) {
        result.set("diff", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeContent)));
      } else {
        throw new RuntimeException("Failed to compute the requested diff.");
      }
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
        Node root = SomaUtils.getDOM (result.get("rawresponse"));
        Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:diff");
        if (nodeContent != null) {
          result.set("diff", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeContent)));
        }
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the get-file operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   * local= ... local file name ... (e.g.c:\\somedir\somefile.xsl)
   * remote=... remove file name ... (e.g. local:///somefile.xsl)
   *
   * The param may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "config" (the desired XML) and "rawresponse".
   * @throws Exception
   */
  public NamedParams doGetFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<soma:get-file name=\"" + params.get("remote") + "\"/>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
    } catch (Exception e) {
      if (errorsAreSignificant(params))
        throw e;
    }

    // Extract, decode, and save the file content.
    Node root = SomaUtils.getDOM (result.get("rawresponse"));
    Node nodeContent = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:file");
    boolean throwException = true;
    if (nodeContent != null) {
      String content = SomaUtils.stringXpathFor(nodeContent, ".");
      if (content != null) {
        // Store the file content, and throw an exception unless the caller doesn't care about auccess or failure.
        Base64.base64ToBinaryFile(content, params.get("local"));
        result.remove("rawresponse");   // It may take up a lot of memory, and won't be needed.
        throwException = false;
      }
    }

    if (throwException && errorsAreSignificant(params)) {
      throw new RuntimeException ("failed to get the file " + params.get("remote") + ", rawresponse=" + result.get("rawresponse"));
    }

    return result;
  }


  /**
   * This method implements the GetFilestore operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   * filestore=(cert | config | export | image | local | logstore | logtemp | pubcert | sharedcert | store | tasktemplates | temporary):
   *
   * The param may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "location" (the desired XML) and "rawresponse".
   * @throws Exception
   */
  public NamedParams doGetFilestore (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "filestore"});

    String filestore = params.get("filestore");
    if (!filestore.endsWith(":")) {
      filestore = filestore + ":";
    }

    // Make the request of the XML Management Interface.
    String body = "<soma:get-filestore location=\"" + filestore + "\"/>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);

      // Extract and return the filestore listing.
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      Node nodeLocation = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:filestore/location");
      if (nodeLocation != null) {
        result.set("location", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeLocation)));
      }
    } catch (Exception e) {
      if (errorsAreSignificant(params))
        throw e;
    }

    return result;
  }


  /**
   * This method gets the log.  This appears to simply be a download of
   * logtemp:///default-log-xml.
   *
   * I experimented with name="default-log-xml.1" and got nothing.
   * name="default-log" returned the same as having no name attribute.
   * I didn't experiment further.  Perhaps the name parameter is useful
   * when you have log targets creating other files.
   *
   * I'd just stick to GetFile instead.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   *
   * @param params
   * @return NamedParams containing "log" (the desired XML) and "rawresponse".
   * @throws Exception
   */
  public NamedParams doGetLog (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    String body = "<soma:get-log/>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      Node nodeLog = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:log");
      result.set("log", SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodeLog)));
    } catch (Exception e) {
      if (errorsAreSignificant(params))
        throw e;
    }

    return result;
  }


  /**
   * This method gathers status information for various classes.
   *
   * You can find a complete list of available classnames in the XML management interface
   * schema for name="StatusEnum".
   *
   * For example, capture.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   *
   * The params may contain:
   *
   * capture.xxx= xxx is a class name (e.g. WSGateway or MultiProtocolGateway)
   *
   * Returns a NamedParams containing a key/value pair for each object found.
   *
   */
  public NamedParams doGetObjectStatus (NamedParams params) throws Exception {
    params.insistOn ("domain");

    // Gather the status of every object in the specified domain.
    NamedParams status = doGetStatus(params, "ObjectStatus");

    // Capture the <soma:status> node, and all its children.
    Node root = SomaUtils.getDOM (status.get("rawresponse"));
    Node nodeStatus = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:status");

    // Iterate over the supplied parameters looking for specification of which
    // classes (and objects) are interesting (and the client wants broken out
    // and returned).
    NamedParams result = new NamedParams();
    String[] in = params.toStringArray();
    boolean bClassSpecificationFound = false;
    for (int i = 0; i < in.length; i += 1) {
      // Pay attention to parameters beginning "capture."
      if (in[i].startsWith("capture.")) {
        // We expect something like capture.xxxx where xxxx
        // xxxx is some classname.  Peel off the classname.
        String classname = in[i].substring("capture.".length());
        if (classname.length() > 0) {
          bClassSpecificationFound = true;

          // Pick out all the <ObjectStatus> elements that apply to this
          // classname and that match the supplied regular expression.
          NodeList list = SomaUtils.nodelistXpathFor(nodeStatus, "ObjectStatus[Class='" + classname + "']");
          for (int k = 0; k < list.getLength(); k += 1) {
            String objStat = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(SomaUtils.nodeXpathFor(list.item(k), ".")));
            String objname = SomaUtils.stringXpathFor(list.item(k), "Name");
            if (objname.matches(in[i + 1])) {
              result.set(classname + "." + objname, objStat);
            }
          }
        }
      }
    }

    if (!bClassSpecificationFound) {
      // No capture.xxxx specifications found, so the caller wants everything.
      NodeList list = SomaUtils.nodelistXpathFor(nodeStatus, "ObjectStatus");
      for (int k = 0; k < list.getLength(); k += 1) {
        String objStat = SomaUtils.xmlNoHeader(SomaUtils.serializeXML(SomaUtils.nodeXpathFor(list.item(k), ".")));
        String objClass = SomaUtils.stringXpathFor(list.item(k), "Class");
        result.set(objClass + "." + SomaUtils.stringXpathFor(list.item(k), "Name"), objStat);
      }
    }

    return result;
  }


  /**
   * This method gathers status information for the specified class.
   *
   * You can find a complete list of available classnames in the XML management interface
   * schema for name="StatusEnum".
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   *
   * Returns a NamedParams containing a key/value pair for "rawresponse".
   *
   */
  private NamedParams doGetStatus (NamedParams params, String objectClass) throws Exception {
    params.insistOn ("domain");

    // Make the request of the XML Management Interface.
    String body = "<soma:get-status class=\"" + objectClass + "\"/>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
    } catch (Exception e) {
      if (errorsAreSignificant(params)) {
        throw e;
      }
    }

    return result;
  }


  /**
   * Fetch the status of one or more things and return them collectively
   * in XML that looks like this:
   *
   * <status>
   *     <.../> (XML returned for each status you requested)
   *     ...
   * </status>
   *
   * The full list of things for which status can be requested is in the
   * SOMA schema under name="StatusEnum".  It is currently 111 items, but
   * that is likely to grow as the system is enhanced over time.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * statuses= ... list of whitespace separated status names ...
   * (e.g. "SystemUsage TCPTable FirmwareStatus")
   *
   * An invalid name causes DataPower to return an HTTP 500, which throws
   * an exception.
   *
   * @param params
   * @return The desired results.
   * @throws Exception
   */
  public NamedParams doGetStatuses (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "statuses"});

    // Parse the list of statuses, which are merely whitespace separated.
    String[] names = params.get("statuses").split("\\s+");

    // Spin through all the supplied statuses, gathering each.  Stop in case of an error, unless errors are unimportant.
    StringBuffer statuses = new StringBuffer();
    statuses.append("<status>");
    for (int i = 0; i < names.length; i += 1) {
      // Fetch the status for this name.
      NamedParams oneresult = doGetStatus(params, names[i]);

      // When it worked correctly, collect the information.  Otherwise do nothing.
      // Why?  Well, for example, suppose statistics collection isn't on in the
      // default domain and you request ConnectionsAccepted.  You get back
      // <some:status/>.  If you specify a name that isn't legal then you
      // will get a failure back from DataPower that causes an exception. So there
      // are no conditions left to test for.
      Node root = SomaUtils.getDOM (oneresult.get("rawresponse"));
      NodeList nodelistStatus = SomaUtils.nodelistXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:status/" + names[i]);
      if (nodelistStatus != null) {
        for (int k = 0; k < nodelistStatus.getLength(); k += 1) {
          statuses.append(SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nodelistStatus.item(k))));
        }
      }
    }

    statuses.append("</status>");

    NamedParams result = new NamedParams(params);
    result.set("status", statuses.toString());

    return result;
  }


  /**
   * This method implements the ImportConfig operation, which imports the DP export
   * file into a domain.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * local= ... DP export file (.zip or .xcfg) e.g. c:\xyzzy.zip ...
   *
   * The params may contain:
   *
   * deployment-policy= name of a ConfigDeploymentPolicy object already present in the domain prior to this operation
   * deployment-policy-file= ... local file containing a ConfigDeploymentPolicy object in XML ...
   * overwrite-files=true/false defaults to true
   * overwrite-objects=true/false default to true
   * rewrite-local-ip=true/false defaults to true
   * source-type= 'zip' or 'xml' - by default the extension of the local file is examined to make a guess.
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doImportConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local"});

    // Construct the XML request.
    String body = "<soma:do-import";
    if (params.get("source-type") != null)
      body += " source-type=\"" + params.get("source-type") + "\"";
    else if (params.get("local").toLowerCase().endsWith(".zip"))
      body += " source-type=\"ZIP\"";
    else
      body += " source-type=\"XML\"";
    body += " dry-run=\"false\"";
    if (params.get("overwrite-files") != null)
      body += " overwrite-files=\"" + params.get("overwrite-files") + "\"";
    else
      body += " overwrite-files=\"true\"";
    if (params.get("overwrite-objects") != null)
      body += " overwrite-objects=\"" + params.get("overwrite-objects") + "\"";
    else
      body += " overwrite-objects=\"true\"";
    if (params.get("rewrite-local-ip") != null)
      body += " rewrite-local-ip=\"" + params.get("rewrite-local-ip") + "\"";
    else
      body += " rewrite-local-ip=\"true\"";
    if (params.get("deployment-policy") != null)
      body += " deployment-policy=\"" + params.get("deployment-policy") + "\"";
    else
      body += " deployment-policy=\"\"";
    body += ">";
    body += "<soma:input-file>" + Base64.base64FromBinaryFile(params.get("local")) + "</soma:input-file>";
    if (params.get("deployment-policy-file") != null) {
      try {
        String filename = params.get("deployment-policy-file");

        // Determine how large the buffer will need to be, and allocate it.
        File file = new File (filename);
        if (file.length() > 0) {
          byte[] buffer = new byte[(int)file.length()];

          // Read the contents of the file into memory.
          BufferedInputStream input = new BufferedInputStream (new FileInputStream (filename));
          try {
              input.read (buffer);
          }
          finally {
              input.close();
          }
          // The input may be empty, may be malformed XML (e.g. a list of ModifiedConfig elements), or
          // a dcm:wrapper around the input.
          String rawcontent = Base64.bytesToString (buffer);
          String content = null;
          try {
            Node doc = SomaUtils.getDOM(rawcontent);
            Node wrapper = SomaUtils.nodeXpathFor(doc, "/*[local-name()='wrapper' and namespace-uri()='urn:datapower:configuration:manager']");
            if (wrapper != null) {
              // Capture all the child elements of the dcm:wrapper
              NodeList nl = SomaUtils.nodelistXpathFor(wrapper, "*");
              StringBuffer sb = new StringBuffer();
              for (int i = 0; i < nl.getLength(); i++) {
                sb.append(SomaUtils.xmlNoHeader(SomaUtils.serializeXML(nl.item(i))));
              }
              content = sb.toString();
            } else {
              // No dcm:wrapper, so just take it verbatim.
              content = SomaUtils.xmlNoHeader(rawcontent);
            }
          } catch (Exception e) {
            // Well, not parseable, so just use it as is, ensuring it doesn't have an <?xml ... ?> header.
            content = SomaUtils.xmlNoHeader(rawcontent);
          }

          // Put the text of the file into the XML request.
          if (content != null) {
            body += "<soma:deployment-policy>" + content + "</soma:deployment-policy>";
          }
        }
      } catch (Exception e) {
        throw new RuntimeException (e);
      }
    }
    body += "</soma:do-import>";

    // Make the request of the XML Management Interface.
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
    } catch (Exception e) {
      if (errorsAreSignificant(params)) {
        throw e;
      }
    }

    return result;
  }


  /**
   * This method implements the ImportExecute operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... some import package object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * Returns a NamedParams containing a key/value pair for "rawresponse".
   *
   */
  public NamedParams doImportExecute (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<ImportExecute><ImportPackageName>" + params.get("name") + "</ImportPackageName></ImportExecute>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method determines whether the specified object is present and "up".
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * classname= ... some DP object class name (e.g. Matching or Domain) ...
   * objname= ... the name of an object of that class ...
   *
   * @param params
   * @return
   * @throws Exception
   */
  public NamedParams doIsUp (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "classname", "objname"});

    NamedParams result = doGetStatus (params, "ObjectStatus");

    Node root = SomaUtils.getDOM(result.get("rawresponse"));
    if (root != null) {
      Node objectStatus = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:status/ObjectStatus[Class='" + params.get("classname") + "' and Name='" + params.get("objname") + "']");
      if (objectStatus != null) {
        String state = SomaUtils.stringXpathFor(objectStatus, "OpState");
        if (state.trim().equals("up")) {
          // Life is good.
          System.out.println(params.get("classname") + "/" + params.get("objname") + " is up in " + params.get("domain") + ".");
        } else {
          throw new RuntimeException (params.get("classname") + "/" + params.get("objname") + " is either down or disabled.");
        }
      } else {
        throw new RuntimeException ("Failed to find the status of " + params.get("classname") + "/" + params.get("objname") + ", it does not exist.");
      }
    } else {
      throw new RuntimeException ("Failed to parse status information for " + params.get("classname") + "/" + params.get("objname") + ", rawresponse=" + result.get("rawresponse"));
    }

    return result;
  }


  /**
   * This method implements the Keygen operation, which creates
   * a private key, a self signed cert, and a cert signing request,
   * all in files.  It can optionally create key/cert objects to wrap
   * the files.  The files are stored in the cert: filestore PLUS the
   * temporary:/// filestore, so you can download them.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * cn= common name
   *
   * The params may optionally contain:
   *
   * ldaporder=on or off
   * c= ... country name ...
   * st= ... state name ...
   * l= ... location name ...
   * o= ... organization name ...
   * ou= ... ourganization unit name ...
   * ou1= ... ourganization unit name 1 ...
   * ou2= ... ourganization unit name 2 ...
   * ou3= ... ourganization unit name 3 ...
   * keylength= (1024 | 2048 | 4096)
   * remote= ... name of a file on the device (for what?) ...
   * days= number of days the key/cert pair will be valid
   * password= ... password for the resulting key file ...
   * passwordalias= ... password alias for the resulting key file ...
   * kwkexportable=on or off
   * exportkey=true to copy the files to the temporary directoy, false otherwise
   * gensscert=true to generate a self signed cert along with the key and the cert signing request
   * exportsscert=true to copy the self signed cert to the temporary directory
   * genobject=true to create key/cert objects wrapping the files
   * objectname= when genobject is true, this is the name of the key and cert objects
   * hsm=on or off
   * usingkey= hmmm.  what is this again?  i used to know
   *
   * ignore-errors = on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doKeygen (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "cn"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<Keygen>");
    if (params.get("ldaporder") != null)
      body.append("<LDAPOrder>" + params.get("ldaporder") + "</LDAPOrder>");
    if (params.get("c") != null)
      body.append("<C>" + params.get("c") + "</C>");
    if (params.get("st") != null)
      body.append("<ST>" + params.get("st") + "</ST>");
    if (params.get("l") != null)
      body.append("<L>" + params.get("l") + "</L>");
    if (params.get("o") != null)
      body.append("<O>" + params.get("o") + "</O>");
    if (params.get("ou") != null)
      body.append("<OU>" + params.get("ou") + "</OU>");
    if (params.get("ou1") != null)
      body.append("<OU1>" + params.get("ou1") + "</OU1>");
    if (params.get("ou2") != null)
      body.append("<OU2>" + params.get("ou2") + "</OU2>");
    if (params.get("ou3") != null)
      body.append("<OU3>" + params.get("ou3") + "</OU3>");
    body.append("<CN>" + params.get("cn") + "</CN>");
    if (params.get("keylength") != null)
      body.append("<KeyLength>" + params.get("keylength") + "</KeyLength>");
    if (params.get("remote") != null)
      body.append("<FileName>" + params.get("remote") + "</FileName>");
    if (params.get("days") != null)
      body.append("<Days>" + params.get("days") + "</Days>");
    if (params.get("password") != null)
      body.append("<Password>" + params.get("password") + "</Password>");
    if (params.get("passwordalias") != null)
      body.append("<PasswordAlias>" + params.get("passwordalias") + "</PasswordAlias>");
    if (params.get("kwkexportable") != null)
      body.append("<KwkExportable>" + params.get("kwkexportable") + "</KwkExportable>");
    if (params.get("exportkey") != null)
      body.append("<ExportKey>" + params.get("exportkey") + "</ExportKey>");
    if (params.get("gensscert") != null)
      body.append("<GenSSCert>" + params.get("gensscert") + "</GenSSCert>");
    if (params.get("exportsscert") != null)
      body.append("<ExportSSCert>" + params.get("exportsscert") + "</ExportSSCert>");
    if (params.get("genobject") != null)
      body.append("<GenObject>" + params.get("genobject") + "</GenObject>");
    if (params.get("objectname") != null)
      body.append("<ObjectName>" + params.get("objectname") + "</ObjectName>");
    if (params.get("hsm") != null)
      body.append("<HSM>" + params.get("hsm") + "</HSM>");
    if (params.get("usingkey") != null)
      body.append("<UsingKey>" + params.get("usingkey") + "</UsingKey>");
    body.append("</Keygen>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Turn the blue LED on the front of device on or off.  This is helpful when
   * trying to locate one device in a rack (or several racks).
   *
   * The params must contain:
   *
   * state=on or off
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doLocateDevice (NamedParams params) throws Exception {
    params.insistOn (new String[] {"state"});

    // Make the request of the XML Management Interface.
    String body = "<LocateDevice><LocateLED>" + params.get("state") + "</LocateLED></LocateDevice>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method fetches the status of memory on the device.  It illustrates how you may
   * choose to write a class of methods that gather status and break it out of XML into
   * individual key/value pairs in the results.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing name/value pairs for Usage, TotalMemory, UsedMemory,
   * FreeMemory, and ReqMemory.
   * @throws Exception
   */
  public NamedParams doMemoryStatus (NamedParams params) throws Exception {
    // Do the operation.
    params.set("domain", "default");
    NamedParams result = doGetStatus(params, "MemoryStatus");

    // Figure out what the response means.
    Node root = SomaUtils.getDOM (result.get("rawresponse"));
    Node nodeStatus = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:status/MemoryStatus");
    boolean throwException = true;
    if (nodeStatus != null) {
      // Test whether the request succeeded by using the heuristic of checking for Usage.
      String usage = SomaUtils.stringXpathFor (nodeStatus, "Usage");
      if (usage.length() > 0) {
        // Populate the various promised key/value pairs.
        result.set("Usage", usage);
        result.set("TotalMemory", SomaUtils.stringXpathFor (nodeStatus, "TotalMemory"));
        result.set("UsedMemory", SomaUtils.stringXpathFor (nodeStatus, "UsedMemory"));
        result.set("FreeMemory", SomaUtils.stringXpathFor (nodeStatus, "FreeMemory"));
        result.set("ReqMemory", SomaUtils.stringXpathFor (nodeStatus, "ReqMemory"));

        throwException = false;
      }
    }

    if (throwException && errorsAreSignificant(params))
      throw new UnsupportedOperationException (result.get("rawresponse"));

    return result;
  }


  /**
   * This method implements the ModifyConfig operation, which updates
   * the configuration of an object based on an XML description.  You
   * need to study the SOMA schema in order to make use of this method.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * config= ... XML as defined in the schema ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doModifyConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "config"});

    // Make the request of the XML Management Interface.
    String body = "<soma:modify-config>" + SomaUtils.xmlNoHeader(params.get("config")) + "</soma:modify-config>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the MoveFile operation, which moves the
   * specified file to another location.  Actually, either the source
   * or the destination can be off box.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * source= ... URL to move from ...
   * destination= ... URL to move to ...
   *
   * The params may optionally contain:
   *
   * overwrite=on or off
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doMoveFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "source", "destination"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<MoveFile>");
    body.append("<sURL>" + params.get("source") + "</sURL>");
    body.append("<dURL>" + params.get("destination") + "</dURL>");
    if (params.get("overwrite") != null)
      body.append("<Overwrite>" + params.get("overwrite") + "</Overwrite>");
    body.append("</MoveFile>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Flush the Password Map.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doNoPasswordMap (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<NoPasswordMap/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Create the Password Map.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doPasswordMap (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    String body = "<PasswordMap/>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the Ping operation, which pings the specified host.
   *
   * The params must contain:
   *
   * host= ... DNS-resolvable host name, or a dotted decimal address ...
   *
   * The params may contain:
   *
   * useipv=4 or 6  (default 4)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doPing (NamedParams params) throws Exception {
    params.insistOn (new String[] {"host"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ("<Ping>");
    body.append("<RemoteHost>" + params.get("host") + "</RemoteHost>");
    if (params.get("useipv") != null) {
      body.append("<useIPv>" + params.get("useipv") + "</useIPv>");
    }
    body.append("</Ping>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Quiesce operation for a device.
   *
   * The params must contain:
   *
   * timeout= ... some number of seconds ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doQuiesceDevice (NamedParams params) throws Exception {
    params.insistOn (new String[] {"timeout"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<QuiesceDP>");
    body.append("<timeout>" +  params.get("timeout")+ "</timeout>");
    body.append("</QuiesceDP>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Quiesce operation for a domain.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * timeout= ... seconds ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doQuiesceDomain (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "timeout"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DomainQuiesce>");
    body.append("<name>" +  params.get("domain")+ "</name>");
    body.append("<timeout>" +  params.get("timeout")+ "</timeout>");
    body.append("</DomainQuiesce>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Quiesce operation for a front side handler.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * timeout= ... seconds ...
   * type= ... Service class name (e.g. MultiProtocolGateway) ...
   * objname= ... name of service object ...
   * fehtype= ... FSH class name (e.g. HTTPSourceProtocolHandler) ...
   * fehname= ... FSH object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doQuiesceFSH (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "type", "objname", "fehtype", "fehname", "timeout"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ServiceStatusQuiesce>");
    body.append("<type>" +  params.get("type")+ "</type>");
    body.append("<name>" +  params.get("objname")+ "</name>");
    body.append("<fehtype>" +  params.get("fehtype")+ "</fehtype>");
    body.append("<fehname>" +  params.get("fehname")+ "</fehname>");
    body.append("<timeout>" +  params.get("timeout")+ "</timeout>");
    body.append("</ServiceStatusQuiesce>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }


    return result;
  }


  /**
   * This function implements the Quiesce operation for a service.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * type= ... class name (e.g. MultiProtocolGateway) ...
   * objname= ... service object name ...
   * timeout= ... seconds ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doQuiesceService (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "type", "objname", "timeout"});

    boolean wait = true;

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ServiceQuiesce>");
    body.append("<type>" +  params.get("type")+ "</type>");
    body.append("<name>" +  params.get("objname")+ "</name>");
    body.append("<timeout>" +  params.get("timeout")+ "</timeout>");
    body.append("</ServiceQuiesce>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
        wait = false;
      }
    }

    if (wait) {
      NamedParams waitOnQuiesce = new NamedParams(params);
      String serviceProp = params.get("type") + "." + params.get("objname");
      waitOnQuiesce.set("capture." + params.get("type"), params.get("objname"));
      while (wait) {
        NamedParams tmp = doGetObjectStatus(waitOnQuiesce);
        Node root = SomaUtils.getDOM (tmp.get(serviceProp));
        // When not fully quiesced:
        // <ObjectStatus><Class>MultiProtocolGateway</Class><OpState>down</OpState><AdminState>enabled</AdminState><Name>to1234</Name><EventCode>0x00000000</EventCode><ErrorCode/><ConfigState>saved</ConfigState></ObjectStatus>
        // When fully quiesced:
        // <ObjectStatus><Class>MultiProtocolGateway</Class><OpState>down</OpState><AdminState>enabled</AdminState><Name>to1234</Name><EventCode>0x0036003d</EventCode><ErrorCode>in quiescence</ErrorCode><ConfigState>saved</ConfigState></ObjectStatus>
        Node nodeResponse = SomaUtils.nodeXpathFor(root, "/ObjectStatus/ErrorCode[contains(., 'quiescence')]");
        if (nodeResponse != null) {
          wait = false;
        } else {
          Thread.sleep(3000);
        }
      }
    }

    return result;
  }


  /**
   * This method implements the RefreshDocument operation, which ensures the
   * document cache associated with the specified XML manager has an up to date
   * copy of the specified URL.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * xmlmanager= ... name of an XML manager object ...
   * doc= ... URL of a document ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRefreshDocument (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "xmlmanager", "doc"});

    // Make the request of the XML Management Interface.
    String body = "<RefreshDocument><XMLManager>" + params.get("xmlmanager") + "</XMLManager><Document>" + params.get("doc") + "</Document></RefreshDocument>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RefreshStlesheet operation, which ensures the
   * stylesheet cache associated with the specified XML manager has an up to date
   * copy of the specified stylesheet.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * xmlmanager= ... name of an XML manager object ...
   * stylesheet= ... URL of a stylesheet ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRefreshStylesheet (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "xmlmanager", "stylesheet"});

    // Make the request of the XML Management Interface.
    String body = "<RefreshStylesheet><XMLManager>" + params.get("xmlmanager") + "</XMLManager><Stylesheet>" + params.get("stylesheet") + "</Stylesheet></RefreshStylesheet>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RefreshTAMCerts operation.
   *
   * The params must contain:
   *
   * domain= ... domain ...
   * objname= ... name of a TAM object ...
   * tamuid= ... TAM userid ...
   * tampwd= ... TAM password ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRefreshTAMCerts (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "tamuid", "tampwd"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<RefreshTAMCerts>");
    body.append("<TAMObject>" + params.get("objname") + "</TAMObject>");
    body.append("<Administrator>" + params.get("tamuid") + "</Administrator>");
    body.append("<Password>" + params.get("tampwd") + "</Password>");
    body.append("</RefreshTAMCerts>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RemoveCheckpoint operation, which removes the
   * specified checkpoint from the domain.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of a checkpoint ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRemoveCheckpoint (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<RemoveCheckpoint><ChkName>" + params.get("name") + "</ChkName></RemoveCheckpoint>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RemoveDirectory operation, which removes
   * a single directory.  Sorry, it won't remove multiple levels.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * remote= ... the remote directory to remove ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRemoveDirectory (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<RemoveDir>" +
    "<Dir>" + params.get("remote") + "</Dir>" +
    "</RemoveDir>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RemoveStylesheet operation, which removes the
   * specified stylesheet(s) from the cache associated with the XML manager.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * xmlmanager= ... name of an XML manager object ...
   *
   * The params may contain:
   *
   * pattern= ... PCRE specifying which stylesheets to remove from the cache ...
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRemoveStylesheet (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "xmlmanager"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<RemoveStylesheet>");
    body.append("<XMLManager>" + params.get("xmlmanager") + "</XMLManager>");
    if (params.get("pattern") != null)
      body.append("<MatchPattern>" + params.get("pattern") + "</MatchPattern>");
    body.append("</RemoveStylesheet>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the ResetDomain method, which returns the domain to
   * a pristine condition, as if it had just been created.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doResetDomain (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    String body = "<ResetThisDomain/>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the RestartDomain method, which restarts the domain.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doRestartDomain (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    String body = "<RestartThisDomain/>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the do-restore operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name to restore ...
   * local=... file to restore from ...
   *
   * The params may contain:
   *
   * depoyment-policy-name= name of deployment policy to use
   * deployment-policy=raw XML for <soma:deployment-policy> elements to use
   * domains=... list of domains to restore, in addition to "domain", separated by spaces ...
   * dry-run= true or false (defaults to false)
   * format= ZIP or XML (defaults to ZIP)
   * ignore-errors=on or off (defaults to off)
   * overwrite-files=true or false (defaults to true)
   * overwrite-objects=true or false (defaults to true)
   * rewrite-local-ip=true or false (defaults to false)
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRestore (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<soma:do-restore");
    if (params.get("format") != null) {
      body.append(" source-type=\"" + params.get("format") + "\"");
    } else {
      body.append(" source-type=\"ZIP\"");
    }
    if (params.get("dry-run") != null) {
      body.append(" dry-run=\"" + params.get("dry-run") + "\"");
    } else {
      body.append(" dry-run=\"false\"");
    }
    if (params.get("overwrite-files") != null) {
      body.append(" overwrite-files=\"" + params.get("overwrite-files") + "\"");
    } else {
      body.append(" overwrite-files=\"true\"");
    }
    if (params.get("overwrite-objects") != null) {
      body.append(" overwrite-objects=\"" + params.get("overwrite-objects") + "\"");
    } else {
      body.append(" overwrite-objects=\"true\"");
    }
    if (params.get("rewrite-local-ip") != null) {
      body.append(" rewrite-local-ip=\"" + params.get("rewrite-local-ip") + "\"");
    } else {
      body.append(" rewrite-local-ip=\"false\"");
    }
    if (params.get("deployment-policy-name") != null) {
      body.append(" deployment-policy=\"" + params.get("deployment-policy-name") + "\"");
    }
    body.append(">");

    body.append("<soma:input-file>" + Base64.base64FromBinaryFile(params.get("local")) + "</soma:input-file>");

    body.append("<soma:domain name=\"" + params.get("domain") + "\" import-domain=\"true\" reset-domain=\"true\"/>");
    if (params.get("domains") != null) {

      // Parse the list of domain names, which are merely whitespace separated.
      String[] names = params.get("domains").split("\\s+");

      // Spin through all the supplied domain names.
      for (int i = 0; i < names.length; i += 1) {
        if (names[i].equals(params.get("domain")) == false) {
          // Not the primary domain so add this to the list.
          body.append("<soma:domain name=\"" + names[i] + "\"/>");
        }
      }
    }

    if (params.get("deployment-policy") != null) {
      body.append(params.get("deployment-policy")); // Raw XML for deployment-policy object
    }
    body.append("</soma:do-restore>");

    String request = SomaUtils.getGeneralEnvelope ("default", body.toString());
    NamedParams result = params;
    try {
      result = conn.sendAndReceive (params, request);
      Node root = SomaUtils.getDOM (result.get("rawresponse"));
      Node nodeResponse = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response/soma:import");
      if (nodeResponse == null && errorsAreSignificant(params)) {
        throw new RuntimeException("Failed to restore on " + params.get("hostname"));
      }
    } catch (Exception e) {
      if (errorsAreSignificant(params)) {
        throw e;
      }
    }

    return result;
  }


  /**
   * This method implements the RollbackCheckpoint operation, which rolls back the
   * specified checkpoint.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of a checkpoint ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doRollbackCheckpoint (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<RollbackCheckpoint><ChkName>" + params.get("name") + "</ChkName></RollbackCheckpoint>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SaveCheckpoint operation, which saves the
   * the state of the specified domain in a checkpoint.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of a checkpoint ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSaveCheckpoint (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<SaveCheckpoint><ChkName>" + params.get("name") + "</ChkName></SaveCheckpoint>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SaveConfig action.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params containing "domain"
   * @return a NamedParams containing "rawresponse"
   * @throws Exception
   */
  public NamedParams doSaveConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    String body = "<SaveConfig/>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SecureBackup operation.
   *
   * The params must contain:
   *
   * domain= ... domain ...
   * objname= ... CryptoCertificate object name ...
   * remote= ... URL to write the backup to ...
   *
   * The params may contain:
   *
   * include-iscsi=on or off (default off)
   * include-raid=on or off (default off)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSecureBackup (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<SecureBackup>");
    body.append("<cert>" + params.get("objname") + "</cert>");
    body.append("<destination>" + params.get("remote") + "</destination>");
    if (params.get("include-iscsi") != null) {
      body.append("<include-iscsi>" + params.get("include-iscsi")+ "</include-iscsi>");
    } else {
      body.append("<include-iscsi>off</include-iscsi>");
    }
    if (params.get("include-raid") != null) {
      body.append("<include-raid>" + params.get("include-raid")+ "</include-raid>");
    } else {
      body.append("<include-raid>off</include-raid>");
    }
    body.append("</SecureBackup>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SecureRestore operation.
   *
   * The params must contain:
   *
   * domain= ... domain ...
   * objname= ... CryptoCertificate object name ...
   * remote= ... URL to restore from ...
   *
   * The params may contain:
   *
   * validate=on or off (default off)
   * type= ... ??? ... (not supported in 3.8.2, optional 4.0 and later)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSecureRestore (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "objname", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<SecureRestore>");
    body.append("<cred>" + params.get("objname") + "</cred>");
    body.append("<source>" + params.get("remote") + "</source>");
    if (params.get("validate") != null) {
      body.append("<validate>" + params.get("validate")+ "</validate>");
    } else {
      body.append("<validate>off</validate>");
    }
    if (params.get("type") != null) {
      body.append("<BackupMachineType>" + params.get("type")+ "</BackupMachineType>");
    }
    body.append("</SecureRestore>");

    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SelectConfig operation, which selects the
   * configuration file to use for the device.
   *
   * The params must contain:
   *
   * remote= ... some file on DP ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSelectConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"remote"});

    // Make the request of the XML Management Interface.
    String body = "<SelectConfig><File>" + params.get("remote") + "</File></SelectConfig>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SendErrorReport operation, which sends an email
   * error report.
   *
   * The params must contain:
   *
   * host= ... address of SMTP server ...
   * location= ... subject line ...
   * address= ... email address ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "rawresponse".
   * @throws Exception
   */
  public NamedParams doSendErrorReport (NamedParams params) throws Exception {
    params.insistOn (new String[] {"host", "location", "address"});

    // Make the request of the XML Management Interface.
    String body = "<SendErrorReport>" +
    "<SmtpServer>" + params.get("host") + "</SmtpServer>" +
    "<LocationIdentifier>" + params.get("location") + "</LocationIdentifier>" +
    "<EmailAddress>" + params.get("address") + "</EmailAddress>" +
    "</SendErrorReport>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SendFile operation, which sends a file via email.
   *
   * The params must contain:
   *
   * host= ... address of SMTP server ...
   * remote= ... url of the file to send ...
   * address= ... email address ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams containing "rawresponse".
   * @throws Exception
   */
  public NamedParams doSendFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"host", "remote", "address"});

    // Make the request of the XML Management Interface.
    String body = "<SendFile>" +
    "<LocationIdentifier>" + params.get("remote") + "</LocationIdentifier>" +
    "<SmtpServer>" + params.get("host") + "</SmtpServer>" +
    "<EmailAddress>" + params.get("address") + "</EmailAddress>" +
    "</SendFile>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SendLogEvent operation, which places an entry
   * in the stream of log messages.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * logcategory= ... some log category (e.g. xsltmsg) ...
   * loglevel= (debug | info | notice | warn | error | critic | alert | emerg)
   * msg= ... text of the message ...
   *
   * The params may optionally contain:
   *
   * eventcode= event code (e.g. '0x12345678')
   * ignore-errors = on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSendLogEvent (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "logcategory", "loglevel", "msg"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<SendLogEvent>");
    body.append("<LogType>" + params.get("logcategory") + "</LogType>");
    body.append("<GenLogLevel>" + params.get("loglevel") + "</GenLogLevel>");
    body.append("<LogEvent>" + params.get("msg") + "</LogEvent>");
    if (params.get("eventcode") != null) {
      body.append("<EventCode>" + params.get("eventcode") + "</EventCode>");
    }
    body.append("</SendLogEvent>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SetConfig operation, which creates an
   * object based on the configuration in XML format, which is identical to
   * that returned by GetConfig.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * config= ... raw XML configuration ...
   *
   * @param params
   * @return NamedParams containing "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "config"});

    // Make the request of the XML Management Interface.
    String body = "<soma:set-config>" + SomaUtils.xmlNoHeader(params.get("config")) + "</soma:set-config>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the set-file operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   * local= ... local file name ... (e.g.c:\\somedir\somefile.xsl)
   * remote=... remove file name ... (e.g. local:///somefile.xsl)
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetFile (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "local", "remote"});

    String base64FileContent = Base64.base64FromBinaryFile(params.get("local"));
    return doSetFileImpl (params, base64FileContent);
  }


  /**
   * This method implements the SetLogLevel operation, which sets the minimum
   * level of severity for entries recorded in the default log.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * loglevel= (debug | info | notice | warn | error | critic | alert | emerg)
   *
   * When domain=default, then the params may contain:
   *
   * internallog=on or off
   * rbmlog=on or off
   *
   * The params may contain:
   *
   *ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetLogLevel (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "loglevel"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<SetLogLevel>");
    body.append("<LogLevel>" + params.get("loglevel") + "</LogLevel>");
    if (params.get("domain").equals("default")) {
      if (params.get("internallog") != null)
        body.append("<InternalLog>" + params.get("internallog") + "</InternalLog>");
      if (params.get("rbmlog") != null)
        body.append("<RBMLog>" + params.get("rbmlog") + "</RBMLog>");
      if (params.get("globalfilter") != null)
        body.append("<GlobalIPLogFilter>" + params.get("globalfilter") + "</GlobalIPLogFilter>");
    }
    body.append("</SetLogLevel>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SetRBMLogLevel operation, which turns
   * RBM detailed logging (to the default log) on and off.
   *
   * The params must contain:
   *
   * rbmlog=on or off
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetRBMLogLevel (NamedParams params) throws Exception {
    params.insistOn (new String[] {"rbmlog"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<SetRBMDebugLog>");
    body.append("<RBMLog>" + params.get("rbmlog") + "</RBMLog>");
    body.append("</SetRBMDebugLog>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SetSystemVar operation, which stores
   * a value in the specified system variable.
   *
   * The params must contain:
   *
   * name=... name of a predefined system variable (e.g. var://system/amp/debug) ...
   * value=... new value of the variable ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetSystemVar (NamedParams params) throws Exception {
    params.insistOn (new String[] {"name", "value"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<SetSystemVar>");
    body.append("<Var>" + params.get("name") + "</Var>");
    body.append("<Value>" + params.get("value") + "</Value>");
    body.append("</SetSystemVar>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the SetTimeAndDate operation, which sets
   * the system time and/or date.
   *
   * The params may contain:
   *
   * time= ... time as you would enter it in the CLI or web gui ...
   * date= ... time as you would enter it in the CLI or web gui ...
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doSetTimeAndDate (NamedParams params) throws Exception {
    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<SetTimeAndDate>");
    if (params.get("date") != null)
      body.append("<Date>" + params.get("date") + "</Date>");
    if (params.get("time") != null)
      body.append("<Time>" + params.get("time") + "</Time>");
    body.append("</SetTimeAndDate>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the Shutdown operation, which restarts
   * or halts the system.
   *
   * The params must contain:
   *
   * mode= (reboot | reload | halt)
   *
   * The params may contain:
   *
   * delay= ... number of seconds to delay ...
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doShutdown (NamedParams params) throws Exception {
    params.insistOn (new String[] {"mode"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer();
    body.append("<Shutdown>");
    body.append("<Mode>" + params.get("mode") + "</Mode>");
    if (params.get("delay") != null)
      body.append("<Delay>" + params.get("delay") + "</Delay>");
    body.append("</Shutdown>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TCPConnectionTest operation, which opens
   * (then closes) a connection to the specified host and port.
   *
   * The params must contain:
   *
   * host= ... DNS-resolvable host name, or a dotted decimal address ...
   * port= ... port in the range of 1 through 65535 ...
   *
   * The params may contain:
   *
   * useipv=4 or 6 (default 4)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTCPConnectionTest (NamedParams params) throws Exception {
    params.insistOn (new String[] {"host", "port"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ("<TCPConnectionTest>");
    body.append("<RemoteHost>" + params.get("host") + "</RemoteHost>");
    body.append("<RemotePort>" + params.get("port") + "</RemotePort>");
    if (params.get("useipv") != null) {
      body.append("<useIPv>" + params.get("useipv") + "</useIPv>");
    }
    body.append("</TCPConnectionTest>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TestPasswordMap operation.
   *
   * The params must contain:
   *
   * aliasname= ... publicly visible "password"
   * type= ... key | cert
   * remote= ... URL ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTestPasswordMap (NamedParams params) throws Exception {
    params.insistOn (new String[] {"type", "aliasname", "remote"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<TestPasswordMap>");
    body.append("<AliasName>" + params.get("aliasname") + "</AliasName>");
    body.append("<Type>" + params.get("type") + "</Type>");
    body.append("<FileURL>" + params.get("remote") + "</FileURL>");
    body.append("<TestPasswordMap>");

    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TestURLMap operation, which tests
   * the supplied URL against the specified URLMap object.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of the URLMap object ...
   * url= ... URL ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTestURLMap (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name", "url"});

    // Make the request of the XML Management Interface.
    String body = "<TestURLMap>" +
    "<URLMap>" + params.get("name") + "</URLMap>" +
    "<URL>" + params.get("url") + "</URL>" +
    "</TestURLMap>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
//            insistSomaResultIsOkay(result);  Apparently the result is always <error-log/>
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TestURLRefresh operation, which tests
   * the supplied URL against the specified URLRefresh object.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of the URLRefresh object ...
   * url= ... URL ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTestURLRefresh (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name", "url"});

    // Make the request of the XML Management Interface.
    String body = "<TestURLRefresh>" +
    "<URLRefreshPolicy>" + params.get("name") + "</URLRefreshPolicy>" +
    "<URL>" + params.get("url") + "</URL>" +
    "</TestURLRefresh>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TestURLRewrite operation, which tests
   * the supplied URL against the specified URLRewrite object.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of the URLRewrite object ...
   * url= ... URL ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTestURLRewrite (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name", "url"});

    // Make the request of the XML Management Interface.
    String body = "<TestURLRewrite>" +
    "<URLRewritePolicy>" + params.get("name") + "</URLRewritePolicy>" +
    "<URL>" + params.get("url") + "</URL>" +
    "</TestURLRewrite>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the TestValidateSchema operation, which tests
   * the supplied XML file (URL) against the specified Schema file (URL).
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * xml= ... URL (possibly on box, e.g. local:///file.xml) for the XML to validate ...
   * schema= ... URL (possibly on box, e.g. local:///schema.xsd) for the schema to use in validating the XML ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doTestValidateSchema (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "xml", "schema"});

    // Make the request of the XML Management Interface.
    String body = "<TestValidateSchema>" +
    "<XMLFile>" + params.get("xml") + "</XMLFile>" +
    "<SchemaFile>" + params.get("schema") + "</SchemaFile>" +
    "</TestValidateSchema>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the UndoConfig operation.  Wish I was sure
   * I knew what it does.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * classname= ... some DP object class name ...
   * objname= ... name of an object ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUndoConfig (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "classname", "objname"});

    // Make the request of the XML Management Interface.
    String body = "<UndoConfig>" +
    "<Class>" + params.get("classname") +  "</Class>" +
    "<Name>" + params.get("objname") + "</Name>" +
    "</UndoConfig>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the UniversalPacketCaptureDebug operation.
   *
   * The params must contain:
   *
   * type=Ethernet, VLAN, Loopback, or All
   * maxsize= ... max size of capture in KB ...
   * maxpacketsize= ... max packets size in bytes (e.g. 9000) ...
   * maxtime= ... seconds ... (required in 3.8.2, optional in 4.0.2 and later)
   *
   * The params may contain:
   *
   * ethernet-int= ... ethernet object name (e.g. eth0) ...
   * vlan-int= ... VLAN object name ...
   * mode=timed or continuous (default timed)
   * maxtime= ... seconds ... (required in 3.8.2, optional in 4.0.2 and later)
   * filter= ... filter string - see web gui ...
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUniversalPacketCaptureDebug (NamedParams params) throws Exception {
    params.insistOn (new String[] {"type", "maxsize", "maxpacketsize"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<UniversalPacketCaptureDebug>");
    body.append("<InterfaceType>" + params.get("type") + "</InterfaceType>");
    if (params.get("ethernet-int") != null) {
      body.append("<EthernetInterface>" + params.get("ethernet-int") + "</EthernetInterface>");
    }
    if (params.get("vlan-int") != null) {
      body.append("<VLANInterface>" + params.get("vlan-int") + "</VLANInterface>");
    }
    if (params.get("mode") != null && params.get("maxtime") != null) {
      body.append("<CaptureMode>" + params.get("mode") + "</CaptureMode>");
      body.append("<MaxTime>" + params.get("maxtime") + "</MaxTime>");
    } else if (params.get("mode").equals("timed") && params.get("maxtime") == null) {
      throw new RuntimeException("universal packet capture - you specified mode=\"timed\" but didn't specify maxtime=\"nnn\".");
    } else if (params.get("mode") != null) {
      body.append("<CaptureMode>" + params.get("mode") + "</CaptureMode>");
    } else {
      body.append("<CaptureMode>timed</CaptureMode>");
      body.append("<MaxTime>10</MaxTime>");
    }
    body.append("<MaxSize>" + params.get("maxsize") + "</MaxSize>");
    body.append("<MaxPacketSize>" + params.get("maxpacketsize") + "</MaxPacketSize>");
    if (params.get("filter") != null) {
      body.append("<Filter>" + params.get("filter") + "</Filter>");
    }
    body.append("</UniversalPacketCaptureDebug>");

    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the UniversalStopPacketCapture operation.
   *
   * The params must contain:
   *
   * type=Ethernet, VLAN, Loopback, or All
   *
   * The params may contain:
   *
   * ethernet-int= ... ethernet object name (e.g. eth0) ...
   * vlan-int= ... VLAN object name ...
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUniversalStopPacketCapture (NamedParams params) throws Exception {
    params.insistOn (new String[] {"type"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<UniversalStopPacketCapture>");
    body.append("<InterfaceType>" + params.get("type") + "</InterfaceType>");
    if (params.get("ethernet-int") != null) {
      body.append("<EthernetInterface>" + params.get("ethernet-int") + "</EthernetInterface>");
    }
    if (params.get("vlan-int") != null) {
      body.append("<VLANInterface>" + params.get("vlan-int") + "</VLANInterface>");
    }
    body.append("</UniversalStopPacketCapture>");

    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }

  /**
   * This function implements the Unquiesce operation for a device.
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doUnquiesceDevice (NamedParams params) throws Exception {

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<UnquiesceDP/>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Unquiesce operation for a domain.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doUnquiesceDomain (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<DomainUnquiesce>");
    body.append("<name>" + params.get("domain") + "</name>");
    body.append("</DomainUnquiesce>");
    String request = SomaUtils.getDoActionEnvelope ("default", body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Unquiesce operation for a front side handler.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * type= ... Service class name (e.g. MultiProtocolGateway) ...
   * objname= ... name of service object ...
   * fehtype= ... FSH class name (e.g. HTTPSourceProtocolHandler) ...
   * fehname= ... FSH object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doUnquiesceFSH (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "type", "objname", "fehtype", "fehname"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ServiceStatusUnquiesce>");
    body.append("<type>" + params.get("type") + "</type>");
    body.append("<name>" + params.get("objname") + "</name>");
    body.append("<fehtype>" + params.get("fehtype") + "</fehtype>");
    body.append("<fehname>" + params.get("fehname") + "</fehname>");
    body.append("</ServiceStatusUnquiesce>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This function implements the Unquiesce operation for a service.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * type= ... service class name (e.g. MultiProtocolGateway) ...
   * objname= ... service object name ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return The NamedParams result.
   * @throws Exception
   */
  public NamedParams doUnquiesceService (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "type", "objname"});

    // Make the request of the XML Management Interface.
    StringBuffer body = new StringBuffer ();
    body.append("<ServiceUnquiesce>");
    body.append("<type>" + params.get("type") + "</type>");
    body.append("<name>" + params.get("objname") + "</name>");
    body.append("</ServiceUnquiesce>");
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body.toString());
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }

  /**
   * This method uploads files from the local computer to DataPower.  You cannot rename files
   * using this mechanism.  (e.g. local=abc.cer remote=def.cer)
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * remote= ... target filestore and directory (not filename) ...
   *
   * The params may contain:
   *
   * local= ... directory on local computer ...  (defaults to current directory)
   * pattern= PCRE for files to upload (defaults to '.*')
   * recurse= true | false (default to false)
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUpload (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Set default values for optional parameters.
    String basedir = params.get("local");
    if (basedir == null || basedir.length() == 0) {
      basedir = ".";      // Default to current directory
    }
    String pattern = params.get("pattern");
    if (pattern == null || pattern.length() == 0) {
      pattern = ".*";     // Default to PCRE for anything (./)
    }
    boolean recurse = isTrue(params, "recurse");

    // Gather the list of all files and directories, starting with basedir and matching the specified pattern.
    LocalFiles locals = new LocalFiles();
    Vector<LocalFiles.Entry> biglist = locals.fill(basedir, pattern, recurse);

    if (biglist.size() > 0) {

      if (basedir.endsWith(File.separator) == false) {
        basedir += File.separator;
      }
      String rmtdir = params.get("remote");
      if (rmtdir.endsWith("/") == false) {
        rmtdir += "/";
      }

      // Ensure that the directory specified in "remote" exists.
      NamedParams p = new NamedParams(params);
      String pat = "[a-z]+:/*[^/]+.*";    // local|temporary|etc. : [/|//|///] anything-but-slashes+ anything*
      // System.out.println ("$$$ (" + pat + ") (" + rmtdir + ") " + Pattern.matches(pat, rmtdir));
      if (Pattern.matches(pat, rmtdir)) {
        p.set("remote", rmtdir);
        System.out.println ("Ensuring exists: " + p.get("remote"));
        doCreateDirectory(p);
      }

      // Create all the necessary subdirectories.
      for (int i = biglist.size() - 1; i >= 0; i -= 1) {
        LocalFiles.Entry entry = biglist.get(i);
        if (entry.isDirectory()) {
          // Create this directory.
          p.set("remote", (rmtdir + entry.getName()).replace('\\', '/'));
          System.out.println ("Ensuring exists: " + p.get("remote"));
          doCreateDirectory(p);
        }
      }

      // Upload the files, now that the directories exist.
      p = new NamedParams(params);
      for (int i = 0; i < biglist.size(); i += 1) {
        LocalFiles.Entry entry = biglist.get(i);
        if (entry.isDirectory() == false) {
          // Upload this file.
          p.set("local", basedir + entry.getName());
          p.set("remote", (rmtdir + entry.getName()).replace('\\', '/'));
          System.out.println ("Uploading " + p.get("local") + " to " + p.get("remote"));
          doSetFile(p);
        }
      }
    } else {

      System.out.println ("No files to upload.");

    }

    return params;
  }


  /**
   * This method implements the UserForcePasswordChange operation, which requires
   * a user to choose a new password when next logging into the web gui.  I'm not
   * sure what effect this has on users that can only log in via SSH or the XML
   * management interface.
   *
   * The params must contain:
   *
   * dpuid=... some user id ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUserForcePasswordChange (NamedParams params) throws Exception {
    params.insistOn (new String[] {"dpuid"});

    // Make the request of the XML Management Interface.
    String body = "<UserForcePasswordChange>" +
    "<User>" + params.get("dpuid") + "</User>" +
    "</UserForcePasswordChange>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the UserResetFailedLogin operation.
   *
   * The params must contain:
   *
   * dpuid=... some user id ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUserResetFailedLogin (NamedParams params) throws Exception {
    params.insistOn (new String[] {"dpuid"});

    // Make the request of the XML Management Interface.
    String body = "<UserResetFailedLogin>" +
    "<User>" + params.get("dpuid") + "</User>" +
    "</UserResetFailedLogin>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the UserResetPassword operation.
   *
   * The params must contain:
   *
   * dpuid= ... some user id ...
   * dppwd= ... password ...
   *
   * The params may contain:
   *
   * ignore-errors=on or off
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doUserResetPassword (NamedParams params) throws Exception {
    params.insistOn (new String[] {"dpuid", "dppwd"});

    // Make the request of the XML Management Interface.
    String body = "<UserResetPassword>" +
    "<User>" + params.get("dpuid") + "</User>" +
    "<Password>" + params.get("dppwd") + "</Password>" +
    "</UserResetPassword>";
    String request = SomaUtils.getDoActionEnvelope ("default", body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the ValCredAddCertsFromDir operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doValCredAddCertsFromDir (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain"});

    // Make the request of the XML Management Interface.
    String body = "<ValCredAddCertsFromDir/>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * This method implements the WSRRSynchronize operation, which forces the
   * specified WSRR subscription to be updated from WSRR.
   *
   * The params must contain:
   *
   * domain= ... some domain name ...
   * name= ... name of a WSRRSubscription object on DP ...
   *
   * @param params
   * @return NamedParams "rawresponse".
   * @throws Exception
   */
  public NamedParams doWSRRSynchronize (NamedParams params) throws Exception {
    params.insistOn (new String[] {"domain", "name"});

    // Make the request of the XML Management Interface.
    String body = "<WsrrSynchronize>" +
    "<WSRRSubscription>" + params.get("name") + "</WSRRSubscription>" +
    "</WsrrSynchronize>";
    String request = SomaUtils.getDoActionEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }










  /**
   * This method implements the set-file operation.
   *
   * The params must contain:
   *
   * domain= ... some domain name ... (e.g. default, regroot, etc.)
   * local= ... local file name ... (e.g.c:\\somedir\somefile.xsl)
   * remote=... remove file name ... (e.g. local:///somefile.xsl)
   *
   * Returns a NamedParams containing a key/value pair for "rawresponse".
   *
   */
  private NamedParams doSetFileImpl (NamedParams params, String base64FileContent) throws Exception {
    params.insistOn (new String[] {"domain", "remote"});

    // Make the request of the XML Management Interface.
    String body = "<soma:set-file name=\"" + params.get("remote") + "\">" +
    base64FileContent +
    "</soma:set-file>";
    String request = SomaUtils.getGeneralEnvelope (params.get("domain"), body);
    NamedParams result = params;
    if (errorsAreSignificant(params)) {
      result = conn.sendAndReceive (params, request);
      insistSomaResultIsOkay(result);
    } else {
      // Do the operation, ignoring the response and any exceptions.
      try {
        result = conn.sendAndReceive (params, request);
      } catch (Exception e) {
      }
    }

    return result;
  }


  /**
   * Errors returned from DataPower are significant unless the user specified
   * ignore-errors=(yes|YES|true|TRUE|1) in the parameters.
   * @param params optionally containing ignore-errors
   * @return true if DP errors are significant, false if they should be ignored.
   */
  private boolean errorsAreSignificant (NamedParams params) {
    boolean important = true;

    if (isTrue(params, "ignore-errors")) {
      important = false;
    }

    return important;
  }


  /**
   * Test whether result.get("rawresponse") contains an element for
   * /env:Envelope/env:Body/soma:response/soma:result=OK
   *
   * @param result the results of a call to SSLConnection.sendAndReceive().
   * @throws Exception when the result isn't successful.
   */
  private void insistSomaResultIsOkay(NamedParams result) throws Exception {
    boolean throwException = true;

    Node root = SomaUtils.getDOM (result.get("rawresponse"));
    Node nodeResponse = SomaUtils.nodeXpathFor(root, "/env:Envelope/env:Body/soma:response");
    if (nodeResponse != null) {
      // Test whether the request succeeded by checking for <soma:result>OK</soma:result>.
      String okay = SomaUtils.stringXpathFor (nodeResponse, "soma:result").trim();
      if (okay.equals("OK")) {
        throwException = false;
      }
    }

    if (throwException) {
      throw new UnsupportedOperationException ("Operation soma=" + result.get("soma") + " failed. rawresponse=" + result.get("rawresponse"));
    }
  }


  /**
   * Determine whether the specified parameter is "true", which tests without regard to case for
   * 'yes', 'true', 'on', or '1'.
   *
   * @param params
   * @param paramName
   *
   * @return boolean
   */
  private boolean isTrue (NamedParams params, String paramName) {
    boolean bRet = false;

    String param = params.get(paramName);
    if (param != null) {
      if (param.equals("yes") || param.equals("YES") || param.equals("on") || param.equals("ON") || param.equals("true") || param.equals("TRUE") || param.equals("1")) {
        bRet = true;
      }
    }

    return bRet;
  }




  private class LocalFiles {

    private String basedir = "";
    private Pattern pattern;
    private boolean recurse = false;

    private Vector<Entry> biglist = new Vector<Entry>();


    public Vector<Entry> fill (String basedir, String pattern, boolean recurse) throws Exception {

      biglist.clear();    // In case the object is being reused.

      this.recurse = recurse;
      this.pattern = Pattern.compile(pattern);

      // Ensure that the basedir is an absolute path.
      this.basedir = new File(basedir).getAbsolutePath();

      fillImpl(this.basedir, 0, "gathering : ");

      return biglist;
    }

    private boolean fillImpl (String dir, int depth, String padding) throws Exception {

      boolean bContainsFiles = false;

      // Find all the files and subdirectories in the specified directory.
      File startHere = new File(dir);
      File[] entries = startHere.listFiles();

      // Add the qualified files/subdirectories, recursing on any directories.
      if (entries != null) {
        for (int i = 0; i < entries.length; i += 1) {
          File entry = entries[i];
          if (entry.getName().charAt(0) != '.') {

            String name = entry.getPath();
            name = name.substring(basedir.length() + File.pathSeparator.length());    // Skip the prefix (e.g. /home/someuser/)

            if (pattern.matcher(entry.getName()).matches()) {

              biglist.add(new Entry(name, entry.isDirectory()));   // Record this winner!
              bContainsFiles = true;

            }

            if (recurse && entry.isDirectory()) {
              if (fillImpl(entry.getAbsolutePath(), depth + 1, padding + "  ")) {
                bContainsFiles = true;
                biglist.add(new Entry(name, entry.isDirectory()));   // Record this directory
              }
            }
          }
        }
      }

      return bContainsFiles;
    }

    public class Entry {

      private boolean isDir;
      private String name;

      public Entry (String name, boolean isDirectory) {
        this.isDir = isDirectory;
        this.name = name;
      }

      public boolean isDirectory () {
        return isDir;
      }

      public String getName () {
        return name;
      }
    }
  }
}
