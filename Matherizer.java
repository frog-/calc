import java.util.StringTokenizer;
import java.util.Stack;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Matherizer {

	/**
	 * Take any expression and make it run the gauntlet. This can
	 * handle basically anything feasible and turn it into a nicely
	 * formatted expression, ready for infix->postfix conversion.
	 * 
	 * @return Well-formed expression on success, null on fail
	 **/
	public static String parseExpression(String expression) {
		/**
		 * Pre-processing
		 **/

		/**
		 * Format for simpler parsing
		 * Add whitespace between every operator and term, cut all whitespace down
		 * to single spaces
		 **/
		//String regex = "([\\(\\)\\{\\}\\[\\]\\!\\%\\^\\*\\+\\-\\/\\@]|\\d*\\.?\\d+)";
		String niceExpr = "";
		String regex = "([^\\d\\.]|\\d*\\.?\\d+)";
		Pattern validate = Pattern.compile(regex);
		Matcher match = validate.matcher(expression);
		while (match.find()) {
			niceExpr += match.group() + " ";
		}
		niceExpr = niceExpr.replaceAll("\\s{2,}", " ");

		/**
		 * Handle brackets:
		 * Test all brackets for opening/closing pairs. If there are
		 * any mismatches, the expression is hopeless.
		 **/
		StringTokenizer token = new StringTokenizer(niceExpr, " ");
		Stack<String> brackets = new Stack<>();
		while (token.hasMoreTokens()) {
			String letter = token.nextToken();

			//Determine kind of bracket
			int bracketType = 0;
			switch (letter) {
				case "(": case "{": case "[":
					bracketType = 1; break; //Found opener
				case ")": case "}": case "]":
					bracketType = 2; break; //Found closer
				default:
					continue; //Not a bracket; ignore
			}

			//Process the bracket
			if (bracketType == 1) {
				//Store opening brackets
				brackets.push(letter);
			} else {
				//Test closing brackets: remove opener on match, fail on mismatch
				if (!brackets.empty() && matchFind(brackets.peek(), letter)) {
					brackets.pop();
				} else {
					return "Failed: bracket mismatch";
				}
			}
		}

		//If any openers were left unclosed, fail
		if (!brackets.empty()) {
			return "Failed: odd number of brackets";
		}

		//Now make all brackets the same (makes the regexes easier on the eyes)
		niceExpr = niceExpr.replaceAll("[\\[\\{]", "(");
		niceExpr = niceExpr.replaceAll("[\\]\\}]", ")");
		System.out.println(niceExpr);

		/**
		 * Basic validation tests
		 **/
		regex = "[^\\(\\)\\!\\%\\^\\*\\+\\-\\/\\d\\@\\s\\.]"; //Test for illegal characters
		if (testExpr(regex, niceExpr)) {
			return "Failed: illegal characters";
		}
		regex = "\\d"; //Make sure a number is present
		if (!testExpr(regex, niceExpr)) {
			return "Failed: no numbers";
		}
		regex = "\\([^\\d]+\\)"; //Test for brackets without numbers
		if (testExpr(regex, niceExpr)) {
			return "Failed: empty brackets";
		}
		regex = "\\d\\s\\d"; //Test for numbers without operators
		if (testExpr(regex, niceExpr)) {
			return "Failed: terms unconnected by operator";
		}
		regex = "([^\\d\\)\\s]|^)\\s*[\\+\\-\\*\\/\\%\\^]|[\\+\\-\\*\\/\\%\\^]\\s*([^\\(\\d\\s]|$)";
		if (testExpr(regex, niceExpr)) { //Test for operators in impossible places
			return "Failed: dangling operator";
		}

		/**
		 * Handle implicit multiplication:
		 * Whenever a closing and opening bracket appear next to each other, or
		 * a number appears directly beside a bracket, insert a "*"
		 **/
		String[] bracketPatterns = { 
			"(\\))\\s(\\()",
			"(\\d+)\\s(\\()",
			"(\\))\\s(\\d+)" };

		for (String search : bracketPatterns) {
			niceExpr = (testExpr(search, niceExpr)) ? preprocess(search, niceExpr, " * ", true) : niceExpr;
		}

		/**
		 * Handle negative values:
		 * All negative values must be inside brackets by themselves. When found
		 * mark them with @, which will later be converted to zero minus the value
		 **/
		regex = "(\\(\\s\\-\\s(\\(|\\d*\\.?\\d+\\s\\)))";
		char[] expr = niceExpr.toCharArray();
		validate = Pattern.compile(regex);
		match = validate.matcher(niceExpr);
		//Convert appropriate "-"s to "@"s
		while (match.find()) {
			//Because of formatting, the "-" is always the 3rd element
			expr[match.start() + 2] = '@';
		}
		niceExpr = String.valueOf(expr);

		/**
		 * Great success!
		 **/
		System.out.println("NICE EXPRESSSIONNN: " + niceExpr);
		return niceExpr;
	}


	/**
	 * Convert a well-formed, human-readable expression into a postfix expression
	 * for later processing.
	 *
	 * @return LinkedList<String> containing the tokenized postfix expression
	 **/
	public static LinkedList<String> infixConverter(String expression) {
		//Tokenize expression
		StringTokenizer args = new StringTokenizer(expression, " ");
		ArrayList<String> tokens = new ArrayList<>();
		while (args.hasMoreTokens()) {
			tokens.add(args.nextToken());
		}

		//Opstack holds unused operators; output is the converted expression
		Stack<String> opstack = new Stack<>();
		LinkedList<String> output = new LinkedList<>();

		for (String token : tokens) {
			//Opening brackets are always added to opstack
			if (token.equals("(")) {
				opstack.push(token);
			}

			//Numerical values are always added to output
			else if (Character.isDigit(token.charAt(0))) {
				output.add(token);
			}

			//Closing brackets cause opstack to pop until an opening bracket is met
			else if (token.equals(")")) {
				while (!opstack.empty() && !matchFind(opstack.peek(), token)) {
					output.add(opstack.pop());
				}
				opstack.pop();
 			}

 			//Turns eg "-1" into "0 - 1"
 			else if (token.equals("@")) {
 				output.add("0");
 				opstack.push("-");
 			}

			//Operators are added to opstack once all lower-precedence operators are popped except brackets
			else {
				while (!opstack.empty() && findPrecedence(token) < findPrecedence(opstack.peek())) {
					output.add(opstack.pop());
				}
				opstack.push(token);
			}
		}

		//Clear remainder from opstack
		while (!opstack.empty()) {
			output.add(opstack.pop());
		}

		//Return finalized expression
		System.out.println("Postfix: " + output);
		return output;
	}


	/**
	 * Receive a well-formed postfix expression and turn it into something beautiful.
	 * By George, it better be a well-formed postfix expression.
	 * 
	 * @return The answer, as a double
	 **/
	public static String crunchExpression(LinkedList<String> expression) {
		Stack<String> terms = new Stack<>();

		while (!expression.isEmpty()) {
			//Test if we're looking at a term or an operator
			if (Character.isDigit(expression.peek().charAt(0))) {
				//If it is a term, add it to the stack
				terms.push(expression.remove());
			} else {
				//If it is an operator, pop twice, do math, and push the result to the stack
				double b = Double.parseDouble(terms.pop());
				double a = Double.parseDouble(terms.pop());
				String operator = expression.pop();

				//Do math
				double result = 0.0;
				switch (operator) {
					case "+": 
						result = a + b; break;
					case "-":
						result = a - b; break;
					case "*":
						result = a * b; break;
					case "/":
						result = a / b; break;
					case "%":
						result = a % b; break;
					case "^":
						result = Math.pow(a, b); break;
					case "!":
				       	for (int i = 1; i <= a; i++)
            				result = result * i;
            			break;
				}

				terms.push(String.valueOf(result));
			}
		}
		return terms.pop();
	}


	/**
	 * General helper function:
	 * Attempts to find a matching opening bracket in the brack-stack
	 * 
	 * @return True if match is found
	 **/
	private static boolean matchFind(String open, String close) {
		if (open.equals("(") && close.equals(")") ||
			open.equals("{") && close.equals("}") ||
			open.equals("[") && close.equals("]")) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * infixConverter helper function:
	 * Find precedence of supplied token
	 *
	 * @return Integer precedence; higher is greater
	 **/
	private static int findPrecedence(String token) {
		int tokenvalue = 0;
		switch (token) {
			case "(":
				tokenvalue = 0; break;
			case "+": case "-":
				tokenvalue = 1; break;
			case "*":
				tokenvalue = 2; break;
			case "/":
				tokenvalue = 3; break;
			case "^":
				tokenvalue = 4; break;
			case "!":
				tokenvalue = 5; break;
			case "@":
				tokenvalue = 6; break;
		}

		return tokenvalue;
	}


	/**
	 * parseExpression helper function:
	 * Find if a string matches the supplied pattern. This is 
	 * not meant to be used in loops.
	 * 
	 * @return True if found
	 **/
	public static boolean testExpr(String regex, String compare) {
		Pattern validate = Pattern.compile(regex);
		Matcher match = validate.matcher(" " + compare + " ");
		if (match.find()) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * parseExpression helper function:
	 *
	 * Searches for and converts all matches of a pattern in an expression into the passed 
	 * replacement.
	 *
	 * @param insert The string to splice into the expression at the match points
	 * @param doubleCapture Whether the insertion occurs between elements that are lost in the matching
	 *
	 * @return The expression with all instances of the pattern replaced with the insertion
	 **/
	public static String preprocess(String regex, String expression, String insert, boolean doubleCapture) {
		/*
		 * Split the expression at the matches, so we can splice in the modified segment.
		 * Add padding to allow for matches that begin and end the expression
		 */
		expression = " " + expression + " ";
		String[] matching = expression.split(regex);
		for (String s : matching) {
			System.out.println("Match:" + s);
		}
		String spliced = matching[0];

		//Find all the matches, and capture everything specified
		Pattern validate = Pattern.compile(regex);
		Matcher match = validate.matcher(expression);

		//For every match,
		for (int i = 1; match.find(); i++) {
			//Splice in the modification and store result as new 'left-hand side'
			String replacement = insert;
			if (doubleCapture) {
				replacement = match.group(1) + insert + match.group(2);
			}
			spliced += replacement + matching[i];
		}

		return spliced;
	}
}