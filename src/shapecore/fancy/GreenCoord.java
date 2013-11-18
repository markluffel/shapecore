package shapecore.fancy;

import static shapecore.Geometry.*;

import java.util.Collections;
import java.util.List;

import shapecore.pt;
import shapecore.vec;

/**
 * Requires that the cage wind clockwise (in screen coordinates, y-downward).
 *
 * Should check input cage before binding
 * if(signedArea(cage) > 0) {
 *    Collections.reverse(cage);
 * }
 * 
 */
public class GreenCoord {
  public float[] phi,psi;
  public float[] edgeLengths;
  
  public GreenCoord(int size) {
    phi = new float[size];
    psi = new float[size];
    edgeLengths = new float[size];
  }
  
  public GreenCoord(pt eta, List<pt> cage) {
    this(cage.size());
    bind(eta, cage);
  }
  
  public void bind(pt eta, List<pt> cage) {
    int size = cage.size();
    int pj = size-1;
    for(int j = 0; j < size; pj = j, j++) {
      pt vj1 = cage.get(pj);
      pt vj2 = cage.get(j);
      vec a = vj1.to(vj2);
      vec b = eta.to(vj1);
      double Q = a.dot(a);
      double S = b.dot(b);
      double R = a.dot(b)*2;
      
      // in the paper they normalize this and then multiply by its length ???
      double BA = b.perpDot(a);
      double SRT = Math.sqrt(4*S*Q - R*R);
      double L0 = Math.log(S);
      double L1 = Math.log(S+Q+R);
      double A0 = Math.atan2(R, SRT) / SRT;
      double A1 = Math.atan2(2*Q+R, SRT) / SRT;
      
      double A10 = A1-A0;
      double L10 = L1-L0;
      
      double edgeLength = Math.sqrt(Q); 
      psi[j] = (float)((-edgeLength / (4*Math.PI)) * ( (4*S - R*R/Q) * A10 + R/(2*Q) * L10 + L1 - 2));
      
      phi[pj] += (BA/(2*Math.PI))*(L10/(2*Q) - A10*(2 + R/Q));
      phi[j]  -= (BA/(2*Math.PI))*(L10/(2*Q) - A10*(R/Q));
      
      edgeLengths[j] = (float)edgeLength;
    }
  }
  
  public pt deform(List<pt> newCage) {
    if(newCage.size() != phi.length || newCage.size() != psi.length) {
      throw new IllegalArgumentException(); // need to have identical size cage
    }
    pt result = new pt();
    int pj = psi.length-1;
    for(int j = 0; j < psi.length; pj = j, j++) {
      vec v = newCage.get(pj).to(newCage.get(j));
      v.turnRight();
      result.add(psi[j]/(edgeLengths[j]), v);
      result.addScaledPt(phi[j], newCage.get(j));
    }
    return result;
  }
}