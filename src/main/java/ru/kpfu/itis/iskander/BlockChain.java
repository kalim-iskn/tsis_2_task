package ru.kpfu.itis.iskander;

import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class BlockChain {

    private int blockCount = 15;
    private List<BlockChainBlock> blockChain = new ArrayList<>();
    private KeyPair keyPair;
    private BlockChainFileStorage fileStorage;
    private Crypto crypto;

    public BlockChain(String fileStorageDir, String keysStorageDir) {
        fileStorage = new BlockChainFileStorage(fileStorageDir);
        crypto = new Crypto(keysStorageDir);
    }

    public void run() {
        try {
            keyPair = crypto.loadKeys();
            makeBlockChain();

            for (BlockChainBlock blockChainBlock : blockChain) {
                fileStorage.save(blockChainBlock);
            }

            System.out.println("First result of verification = " + verify());

            brokeBlockChain();

            System.out.println("BLOCKS");
            List<BlockChainBlock> blockInfos = fileStorage.getAll();
            printBlock(blockInfos);

            fileStorage.clear();

            System.out.println("Second result of verification = " + verify());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeBlockChain() {
        byte[] prevHash = null;

        for (int i = 0; i < blockCount; i++) {
            BlockChainBlock blockInfo = new BlockChainBlock(i);
            blockInfo.getData().add("{\"data\":\"data " + i + "\"}");
            blockInfo.getData().add("{\"timestamp\":\"" + new Date() + "\"}");
            blockInfo.setPrevHash(prevHash);

            try {
                prevHash = crypto.getHash(blockInfo);

                blockInfo.setSign(crypto.generateRSAPSSSignature(keyPair.getPrivate(), prevHash));
                blockInfo.setSign2(crypto.generateRSAPSSSignature2(keyPair.getPrivate(), blockInfo.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }


            blockChain.add(blockInfo);
        }
    }

    private void printBlock(List<BlockChainBlock> blocks) {
        blocks.forEach(block -> {
            System.out.println("- BLOCK NUMBER: " + block.getBlockNum());
            System.out.println("- PREVIOUS HASH: " + (block.getPrevHash() != null ? new String(Hex.encode(block.getPrevHash())) : ""));

            System.out.println("- DATA: ");
            for (String s : block.getData()) {
                System.out.println(s);
            }

            try {
                System.out.println("- DIGEST: " + new String(Hex.encode(crypto.getHash(block))));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException | NoSuchProviderException e) {
                e.printStackTrace();
            }

            System.out.println("- SIGNATURE: " + new String(Hex.encode(block.getSign())));
            System.out.println("-------------------------------------------------------------");
        });
    }

    private boolean verify() throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] prevHash = crypto.getHash(blockChain.get(0));

        for (int i = 1; i < blockCount; i++) {
            if (!Arrays.equals(prevHash, blockChain.get(i).getPrevHash())) {
                return false;
            }

            prevHash = crypto.getHash(blockChain.get(i));

            String data = "";
            for (String s : blockChain.get(i).getData()) {
                data = data + s;
            }

            if (!crypto.verifyRSAPSSSignature(keyPair.getPublic(), prevHash, blockChain.get(i).getSign())) {
                return false;
            }

            if (!crypto.verifyRSAPSSSignature(keyPair.getPublic(), data.getBytes(StandardCharsets.UTF_8), blockChain.get(i).getSign2())) {
                return false;
            }
        }

        return true;
    }

    private void brokeBlockChain() {
        blockChain.get(3).getData().remove(0);
        blockChain.get(3).getData().add(0, "{\"data\":\"HACK\"}");
    }
}
