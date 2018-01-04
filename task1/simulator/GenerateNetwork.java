package simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class GenerateNetwork {
	private static final class Edge
	{
		private final int node1;
		private final int node2;
		private Edge(final int node1, final int node2) {
			super();
			this.node1 = node1;
			this.node2 = node2;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Random rnd = new Random();
		final List<Edge> g1=new LinkedList<Edge>();
		final List<Edge> g2=new LinkedList<Edge>();
		for (int i = 0; i < Constants.N; i++) {
			for (int j = i + 1; j < Constants.N; j++) {
				if (rnd.nextDouble() < Constants.P) {
					if (rnd.nextDouble() < Constants.S1) {
						g1.add(new Edge(i,j));
					}
					if (rnd.nextDouble() < Constants.S2) {
						g2.add(new Edge(i,j));
					}
				}
			}
		}
		final int[] mappings = new int[Constants.N];
		for (int i = 0; i < mappings.length; i++) {
			mappings[i] = i;
		}
		for (int j = 0; j < mappings.length * 10; j++) {
			final int n1 = rnd.nextInt(mappings.length);
			final int n2 = rnd.nextInt(mappings.length);
			final int temp = mappings[n1];
			mappings[n1] = mappings[n2];
			mappings[n2] = temp;
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("G1_Test.txt"));
			// output
			for(final Edge edge: g1)
			{
				writer.write(edge.node1 + " " + edge.node2);
				writer.newLine();
			}
			System.out.println("G1 contains " + g1.size() + " edges");
			writer.close();
			writer = new BufferedWriter(new FileWriter("G2_Test.txt"));
			for(final Edge edge: g2)
			{
				writer.write(mappings[edge.node1] + " " + mappings[edge.node2]);
				writer.newLine();
			}
			writer.close();
			System.out.println("G2 contains " + g2.size() + " edges");
			writer = new BufferedWriter(new FileWriter("T1_Test.txt"));
			for (int i = 0; i < Constants.N; i++) {
				writer.write(i + " " + mappings[i]);
				writer.newLine();
			}
			writer.close();
			writer = new BufferedWriter(new FileWriter("L_Test.txt"));
			for (int i = 0; i < Constants.N; i++) {
				if (rnd.nextDouble() < Constants.L) {
					writer.write(i + " " + mappings[i]);
					writer.newLine();
				}
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
