package megamu.mesh;

import java.util.ArrayList;
import java.util.List;

public class LinkedIndex {

	List<Integer> links;

	public LinkedIndex() {
		links = new ArrayList<Integer>();
	}

	public void linkTo(int i) {
		links.add(i);
	}

	public boolean linked(int i) {
		for(int j : links) {
			if(j == i) {
				return true;
			}
		}
		return false;
	}

	public List<Integer> getLinks() {
	  return links;
	}

  public int size() {
    return links.size();
  }
}