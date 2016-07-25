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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class collects parameters for various SOMA calls.  It is a thin
 * wrapper around a key/value list.
 * 
 */
public class NamedParams {

  private TreeMap<String, String> mapParams;



  public NamedParams() {
    mapParams = new TreeMap<String, String>();
  }

  public NamedParams(String[] params){
    mapParams = new TreeMap<String, String>();
    set(params);
  }

  public NamedParams (NamedParams other) {
    mapParams = new TreeMap<String, String>(other.mapParams);
  }


  public String get(String paramId) {
    return mapParams.get(paramId);
  }

  public void set(String paramId, String paramVal) {
    mapParams.put(paramId, paramVal);
  }

  public void set(String[] params) {
    if ((params.length % 2) == 1)
      throw new UnsupportedOperationException("The number of params must be even (multiple of 2) since these are key/value pairs.");

    for (int i = 0; i < params.length; i += 2) {
      set(params[i], params[i+1]);
    }
  }

  public void set (NamedParams other) {
    Iterator<Map.Entry<String, String>> it = other.mapParams.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      mapParams.put(entry.getKey(), entry.getValue());
    }
  }

  public void clear () {
    mapParams.clear();
  }

  public void remove (String paramId) {
    mapParams.remove(paramId);
  }


  public String[] toStringArray () {
    String[] ret = new String[mapParams.size() * 2];

    Iterator<Map.Entry<String, String>> it = mapParams.entrySet().iterator();
    int i = 0;
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      ret[i++] = entry.getKey();
      ret[i++] = entry.getValue();
    }

    return ret;
  }


  public String dump(){
    StringBuffer buf = new StringBuffer();

    Iterator<Map.Entry<String, String>> it = mapParams.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, String> entry = it.next();
      String key = entry.getKey();
      if (key.equals("pwd")) {
        buf.append(", \n" + entry.getKey() + " = XXXXX");
      } else {
        buf.append(", \n" + entry.getKey() + " = " + entry.getValue());
      }
    }

    String strRet = "";
    if (buf.length() > 0) {
      strRet = buf.substring(", \n".length());
    }

    return strRet;
  }


  public void insistOn (String paramId) throws IllegalArgumentException {
    if (get(paramId) == null) {
      throw new IllegalArgumentException ("\"" + paramId + "\" is missing from params");
    }
  }

  public void insistOn (String[] paramIds) throws IllegalArgumentException {
    for (int i = 0; i < paramIds.length; i += 1) {
      if (get(paramIds[i]) == null) {
        throw new IllegalArgumentException ("\"" + paramIds[i] + "\" is missing from params");
      }
    }
  }


  public void doSubstitutions () {
    Pattern pat = Pattern.compile("(%\\{[a-zA-Z0-9\\.\\-_]+\\})");
    Iterator<Map.Entry<String, String>> it = mapParams.entrySet().iterator();
    while (it.hasNext()) {
      // Access the parameter id and value, for convenience.
      Map.Entry<String, String> entry = it.next();
      String paramId = entry.getKey();
      String paramVal = entry.getValue();

      // Make any required substitutions, left to right.
      Matcher matches = pat.matcher(paramVal);
      boolean substitutionsMade = false;
      StringBuffer sb = new StringBuffer();
      while (matches.find()) {
        // Extract the name of the substitution parameter.  For example,
        // "%{sub.domain}" yields "sub.domain".
        String substitutionId = matches.group();
        substitutionId = substitutionId.substring(2, substitutionId.length() - 1);

        // Make the substitution provided the needed string is defined.
        String newValue = get(substitutionId);
        if (newValue != null) {
          matches.appendReplacement(sb, newValue);
          substitutionsMade = true;
        } else {
          // Leave the original reference, for which no definition is available, in place.
          matches.appendReplacement(sb, "%{" + substitutionId + "}");
        }
      }
      if (substitutionsMade) {
        // Ah!  One or more substitutions were made.  Complete the revised string and save it.
        matches.appendTail(sb);
        set(paramId, sb.toString());
      }
    }
  }

} 
