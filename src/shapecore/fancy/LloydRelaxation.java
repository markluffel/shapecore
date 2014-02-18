package shapecore.fancy;

import java.util.ArrayList;
import java.util.List;

import shapecore.Oplet;
import shapecore.Voronoi;
import shapecore.pt;

import megamu.mesh.InteriorTest;


public class LloydRelaxation {
  
  public List<pt> pts;
  int numMovable;
  InteriorTest pip;
  
  public LloydRelaxation(List<pt> movable) {
    this(movable, null, null);
  }
  
  public LloydRelaxation(List<pt> movable, List<pt> unmovable, InteriorTest pip) {
    pts = new ArrayList<pt>(movable);
    if(unmovable != null) pts.addAll(unmovable);
    numMovable = movable.size();
    this.pip = pip;
  }
  
  public Voronoi step() {
    try {
      Voronoi vor = new Voronoi(pts);
    
      List<pt> newPts = new ArrayList<pt>(pts.size());
      for(int i = 0; i < pts.size(); i++) {
        if(movable(pts.get(i), i)) {
          List<pt> region = vor.region(i);
          if(!region.isEmpty()) {
            pt centroid = centroid(region);
            if(pip == null || pip.contains(centroid.x, centroid.y)) {
              newPts.add(centroid);
              continue;
            }
          }
        }
        // if the point is successfully moved and added above it will skip this add
        newPts.add(pts.get(i));
      }
      pts = newPts;
      
      return vor;
      // it seems that repeated iteration can cause this part to break quickly
    } catch(NullPointerException e) {
      e.printStackTrace();
      return null;
    } catch(ArrayIndexOutOfBoundsException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public boolean movable(pt p, int i) {
    return i < numMovable;
  }
  
  public pt centroid(List<pt> region) {
    return Oplet.centroidOfPolygon(region);
  }
}
