import javax.swing.*;
import java.awt.event.*;
import java.util.LinkedList;

public class Calculator extends JFrame {
	private JPanel panel;
	private JTextField expressionField, answerField, dangerField;
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
		setFocusable(true);

		setVisible(true);
	}

	public void buildPanel() {
		expressionField = new JTextField(40);
		expressionField.addKeyListener(new KeyboardListener());

		answerField = new JTextField(20);
		answerField.setEditable(false);

		dangerField = new JTextField(18);
		dangerField.setEditable(false);
		
		panel = new JPanel();

		panel.add(expressionField);
		panel.add(answerField);
		panel.add(dangerField);
	}

	private class KeyboardListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				String expression = expressionField.getText();
				String parsedExpression = Matherizer.parseExpression(expression);
				if (!parsedExpression.contains("Failed")) {
					LinkedList<String> infix = Matherizer.infixConverter(parsedExpression);
					answerField.setText(Matherizer.crunchExpression(infix));
					dangerField.setText("Success");
				} else {
					dangerField.setText(parsedExpression);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}

	public static void main(String[] args) {
		new Calculator();
	}
}