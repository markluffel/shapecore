package megamu.mesh;

import processing.core.*;

public class IntArray {

	public int[] data;
	private int length;

	public IntArray() {
		this(0);
	}

	public IntArray(int l) {
	  // at least two entries to start
	  // otherwise PApplet.expand won't work
		data = new int[Math.max(l,2)];
		length = 0;
	}

	public void add(int val) {
		if(length >= data.length) {
			data = PApplet.expand(data);
		}
		data[length] = val;
		length++;
	}

	public int get(int i) {
		return data[i];
	}

	public boolean contains(int val) {
		for(int i = 0; i < length; i++) {
			if(data[i] == val) {
				return true;
			}
		}
		return false;
	}

  public int size() {
    return length;
  }
}