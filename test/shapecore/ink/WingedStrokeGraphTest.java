package shapecore.ink;

import shapecore.*;
import shapecore.ink.WingedStrokeGraph;
import shapecore.ink.WingedStrokeGraph.*;
import junit.framework.TestCase;

public class WingedStrokeGraphTest extends TestCase {
  
  public void testBuild() throws Exception {
    
    WingedStrokeGraph g = WingedStrokeGraph.build(new pt[] {
        new pt(10,50),
        new pt(20,10),
        new pt(40,40),
        new pt(40,20),
        new pt(50,10),
    }, new int[][] {
        {1,2},
        {2,2,3,4},
        {3},
        {},
        {2}
    });
    
    assertEquals(8, g.strokes.size());
    assertEquals(5, g.junctions.size());
    
    // check that everything is (or isn't) set
    for(Stroke s : g.strokes) {
      assertNotSame(-1, s.headCcwStroke);
      assertNotSame(-1, s.tailCcwStroke);
      assertNotSame(-1, s.headCwStroke);
      assertNotSame(-1, s.tailCwStroke);
      assertEquals(-1, s.headContinuationStroke);
      assertEquals(-1, s.tailContinuationStroke);
    }
  }
}
