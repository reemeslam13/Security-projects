import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Random;

public class Main {
    public static final float MINIMUM_TRANSACTION = 0.1f;
    public static final int MAX_TRANS = 10;
    public static final int MAX_USERS = 20;
    static Hashtable<String, ArrayList<User>> networkGraph;
    static ArrayList<User> users;

    private static void runTransactionsProcess() throws Exception {
        int size = networkGraph.keySet().size();
        Random rand = new Random();
        int senders = rand.nextInt(size);
        int randomUser, tranRand;
        Transaction t;
        User user;
        for (int i = 0; i < senders; i++) {
            randomUser = rand.nextInt(size - 1);
            user = users.get(randomUser);
            tranRand = rand.nextInt(MAX_TRANS);
            for (int j = 0; j < tranRand; j++) {
                t = user.generateTransaction();
                if (t != null) {
                    user.announceTransaction(t);
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider()); //Setup Bouncy castle as a Security Provider

        networkGraph = new Hashtable<>();
        users = new ArrayList<>();


        for (int i = 1; i <= MAX_USERS; i++) {
            users.add(new User("User " + i, new ArrayList<>()));
        }

        int random;
        ArrayList<User> tmpUsers = new ArrayList<>(users);
        for (User user : tmpUsers) {
            random = new Random().nextInt(MAX_USERS / 2);
            Collections.shuffle(users);
            for (int j = 0; j <= random; j++) {
                if (users.indexOf(user) != j) {
                    user.getPeers().add(users.get(j));
                }
            }
        }

        for (User user : tmpUsers) {
            networkGraph.put(user.getName(), user.getPeers());
        }

        //Prints users and their peers
//        for (User user : tmpUsers) {
//            System.out.println("Peer of " + user.getName());
//            for (User u : user.getPeer()) {
//                System.out.println(u.getName());
//            }
//            System.out.println();
//        }
        runTransactionsProcess();
    }
}
