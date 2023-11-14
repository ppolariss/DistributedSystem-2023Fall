package impl;
//TODO: your implementation

import api.DataNodePOA;
import api.NameNode;
import com.google.gson.Gson;
import utils.ParseJson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DataNodeImpl extends DataNodePOA {
    private static final int MAX_DATA_NODE = 4;
//    private static final int maxBlockSize = 4 * 1024;

    public static int numberOfDataNode = 0;

    private NameNode nameNode;

    private static String fileName = "node/dataNode/dataNode";

    private HashMap<Integer, Integer> blockIds;

    public DataNodeImpl() {
        numberOfDataNode++;
        init(numberOfDataNode);
    }

    public DataNodeImpl(NameNode nameNode) {
        this.nameNode = nameNode;
        numberOfDataNode++;
        init(numberOfDataNode);
    }

    public DataNodeImpl(int dataNodeId) {
        init(dataNodeId);
        numberOfDataNode = dataNodeId;
    }

    private void init(int dataNodeId) {
        if (numberOfDataNode > MAX_DATA_NODE) {
            System.out.println("INFO: DataNode number exceed maxDataNode");
            return;
        }
        if (fileName.equals("node/dataNode/dataNode"))
            fileName += dataNodeId + "/";
        else fileName = "node/dataNode/dataNode" + dataNodeId + "/";
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        blockIds = ParseJson.getDataInfo(fileName + "info.json");
        if (blockIds == null) {
            blockIds = new HashMap<>();
        }
        if (nameNode != null)
            nameNode.registerDataNode(dataNodeId, new Gson().toJson(blockIds));
    }


    @Override
    public byte[] read(int blockId) {
        byte[] res = new byte[4 * 1024];
        if (!blockIds.containsKey(blockId)) {
            createBlock(blockId);
            return res;
        }
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(fileName + blockId));
            System.arraycopy(fileContent, 0, res, 0, fileContent.length);
            return res;
        } catch (Exception e) {
            System.arraycopy("null".getBytes(), 0, res, 0, "null".getBytes().length);
            return res;
//            return "null".getBytes();
        }
    }

    @Override
    public void append(int blockId, byte[] appendData) {
        try {
            byte[] originData = new byte[0];
            if (!blockIds.containsKey(blockId)) {
                createBlock(blockId);
            } else {
                originData = Files.readAllBytes(Paths.get(fileName + blockId));
            }
//            int len = originData.length + appendData.length;
//            if (len <= maxBlockSize) {
            int originLength = blockIds.get(blockId);
            int appendLength = 0;
            for (byte b : appendData) {
                if (b != 0) {
                    appendLength++;
                }
            }
            byte[] result = new byte[originLength + appendLength];
            System.arraycopy(originData, 0, result, 0, originLength);
            System.arraycopy(appendData, 0, result, originLength, appendLength);
            Files.write(Paths.get(fileName + blockId), result);

//                update and write back
            blockIds.put(blockId, originLength + appendLength);
            close();
//                return;
//            }
//
//            byte[] result = new byte[maxBlockSize];
//            System.arraycopy(originData, 0, result, 0, originData.length);
//            System.arraycopy(appendData, 0, result, originData.length, maxBlockSize - originData.length);
//            Files.write(Paths.get(fileName + blockId), result);
//
//            result = new byte[appendData.length - (maxBlockSize - originData.length)];
//            System.arraycopy(appendData, maxBlockSize - originData.length, result, 0, result.length);
//            addBlock(blockId, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void addBlock(int blockId, byte[] data) {
//        int newBlockId = randomBlockId();
//        nameNode.registerBlock(blockId, newBlockId);
//        append(newBlockId, data);
//        close();
//    }

    @Override
    public int randomBlockId() {
        ArrayList<Integer> bis = new ArrayList<>(blockIds.keySet());
        int newBlockId;
        if (bis.isEmpty()) {
            newBlockId = 1;
        } else newBlockId = Collections.max(bis) + 1;
        createBlock(newBlockId);
        return newBlockId;
    }

    void createBlock(int blockId) {
        Path path = Paths.get(fileName + blockId);
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            } else {
                Files.write(path, "".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        blockIds.put(blockId, 0);
    }


    public void close() {
        ParseJson.writeBackDataInfo(fileName + "info.json", blockIds);
    }

    public void setNameNode(NameNode nameNode) {
        this.nameNode = nameNode;
    }
}
