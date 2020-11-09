
public class Main {
    public static void main(String[] args) {
        try {
            CertificateAuthority.generateKeyPair();
            User user1 = new User();
            User user2 = new User();
            user1.requestACertificate();
            System.out.println(user2.verifyACertificate() ? "Valid" : "Invalid");
            Thread.sleep(10000);
            System.out.println(user2.verifyACertificate() ? "Valid" : "Invalid");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
