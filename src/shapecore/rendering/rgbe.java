package shapecore.rendering;

/**
 * Copyright (C) 2007 Yi Chun Huang
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * http://www.gnu.org/licenses/gpl.html
 */

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import shapecore.vec3;


import static shapecore.Geometry.*;
import static shapecore.Oplet.*;

/**
 * Class to read Radiance rgbe (.pic) files
 * <br>Information <a href="http://www.andrew.cmu.edu/user/yihuang/radiance_pic/
 * rgbe.html">here</a>, official Radiance .pic format documentation 
 * <a href="http://radsite.lbl.gov/radiance/refer/filefmts.pdf#page=28">here</a>.
 * <p>This version only parses  New runlength encoded format 
 * (FORMAT=32-bit_rle_rgbe)</p>
 * <p>Header information -
 * <br>COLORCORR, PIXASPECT & PRIMARIES not implemented in readHeader() yet</p>
 * @author Yi Chun Huang yihuang@andrew.cmu.edu
 * @version 0.1 March 02, 2007
 */
public class rgbe{
  
  /**
   * Class to facilitate parsing of signed and unsigned bytes from 
   * a .pic formatted file
   */
  class RgbeFileReader {
      private InputStream stream;
      private File file;
      
      public RgbeFileReader (String filename) throws FileNotFoundException {
        file = new File(filename);
        stream = new BufferedInputStream(new FileInputStream(file));
      }
      
      /**
       * Reads an signed byte
       * @return signed btye (-128 to 127)
       * @throws EOFException
       * @throws IOException
       */
      public byte readByte() throws EOFException, IOException {
          int next = stream.read();
          if (next == -1) {
              throw new EOFException();
          } else {
              return (byte)next;
          }
      }
      
      /**
       * Reads a signed byte, where 1st bit is sign bit and following 7 bits is 
       * magnitude (as opposed to two's complement convention)
       * @return signed btye (-128 to 127)
       * @throws EOFException
       * @throws IOException
       */
      public byte readSignedByte() throws EOFException, IOException {
          int next = stream.read();
          if (next == -1) {
              throw new EOFException();
          } else {
              byte conv = (byte)next;
              if(conv>=0) return conv;
              else{
                if(conv>-128)return (byte)-(conv+128);
                else return conv;
              }
                
          }
      }
      
      /**
       * Reads an unsigned byte 
       * @return unsigned byte (0 to 255)
       * @throws EOFException
       * @throws IOException
       */
      public int readUnsignedByte() throws EOFException, IOException {
          int next = stream.read();
          if (next == -1) {
              throw new EOFException();
          } else {
              if((byte)next>=0) return (byte)next;
              else return (byte)next+256;               
          }
      }
      
      /**
       * Reads a btye and converts it to HEX representation
       * @return String denoting the hex
       * @throws EOFException
       * @throws IOException
       */
      public String readHex()throws EOFException, IOException {
        int next = stream.read();
          if (next == -1) {
                throw new EOFException();
            } else {
                if((byte)next>=0) return Integer.toHexString((byte)next);
                else return Integer.toHexString((byte)next+256);                
            }
      }
      
      /**
       * Returns string on a line as delimited by HEX 0A (Ascii LF)
       * <br> Returns string of length 0 if line is empty
       * @throws EOFException
       * @throws IOException
       */
      public String readLine() throws EOFException, IOException{
        int next = stream.read();
        if (next == -1) {
              throw new EOFException();
          }else if(next == 10){
            return new String();  //empty line
          }else{          
            LinkedList<Integer> buffer = new LinkedList<Integer>();
            do{ 
              buffer.add(next);
              next = stream.read();
              }while(next!=10); //DEC 10 aka HEX 0A is Line Feed
            
            //put all characters read as byte[]
            byte[] buffer2 = new byte[buffer.size()];
            for(int i=0; i<buffer.size(); i++){
              buffer2[i]=(byte)(int)buffer.get(i);
            }
            
            //default encoding will convert to text
            return new String(buffer2);
          }
      }
      
