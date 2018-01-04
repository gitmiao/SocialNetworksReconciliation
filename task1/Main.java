

import java.util.Date;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(new Date());
		//new Solver().solve("G1_Test.txt", "G2_Test.txt", "L_Test.txt");
		new Solver().solve("G1.txt", "G2.txt", "L.txt");
		System.out.println(new Date());
	}

}
