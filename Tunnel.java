// the purpose of this file is to provide the program
// with active monitoring going through the tunnel and 
// managing what goes through what side
// import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import javax.net.ssl.SSLContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// the purpose of this class is to perform all of the proxy 
// in regards to HTTP operations. 
public class Tunnel {
	
	private SSLSocket connection;	// client
	private SSLSocket destination;	// server

	private String host;			// server host
	private int port;				// server port
	private int statusCode;			// host response code

	//i/o recv and send
	private Tunneler client_to_host;	
	private Tunneler host_to_client;

	// provides ssl functionality for sockets
	private CertManager CM = new CertManager();		

	
	public Tunnel(Socket connection, String host, int port) {
	
		// creates an SSL Socket out of new connection
		this.connection = CM.wrapSocket(connection);
		this.connection.setUseClientMode(false);
		
		this.host = host;
		this.port = port;
	}


	// cleans up the opened resources
	public void clean() {
	
		try {
			
			this.destination.close();
			this.connection.close();
		
		} catch(Exception error) {
			return;		
		}
	}


	// initiates a TLS handshake with the client for 
	// encrypted communication
	private void clientTLSHandshake() throws Exception {
			
		try {
			
			// initialize the new handshake listener and 
			// add it to the connection's functionality
			HandshakeHandler HH = new HandshakeHandler();
			connection.addHandshakeCompletedListener(HH);

			// start handshake
			connection.startHandshake();

			// waits until handshake is completed
			while(!HH.isComplete()) {
				Thread.sleep(100);
			}			
		
		} catch(Exception error) {
			throw new Exception("[Tunnel]: Couldnt perform TLS handshake with the Client");
		}
	}

	
	// initiates a TLS handshake with the host for 
	// encrypted communication
	private void hostTLSHandshake() throws Exception {
		
		try {
		
			// creating a new ssl context
			SSLContext context = CM.getNewTLSContext();
			
			this.destination = (SSLSocket) context.getSocketFactory().createSocket(this.host, this.port);


			// setting up the client supported protocols and ciphers
			this.destination.setEnabledProtocols(this.destination.getSupportedProtocols());
			this.destination.setEnabledCipherSuites(this.destination.getSupportedCipherSuites());

			// this.destination.setEnabledCipherSuites({""});
			// start tls handshake with host
			HandshakeHandler HH = new HandshakeHandler();
			this.destination.addHandshakeCompletedListener(HH);


			this.destination.startHandshake();

			// waits until handshake is completed
			while(!HH.isComplete()) {
				Thread.sleep(100);
			}		
			
		} catch(Exception error) {
			throw new Exception("[Tunnel]: Couldnt perform TLS handshake with the Host");
		}
	}
	
	
	// configures the i/o streams for both the 
	// client and host for packet forwarding and logging
	private void setTunnelEnds() throws Exception {

		try {
			// connects clients out to host's in
			this.client_to_host = new Tunneler(
				connection.getInputStream(),
				destination.getOutputStream()		
			);
			
			// connects host's out to client's in
			this.host_to_client = new Tunneler(
				destination.getInputStream(),
				connection.getOutputStream()
			);

			
		} catch(Exception error) {
			throw new Exception("[Tunnel]: Couldnt create ends of the tunnel");
			
		}
	}
	
	// parses the server's response and returns 
	// true if proxy should continue operations
	
	// find content length or chunked and the status code
	private void parseResponse(String response) {
	
		// System.out.println("|"+response+"|");
		
		Pattern ContentLength = Pattern.compile("Content-Length:\\s(\\d*?)(?:\\r|$)");
		Matcher matcher = ContentLength.matcher(response);

	
		if(matcher.find()) {	
			// set the tunneler to parse data via content langth
			host_to_client.setContentLength(
				Integer.parseInt(matcher.group(1))
			);
		}

		Pattern StatusCode = Pattern.compile("HTTP.....(\\d{3})");
		matcher = StatusCode.matcher(response.split("\r\n")[0]);
		
		if(matcher.find()) {
			statusCode = Integer.parseInt(matcher.group(1));
		} 
	}

	
	// connects ins and outs of each side of the proxy and 
	private Log tunnel() {
		System.out.println("[Proxy]: STARTING TUNNEL");

		// log and send HTTP request
		client_to_host.sendInitReq();  	
	
		// log and send HTTP response
		host_to_client.sendInitReq();	
		
		// DEBUG: System.out.println("[Proxy]: sent init requests: " + client_to_host.getReq());
		// DEBUG: System.out.println("[Proxy]: sent init requests: " + host_to_client.getReq());

		parseResponse(host_to_client.getReq());
		boolean forward = false;
		
		if(statusCode == 200) {
			// false when tunnel is completed
			forward = host_to_client.checkStop();

		}
		

		// continues to tunnel data from Host to Client
		// until the Client has disconnected or EOF has 
		// been recieved from Host
		while(forward) {
			
			forward = host_to_client.forward();
		}

		// removes unneeded resources post https request
		host_to_client.clean();
		client_to_host.clean();

		// returns a new log of the operations done
		return new Log(
			this.host, 
			this.statusCode,
			client_to_host.getReq(),
			host_to_client.getReq()
		);
	}


	// starts the MITM porxy functionality
	public Log startTunnel() throws Exception {

		try {
			// makes sure that the client connected
			if (connection.isConnected()) {
				
				// initiate TLS encryption with the host
				this.hostTLSHandshake();
				System.out.println("[Proxy]: Handshake with " + this.host + " completed.");
				

				// initiate TLS encryption with the client
				this.clientTLSHandshake();
				System.out.println("[Proxy]: Handshake with the [Client] completed.");

						
				// initialize packet forwarding/logging between client and host
				this.setTunnelEnds();
			
				// provides a log of https data 
				//between the client and host
				Log new_log = this.tunnel();	
								
				// cleaning resources used in tunnel operations
				this.clean();

				// report this back to the proxy server
				return new_log;
				
					
			} else {
				this.clean();
				throw new Exception("[Proxy]: Couldnt proxy request.");
			}
			
			
		} catch(Exception error) {
			this.clean();
			throw error;
		}
	}
}
