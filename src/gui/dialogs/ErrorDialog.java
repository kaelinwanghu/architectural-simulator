package gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog {
	
	private JTextArea text;
	
	public ErrorDialog(JFrame main) {
		super(main, "Error", true);
		
		JLabel image = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("gui/resources/error.png")));
		
		setIconImage(main.getIconImage());
		
		text = new JTextArea();
		text.setOpaque(false);
		text.setEditable(false);
		text.setFocusable(false);
		
		JButton ok = new JButton("OK");
		ok.setFocusable(false);
		ok.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			
		});
		
		JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		p1.add(image);
		p1.add(text);
		p1.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		p2.add(ok);
		
		JPanel p3 = new JPanel(new BorderLayout());
        p3.add(p1);
		p3.add(p2, BorderLayout.SOUTH);
		
		setResizable(false);
		add(p3);
	}
	
	public void showError(String message) {
		text.setText(message + "!");
		pack();
		setLocationRelativeTo(null);
		setVisible(true);		
	}
		
}

