
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;

public class CertificateAuthority {
	static PublicKey publicKey;
	static PrivateKey privateKey;

	public static void generateKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048, new SecureRandom());
		KeyPair keyPair = keyGen.generateKeyPair();
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
	}

	public static void generateCertificate(PublicKey userKey) throws Exception {
		Certificate certificate = new Certificate(userKey);
		certificate.attributes[2] = Base64.getEncoder().encodeToString(signCertificate(certificate));
		writeFile("certificate", certificate);
	}

	private static byte[] signCertificate(Certificate certificate) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA256withRSA");
		privateSignature.initSign(privateKey);
		String str = "";
		for (int i = 0; i < 2; i++) {
			str += certificate.attributes[i];
		}
		privateSignature.update(str.getBytes());
		return privateSignature.sign();
	}

	public static void writeFile(String fileName, Certificate certificate) throws Exception {
		// Assume default encoding.
		FileWriter fileWriter = new FileWriter(fileName);
		// Always wrap FileWriter in BufferedWriter.
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		// Note that write() does not automatically
		// append a newline character.
		for (int i = 0; i < certificate.attributes.length; i++) {
			bufferedWriter.write(certificate.attributes[i]);
			bufferedWriter.newLine();
		}
		// Always close files.
		bufferedWriter.close();
	}
}
