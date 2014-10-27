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


package com.ibm;

import java.text.ParsePosition;

import org.apache.tools.ant.property.ParseNextProperty;
import org.apache.tools.ant.property.PropertyExpander;


/**
 *
 * This class implements ANT's PropertyExpander interface in order to support nested 
 * property names.  The parsePropertyName() method is the one of primary interest, 
 * the others are merely obligatory methods due to the derivation of all objects from
 * the Object class.
 * 
 * At this point, parsePropertyName() is called from ParseProperties.containsProperties()
 * and ParseProperties.parseNextProperty().
 * 
 * Simple example: ${q} is '17' so ${abc_${q}} references an ANT property named ${abc_17}.
 */

public class NestedPropertyExpander implements PropertyExpander {

  /**
   * 
   * This method is straightforward.  The ParsePosition may point to a '${' prefix, and
   * if it does then we scan to the next '}' to pick out the name.  If, in the course
   * of that scan, another '${' is found then we recurse through ParseProperties.parseNextProperty().
   * 
   * Return either a String with a bare ANT property name (e.g. value is '${abc} so 
   * return 'abc'}, or null.
   * 
   */
  public String parsePropertyName(String value, ParsePosition pos, ParseNextProperty pnp) {

    String ret = null;
    int retPos = pos.getIndex(); 

    // Check whether we are looking at '${' and behave appropriately.
    int firstChr = pos.getIndex();
    int remainingLength = value.length() - firstChr;
    // System.out.println("parsePropertyName: remainingLength=" + remainingLength + "(" + value.length() + " - " + firstChr + ")");
    if (remainingLength >= "${x}".length()) {
      if ((value.charAt(firstChr) == '$') && (value.charAt(firstChr + 1) == '{')) {

        // Okay, we're off and running.  Skip the prefix and gather the property name.
        pos.setIndex(firstChr + "${".length());
        // System.out.println("parsePropertyName: beginning scan of " + value.substring(pos.getIndex()));

        // Scan in chunks (peeled off by ParseProperties.parseNextProperty()) until we reach
        // the end of this property name, or we fall off the end of value.
        StringBuffer buf = new StringBuffer();
        for (int chr = pos.getIndex(); chr < value.length(); chr = pos.getIndex()) {

          if (value.charAt(chr) == '}') {
            // Found the terminator for the property name.
            // System.out.println("parsePropertyName: found " + ret);
            retPos = chr + 1; // Resume parsing at the character following the '}'.
            ret = buf.toString();
            break;
          }

          // Ask someone else to parse out this chunk.
          Object next = pnp.parseNextProperty(value, pos);
          if (next != null) {

            // They found a nested property name and returned its value, which is now part of this property name.
            buf.append(next);

          } else {

            // Found nothing, so add this character to the property name and keep scanning.
            buf.append(value.charAt(pos.getIndex()));
            pos.setIndex(pos.getIndex() + 1);
          }

        }
      }
    }

    // System.out.println("parsePropertyName: returning (pos=" + pos.getIndex() + ") " + ret);

    pos.setIndex(retPos);
    return ret;
  }


}
