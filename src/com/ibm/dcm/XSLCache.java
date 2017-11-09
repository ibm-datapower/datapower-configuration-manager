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

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.Project;


/**
 * XSLCache caches stylesheets (specifically javax.xml.transform.Transformer objects).
 */
public class XSLCache {

  private Project antProject = null;
  private TransformerFactory xformFactory = null;
  private Map<String, Transformer> cache = new HashMap<String, Transformer>();

  private static XSLCache singleton = null;

  private static XSLCache getCache(Project project) throws Exception {
    if (singleton == null) {
      singleton = new XSLCache(project);
    }
    return singleton;
  }

  private XSLCache (Project project) {
    xformFactory = new org.apache.xalan.processor.TransformerFactoryImpl();;
    antProject = project;
  }


  /**
   * Return a Transformer object for the specified stylesheet.  Note that once a stylesheet is cached it is never checked 
   * to see whether it is stale.
   * 
   * @param xslFilename Path to a stylesheet.
   * 
   * @return Transformer 
   */
  public static Transformer getTransformer (Project project, String xslFilename) throws Exception {
    XSLCache xslcache = getCache(project);
    Transformer ret = xslcache.cache.get (xslFilename);
    if (ret == null) {

      String path = xslcache.antProject.resolveFile(xslFilename).getParent();
      TransformerFactory transFactory = xslcache.xformFactory;
      transFactory.setURIResolver(xslcache.new DCMResolver(path, xslcache.antProject));
      ret = transFactory.newTransformer(new StreamSource(new FileInputStream(xslFilename)));
      xslcache.cache.put (xslFilename, ret);
      // System.out.println("%%% Returning " + xslFilename + " for the first time.");

    } else {
      // System.out.println("%%% Returning cached " + xslFilename);
    }
    return ret;
  }


  /**
   * I was surprised by the need to add a URIResolver, and it is a bit of a hack in my opinion. 
   * Why doesn't the base implementation simply work as expected? 
   */
  private class DCMResolver implements URIResolver {

    private Project antProject = null;
    private String originalPath = "";
    private String basePath = "";
    private String dcmPath = "";
    private String workPath = "";
    private DocumentBuilderFactory dbFactory = null;

    public DCMResolver (String path, Project project) throws Exception {
      originalPath = path;
      antProject = project;
      basePath = antProject.getBaseDir().getPath();
      dcmPath = antProject.getProperty("dcm.dir");
      workPath = antProject.getProperty("work.dir");
      dbFactory = DocumentBuilderFactory.newInstance();
      dbFactory.setNamespaceAware (true);
    }

    public Source resolve (String href, String base) throws TransformerException {
      try {
        String fullFilename = href;
        File test = new File(href);
        if (test.isAbsolute() == false) {
          if (new File(originalPath + "/" + href).exists()) {
            fullFilename = originalPath + "/" + href;
          } else if (new File(basePath + "/" + href).exists()) {
            fullFilename = basePath + "/" + href;
          } else if ((dcmPath != null) && new File(dcmPath + "/" + href).exists()) {
            fullFilename = dcmPath + "/" + href;
          } else if ((workPath != null) && new File(workPath + "/" + href).exists()) {
            fullFilename = workPath + "/" + href;
          } else {
            System.out.println ("DCMResolver originalPath=" + originalPath);
            System.out.println ("DCMResolver basePath=" + basePath);
            System.out.println ("DCMResolver dcmPath=" + dcmPath);
            System.out.println ("DCMResolver workPath=" + workPath);
            throw new TransformerException("Can't locate " + href);
          }
        }

        // System.out.println ("   --- MyResolver.resolve href=\"" + href + "\", base=\"" + base + "\", full=" + fullFilename);

        return new DOMSource (dbFactory.newDocumentBuilder().parse(new File(fullFilename)));

      } catch (Exception e) {
        throw new TransformerException (e);
      }
    }
  }


}
