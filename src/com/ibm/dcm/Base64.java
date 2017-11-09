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

import java.io.*;


/**
 * This class provides a simple conversion between binary (okay, a byte array) and
 * base 64 (real base 64, not MIME base 64 with line breaks).
 * 
 */
public class Base64 {

  // This array runs from 0 to 63 (surprised?) and is intialized with the values
  // for A-Z, a-z, 0-9, +, and /.
  private final static int[] binaryToBase64 =
  {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 
    'w', 'x', 'y', 'z', '0', '1', '2', '3', 
    '4', '5', '6', '7', '8', '9', '+', '/'
  };

  // This table runs from 0 to 127 and is the complement of the table above.  In other
  // words, if you take a 6 bit binary value (0-63) and index into the table above,
  // you get some character.  If you then take that character and index into this table
  // with it then you will get back the original 6 bit value.
  // A-Z are characters 0x41 - 0x5a representing values 0x00-0x19
  // a-z are characters 0x61-0x7a representing values 0x1A-0x33
  // 0-9 are characters 0x30 - 0x39 representing values 0x34-3D
  // + is character 0x2b representing value 0x3E
  // / is character 0x2f representing value 0x3F
  //
  // = (character 0x3D) is included for error checking.
  //
  private final static int[] base64ToBinary =
  {
    // 0     1     2     3      4     5     6     7       8     9     A     B      C     D     E     F
    0xff, 0xff, 0xff, 0xff,  0xff, 0xff, 0xff, 0xff,   0xff, 0xff, 0xff, 0xff,  0xff, 0xff, 0xff, 0xff, // 0x00 - 0x0f 
    0xff, 0xff, 0xff, 0xff,  0xff, 0xff, 0xff, 0xff,   0xff, 0xff, 0xff, 0xff,  0xff, 0xff, 0xff, 0xff, // 0x10 - 0x1f 
    0xff, 0xff, 0xff, 0xff,  0xff, 0xff, 0xff, 0xff,   0xff, 0xff, 0xff, 0x3e,  0xff, 0xff, 0xff, 0x3f, // 0x20 - 0x2f 
    0x34, 0x35, 0x36, 0x37,  0x38, 0x39, 0x3a, 0x3b,   0x3c, 0x3d, 0xff, 0xff,  0xff, 0x00, 0xff, 0xff, // 0x30 - 0x3f 
    0xff, 0x00, 0x01, 0x02,  0x03, 0x04, 0x05, 0x06,   0x07, 0x08, 0x09, 0x0a,  0x0b, 0x0c, 0x0d, 0x0e, // 0x40 - 0x4f 
    0x0f, 0x10, 0x11, 0x12,  0x13, 0x14, 0x15, 0x16,   0x17, 0x18, 0x19, 0xff,  0xff, 0xff, 0xff, 0xff, // 0x50 - 0x5f 
    0xff, 0x1a, 0x1b, 0x1c,  0x1d, 0x1e, 0x1f, 0x20,   0x21, 0x22, 0x23, 0x24,  0x25, 0x26, 0x27, 0x28, // 0x60 - 0x6f 
    0x29, 0x2a, 0x2b, 0x2c,  0x2d, 0x2e, 0x2f, 0x30,   0x31, 0x32, 0x33, 0xff,  0xff, 0xff, 0xff, 0xff  // 0x70 - 0x7f 
  };


