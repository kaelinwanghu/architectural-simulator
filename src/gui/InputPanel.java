package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class InputPanel extends JPanel{

	private JTextArea data;
	private JTextArea program;
	
	public InputPanel(final Simulator simulator, int dataRows, int programRows, int columns) {
		super(new BorderLayout());
		data = new JTextArea(dataRows, columns);
		data.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		
		program = new JTextArea(programRows, columns);
		program.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
		
		JScrollPane scrollPane1 = new JScrollPane(data);
		scrollPane1.setWheelScrollingEnabled(true);
		scrollPane1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0), new LineBorder(Color.GRAY, 1)));
		scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JScrollPane scrollPane2 = new JScrollPane(program);
		scrollPane2.setWheelScrollingEnabled(true);
		scrollPane2.setBorder(new LineBorder(Color.GRAY, 1));
		scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JButton instructionSet = new JButton("Instruction Set");
		instructionSet.setFocusable(false);
		instructionSet.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				simulator.instructionSetDialog.setVisible(true);
			}

		});
		
		JLabel l1 = new JLabel("Program");
		l1.setFont(new Font("Consolas", Font.PLAIN, 19));
		l1.setForeground(Color.RED);
		
		JLabel l2 = new JLabel("Data");
		l2.setFont(new Font("Consolas", Font.PLAIN, 19));
		l2.setForeground(Color.RED);
		
		JPanel p1 = new JPanel(new BorderLayout(0, 10));
		p1.add(l1, BorderLayout.WEST);
		p1.add(instructionSet, BorderLayout.EAST);
		
		JPanel p2 = new JPanel(new BorderLayout(0, 10));
		p2.add(l2, BorderLayout.NORTH);
		p2.add(scrollPane1);
		
		JPanel p3 = new JPanel(new BorderLayout(0, 10));
		p3.add(p1, BorderLayout.NORTH);
		p3.add(scrollPane2);
		
		add(p2, BorderLayout.NORTH);
		add(p3);
	}
	
	public String getInput() {
		return data.getText();
	}
	
	public String getProgram() {
		return program.getText();
	}
	
	public void clear() {
		data.setText("");
		data.setCaretPosition(0);
		program.setText("");
		program.setCaretPosition(0);
	}
	
}