      public void close(){
        try{stream.close();}
        catch(Exception e){
          System.out.println(e);
        }
      }
  }
  
  String filePath;
  /**
   * Header information, http://radsite.lbl.gov/radiance/refer/filefmts.pdf
   * #page=28
   */
  String commands;  //commands used to generate pic, documented in header
  String format;
  String view;
  float exposure;
  String software;

  int height, width;
  float[] imageData;
  
  
  /**
   * Constructor, defaults attributes
   * <li>FORMAT=32-bit_rle_rgbe</li>
   * <li>EXPOSURE=1.0</li>
   */
  public rgbe() {
    format = "32-bit_rle_rgbe";
    exposure = 1.0f;
  }
  
  public static rgbe load(String path) {
    rgbe img = new rgbe();
    img._load(path);
    return img;
  }
  /**
   * Parses a Radiance rgbe (.pic) file
   * @param path Absolute filepath, eg. "D:/Test/Trial.pic" 
   * @return 1 if ok, -1 if error
   */
  rgbe _load(String path) {
    try {
      RgbeFileReader data = new RgbeFileReader(path);
        
      //parse the header, it is terminated by an empty line
      readHeader(data);
      
      //track image resolution and orientation
      readResolution(data);

      //parse data according to appropriate encoding
      if(format.compareTo("32-bit_rle_rgbe") == 0) {
        // here's the heavy part
        readData_RLE(data);
      } else {
        System.out.println("rgbe.getFile() - encoding not supported close stream");
        data.close();
        return null;
      }
      
      data.close();
        
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }

    filePath = path;
    return this;
  }

  /**
   * Parse header and populates class attributes, overriding defaults 
   * set by constructor
   */
  private void readHeader(RgbeFileReader data) throws Exception{
    //System.out.println("rgbe.readheader() - Start");
    String lineIn;
    
    //first line must be #?Radiance
    lineIn = data.readLine();
    if(lineIn==null)
      throw new DataFormatException("File is empty");
    if(lineIn.compareTo("#?RADIANCE")!=0) 
      throw new DataFormatException("File is not Radiance Picture");
    
    //following lines contain commands used to generate .pic
    //may be interspersed with variable assignments (see filefmts.pdf)
    while(lineIn.length()!=0){
      lineIn = data.readLine();
      if(lineIn==null)
        throw new DataFormatException("File incomplete");
      
      //System.out.println(lineIn);
      if(lineIn.startsWith("FORMAT=")){
        format=lineIn.substring(7).trim();
        //System.out.println("FORMAT >"+format+"<");
      }else if(lineIn.startsWith("EXPOSURE=")){
        exposure=new Float(lineIn.substring(9).trim());
        //System.out.println("EXPOSURE >"+exposure+"<");
      }else if(lineIn.startsWith("SOFTWARE=")){
        software=lineIn.substring(9).trim();
        //System.out.println("SOFTWARE >"+software+"<");
      }else if(lineIn.startsWith("VIEW=")){
        view=lineIn.substring(5).trim();
        //System.out.println("VIEW >"+view+"<");
      }else if(commands!=null){
        commands=commands.concat("\n");
        commands=commands.concat(lineIn);
      }else{
        commands=lineIn;
      }
    }
  }
  
  /**
   * Reads the resolution, MUST be called after readHeader()
   */
  private void readResolution(RgbeFileReader data) throws Exception{
    //System.out.println("rgbe.readResolution() - Start");
    
    //To Implement, track image orientation
    String lineIn = data.readLine();
    if(lineIn==null)
      throw new DataFormatException("File is empty");
    StringTokenizer st = new StringTokenizer(lineIn);
    
    //Line must have 4 entries, Y then X
    if(st.countTokens()!=4)
      throw new DataFormatException("Invaild entries on resolution line");
    if(st.nextToken().compareTo("-Y")==0){
      height=new Integer(st.nextToken());
      if(st.nextToken().compareTo("+X")==0){
        width=new Integer(st.nextToken());
        }else{
          //x fliped
          throw new DataFormatException("Flipped data not supported yet");
        }
      }else{
        //y fliped
        throw new DataFormatException("Flipped data not supported yet");
      }
    //System.out.println("rgbe.readResolution() - height:"+height+" width:"+width);
  }
  
