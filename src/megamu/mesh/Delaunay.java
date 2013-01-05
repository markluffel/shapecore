package megamu.mesh;

import java.util.ArrayList;

import quickhull3d.QuickHull3D;

public class Delaunay {

	float[][] edges;
	LinkedArray mesh;
	int[][] links;
	int[][] faces;
	int linkCount;

	public Delaunay( float[][] points ){

		if( points.length < 1 ){
			edges = new float[0][4];
			mesh = new LinkedArray(0);
			links = new int[0][2];
			linkCount = 0;
			return;
		}

		// build points array for qhull
		double qPoints[] = preprocess(points);

		// prepare quickhull
		QuickHull3D quickHull = new QuickHull3D(qPoints);
		faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE);
		
		// trim off the "artifact"
		faces = postprocess(faces, points.length);
		
		// turn faces into links
		int numPoints = points.length; 
		mesh = new LinkedArray(points.length+3);
		links = new int[1][2];
		linkCount = 0;
		for(int i = 0; i < faces.length; i++) {
		  int numSides = faces[i].length;
			for(int j = 0; j < numSides; j++) {
				int p = faces[i][j];
				int q = faces[i][(j+1)%numSides];
				
				// to remove the postprocess step, need to add a check back here
				// if p and q are valid point indicies (less that the original number of points)
				if(!mesh.linked(p,q)) {
					mesh.link(p,q);
					if(linkCount==links.length){
						int[][] tmplinks = new int[links.length*2][2];
						System.arraycopy(links, 0, tmplinks, 0, links.length);
						links = tmplinks;
					}
					links[linkCount][0] = p;
					links[linkCount][1] = q;
					linkCount++;
				}
			}
		}

		// turn connectivity info into edges
		edges = new float[linkCount][4];
		for(int i=0; i<linkCount; i++){
			edges[i][0] = points[links[i][0]][0];
			edges[i][1] = points[links[i][0]][1];
			edges[i][2] = points[links[i][1]][0];
			edges[i][3] = points[links[i][1]][1];
		}
	}

	public static double[] preprocess(float[][] points) {
	  
    double qPoints[] = new double[ points.length*3 + 9 ];
    for(int i=0; i<points.length; i++){
      qPoints[i*3] = points[i][0];
      qPoints[i*3+1] = points[i][1];
      qPoints[i*3+2] = -(points[i][0]*points[i][0] + points[i][1]*points[i][1]); // standard half-squared euclidean distance
    }
    
    int M = 1;
    int N = M*2000;
    // create some virtual points that are far from the region where the actual points are
    // this is needed so that the convex hull is also a delaunay triangulation
    // 1
    qPoints[ qPoints.length-9 ] = -N;
    qPoints[ qPoints.length-8 ] = 0;
    qPoints[ qPoints.length-7 ] = -4000000*M;
    // 2
    qPoints[ qPoints.length-6 ] = N;
    qPoints[ qPoints.length-5 ] = N;
    qPoints[ qPoints.length-4 ] = -8000000*M;
    // 3
    qPoints[ qPoints.length-3 ] = N;
    qPoints[ qPoints.length-2 ] = -N;
    qPoints[ qPoints.length-1 ] = -8000000*M;
    
    return qPoints;
  }
	
	public static int[][] postprocess(int[][] faces, int numPoints) {
	  ArrayList<int[]> validFaces = new ArrayList<int[]>();
	  for(int i = 0; i < faces.length; i++) {
	    int[] vertIndicies = faces[i];
	    boolean valid = true; 
	    for(int j = 0; j < vertIndicies.length; j++) {
	      if(vertIndicies[j] >= numPoints) {
	        valid = false;
	        break;
	      }
	    }
	    if(valid) {
	      validFaces.add(vertIndicies);
	    }
	  }
	  int[][] validFacesArray = new int[validFaces.size()][];
	  validFacesArray = validFaces.toArray(validFacesArray);
	  return validFacesArray;
	}

  public float[][] getEdges(){
		return edges;
	}

	public int[][] getLinks(){
		return links;
	}
	
	public int[][] getFaces() {
	  return faces;
	}

	public int[] getLinked( int i ){
		return mesh.get(i).links;
	}

	public int edgeCount(){
		return linkCount;
	}

}