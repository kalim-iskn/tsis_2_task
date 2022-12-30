package ru.kpfu.itis.iskander;

import ru.kpfu.itis.iskander.dz2_2.BlockChain;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BlockChain blockChain = new ru.kpfu.itis.iskander.dz2_2.BlockChain(
                "C:\\Users\\Гулия\\IdeaProjects\\tsis_2_task\\src\\blockChainBlocks",
                "C:\\Users\\Гулия\\IdeaProjects\\tsis_2_task\\src\\main\\resources"
        );
        blockChain.run();
    }
}
