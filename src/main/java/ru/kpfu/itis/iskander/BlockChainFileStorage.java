package ru.kpfu.itis.iskander;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockChainFileStorage {

    private String blocksDirectory;
    private ObjectMapper objectMapper;
    private File blocks;

    public BlockChainFileStorage(String blocksDirectory) {
        this.blocksDirectory = blocksDirectory;
        objectMapper = new ObjectMapper();
        blocks = new File(blocksDirectory);
    }

    public void save(BlockChainBlock blockChainBlock) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(blocksDirectory + "/block-" + blockChainBlock.getBlockNum() + ".json");
        objectMapper.writeValue(file, blockChainBlock);
    }

    public void save(ru.kpfu.itis.iskander.dz2_2.BlockChainBlock blockChainBlock) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(blocksDirectory + "/block-" + blockChainBlock.getBlockNum() + ".json");
        objectMapper.writeValue(file, blockChainBlock);
    }

    public void clear() throws IOException {
        FileUtils.cleanDirectory(blocks);
    }

    public List<BlockChainBlock> getAll() throws IOException {
        Iterator<File> iterator = FileUtils.iterateFiles(blocks, new String[]{"json"}, false);
        ArrayList<BlockChainBlock> list = new ArrayList<>();

        while (true) {
            if (!iterator.hasNext()) {
                return list;
            }
            list.add(objectMapper.readValue(FileUtils.readFileToString(iterator.next()), BlockChainBlock.class));
        }
    }

    public List<ru.kpfu.itis.iskander.dz2_2.BlockChainBlock> getAllDZ2() throws IOException {
        Iterator<File> iterator = FileUtils.iterateFiles(blocks, new String[]{"json"}, false);
        ArrayList<ru.kpfu.itis.iskander.dz2_2.BlockChainBlock> list = new ArrayList<>();

        while (true) {
            if (!iterator.hasNext()) {
                return list;
            }
            list.add(objectMapper.readValue(FileUtils.readFileToString(iterator.next()), ru.kpfu.itis.iskander.dz2_2.BlockChainBlock.class));
        }
    }
}
