import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.ProxySelector;
import java.net.URI;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLContext;

import java.time.Duration;

// Contains an HTTPS client that is configured to use the proxy server.
// this HTTPS client trusts the proxy server and all domains and sends
// a user's request to the proxy
public class Client {

	// needed for authenticating the proxy server
	private CertManager CM = new CertManager();
	
	// ssl context & params for encrypted communication
	private SSLContext cli_context = CM.getClientContext();
	private SSLParameters ssl_params = cli_context.getSupportedSSLParameters(); 
	
	private InetSocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", 9080); // proxy server address
	private ProxySelector proxySelector = ProxySelector.of(proxyAddr);	// proxy server selector

	private HttpClient https_client;  									// client

	public Client() {
	
		// configures the http client 
		https_client = HttpClient.newBuilder()
						.sslContext(cli_context) 				// use encryption
						.sslParameters(ssl_params)				// type of encryption allowed
						.proxy(proxySelector)					// proxy to use
						.connectTimeout(Duration.ofSeconds(4))	// timeout for error handling
						.version(HttpClient.Version.HTTP_1_1)	// use http 1/1
						.build();
	}

	// starts a new request to the specified URL
	public void startRequest(String dn) {
		System.out.println("[Client]: Sending request to " + dn + ".");
		
		try {
			URI uri = new URI(dn);

			// creates a new request for the specified URL
			HttpRequest newRequest = HttpRequest.newBuilder(uri).GET().build();

			// sends request and returns the server's response
			HttpResponse<String> response = https_client.send(
				newRequest, 		
				BodyHandlers.ofString()
			);
			
			System.out.println("[Client]: Success!");
			
		} catch(Exception error) {
			return;
		}
	}
}
