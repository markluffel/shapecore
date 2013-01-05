package megamu.mesh;

import java.util.List;

public class LinkedArray {

	LinkedIndex[] array;

	public LinkedArray(int size){
		array = new LinkedIndex[size];
		for(int i=0; i<array.length; i++)
			array[i] = new LinkedIndex(this, i);
	}

	public LinkedIndex get(int i){
		return array[i];
	}
	
	public int size() {
	  return array.length;
	}

	public void link(int a, int b){
		array[a].linkTo( b );
		array[b].linkTo( a );
	}

	public boolean linked(int a, int b){
		return array[a].linked(b);
	}

}