package ru.kpfu.itis.iskander.dz2_2;

import java.util.ArrayList;
import java.util.List;

public class BlockChainBlock {
    private int blockNum;
    private String data;
    private byte[] prevHash;
    private String sign;
    private String timeStamp;

    public BlockChainBlock() {
    }

    public BlockChainBlock(int blockNum) {
        this.blockNum = blockNum;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(byte[] prevHash) {
        this.prevHash = prevHash;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