  /**
   * Convert the incoming "binary" information to base 64 in the output buffer.
   * 
   * @param in contains the data to convert
   * @param out is an optional buffer to return the data in.  If it is null or too small then 
   * a new buffer is allocated.
   * @return The buffer containing the data in base 64.
   */
  public static byte[] toBase64(byte[] in, byte[] out) {

    // Ensure the output buffer is of sufficient size.  Allow for padding that
    // will be added to bring the output up to a multiple of 4 bytes.
    // int needed = (((in.length * 4 / 3) + 3) / 4) * 4;
    int needed = (((int)((long)in.length * 4 / 3) + 3) / 4) * 4;
    // System.out.println("### in.length=" + in.length + ", needed=" + needed);
    byte[] result = out;
    if ((result == null) || (needed > result.length))
      result = new byte[needed];

    // Convert the input buffer (in multiples of 3 bytes) to base 64.
    for (int inOffset = 0, outOffset = 0; inOffset < in.length; inOffset += 3, outOffset += 4) {
      // Determine how many bytes of input to convert.
      int count = in.length - inOffset;
      if (count > 3)
        count = 3;

      // We do some bit-fiddling to extract the four sextets from the (up to) three
      // input bytes.  Here are the three bytes (octets) and the mapping to sextets:
      //
      // byte   00000000 11111111 22222222
      // sextet 00000011 11112222 22333333
      //

      // Convert the input bytes, which may require padding.
      switch (count) {
      case 1:
        // One byte becomes two bytes plus two bytes of padding.
        result[outOffset + 0] = (byte)binaryToBase64[(in[inOffset] >> 2) & 0x3F]; 
        result[outOffset + 1] = (byte)binaryToBase64[(in[inOffset] & 0x03) << 4]; 
        result[outOffset + 2] = (byte)'='; 
        result[outOffset + 3] = (byte)'='; 
        break;

      case 2:
        // Two bytes becomes three bytes plus one byte of padding.
        result[outOffset + 0] = (byte)binaryToBase64[(in[inOffset] >> 2) & 0x3F]; 
        result[outOffset + 1] = (byte)binaryToBase64[((in[inOffset] & 0x03) << 4) | ((in[inOffset+1] >> 4) & 0x0F)]; 
        result[outOffset + 2] = (byte)binaryToBase64[(in[inOffset+1] & 0x0F) << 2]; 
        result[outOffset + 3] = (byte)'='; 
        break;

      case 3:
        // Three bytes becomes four bytes in the output.
        result[outOffset + 0] = (byte)binaryToBase64[(in[inOffset] >> 2) & 0x3F]; 
        result[outOffset + 1] = (byte)binaryToBase64[((in[inOffset] & 0x03) << 4) | ((in[inOffset+1] >> 4) & 0x0F)]; 
        result[outOffset + 2] = (byte)binaryToBase64[((in[inOffset+1] & 0x0F) << 2) | ((in[inOffset+2] >> 6) & 0x03)]; 
        result[outOffset + 3] = (byte)binaryToBase64[in[inOffset+2] & 0x3F]; 
        break;

      default:
        break;
      }
    }

    return result;
  }


  /**
   * Convert the incoming base 64 data to "binary" data in the output buffer.
   * 
   * @param in contains the base 64 data to convert
   * @param out is an optional buffer to return the data in.  If it is null or too small then 
   * a new buffer is allocated.
   * @return The buffer containing the binary data.
   */
  public static byte[] fromBase64(byte[] in, byte[] out) {

    // The input buffer must be a multiple of 4 in length.
    if ((in.length % 4) != 0) {
      throw new RuntimeException("Base64 input must be a multiple of 4 characters.");
    }

    // Ensure the output buffer is of sufficient size.  There may be a byte or
    // two of wasted space due to padding at the end of the input.
    int needed = in.length * 3 / 4;
    byte[] result = out;
    if ((result == null) || (needed > result.length))
      result = new byte[needed];

    // We do some bit-fiddling to extract the (up to) three octets from the four
    // input bytes.  Here are the four sextets and the mapping to the three bytes:
    //
    // sextet 000000111111222222333333
    // byte   000000001111111122222222
    //

    // Process the input data, converting it from base 64 back to "binary".
    int originalLength = 0;
    for (int inOffset=0, outOffset=0; inOffset < in.length; inOffset += 4, outOffset += 3) {

      // Check this set of four characters for any that are out of range.
      int sextet0 = base64ToBinary[in[inOffset]];
      int sextet1 = base64ToBinary[in[inOffset + 1]];
      int sextet2 = base64ToBinary[in[inOffset + 2]];
      int sextet3 = base64ToBinary[in[inOffset + 3]];
      if ((sextet0 < 0) || (sextet0 > 255))
        throw new RuntimeException("Non-base64 character encountered at offset " + Integer.toString(inOffset + 0));
      if ((sextet1 < 0) || (sextet1 > 255))
        throw new RuntimeException("Non-base64 character encountered at offset " + Integer.toString(inOffset + 1));
      if ((sextet2 < 0) || (sextet2 > 255))
        throw new RuntimeException("Non-base64 character encountered at offset " + Integer.toString(inOffset + 2));
      if ((sextet3 < 0) || (sextet3 > 255))
        throw new RuntimeException("Non-base64 character encountered at offset " + Integer.toString(inOffset + 3));

      // Convert all four sextets to three output bytes, treating '=' characters as having zero values.
      byte one = (byte)((sextet0 << 2) | (sextet1 >>> 4));
      byte two = (byte)(((sextet1 & 0x0F) << 4) | (sextet2 >>> 2));
      byte three = (byte)(((sextet2 & 0x03) << 6) | sextet3);

      // Detect whether 1, 2, or 3 bytes were originally encoded.
      if (in[inOffset + 3] == '=') {
        // Might have been 1 or 2 bytes originally.  Decide which.
        if (in[inOffset + 2] == '=') {
          // 1 byte was originally encoded.
          originalLength += 1;
          result[outOffset + 0] = one;
        } else {
          // 2 bytes were originally encoded.
          originalLength += 2;
          result[outOffset + 0] = one;
          result[outOffset + 1] = two;
        }
      } else {
        // 3 bytes were originally encoded.
        originalLength += 3;
        result[outOffset + 0] = one;
        result[outOffset + 1] = two;
        result[outOffset + 2] = three;
      }
    }

    // When we have wasted space at the end of the output buffer, then we must
    // reallocate the buffer. The caller expects the length of the buffer to 
    // match the length of the original binary data.
    if (originalLength != result.length) {
      byte[] tmp = new byte[originalLength];
      System.arraycopy(result, 0, tmp, 0, originalLength);
      result = tmp;
    }

    return result;
  }


