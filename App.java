import javax.swing.JFrame;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Font;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;


// the purpose of this class is to put 
// everything together using a GUI to 
// control, simulate and display the MITM proxy
public class App {

	// window elements
	private JFrame window = new JFrame();
	private JTextField input = new JTextField("https://"); 	// url input area
	private JButton goButton = new JButton("Go"); 
	private JPanel randr = new JPanel();					// panel for reqest and respose
	private JTextField statusField = new JTextField();
	private JTextArea http_req = new JTextArea("HTTP Request: "); // request text area
	private JTextArea http_res = new JTextArea("HTTP Reponse: "); // response text area

	private ProxyServer proxy = new ProxyServer(9080);		// proxy server
	private Client client = new Client();					// http client


	// configure and creates window and its elements
	public void createWindow() {

		// configure GUI window
		window.setTitle("HTTPS Proxy");
		window.setSize(500,500);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLayout(null);

		// Configure GUI components
		Font textFont = new Font("Arial", Font.PLAIN, 15);
		Font goFont = new Font("Arial", Font.PLAIN, 25);

		JPanel topPanel = new JPanel();

		statusField.setBounds(10, 110, 300, 40);
		statusField.setBorder(new EmptyBorder(10,10,10,10));
		statusField.setForeground(Color.RED);
		statusField.setFont(textFont);
		
		topPanel.setLayout(new GridLayout(2, 1));
		topPanel.setBounds(0, 0, 500, 100);
		topPanel.setVisible(true);
		topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


		input.setHorizontalAlignment(JTextField.LEFT);
		input.setFont(textFont);

		// GO button action listener has access to the client
		// and the input area to supply the user's url to the http client
		goButton.addActionListener(new GoListener(client, input));

		goButton.setFont(goFont);
		goButton.setVisible(true);

		
		randr.setLayout(new GridLayout(1, 2));
		randr.setBounds(10, 150, 800, 600);
		randr.setBorder(new EmptyBorder(10, 10, 10, 10));
	
		// add GUI components
		topPanel.add(input);
		topPanel.add(goButton);
		
		window.add(topPanel);

		window.add(statusField);
		
		randr.add(http_req);
		randr.add(http_res);
		window.add(randr);
		
		window.setVisible(true);
	}

	// displays the proxy log after successfull proxy operations
	public void newRequest(Log requestLog) {
	
		http_req.setText("HTTP Request: \n\n" + requestLog.getRequest());
		http_res.setText("HTTP Reponse: \n\n" + requestLog.getResponse());
		input.setText("https://");
		statusField.setForeground(Color.GREEN);
		statusField.setText("" + requestLog);
	}

	// for when the user inputs an invalid hostname
	public void invalidHostname() {
		input.setText("https://");
		statusField.setForeground(Color.RED);
		statusField.setText("ERROR: Invalid Hostname!");

		http_req.setText("HTTP Request:");
		http_res.setText("HTTP Reponse:");
	}

	
	public void start() {
		// create the window and window elements
		createWindow();
		System.out.println();
		// makes the proxy continously listen 
		// for new requests from the input
		while(true) {	
			Log new_log = null;
		
			try {
			
				new_log = proxy.listen();
				
			} catch(Exception error) {
				// if there is an error, then 
				// its an invalid hostname 
				invalidHostname();
				continue;
			}
			
			if(new_log != null) {
				// displays the log from the proxy
				newRequest(new_log);
				System.out.println();

			}

			continue;
		}
    }

	// MAIN functionality
    public static void main(String[] args) {
    
    	App https_proxy = new App();
    	
    	https_proxy.start();
    	
    }

}
