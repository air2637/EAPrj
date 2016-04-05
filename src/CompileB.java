
public class CompileB {

	public static void main(String[] args) {

		String[][] parameters = new String[][] { { "5", "6", "1" }, { "5", "6", "2" },
				{ "10", "15", "1" }, { "10", "15", "2" }, { "20", "25", "1" }, { "20", "25", "2" },
				{ "50", "60", "1" }, { "50", "60", "2" }, { "100", "120", "1" },
				{ "100", "120", "2" } };

		for (String[] s : parameters) {
			System.out.println(s[0] + "-" + s[1] + "-" + s[2]);
			TesterB.main(s);
		}

	}

}
