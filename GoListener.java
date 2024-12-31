import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;

// when go button is clicked...
// 		get text from URL JTextField
// 		call the HTTPClient function of Client
// 		set the URL JTextField to 
public class GoListener implements ActionListener {

	private Client cli;
	private JTextField input;

	public GoListener(Client cli, JTextField input) {
		this.cli = cli;
		this.input = input;
	}

	// performs clients start a HTTP request through the proxy
	public void actionPerformed(ActionEvent event) {
		cli.startRequest(input.getText());
	}
}
