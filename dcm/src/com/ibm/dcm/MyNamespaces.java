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
import javax.xml.*;



/**
 * This class defines namespaces (and prefixes) that can be used in XPath expressions
 * passed to SomaUtils.xxxXpathFor() methods.
 */
public class MyNamespaces implements javax.xml.namespace.NamespaceContext {

  public String getNamespaceURI(String prefix) {
    if (prefix == null)
      throw new NullPointerException("Null prefix");
    else if ("dcm".equals(prefix))
      return "urn:datapower:configuration:manager";
    else if ("env".equals(prefix))
      return "http://schemas.xmlsoap.org/soap/envelope/";
    else if ("soap".equals(prefix))
      return "http://schemas.xmlsoap.org/soap/envelope/";
    else if ("soma".equals(prefix))
      return "http://www.datapower.com/schemas/management";
    else if ("xml".equals(prefix))
      return XMLConstants.XML_NS_URI;
    return XMLConstants.NULL_NS_URI;
  }

  public String getPrefix(String namespaceURI) {
    throw new UnsupportedOperationException();
  }

  public Iterator<?> getPrefixes(String namespaceURI) {
    throw new UnsupportedOperationException();
  }
}

