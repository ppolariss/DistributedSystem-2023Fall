package impl;
//TODO: your implementation

import api.NameNodePOA;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import utils.File;
import utils.FileDesc;
import utils.FileInfo;
import utils.ParseJson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NameNodeImpl extends NameNodePOA {
//    private static final int MAX_DATA_NODE = 4;
//    private DataNode[] dataNodes = new DataNode[MAX_DATA_NODE];
//
//    private NamingContextExt ncRef;
//
//    public void setDataNodes(DataNode[] dataNodes) {
//        this.dataNodes = dataNodes;
//    }
//
//    public void setNcRef(NamingContextExt ncRef) {
//        this.ncRef = ncRef;
//    }


    public static class DataNodeInfo {
        int dataNodeId;
        HashMap<Integer, Integer> blockIds;

        public DataNodeInfo(int dataNodeId, HashMap<Integer, Integer> blockIds) {
            this.dataNodeId = dataNodeId;
            this.blockIds = blockIds;
        }
    }

    //../../node/nameNode/info.json
    private static final String jsonPath = "node/nameNode/info.json";

    ArrayList<DataNodeInfo> dataNodeInfos = new ArrayList<>();

    //    fileName -> fileInfo
    private HashMap<String, FileInfo> fileMap;
    //    fileName -> fileDesc
    Map<String, ArrayList<FileDesc>> fileDescMap = new HashMap<>();

    public NameNodeImpl() {
        fileMap = ParseJson.getFileInfo(jsonPath);
    }

    private FileInfo getFileInfo(String name, boolean writeAble) {
        if (writeAble) {
            ArrayList<FileDesc> fileDescs = fileDescMap.get(name);
            if (fileDescs != null) {
                for (FileDesc fileDesc : fileDescs) {
                    if (isWrite(fileDesc.mode)) {
//                        System.out.println(name + writeAble);
//                        System.out.println("file is being written");
                        return null;
                    }
                }
            }
        }
        if (fileMap.containsKey(name)) {
//            System.out.println(fileMap.get(name).toString());
            return fileMap.get(name);
        }
        return create(name);
    }


    @Override
    public String open(String filepath, int mode) {
//        System.out.println("open " + filepath + " " + mode);
        FileInfo fileInfo = getFileInfo(filepath, isWrite(mode));
        if (fileInfo == null) {
            return "null";
        }
        FileDesc fileDesc = new FileDesc(mode);
        fileDescMap.putIfAbsent(filepath, new ArrayList<>());
        fileDescMap.get(filepath).add(fileDesc);

        File file = new File(fileInfo, fileDesc);
        return file.toString();
    }

    @Override
    public void close(String fdString) {
        Long fd = FileDesc.fromString(fdString).getId();
        String fileName = closeFd(fd);

        ParseJson.writeBackFileInfo(jsonPath, fileMap);
    }

    private String closeFd(Long fd) {
        for (String fileName : fileDescMap.keySet()) {
            ArrayList<FileDesc> fileDescs = fileDescMap.get(fileName);
            for (FileDesc fileDesc : fileDescs) {
                if (fileDesc.getId() == fd) {
                    fileDescs.remove(fileDesc);
                    return fileName;
                }
            }
        }
        return null;
    }

    //        find the dataNode with the least block
//        not write back yet
    public FileInfo create(String fileName) {
//        getMinSize -> dataNode nextId
//        update fileMap and dataNodeInfo
        int dataNodeId = 0;
        int minSize = -1;
        int index = 0;
//        for (int i = 0; i < dataNodeInfos.size(); i++) {
//            DataNodeInfo dataNodeInfo = dataNodeInfos.get(i);
//            int tmpSize = 0;
//            for (int size : dataNodeInfo.blockIds.values()) {
//                tmpSize = Math.max(tmpSize, size);
//            }
//            if (minSize == -1 || (tmpSize < minSize)) {
//                minSize = tmpSize;
//                dataNodeId = dataNodeInfo.dataNodeId;
//                index = i;
//            }
//        }
        for (int i = 0; i < dataNodeInfos.size(); i++) {
            DataNodeInfo dataNodeInfo = dataNodeInfos.get(i);
            int tmpSize = dataNodeInfo.blockIds.size();
            if (minSize == -1 || (tmpSize < minSize)) {
                minSize = tmpSize;
                dataNodeId = dataNodeInfo.dataNodeId;
                index = i;
            }
        }
        if (minSize == -1) {
//            according to the test, you can't return null
            return new FileInfo(1, 1, fileName);
//            return null;
        }
        int nextId = 0;
        for (int id : dataNodeInfos.get(index).blockIds.keySet()) {
            nextId = Math.max(nextId, id);
        }
        nextId++;
        dataNodeInfos.get(index).blockIds.put(nextId, 0);
        FileInfo fileInfo = new FileInfo(dataNodeId, nextId, fileName);
        fileMap.put(fileName, fileInfo);
        return fileInfo;
    }


    public boolean isWrite(int flag) {
        return (flag & 2) != 0;
    }

    @Override
    public void registerDataNode(int dataNodeId, String s) {
        HashMap<Integer, Integer> blockIds;
        Type type = new TypeToken<HashMap<Integer, Integer>>() {
        }.getType();
        blockIds = new Gson().fromJson(s, type);
        if (blockIds == null) {
            blockIds = new HashMap<>();
        }
        DataNodeInfo dataNodeInfo = new DataNodeInfo(dataNodeId, blockIds);
        dataNodeInfos.add(dataNodeInfo);

//        for (int dataNodeId = 0; dataNodeId < MAX_DATA_NODE; dataNodeId++) {
//        try {
//            dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
//            dataNodes[dataNodeId].read(1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//            System.out.println("DataNode" + dataNodeId + " is obtained.");
//        }
    }

    @Override
    public void registerBlock(String fileName, String info) {
        FileInfo newFileInfo = FileInfo.fromString(info);
        fileMap.put(fileName, newFileInfo);
//        for (String fileName : fileMap.keySet()) {
//            FileInfo fileInfo = fileMap.get(fileName);
//            if (fileInfo.blockIdToDataNodeId.containsKey(oldBlockId)) {
//                fileInfo.blockIdToDataNodeId.put(newBlockId, fileInfo.blockIdToDataNodeId.get(oldBlockId));
//                fileInfo.dirty = true;
//                return;
//            }
//        }
    }


    @Override
    public String updateFile(String fileName) {
        FileInfo fileInfo = fileMap.get(fileName);
        if (!fileInfo.dirty)
            return "null";
        return fileInfo.toString();
    }
}
