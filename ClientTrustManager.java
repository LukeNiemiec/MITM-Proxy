import javax.net.ssl.X509ExtendedTrustManager;
import java.security.cert.X509Certificate;
import java.net.Socket;
import javax.net.ssl.SSLEngine;

// this is a custom strust manger for the Client to not 
// check for trusted certificates
public class ClientTrustManager extends X509ExtendedTrustManager {

	public X509Certificate[] getAcceptedIssuers() {
	        return new X509Certificate[]{};
	}
	
	public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}
	public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}
	public void checkClientTrusted(X509Certificate[] chain, String authType) {}
	public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}
	public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}
	public void checkServerTrusted(X509Certificate[] chain, String authTypee) {}
}
