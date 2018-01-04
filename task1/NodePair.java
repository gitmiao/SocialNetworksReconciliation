

import java.util.HashSet;
import java.util.Set;

public class NodePair {
	private final int node1;
	private final int node2;
	// node id in graph 1
	private final Set<Integer> commonKnownNodes;

	public NodePair(final int node1, final int node2, final Set<Integer> commonKnownNodes) {
		super();
		this.node1 = node1;
		this.node2 = node2;
		this.commonKnownNodes = commonKnownNodes;
	}
	
	public NodePair(final int node1, final int node2) {
		this.node1 = node1;
		this.node2 = node2;
		commonKnownNodes = new HashSet<Integer>();
	}

	public int getNode1() {
		return node1;
	}

	public int getNode2() {
		return node2;
	}

	public Set<Integer> getCommonKnownNodes() {
		return commonKnownNodes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + node1;
		result = prime * result + node2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodePair other = (NodePair) obj;
		if (node1 != other.node1)
			return false;
		if (node2 != other.node2)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "NodePair [node1=" + node1 + ", node2=" + node2
				+ ", commonKnownNodes=" + commonKnownNodes.size() + "]";
	}
}
