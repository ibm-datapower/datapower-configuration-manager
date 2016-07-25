/**
 * Copyright 2014, 2015 IBM Corp.
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
import org.apache.tools.ant.util.XMLFragment;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.dcm.Soma;
import com.ibm.dcm.NamedParams;
import com.ibm.dcm.SSLConnection;


public class taskWdp extends XMLFragment {

  private String operation = null;
  private String successProperty = null;
  private String responseProperty = null;
  private String xpath = null;
  private String xpathProperty = null;
  private String xpathType = null;
  private String ifProperty = null;
  private String unlessProperty = null;
  private boolean dumpInput = false;
  private boolean dumpOutput = false;
  private String capturesoma = null;

  private static SSLConnection singletonConnection = null;
  private static Soma singletonSoma = null;

  public static Soma getSoma() throws Exception {
    if (singletonSoma == null) {
      singletonConnection = new SSLConnection();
      singletonSoma = new Soma(singletonConnection);
    }
    return singletonSoma;
  }

  public taskWdp () {}

  public void setOperation(String op) {
    operation = op;
  }

  public void setResponseprop(String propname) {
    if (propname.length() > 0) {
      responseProperty = propname;
    }
  }

  public void setSuccessprop(String propname) {
    if (propname.length() > 0) {
      successProperty = propname;
    }
  }

  public void setXpath(String xpath) {
    if (xpath.length() > 0) {
      this.xpath = xpath;
    }
  }

  public void setXpathprop(String propname) {
    if (propname.length() > 0) {
      xpathProperty = propname;
    }
  }

  public void setXpathtype(String typename) {
    if (typename.length() > 0) {
      xpathType = typename;
    }
  }

  public void setIf(String propname) {
    if (propname.length() > 0) {
      ifProperty = propname;
    }
  }

  public void setUnless(String propname) {
    if (propname.length() > 0) {
      unlessProperty = propname;
    }
  }

  public void setDumpinput(boolean flag) {
    dumpInput = flag;
  }

  public void setDumpoutput(boolean flag) {
    dumpOutput = flag;
  }

  public void setCapturesoma(String filename) {
    capturesoma = filename; // filename ending in '.soma'
  }


  public void execute()
  {

    Project proj = getProject();

    NamedParams params = new NamedParams();

    if (operation != null) {
      params.set("soma", operation);
    } else {
      throw new RuntimeException("<wdp> requires an operation=\"...\" attribute.");
    }

    // Since this class is derived from XMLFragment, ANT stores any child elements under the
    // <wdp> element as individual elements in this fragment.  Stash these as named parameters.
    // In the case of duplicates the last one encountered wins.
    NodeList kids = getFragment().getChildNodes();
    for (int i = 0; i < kids.getLength(); i += 1) {
      Node node = kids.item(i);
      if ((node.getNodeType() == Node.ELEMENT_NODE) && (node.getNodeName().equals("return") == false)) {

        // This element may contain either text or XML.  By convention, this represents a name/value
        // pair where the name is the name of the element itself, and the content of the element is
        // the value of the name/value pair.
        try {
          setParam (params, node);
        } catch (Exception e) {
        }

      } else if (node.getNodeType() == Node.TEXT_NODE) {

        // This text node may actually be serialized XML, so attempt to parse it and add it as one
        // or more parameters.
        // (e.g. <objects><mgmt:object xmlns:mgmt="http://www.datapower.com/schemas/management" class="WSGateway" name="fedbca" ref-files="true" ref-objects="true" include-debug="false"/></objects><deployment-policy/>)
        try {
          Node doc = SomaUtils.getDOM("<wrapper>" + node.getTextContent() + "</wrapper>");
          if (doc != null) {
            for (Node kid = doc.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
              if (kid.getNodeType() == Node.ELEMENT_NODE) {

                // Add this element as a parameter.
                setParam (params, kid);

              }
            }
          }
        } catch (Exception e) {
        }
      }
    }

    if (params.get("port") == null || params.get("port").equals("")) {
      // Provide the default port since it wasn't supplied.
      params.set("port", "5550");
    }

    if (dumpInput) {
      System.out.println("<wdp> input:");
      System.out.println(params.dump());
      params.set("dumpinput", "true");
    }
    if (dumpOutput) {
      params.set("dumpoutput", "true");
    }
    if (capturesoma != null && !capturesoma.isEmpty()) {
      params.set("capturesoma", capturesoma);
    }
    
    // Decide whether to do this operation at all, when the @if or @unless attributes were
    // specified.  If both were specified then @if must evaluate to true and @unless must
    // evaluate to false in order for the operation to be carried out.
    boolean doIt = true;
    if (ifProperty != null && (proj.getProperty(ifProperty) == null || proj.getProperty(ifProperty).equals("false")))
      doIt = false;
    if (unlessProperty != null && proj.getProperty(unlessProperty) != null && proj.getProperty(unlessProperty).equals("true"))
      doIt = false;

    if (doIt == true) {

      NamedParams result = params;
      try {

        // Send the request to DataPower and deal with the response.
        result = getSoma().performOperation(params);

        if (successProperty != null)
          proj.setProperty(successProperty, "true");

        if (responseProperty != null) {
          String rawresponse = result.get("rawresponse");
          if (rawresponse != null) {
            proj.setProperty(responseProperty, rawresponse);
          } else {
            proj.setProperty(responseProperty, "");
          }
        }

        if (xpath != null && xpathProperty != null) {

          String xpathResult = "";

          String rawresponse = result.get("rawresponse"); // Most, but not all, operations return rawresponse
          if (rawresponse != null) {

            if (xpathType != null && xpathType.equals("node")) {
              xpathResult = expungeWrappers(SomaUtils.nodeXpathFor(SomaUtils.getDOM(rawresponse), xpath));
            } else {
              xpathResult = SomaUtils.stringXpathFor(SomaUtils.getDOM(rawresponse), xpath);
            }

          }

          proj.setProperty(xpathProperty, xpathResult);

        }

      } catch (Exception e) {

        // When the caller specifies successprop="..." then they implicitly want to handle failures.
        // Otherwise permit exceptions to interrupt the execution of the ant script.
        if (successProperty != null) {
          result.set("wdp.exception.message", e.getMessage());
        } else
          throw new BuildException(e);

      } finally {

        if (result != null) {
          // Return any SOMA param values requested by the caller.  Remember: <return antprop="..." somaprop="..."/>
          for (int i = 0; i < kids.getLength(); i += 1) {
            Node node = kids.item(i);
            if ((node.getNodeType() == Node.ELEMENT_NODE) && node.getNodeName().equals("return")) {
              String antname = ((Element)node).getAttribute("antprop");
              String somaname = ((Element)node).getAttribute("somaprop");
              if (antname.length() > 0 && somaname.length() > 0 && result.get(somaname) != null) {
                proj.setProperty(antname, result.get(somaname));
              }
            }
          }

          if (dumpOutput) {
            System.out.println("<wdp> output:");
            System.out.println(result.dump());
          }
        }
      }
    } else {

      if (successProperty != null)
        proj.setProperty(successProperty, "false");

    }
  }


  /**
   * Set an Ant property that is the name of the root node plus its serialized content, ignoring <wrapper> elements.
   * 
   * @param root 
   */
  private void setParam(NamedParams params, Node root) throws Exception {
    boolean debug = false;
    
    if (root != null) {

      if (debug) {
        System.out.println("setParam: working on " + serializeXML(root));
      }

      String key = root.getNodeName();
      String value = "";

      if (root.getNodeType() == Node.ELEMENT_NODE && root.getNodeName().equals("wrapper")) {

        // The <wrapper> element contains serialized XML, which may itself contain <wrapper> elements.
        // Parse it and remove any deeper <wrapper> elements, then store the serialized result.
        Node node = findFirstNonWrapperElement(SomaUtils.getDOM(root.getTextContent()));
        if (node != null) {
          
          key = node.getNodeName();
          value = expungeWrappers(node.getFirstChild());

        } else {
          throw new RuntimeException("A <wrapper> element was found that does not contain a child element (e.g. <wrapper><a/></wrapper>");
        }

      } else {

        // The element may contain plain text or it may contain one or more elements.
        value = serializeXML(root.getFirstChild());

      }

      // Store the information.
      params.set (key, value);
      if (debug) {
        System.out.println("setParam: " + key + "=" + value);
      }

    }
  }


  private Node findFirstNonWrapperElement(Node node) {
    return findFirstNonWrapperElement(node, false);
  }


  private Node findFirstNonWrapperElement(Node node, boolean doSiblings) {
    Node ret = null;
    boolean debug = false;

    if (node != null) {

      if (debug) {
        System.out.println("$$$ node type = " + node.getNodeType() + ", doSibling=" + doSiblings);
      }

      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (node.getNodeName().equals("wrapper")) {

          if (debug) {
            System.out.println("$$$ found <wrapper>, checking first child.");
          }
          ret = findFirstNonWrapperElement(node.getFirstChild(), true); // Check this <wrapper>'s children


        } else {

          if (debug) {
            System.out.println("$$$ found <" + node.getNodeName() + ">, finished");
          }
          ret = node; // This is an element that isn't a <wrapper>

        }
      } else if (node.getNodeType() == Node.DOCUMENT_NODE) {

        ret = findFirstNonWrapperElement(node.getFirstChild(), true); // Check this document's children

      }

      // Check the next sibling.
      if (ret == null && doSiblings) {
        if (debug) {
          if (node.getNextSibling() == null) {
            System.out.println("$$$  recursing - " + serializeXML(node));
            System.out.println("$$$  recursing but the next sibling is null!");
          } else {
            System.out.println("$$$  recursing on some node");
          }
        }
        ret = findFirstNonWrapperElement(node.getNextSibling(), true);
      }
    } else {
      if (debug) {
        System.out.println ("$$$  node = null - simply returning");
      }
    }

    return ret;
  }


  /**
   * Remove any <wrapper> elements anywhere in the set of nodes, returning the node set as text.  All nodes 
   * are kept except for <wrapper> elements. For these their children are promoted into the place of the 
   * <wrapper> element. 
   * 
   * @param list 
   * 
   * @return NodeList 
   */
  private String expungeWrappers (Node node) {
    StringBuffer buf = new StringBuffer();

    if (node != null) {

      if (node.getNodeType() == Node.ELEMENT_NODE) {

        if (node.getNodeName().equals("wrapper")) {

          // Skip this element and emit its children.
          buf.append(expungeWrappers(node.getFirstChild()));

        } else {

          // Emit this element in either a <xxx /> or <xxx> ... </xxx> form.
          if (node.getFirstChild() != null) {

            // Emit the <xxx> ... </xxx> form.
            buf.append("<" + node.getNodeName() + " ");

            NamedNodeMap attrs = ((Element)node).getAttributes();
            for (int k = 0; k < attrs.getLength(); k += 1) {
              Node attr = attrs.item(k);
              buf.append(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\" ");
            }

            buf.append(">");

            // Serialize this node's children.
            buf.append(expungeWrappers(node.getFirstChild()));

            buf.append("</" + node.getNodeName() + ">");

          } else {

            // Emit the <xxx /> form.
            buf.append("<" + node.getNodeName() + " ");

            NamedNodeMap attrs = ((Element)node).getAttributes();
            for (int k = 0; k < attrs.getLength(); k += 1) {
              Node attr = attrs.item(k);
              buf.append(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\" ");
            }

            buf.append("/>");

          }
        }

      } else if (node.getNodeType() == Node.TEXT_NODE) {

        // Serialize some text.
        buf.append(node.getNodeValue());

      } else if (node.getNodeType() == Node.COMMENT_NODE) {

        // Serialize a comment.
        buf.append("<!--" + node.getNodeValue() + "-->");

      } else {

        throw new RuntimeException("Unable to handle node type " + node.getNodeType());

      }

      // Move to the next sibling, if one exists.
      buf.append(expungeWrappers(node.getNextSibling()));
    }

    return buf.toString();
  }


  /**
   * Simple XML-to-text method. 
   * 
   * @param node 
   * 
   * @return String
   */
  private String serializeXML (Node node) {
    StringBuffer buf = new StringBuffer();

    if (node != null) {

      // Emit text for this node, and any attributes and children if it is an element.
      if (node.getNodeType() == Node.ELEMENT_NODE) {

        // Emit this element in either a <xxx /> or <xxx> ... </xxx> form.
        if (node.getFirstChild() != null) {

          // Emit the <xxx> ... </xxx> form.
          buf.append("<" + node.getNodeName() + " ");

          NamedNodeMap attrs = ((Element)node).getAttributes();
          for (int k = 0; k < attrs.getLength(); k += 1) {
            Node attr = attrs.item(k);
            buf.append(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\" ");
          }

          buf.append(">");

          // Serialize this node's children.
          buf.append(serializeXML(node.getFirstChild()));

          buf.append("</" + node.getNodeName() + ">");

        } else {

          // Emit the <xxx /> form.
          buf.append("<" + node.getNodeName() + " ");

          NamedNodeMap attrs = ((Element)node).getAttributes();
          for (int k = 0; k < attrs.getLength(); k += 1) {
            Node attr = attrs.item(k);
            buf.append(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\" ");
          }

          buf.append("/>");

        }

      } else if (node.getNodeType() == Node.TEXT_NODE) {

        // Serialize some text.
        buf.append(node.getNodeValue());

      } else if (node.getNodeType() == Node.COMMENT_NODE) {

        // Serialize a comment.
        buf.append("<!--" + node.getNodeValue() + "-->");

      } else {

        throw new RuntimeException("Unable to handle node type " + node.getNodeType());

      }

      // Take care of any following sibling nodes.
      buf.append(serializeXML(node.getNextSibling()));
    }

    return buf.toString();
  }
}
