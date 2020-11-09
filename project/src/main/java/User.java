import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    public static final int DIFFICULTY = 5;
    public static final float MINIMUM_TRANSACTION = 0.1f;
    public static Map<String, TransactionOutput> UTXOs;
    private String name;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private ArrayList<User> peers;
    private List<Block> blockChain;
    private ArrayList<Transaction> receivedTransactions;
    private Transaction genesisTransaction;
    private ArrayList<Block> cachedBlocks;

    public User(String name, ArrayList<User> peers) throws Exception {
        generateKeyPair();
        this.name = name;
        this.peers = peers;
        receivedTransactions = new ArrayList<>();
        blockChain = new CopyOnWriteArrayList<>();
        cachedBlocks = new ArrayList<>();
        UTXOs = new HashMap<>();

        genesisTransaction = new Transaction(publicKey, publicKey, 100f, null);
        genesisTransaction.generateSignature(privateKey);     //manually sign the genesis transaction
        genesisTransaction.setTransactionId("0"); //manually set the transaction id
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getPeers() {
        return peers;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public List<Block> getBlockChain() {
        return blockChain;
    }

    public ArrayList<Transaction> getReceivedTransactions() {
        return receivedTransactions;
    }

    public ArrayList<Block> getCachedBlocks() {
        return cachedBlocks;
    }

    public Transaction getGenesisTransaction() {
        return genesisTransaction;
    }

    public void generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
        // Initialize the key generator and generate a KeyPair
        keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
        KeyPair keyPair = keyGen.generateKeyPair();
        // Set the public and private keys from the keyPair
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.getId(), UTXO); //add it to our list of unspent transactions.
                total += UTXO.getValue();
            }
        }
        return total;
    }

    //Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey recipient, float value) throws Exception {
        if (getBalance() < value) { //gather balance and check funds.
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.getTransactionOutputId());
        }
        return newTransaction;
    }

    public void announceTransaction(Transaction t) throws Exception {
        if (!receivedTransactions.contains(t) && t.processTransaction()) {
            receivedTransactions.add(t);
            int random = new Random().nextInt(peers.size());
            for (int i = 0; i <= random; i++) {
                Collections.shuffle(peers);
                if (peers.get(i) != this) {
                    System.out.println(t.toString() + ", Announced");
                    peers.get(i).announceTransaction(t);
                }
            }
        }
    }

    public void addBlock(Block newBlock) throws Exception {
        newBlock.mineBlock(DIFFICULTY);
        blockChain.add(newBlock);
        announceBlock(newBlock);
    }

    public Boolean isChainValid() throws Exception {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');
        Map<String, TransactionOutput> tempUTXOs = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!currentBlock.getHash().substring(0, DIFFICULTY).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop through blockchains transactions:
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if (!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }

    private Block isBuildingOnCachedBlock(Block b) {
        for (Block currentBlock : cachedBlocks) {
            if (currentBlock.getHash().equals(b.getPreviousHash())) {
                return currentBlock;
            }
        }
        return null;
    }

    public void announceBlock(Block b) {
        if (!blockChain.contains(b)) {
            Block newBlock = b;
            Block buildingBlock = isBuildingOnCachedBlock(b);
            if (buildingBlock != null) {
                for (Block currentBlock : blockChain) {
                    if (currentBlock.getPreviousHash().equals(buildingBlock.getPreviousHash())) {
                        cachedBlocks.remove(buildingBlock);
                        cachedBlocks.add(currentBlock);
                        blockChain.add(blockChain.indexOf(currentBlock), buildingBlock);
                        blockChain.remove(currentBlock);
                        newBlock = buildingBlock;
                        break;
                    }
                }
            }
            Random rand = new Random();
            boolean found = false;
            for (Block currentBlock : this.blockChain) {
                if (currentBlock.getHash().equals(newBlock.getHash()) && !currentBlock.getPreviousHash().equals(newBlock.getPreviousHash())) {
                    if (rand.nextInt(2) == 0) {
                        cachedBlocks.add(blockChain.remove(blockChain.indexOf(currentBlock)));
                        blockChain.add(newBlock);
                    } else {
                        newBlock = currentBlock;
                        cachedBlocks.add(newBlock);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                blockChain.add(newBlock);
            }
            int random = new Random().nextInt(peers.size());
            for (int i = 0; i <= random; i++) {
                Collections.shuffle(peers);
                if (peers.get(i) != this) {
                    System.out.println(b.toString() + ", Announced");
                    peers.get(i).announceBlock(newBlock);
                }
            }
        }
    }

    public Transaction generateTransaction() throws Exception {
        Random rand = new Random();
        User user = peers.get(rand.nextInt(peers.size()));
        if (user != null) {
            Transaction t = sendFunds(user.getPublicKey(), rand.nextFloat());
            if (t != null) {
                t.generateSignature(privateKey);
                return t;
            }
        }
        return null;
    }
}
