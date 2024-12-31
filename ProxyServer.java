import java.net.Socket;
import java.net.ServerSocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;

// the purpose of this class is to create a 
// proxy server that will accept incoming connections
// and once a connection has been established
// perform a MITM with the connection in HTTPS
class ProxyServer {
 
	ServerSocket server;						 // proxy server
	
	Socket connection;							 // connected client

	Tunnel tunnel;								 // tunnel between the client and the server

	ArrayList<Log> logs = new ArrayList<Log>();	 // logs of completed requests



	// initializes the proxy server socket
	// to recv incoming connections from client
	public ProxyServer(int port) {
		
		try {
		
			// create a new server socket
			server = new ServerSocket(port);
			server.setReuseAddress(true);
			
		} catch(Exception error) {
			System.out.println("[Proxy]: Couldnt create server socket");			
			
		} finally {
			System.out.println("[Proxy]: Created server socket");
		}
	}


	// disconnect the server
	public void disconnect() {
	
		try {
			server.close();
		} catch(Exception error) {
			return;
		}
	}

	// closes the client's connection 
	public void resetConnection() {
		try {
			this.connection.close();	
		} 
		catch(Exception error) {} 
		
		finally {
			this.connection = null;
		}
	}


	// for getting the host and port from the proxy CONNECT request
	private Matcher find(String match, String pattern) {
	
		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(match);

		return matcher;
	}


	// performs proxy handshake with new client connection
	// and starts ther tunneling process
	private void on_connect(Socket connection) throws Exception {
	
		if (connection.isConnected()) {
			try {
			
				// create an endpoint to send and 
				// recv socket data to and from the new connection
				Endpoint con_end = new Endpoint(
					connection.getInputStream(),
					connection.getOutputStream()
				);

				// retrieve host url from client
				String connect_data = new String(con_end.recv());
				Matcher url_match = find(connect_data, "Host:\\s(\\S*?)\\s");

				
				if(url_match.find()) {
				
					// get url and port from the CONNECT data
					String URI = url_match.group(1);
					String host = URI.split(":")[0];
					int port =  Integer.parseInt(URI.split(":")[1]);

					// send back connection established
					con_end.send("HTTP/1.1 200 Connection established\r\n\r\n");

					// create tunnel 					
					tunnel = new Tunnel(connection, host, port);
					
					// start tunnel operations and get the 
					// returned Log of the proxy
					Log https_log = tunnel.startTunnel();
					
					if(https_log != null) {
						// add the log to the proxy server's logs
						logs.add(https_log);
					}
				
				} else {
					throw new Exception("[Proxy]: [Client] made invalid proxy CONNECT request.");
				}
				
			} catch(Exception error) {
				throw error;
			}
		}		
	}


	// starts listening for incoming connections.
	// once completed MITM proxy operations, 
	// return a log of the event
	public Log listen() throws Exception {
		while(true) {
			
			try {
				// try to accept a new connection
				connection = server.accept();
				
			} catch(Exception error) {
				continue;
			} 

			if(connection == null) {
				continue;
				
			} else {
				// do proxy handshake with incomming socket connection
				System.out.println("[Proxy]: Accepted new [Client] connection.");
		
				try {
					// try establish a proxy connection to the 
					// new connection to the proxy server
					on_connect(connection);

					
					resetConnection(); 			// reset the active connection
					this.tunnel = null;					// reset the tunnel
					
					System.out.println("[Proxy]: Success!");
					return logs.get(logs.size()-1);		// return the last log

				// error making connection...
				} catch(Exception error) {
				
					// clean tunnel
					if(tunnel != null) {
						this.tunnel.clean();
						this.tunnel = null;
						resetConnection();
					}

					throw error;
				} 
				
			}
		}
	}
}

