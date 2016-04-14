import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CompileB {

	public static void main(String[] args) {

		String[][] parameters = new String[][] { { "5", "6" }, { "10", "15" }, { "20", "25" },
				{ "50", "60" }, { "100", "120" } };

		try {
			PrintWriter w = new PrintWriter(
					new BufferedWriter(new FileWriter("part b/overall_summary.txt", false)));
			w.println("");
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String[] s : parameters) {
			System.out.println(s[0] + "-" + s[1]);
			TesterB.main(s);
		}

	}

}