  /**
   * Reads data in Run-Length Encoded format.
   * MUST be called after readHeader() & readResolution() 
   * Apparently each scanline encodes r,g,b&e seperately
   */
  private void readData_RLE(RgbeFileReader data) throws Exception{

    //holds rgbe arranged data
    int[] buffer = new int[width*height*4]; 
    
    //1. RLE scanline length must be between 8 and 0x7fff (32767)
    if ( width<8 || width>0x7fff ){
        //not allowed so read flat
      System.out.println("rgbe.readData_RLE() - run length encoding is not allowed");
      return;
        //return RGBE_ReadPixels(fp,data,scanline_width*num_scanlines);
    }
    
    /** 
     * PROCESS SCANLINE LOOP
     * There must be *height* number of scanlines
     */
    int lineNum=0; // number of scanline being processed
    for(int k=0; k<height; k++){

      //reads the 1st 4 bytes
      byte pos0 = data.readByte();
      byte pos1 = data.readByte();
      byte pos2 = data.readByte();
      byte pos3 = data.readByte();
      //System.out.println("rgbe.readData_RLE() 1st 4 bytes:"+pos0+" "+pos1+" "+pos2+" "+pos3);
      
      //2. RLE begins with 2 bytes equal to 2
      //3. Since 3rd btye is upper byte of scanline length, it must be >0 (signed)
      if(pos0!=2 || pos1!=2 || pos2<0){
        System.out.println("rgbe.readData_RLE() process scanline loop, line"+lineNum);
        throw new DataFormatException("Data not RLE");
      }
      
      //4. 3rd byte and 4th byte equals scanline length
      if( ((pos2 & 0xff) << 8 |(pos3 & 0xff)) != width){
        System.out.println("rgbe.readData_RLE() process scanline loop, line"+lineNum);
        throw new DataFormatException("Scanline length encoding error");
      }

      //process 4 times scanline width for the 4 rgbe components
      for(int j=0; j<4; j++){
        int cursor=0; //position in scanline
        
        while(cursor<width){
          byte ind = data.readSignedByte();
          int value;
          /**
           * -128 is a dump of 128 values, 
           * but byte cannot have val 128, so 0 is used as a marker
           * since not used (cannot run or dump 0 values)
           */
          if(ind==-128){
            ind = 0;
          }
          if(ind<0){
            // run of abs ind
            value = data.readUnsignedByte();
            for(int i=ind; i<0; i++){
              buffer[lineNum*width*4+cursor*4+j]=value;
              cursor++;
            }
          }else{
            // dump of ind
            //special case ind=-128, changed to marker
            if(ind==0){
              value = data.readUnsignedByte();
              buffer[lineNum*width*4+cursor*4+j]=value;
              cursor++;
              ind=127;
            }
            for(int i=0; i<ind; i++){
              value = data.readUnsignedByte();
              buffer[lineNum*width*4+cursor*4+j]=value;
              cursor++;
            }
          }
        }
        if(cursor!=width){
          System.out.println("rgbe.readData_RLE() process scanline loop, Line:"+lineNum+" Component:"+j);
          throw new DataFormatException("RLE data error");
        }
      }// END 4 times loop for 4 rgbe components
      lineNum++;
    }// END PROCESS SCANLINE LOOP
    
    if(lineNum!=height){
      System.out.println("rgbe.readData_RLE() Not enough scanlines");
      throw new DataFormatException("RLE data error");
    }
    
    //prep soultion buffer
    imageData = new float[width*height*3];
    
    //convert rgbe to floats
    float r,g,b,f;
    int e;
    for(int i=0; i<height; i++){
      for(int j=0; j<width; j++){
        e = buffer[i*width*4+j*4+3];
        if(e==0){
          r=g=b=0;
        }else{
          f = (float)Math.pow(2, (e-128));
          r = ( buffer[i*width*4+j*4]+0.5f )* f /(256.0f*exposure);
          g = ( buffer[i*width*4+j*4+1]+0.5f )* f /(256.0f*exposure);
          b = ( buffer[i*width*4+j*4+2]+0.5f )* f /(256.0f*exposure);
          
          //correct by exposure?
          imageData[i*width*3+j*3]=r;
          imageData[i*width*3+j*3+1]=g;
          imageData[i*width*3+j*3+2]=b;
        }
      }
    }
    
    r=imageData[0];g=imageData[1];b=imageData[2];
  }
  
