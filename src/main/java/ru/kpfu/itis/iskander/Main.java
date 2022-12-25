package ru.kpfu.itis.iskander;

public class Main {

    public static void main(String[] args) {
        BlockChain blockChain = new BlockChain(
                "/home/iskander/Desktop/ТСИС/sys_analyze/tsis_2_task/src/blockChainBlocks",
                "/home/iskander/Desktop/ТСИС/sys_analyze/tsis_2_task/src/main/resources"
        );
        blockChain.run();
    }
}
