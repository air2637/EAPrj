
public class CompileA {

	public static void main(String[] args) {

		String[][] parameters = new String[][] { { "5" }, { "10" }, { "20" }, { "50" }, { "100" } };

		for (String[] s : parameters) {
			System.out.println(s[0]);
			Tester.main(s);
		}

	}

}