  /***
   * Convert base64 data to binary and write it to a file.
   * 
   * @param base64Input Base 64 encoded data
   * @throws various exceptions in case of errors
   */
  public static void base64ToBinaryFile (String base64Input, String filename) {
    // Convert the input to a byte array.
    byte[] binary = fromBase64 (base64Input.getBytes(), null);

    // Write it to a file.
    try {
      OutputStream out = new FileOutputStream (filename);
      out.write(binary);
      out.close();
    } catch (Exception e) {
      throw new RuntimeException (e);
    }
  }


  /**
   * Read the "binary" data from the file and convert it to base 64.
   * 
   * @param filename The name of a file to convert to base 64.
   * @return The contents of the file in base 64
   * @throws various exceptions in case of errors
   */
  public static String base64FromBinaryFile (String filename) {

    String ret = "";

    try {
      // Determine how large the buffer will need to be, and allocate it.
      File file = new File (filename);
      byte[] buffer = new byte[(int)file.length()];

      // Read the contents of the file into memory.
      BufferedInputStream input = new BufferedInputStream (new FileInputStream (filename));
      try {
          input.read (buffer);
      }
      finally {
          input.close();
      }
      // Convert from binary to base 64.
      ret = bytesToString (toBase64 (buffer, null));
    } catch (Exception e) {
      throw new RuntimeException (e);
    }

    return ret;
  }


//  /**
//   * Compare two byte[] arrays to determine whether they contain the same data.
//   * 
//   * @param one First buffer
//   * @param two Second buffer
//   * @param complaint A place to return any complaint string when the method returns false.
//   * @return true if the buffers have identical content, false otherwise
//   */
//  private static boolean compareBuffers(byte[] one, byte[] two, StringBuffer complaint) {
//    boolean bSuccess = true;
//    
//    // Compare the lengths of the two buffers.
//    if (one.length != two.length) {
//      bSuccess = false;
//      complaint.delete(0, complaint.length());
//      complaint.append("First buffer is ");
//      complaint.append(one.length);
//      complaint.append(" bytes, second buffer is ");
//      complaint.append(two.length);
//      complaint.append(" bytes.");
//    } else {
//      // Compare the contents of the two buffers.
//      for (int i = 0; i < one.length; i += 1) {
//        if (one[i] != two[i]) {
//          bSuccess = false;
//          complaint.delete(0, complaint.length());
//          complaint.append("At offset ");
//          complaint.append(i);
//          complaint.append(" decimal, one and two differ.");
//          break;
//        }
//      }
//    }
//    
//    return bSuccess;
//  }
//  
//  
//  /**
//   * Test the toBase64() and fromBase64() methods based on a supplied length and
//   * a buffer containing "binary" data to convert to/from base 64.
//   * 
//   * When the pattern data is shorter than the required length it simply repeated
//   * as necessary.
//   * 
//   * @param length Number of bytes to convert between binary and base64.
//   * @param pattern Data to initialize the allocated buffer with.
//   */
//  private static void testPattern(int length, byte[] pattern) {
//    StringBuffer complaint = new StringBuffer();
//    
//    // Create a buffer of the desired length and populate it with the supplied data pattern.
//    byte[] input = new byte[length];
//    int offset = 0;
//    while (offset < length) {
//      int remaining = length - offset;
//      if (remaining > pattern.length)
//        remaining = pattern.length;
//      System.arraycopy(pattern, 0, input, offset, remaining);
//      offset += remaining;
//    }
//
//    // Convert the data to and from base 64.
//    byte[] result1 = toBase64(input, null);
//    byte[] result2 = fromBase64(result1, null);
//    if (compareBuffers(input, result2, complaint) == false) {
//      // The conversion failed in some fashion.  Complain.
//      System.out.println ("10 : Failed for length " + Integer.toString(length) + " : " + complaint.toString());
//      System.exit(10);
//    }
//  }


