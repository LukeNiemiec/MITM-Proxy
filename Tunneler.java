import java.io.OutputStream;
import java.io.InputStream;

import java.io.PrintWriter;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Arrays;

import java.util.ArrayList;

// the purpose of this class is to connect the inputStream of
// either the client or host endpoint to the outputStream of the
// other endpoint. note, this class should only be used for SSLSockets
public class Tunneler {

	private BufferedReader in;  	
	private PrintWriter out;

	// defines the first http request/response recieved
	private char[] initReq = null;

	private int contentLength = 0;
	private int bytesRecieved = 0;
	private ArrayList<Character> recievedData = new ArrayList<Character>();

	private String recieveType = "chunked";

	// constructor
	public Tunneler(InputStream in, OutputStream out) {
	
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = new PrintWriter(out);
	}


	// closes the recourses for the i/o streams
	public void clean() {
		try {
			this.in.close();
			this.out.close();
		} catch(Exception error) {
			return;
		}
	}

	// for DEBUG purposes
	private void verbose() {
		if(this.recieveType == "length") {
			System.out.println("[VERB]: VIA Content-Length: " + this.contentLength + "\n\tBytes Recieved: " + this.bytesRecieved + "\n");
			System.out.println("[VERB]: CHUNKED Current size: " + this.recievedData.size() + "\n");
		}
	}

	
	// makes the tunneler stop reading data after content length
	public void setContentLength(int length) {
		this.contentLength = length;
		this.recieveType = "length";
	}


	// checks whether the last chunk of data has been recieved 
	private boolean checkChunkedEOF() {
		int ds = recievedData.size();
		
		// checks if the last four bytes of 
		// the recieved data are: "\r\n\r\n"
		if(recievedData.size() > 4) {

			int[] lastFour = {
				(int) recievedData.get(ds - 1),
				(int) recievedData.get(ds - 2),
				(int) recievedData.get(ds - 3),
				(int) recievedData.get(ds - 4)
			};
			
			// DEBUG: System.out.println(lastFour[0] + " " + lastFour[1] + " " + lastFour[2] + " " + lastFour[3] );
			if(lastFour[0] == 10 && lastFour[0] == lastFour[2]) {
				if(lastFour[1] == 13 && lastFour[1] == lastFour[3]) {
					return false;
				}
			}
		}

		return true;
	}

	// checks whether the content length has been reached 
	public boolean checkContentLength() {
		// DEBUG: System.out.println("CL: " + contentLength + " <- " + bytesRecieved);
		
		if (contentLength <= bytesRecieved + 10) {
			return false;
		} else {
			return true;
		}
	}

	// checks if the the forwarding sholuld top 
	//based on the way that the data shoiuld be 
	//parsed(content-length OR chunked)
	public boolean checkStop() {
		
		if(this.recieveType == "length") {
			return checkContentLength();
		} else {
			return checkChunkedEOF();
		}
	}


	// sets the initial request for later processing
	// and sends the request to the other endpoint
	public void sendInitReq() {
		if(initReq == null) {
		
			initReq = this.recv();
			
			if(initReq != null) {
				this.send(initReq);
				
				String[] splitReq =  new String(this.initReq).split("\r\n\r\n");
				
				// spliting extra data off of the HTTP request/response 
				this.initReq = splitReq[0].toCharArray();

				// if there is extra data attached, add it
				// to the number of bytes recived and received data
				if(splitReq.length > 1) {
					char[] data = splitReq[1].toCharArray();
					
					this.bytesRecieved += data.length;
					
					for(char c : data) {
						this.recievedData.add(c);
					}
				} 
									
			} else {
				System.out.println("[Tunneler]: couldnt get initial request/response.");
			}
		}
	}

	
	// allows for packet forwarding between client and host
	// returns true when forwarding is still in process
	// and false when forwarding is completed(content-length OR chunked)
	public boolean forward() {
		
		try {
			char[] data = this.recv();
			
			if(data != null) {

				this.send(data);
				
				int dl = data.length;

				// add to bytes recieved and 
				// add data to recieved data
				this.bytesRecieved += dl;
				
				for(char c : data) {
					this.recievedData.add(c);
				}
				
				return this.checkStop();
			}
		
		} catch(Exception error) {
			System.out.println("[Tunneler] forward(): " + error);
			return false;
		}

		return false;
	}


	// returns the initReq as a string
	public String getReq() {
		return new String(initReq);
	}

	public void printBytes() {
		for(char c : recievedData) {
			System.out.println((int) c);
		}
	}

	// recieves packet data from input stream
	private char[] recv() {
		char[] sbuffer = null;

		
		try {

			char[] cbuffer = new char[2048];
							
			this.in.read(cbuffer, 0, 2048);
			
			sbuffer = cbuffer;

			// for getting rid of padding between packets
			for(int i = 0; i < 2048; i++) {
			
				if((int) cbuffer[i] == 0) {
					
					sbuffer = Arrays.copyOf(cbuffer, i);
					break;
				}
			}

		} catch(Exception error) {
			System.out.println("[Tunneler] recv(): " + error);
		}

		return sbuffer;
	}


	// sends packet data through the output stream
	private void send(char[] data) {
		try {
		
			this.out.write(data);
			this.out.flush();
		
		} catch(Exception error) {
			System.out.println("[Tunneler] send(): " + error);
		}
	}
}
