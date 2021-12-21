package client;

import common.CryptoUtils;
import common.Hash;
import common.Request;
import common.Response;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class ClientMain {

    private static final Map<Sender, Long> connections = new HashMap<>();
    private static final Random r = new Random(3);


    public static void main(String[] args) {
        try {
            for (String password : getRandomPasswordList()) { //This method will always give you the same list of 75 passwords
                SecretKey keyGenerated = CryptoUtils.getKeyFromPassword(password);

                File inputFile = getRandomFileToBeEncrypted();
                File encryptedFile;
                if (inputFile != null) {
                    encryptedFile = new File("encrypted-" + inputFile.getName());
                } else {
                    throw new NoSuchElementException();
                }

                // This is an example to help you create your request
                CryptoUtils.encryptFile(keyGenerated, inputFile, encryptedFile);
                System.out.println("Password used: " + password);
                System.out.println("Input file length of " + inputFile.getName() + " : " + inputFile.length());
                System.out.println("Encrypted file length of " + encryptedFile.getName() + " : " + encryptedFile.length());

                // SEND THE PROCESSING INFORMATION AND FILE
                byte[] hashPwd = Hash.sha1(password);
                int pwdLength = password.length();
                long fileLength = encryptedFile.length();
                // Creating socket to connect to server (in this example it runs on the localhost on port 3333)
                Sender sender;

                Thread.sleep(getPoissonRandomNumber(0.06)); // 1 request every 15 seconds aka 1/15
                long startTime = System.currentTimeMillis();
                Socket socket = new Socket("localhost", 3333);
                Request request = new Request(hashPwd, pwdLength, fileLength, encryptedFile);
                sender = new Sender(request, socket);
                connections.put(sender, startTime);
                sender.start();
            }


            int requestCounter = 1;
            for (Sender connection : connections.keySet()) {
                connection.join();
                Response response = connection.getResponse();
                if (response != null) {
                    //System.out.println("Decrypted file length from the server response: " + response.getFileLength());
                    //System.out.println("Decrypted file length: " + response.getFile().length());
                    long elapsedTime = System.currentTimeMillis() - connections.get(connection);
                    getFormattedTimeMeasurements(elapsedTime, requestCounter);
                } else {
                    System.out.println("No response from the server");
                }
                requestCounter++;
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException
                | NoSuchPaddingException | IllegalBlockSizeException | IOException | BadPaddingException
                | InvalidKeyException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void getFormattedTimeMeasurements(long timeInMilliseconds, int senderID) {
        long minutes = (timeInMilliseconds / 1000) / 60;
        long seconds = (timeInMilliseconds / 1000) % 60;
        System.out.println(
                "Request n°" + senderID + " ---> Request/Response Time : " + minutes + " minutes and "
                        + seconds + " seconds");
    }

    private static int getPoissonRandomNumber(double rate) {
        Random r = new Random();
        double L = Math.exp(-rate);
        int k = 0;
        double p = 1.0;
        do {
            p = p * r.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
    }

    /*private static List<String> getListOfPasswords() {
        ArrayList<String> listOfPasswords = new ArrayList<>(Arrays.asList("alley", "lillie", "manga", "kinky",
                "schmidt", "dogboy", "billybob", "volvo", "perry",
                "sentra", "zurich", "forum", "safety", "candyass",
                "mailman", "smokie", "redskin", "punkrock", "anne",
                "malone", "fastball", "diane", "classics", "ccccccc",
                "sparky"));
        int numberOfRandomPasswords = 50;
        for (int i = 1; i <= numberOfRandomPasswords; i++) {
            listOfPasswords.add(randomPasswordGenerator());
        }
        Collections.shuffle(listOfPasswords);
        return listOfPasswords;
    }

    private static String randomPasswordGenerator() {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        return RandomStringUtils.random(5, characters);
    }*/

    private static List<String> getRandomPasswordList() {
        return new ArrayList<>(Arrays.asList("vrmeh", "oiwfm", "hhfsp", "alley", "redskin",
                "billybob", "zvgeo", "qckwd", "sparky","punkrock", "ghihv", "jeqnb", "volvo",
                "zurich", "jnggw", "ymmpa", "dfkyd","uihup", "mbabw", "trinh", "xgvgd",
                "wgqyl", "pgkbh", "pqdqg", "forum","pjhek", "pgkiy", "sentra", "smokie",
                "nbkij", "diane", "xqtfg", "schmidt","tcluo", "mailman", "ewddr", "aefqd",
                "kinky", "sbhba", "zjpof", "lrfft", "inyqc","kngfg", "ybuxx", "amasz",
                "fastball", "ccccccc","pszbw", "miffs", "qahfr", "malone", "bppmn", "anne",
                "mesdr", "lpsok", "gvaox", "classics", "fmzsh", "blnlo", "lillie", "candyass",
                "kkbnb", "umgtf", "lksty", "dogboy","qasiz", "manga", "safety", "saqfs",
                "fnlbi", "nsene", "wgpwd", "ixumt", "perry", "pmrho"
                ));
    }

    private static File getRandomFileToBeEncrypted() {
        File directory = new File("src/main/resources/filesToBeEncrypted");
        File[] files = directory.listFiles();
        if (files != null) {
            int randomIndex = r.nextInt(files.length);
            return files[randomIndex];
        }
        return null;


    }
}
