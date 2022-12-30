package ru.kpfu.itis.iskander.dz2_2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import ru.kpfu.itis.iskander.BlockChainFileStorage;
import ru.kpfu.itis.iskander.Crypto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockChain {
    /* алгоритм ключа сервиса */
    public final String KEY_ALGORITHM = "RSA";
    /* алгоритм подписи, формируемой сервисом */
    public final String SIGN_ALGORITHM = "SHA256withRSAandMGF1";
    /* 16-ричное представление публичного ключа сервиса */
    public String publicKey = null;
    private int blockCount = 15;
    private final List<BlockChainBlock> blockChain = new ArrayList<>();
    private BlockChainFileStorage fileStorage;
    private Crypto crypto;

    public BlockChain(String fileStorageDir, String keysStorageDir) {
        fileStorage = new BlockChainFileStorage(fileStorageDir);
        crypto = new Crypto(keysStorageDir);
    }

    public void run() throws IOException {
        makeBlockChain();
        readPublicKey();

        for (BlockChainBlock blockChainBlock : blockChain) {
            fileStorage.save(blockChainBlock);
        }

        printBlock(fileStorage.getAllDZ2());

        damage();

        try {
            boolean verification = verification(publicKey);
            System.out.println(verification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        Запрос публичного ключа
     */
    public void readPublicKey() {
        try {
            URL url = new URL("http://89.108.115.118/ts/public");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int rcode = con.getResponseCode();

            if (rcode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                publicKey = reader.readLine();

                System.out.println(publicKey);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Подписываем хеш с меткой времени
     */
    public TimeStampResp getSignatureAndTimeStamp(String digest) {
        TimeStampResp timeStampResp = null;
        try {
            URL url = new URL("http://89.108.115.118/ts?digest=" + digest);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");

            int rcode = con.getResponseCode();

            if (rcode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String response = reader.readLine();

                ObjectMapper mapper = new ObjectMapper();
                timeStampResp = mapper.readValue(response, TimeStampResp.class);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return timeStampResp;
    }

    public boolean verify(String publicKeyHexStr, byte[] data, String signHexStr) {
        Security.addProvider(new BouncyCastleProvider());

        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM, "BC");

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(Hex.decode(publicKeyHexStr));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
            signature.initVerify(pubKey);

            signature.update(data);

            return signature.verify(Hex.decode(signHexStr));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void makeBlockChain() {
        byte[] prevHash = null;
        try {
            for (int i = 0; i < blockCount; i++) {
                BlockChainBlock blockChainBlock = new BlockChainBlock(i);
                blockChainBlock.setData(String.valueOf(i));
                blockChainBlock.setPrevHash(prevHash);

                prevHash = crypto.getHash(blockChainBlock);
                String digest = new String(Hex.encode(prevHash), StandardCharsets.UTF_8);
                TimeStampResp signatureAndTimeStamp = getSignatureAndTimeStamp(digest);
                blockChainBlock.setSign(signatureAndTimeStamp.getTimeStampToken().getSignature());
                blockChainBlock.setTimeStamp(signatureAndTimeStamp.getTimeStampToken().getTs());

                blockChain.add(blockChainBlock);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void printBlock(List<BlockChainBlock> blocks) {
        blocks.forEach(block -> {
            System.out.println("BLOCK #" + block.getBlockNum());
            System.out.println("prev hash: " + (block.getPrevHash() != null ? new String(Hex.encode(block.getPrevHash())) : ""));
            System.out.println(block.getData());
            try {
                System.out.println("digest: " + new String(Hex.encode(crypto.getHash(block))));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchProviderException e) {
                e.printStackTrace();
            }
            System.out.println("signature: " + new String(Hex.encode(block.getSign().getBytes(StandardCharsets.UTF_8))));
            System.out.println();
        });
    }

    private boolean verification(String publicKey) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] prevHash = crypto.getHash(blockChain.get(0));
        for (int i = 1; i < blockCount; i++) {
            BlockChainBlock blockInfo = blockChain.get(i);

            String digest = new String(Hex.encode(crypto.getHash(blockInfo)), StandardCharsets.UTF_8);
            byte[] data = new byte[blockInfo.getTimeStamp().getBytes().length + Hex.decode(digest).length];

            System.arraycopy(blockInfo.getTimeStamp().getBytes(), 0, data, 0, blockInfo.getTimeStamp().getBytes().length);
            System.arraycopy(Hex.decode(digest), 0, data, blockInfo.getTimeStamp().getBytes().length, Hex.decode(digest).length);

            if (!Arrays.equals(prevHash, blockChain.get(i).getPrevHash())) {
                return false;
            }
            prevHash = crypto.getHash(blockChain.get(i));

            boolean isOk = verify(publicKey, data, blockInfo.getSign());

            if (!isOk) return false;
        }

        return true;
    }

    private void damage() {
        blockChain.get(3).setData("damaged");
    }
}
