package shapecore.motion;

import shapecore.pt;

/**
 * A set of trajectories covering the plane.
 *
 */
public abstract class Field {

  /**
   * Construct a trajectory starting at t=0 of traj1 and ending at t=1 of traj2
   * 
   * Follows the gradient of this field (or something like that)
   * 
   * @param traj1
   * @param traj2
   * @return
   */
  //abstract public Trajectory compose(Trajectory traj1, Trajectory traj2);

  /**
   * Build a trajectory following this field starting at point p.
   * 
   * @param p
   * @return
   */
  abstract public Trajectory trajectory(pt p);
}
