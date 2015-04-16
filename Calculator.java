import javax.swing.*;
import java.awt.event.*;
import java.util.LinkedList;

public class Calculator extends JFrame {
	private JPanel panel;
	private JTextField expressionField, answerField;
	private JButton go;

	private final int WINDOW_WIDTH = 500;
	private final int WINDOW_HEIGHT = 90;

	public Calculator() {

		buildPanel();
		add(panel);

		setTitle("frog's Calculator");
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		setVisible(true);
	}

	public void buildPanel() {
		expressionField = new JTextField(40);

		answerField = new JTextField(15);
		answerField.setEditable(false);

		go = new JButton("Go");
		go.addActionListener(new GoButtonListener());
		
		panel = new JPanel();

		panel.add(expressionField);
		panel.add(answerField);
		panel.add(go);
	}

	private class GoButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String expression = expressionField.getText();
			String parsedExpression = Matherizer.parseExpression(expression);
			if (parsedExpression != null) {
				LinkedList<String> infix = Matherizer.infixConverter(parsedExpression);
				answerField.setText(Matherizer.crunchExpression(infix));
			} else {
				answerField.setText("Malformed expression");
			}
		}
	}

	public static void main(String[] args) {
		new Calculator();
	}
}