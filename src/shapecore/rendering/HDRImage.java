package shapecore.rendering;

import static processing.core.PApplet.*;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HDRImage {

  float[] r,g,b;
  
  HDRImage(int width, int height, float[] r, float[] g, float[] b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }
  
  public static HDRImage load(File file) {
    byte[] data = loadBytes(file);
    byte[] header = "#?RADIANCE\n".getBytes();
    if(!prefixMatch(data,header)) return null;
    
    // TODO: verify FORMAT and EXPOSURE
    // data starts after two newlines
    int i = 1;
    while(data[i-1] != '\n' && data[i] != '\n') i++;
    
    int j = i;
    while(data[j] != '\n') j++;
    byte[] sizeLine = new byte[j-i];
    System.arraycopy(data, i, sizeLine, 0, sizeLine.length);
    String size = new String(sizeLine);
    Matcher my = Pattern.compile("Y\\s([0-9]+)").matcher(size);
    my.find();
    int y = Integer.parseInt(my.group(1));
    Matcher mx = Pattern.compile("X\\s([0-9]+)").matcher(size);
    mx.find();
    int x = Integer.parseInt(my.group(1));
    
    i = j+1;
    
    HDRImage img = new HDRImage(x,y,null,null,null);
    return img;
  }
  
  static boolean prefixMatch(byte[] a, byte[] b) {
    int len = min(a.length, b.length);
    for(int i = 0; i < len; i++) {
      if(a[i] != b[i]) return false;
    }
    return true;
  }
  
  public static float bytesToFloat(byte[] data, int i) {
    return Float.intBitsToFloat((data[i] << 24) | (data[i] << 16) | (data[i] << 8) | (data[i] & 0xff << 16));
  }
}
