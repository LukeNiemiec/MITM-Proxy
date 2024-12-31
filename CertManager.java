import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManagerFactory;

import java.security.KeyStore;

import java.io.FileInputStream;

import java.net.Socket;

// the purpose of this class is to supply the proxy server and
// the client with encrypted communication using SSLSockets
public class CertManager {
	private String keyfile = "certs/keystore.jks";
	private String trustfile = "certs/truststore.jks";

	// password for keyfile and trustfile
	char[] password = "password".toCharArray(); 

	private KeyStore keystore;			// file of self signed CA's 
	private KeyStore truststore;		// file of trusted CA's

	// factories of keymanaers and trust managers
	private TrustManagerFactory tmf;
	private KeyManagerFactory kmf;

	// ssl context for the proxy to use
	private SSLContext sslContext;

	public CertManager() {
		try {
			// load keystore from file and initialize it
			keystore = KeyStore.getInstance("JKS");
			keystore.load(new FileInputStream(keyfile), password);

			kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keystore, password);

			// load the trust store from a file and initialize it
			truststore = KeyStore.getInstance("JKS");
			truststore.load(new FileInputStream(trustfile), password);

			tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(truststore);

			// create a tls context and supply it with the keymanager and trust manager
			// this context allows the proxy server to establish encrypted communication 
			// with the client
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
		} catch(Exception error) {
			
		}
	}
	
	// gets a new TLS context for the 
	// proxy's connection to the host
	public SSLContext getNewTLSContext() {
		SSLContext newContext = null;
		
		try {
			// creates a default context
			newContext = SSLContext.getInstance("TLS");
			newContext.init(null, null, null);
			
		} catch(Exception error) {
			return null;
		}	

		return newContext;
	}


	// returns a tls context for the client 
	// in order for the client to be able 
	// to trust the proxy's tls handshake
	public SSLContext getClientContext() {
		SSLContext cliContext = null;
		
		try {
			
			cliContext = SSLContext.getInstance("TLS");

			// makes the client's context trust all CA's
			TrustManager[] cliTMS = {new ClientTrustManager()};
			cliContext.init(null, cliTMS, null);
			
		} catch(Exception error) {
			return null;
		}
			
		return cliContext;
	}


	// wraps an active socket connection in a tls context
	// and allows the proxy server to perform a tls handshake with
	// the client
	public SSLSocket wrapSocket(Socket socket) {
		SSLSocket newSocket = null;
		
		try {
			// from class' ssl context create a ssl socket from an existing socket
			newSocket = (SSLSocket) sslContext.getSocketFactory()
									.createSocket(socket, socket.getInputStream(), true);
		} catch(Exception error) {
			return null;
		}
		
		return newSocket;
	}
}
