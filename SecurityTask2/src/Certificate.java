
import java.security.PublicKey;
import java.sql.Timestamp;

public class Certificate {
	Timestamp timestamp;
	String[] attributes;

	public Certificate(PublicKey publicKey) throws Exception {
		timestamp = new Timestamp(System.currentTimeMillis() + 10000);
		attributes = new String[3];
		attributes[0] = timestamp.toString();
		attributes[1] = String.valueOf(publicKey.hashCode());
	}
}
