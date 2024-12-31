
// this class is a log of each request and response 
// that the proxy server recieves during MITM proxy operations
public class Log {
	private String host;
	private String request;
	private String response;

	private int statusCode;
	private String requestMethod = "GET";

	public Log(String host, int statusCode, String request, String response) {
		this.host = host;
		this.statusCode = statusCode;
		this.request = request;
		this.response = response;
		
	}
	
	// for DEBUG
	public String print() {
		return "------------------\nHTTP REQUEST: \n\n"+request + "------------------\nHTTP RESPONSE: \n\n" + response + "------------------\n";
	}

	public String getRequest() {
		return this.request;
	}
	
	public String getResponse() {
		return this.response;
	}
	
	// for GUI
	public String toString() {
		// request method, host + path, 
		return requestMethod + ": " + host + "  ->  " + statusCode;
	}
}
