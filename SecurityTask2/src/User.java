
import java.io.BufferedReader;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;

public class User {
	PublicKey publicKey;
	PrivateKey privateKey;
	String otherCert = "certificate";

	public User() throws Exception {
		generateKeyPair();
	}

	private void generateKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048, new SecureRandom());
		KeyPair keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}

	public void requestACertificate() throws Exception {
		CertificateAuthority.generateCertificate(publicKey);
	}

	public boolean verifyACertificate() throws Exception {
		ArrayList<String> certificate = readCertificate();
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		publicSignature.initVerify(CertificateAuthority.publicKey);
		String str = "";
		for (int i = 0; i < 2; i++) {
			str += certificate.get(i);
		}
		publicSignature.update(str.getBytes());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		return !new Timestamp(System.currentTimeMillis())
				.after(new Timestamp(dateFormat.parse(certificate.get(0)).getTime()))
				&& publicSignature.verify(Base64.getDecoder().decode(certificate.get(2)));
	}

	ArrayList<String> readCertificate() throws Exception {
		ArrayList<String> readLines = new ArrayList<String>();
		String line;
		// FileReader reads text files in the default encoding.
		FileReader fileReader = new FileReader(otherCert);
		// Always wrap FileReader in BufferedReader.
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		while ((line = bufferedReader.readLine()) != null) {
			readLines.add(line);
		}
		// Always close files.
		bufferedReader.close();
		return readLines;
	}
}
