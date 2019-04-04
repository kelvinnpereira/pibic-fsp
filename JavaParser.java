public class JavaParser {

		public static void main(String[] args) {
			System.out.println("___________________________________________________________________");
			System.out.println("JavaParser");

			if (args.length > 0) {
				System.out.println("   Reading source file " + args[0]);
				Scanner scanner = new Scanner(args[0]);
				System.out.println("   Parsing source file " + args[0]);
				Parser parser = new Parser(scanner);
				parser.Parse();
				System.out.println("-- " + parser.errors.count + " errors dectected");

			}
			else
				System.out.println("Syntax: JavaParser <java source file>");

		}

}