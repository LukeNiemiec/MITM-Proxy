import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;

// this class is to check whether a TLS handshake has been completed
public class HandshakeHandler implements HandshakeCompletedListener {
	private boolean completed = false;

	public void handshakeCompleted(HandshakeCompletedEvent event) {
		// System.out.println("Handshake has been completed!.");
		completed = true;
	}

	public boolean isComplete() {
		return completed;
	}
}