  /**
   * Simple method to convert a byte array containing base 64 text to a String.
   * 
   * @param ary Byte array containing base 64 text
   * @return A string containing the same text.
   */
  public static String bytesToString (byte[] ary) {
    StringBuffer ret = new StringBuffer (ary.length);

    for (int i = 0; i < ary.length; i += 1) {
      ret.append((char)ary[i]);
    }

    return ret.toString();
  }


//  public static void main(String[] args) {
//    StringBuffer complaint = new StringBuffer();
//    
//    // Test the compareBuffers method for correct operation.
//    byte[] input = new byte[1];
//    byte[] prealloc = new byte[2];
//    if (compareBuffers(input, prealloc, complaint) == false) {
//      // Correctly detected a length difference.
//      System.out.println ("01 : Passed : " + complaint.toString());
//    } else {
//      // Failed to detect a length difference.
//      System.out.println ("01 : Failed");
//      System.exit (1);
//    }
//    input = new byte[] { 0x01, 0x02 };
//    byte result[] = new byte[] { 0x01, 0x03 };
//    if (compareBuffers(input, result, complaint) == false) {
//      // Correctly detected a difference in the content.
//      System.out.println ("02 : Passed : " + complaint.toString());
//    } else {
//      // Failed to detect a difference in content.
//      System.out.println ("02 : Failed");
//      System.exit(2);
//    }
//    if (compareBuffers(input, input, complaint)) {
//      // Correctly detected equality of the two buffers.
//      System.out.println ("03 : Passed");
//    } else {
//      // Incorrectly detected a difference.
//      System.out.println ("03 : Failed : " + complaint.toString());
//      System.exit(3);
//    }
//    
//    // Test the zero-length case.
//    input = new byte[0];
//    prealloc = new byte[0];
//    result = toBase64(input, prealloc);
//    if (compareBuffers(input, prealloc, complaint) == false) {
//      // Incorrectly detected a difference.
//      System.out.println ("04 : Failed : " + complaint.toString());
//      System.exit(4);
//    }
//    
//    // The first test pattern is a sequence from 0 to 255.
//    byte[] pattern1 = new byte[256];
//    for (int k = 0; k < pattern1.length; k += 1) {
//      pattern1[k] = (byte)k;
//    }
//    
//    // The second test pattern is all zeroes.
//    byte[] pattern2 = new byte[100];
//    for (int k = 0; k < pattern2.length; k += 1) {
//      pattern2[k] = 0;
//    }
//    
//    // The third test pattern is all 0x00FE.
//    byte[] pattern3 = new byte[100];
//    for (int k = 0; k < pattern3.length; k += 1) {
//      pattern3[k] = (byte)0xFE;
//    }
//    
//    // The fourth test pattern is all 0x00FF.
//    byte[] pattern4 = new byte[100];
//    for (int k = 0; k < pattern4.length; k += 1) {
//      pattern4[k] = (byte)0xFF;
//    }
//    
//    // The fifth test pattern is all 0xFF down to 0x00.
//    byte[] pattern5 = new byte[100];
//    for (int k = 0; k < pattern5.length; k += 1) {
//      pattern5[k] = (byte)(-1 - k);
//    }
//    
//    // Test several lengths of buffers for pattern 1.
//    for (int i = 0; i < 100; i += 1) {
//      testPattern(i, pattern1);
//      System.out.println ("10 : Passed for length " + Integer.toString(i));
//    }
//    
//    // Test several lengths of buffers for pattern 2.
//    for (int i = 0; i < 100; i += 1) {
//      testPattern(i, pattern2);
//      System.out.println ("11 : Passed for length " + Integer.toString(i));
//    }
//    
//    // Test several lengths of buffers for pattern 3.
//    for (int i = 0; i < 100; i += 1) {
//      testPattern(i, pattern3);
//      System.out.println ("12 : Passed for length " + Integer.toString(i));
//    }
//    
//    // Test several lengths of buffers for pattern 4.
//    for (int i = 0; i < 100; i += 1) {
//      testPattern(i, pattern4);
//      System.out.println ("13 : Passed for length " + Integer.toString(i));
//    }
//    
//    // Test several lengths of buffers for pattern 5.
//    for (int i = 0; i < 100; i += 1) {
//      testPattern(i, pattern5);
//      System.out.println ("14 : Passed for length " + Integer.toString(i));
//    }
//    
//    // Try a longer length.
//    testPattern(100*1024, pattern1);
//    System.out.println("15 : Passed");
//    
//    // Write pattern 5 to a file, read it back in, and compare the result.
//    String pattern5base64 = bytesToString (toBase64 (pattern5, null));
//    base64ToBinaryFile (pattern5base64, "pattern5base64");
//    String base64Result = base64FromBinaryFile ("pattern5base64");
//    if (compareBuffers(base64Result.getBytes(), pattern5base64.getBytes(), complaint)) {
//      System.out.println ("20 : Passed");
//    } else {
//      System.out.println ("20 : Failed : " + complaint.toString());
//      System.out.println ("original : " + pattern5base64);
//      System.out.println ("mine     : " + base64Result);
//      System.exit(20);
//    }
//  } 
}
