import java.io.OutputStream;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;

import java.lang.Thread;


// the purpose of this class is to be an easy
// reader and writer for non-encrypted sockets
public class Endpoint {
	
	private BufferedReader in;
	private InputStream inStream;
	
	private PrintWriter out;
	private OutputStream outStream;

	
	public Endpoint(InputStream in, OutputStream out) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.inStream = in;
		
		this.out = new PrintWriter(out, true);
		this.outStream = out;
	}


	// wait for recv bytes from endpoint
	public char[] waitRecv() {

		char[] data = new char[1];
	
		try {
			while(!this.in.ready()) {
				Thread.sleep(100);
			}

			data = this.recv();
			
		} catch(Exception error) {
			System.out.println("[E ERROR]: couldnt wait for response.");
		}
		
		return data;
	}


	// recieve from endpoint 
	public char[] recv() throws Exception {
		try {
			
			char[] bytes = new char[2048];
			
			this.in.read(bytes, 0, 2048);
			
			
			return bytes;
			
			
		} catch(Exception error) {
		
			throw error;
		}
	}


	// sends data to the socket
	public void send(String data) throws Exception {
		
		char[] write_data = data.toCharArray();
		
		try {
		
			out.write(write_data);
			out.flush();
			
		} catch(Exception error) {
			throw error;
		}
	
	}
}