  /**
   * Gets width of the encoded .pic image
   * <br/>MUST be called after readHeader() 
   * @return int width
   */
  public int getWidth(){
    if(width==0)throw new IllegalStateException("rgbe object no data yet");
    return width;
  }
  
  /**
   * Gets height of the encoded .pic image
   * <br/>MUST be called after readHeader() 
   * @return int Height
   */
  public int getHeight(){
    if(height==0) throw new IllegalStateException("rgbe object no data yet");
    return height;
  }
  
  /**
   * Gets the image data after it has been processed.
   * <br>Pixel by pixel listing of r,g,b spectral radiance values in floats
   * <br>Pixels listed in scanline order, top to bottom, left to right
   * <br>data[0] - Pixel 1, R component
   * <br>data[1] - Pixel 1, R component
   * <br>data[2] - Pixel 1, R component
   * <br>data[3-5] - Pixel 2, data[6-8] - Pixel 3 ...
   * <br/>MUST be called after readData_RLE()
   * @return float[]
   */
  public float[] getImageData(){
    if(imageData==null)throw new IllegalStateException("rgbe object no data yet");
    return imageData;
  }
  
  public float[] getPixel(int x, int y) {
    while(x < 0) x += getWidth();
    while(y < 0) y += getHeight();
    float[] data = getImageData();
    float[] pixel = new float[3];
    int location = (x+y*getWidth())*3;
    if(location+3 >= data.length) return pixel; // black outside image
    try {
      System.arraycopy(data, location, pixel, 0, 3);
    } catch(ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    return pixel;
  }
  
  /**
   * Gets the commands used to generate the .pic file as documented in header.
   * <br/>Lines of commands in the header concatenated into a single string, 
   * with "\n" between commands 
   */
  public String getCommands(){
    return commands;
  }
  
  /**
   * Gets the absolute path of the parsed .pic file
   * @return String path
   */
  public String getFilePath(){
    if(filePath==null)throw new IllegalStateException("no file parsed yet");
    return filePath;
  }

  public float[] bilinearSphereLookup(vec3 dir) {
    if(imageData == null) throw new IllegalStateException("rgbe object no data yet");
    float r = acos(dir.z)/(PI*sqrt(sq(dir.x)+sq(dir.y)));
    float u = dir.x*r;
    float v = dir.y*r;
    float x = (getWidth()*(1+u)/2);
    float y = (getHeight()*(1-v)/2); 
    float[] c1 = getPixel((int)x,(int)y);
    float[] c2 = getPixel(((int)x+1)%width,(int)y);
    float[] c3 = getPixel((int)x,((int)y+1)%height);
    float[] c4 = getPixel(((int)x+1)%width,((int)y+1)%height);
    float xn = x-floor(x);
    float yn = y-floor(y);
    float[] color = new float[3];
    color[0] = lerp(lerp(c1[0],c2[0],xn), lerp(c3[0],c4[0],xn), yn);
    color[1] = lerp(lerp(c1[1],c2[1],xn), lerp(c3[1],c4[1],xn), yn);
    color[2] = lerp(lerp(c1[2],c2[2],xn), lerp(c3[2],c4[2],xn), yn);
    return color;
  }
}