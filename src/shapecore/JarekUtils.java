package shapecore;

import processing.core.PApplet;

public class JarekUtils extends PApplet {

  
  public static int red, yellow, green, cyan, blue, magenta, dred, dyellow, dgreen, dcyan,
      dblue, dmagenta, white, black, orange, grey, metal; // declares color
                                                          // names

  colorServer cS = new colorServer(4);

  public void createColors() {
    colorMode(HSB, 121);
    red = color(0, 120, 120);
    yellow = color(20, 120, 120);
    green = color(40, 120, 120);
    cyan = color(60, 120, 120);
    blue = color(80, 120, 120);
    magenta = color(100, 120, 120);
    dred = color(0, 120, 60);
    dyellow = color(20, 120, 60);
    dgreen = color(40, 120, 60);
    dcyan = color(60, 120, 60);
    dblue = color(80, 120, 60);
    dmagenta = color(100, 120, 60);
    white = color(0, 0, 120);
    black = color(0, 120, 0);
    grey = color(0, 0, 60);
    orange = color(10, 100, 120);
    metal = color(70, 60, 100);
  }

  int ramp(int c, int m) {
    float f = (float) (255. * c / m);
    return color(f, 255 - f, 255 - f / 2);
  }

  class colorServer {
    int c = 0;
    int nc = 10;

    colorServer() {
    }

    colorServer(int n) {
      nc = n;
    }

    void reset(int n) {
      nc = n;
      c = nc - 1;
    }

    void first() {
      c = nc - 1;
    }

    int next() {
      c = (c + 1) % nc;
      return color(121 * c / nc, 121, 121);
    }
  }

  int pic = 0; // picture number for saving movies

  void picture() {
    String S = "data/startCurve/f" + Format0(pic++, 3) + ".tif";
    saveFrame(S);
  }

  int fn = 0; // number of the file last read

  String Format0(int v, int n) {
    String s = str(v);
    String spaces = "00000000000000000000000000";
    int L = max(0, n - s.length());
    String front = spaces.substring(0, L);
    return (front + s);
  }


}
