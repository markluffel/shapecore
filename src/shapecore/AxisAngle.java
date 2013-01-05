package shapecore;


class AxisAngle {
  vec3 axis;
  float angle;
  
  AxisAngle(vec3 _axis, float _angle) {
    this.axis = _axis;
    this.angle = _angle;
  }
}