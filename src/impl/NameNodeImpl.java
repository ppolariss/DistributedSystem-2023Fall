package impl;
//TODO: your implementation

import api.DataNode;
import api.DataNodeHelper;
import api.NameNodePOA;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import utils.File;
import utils.FileDesc;
import utils.FileInfo;
import utils.ParseJson;

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


    public class DataNodeInfo {
        int dataNodeId;
        int maxBlockId;

        public DataNodeInfo(int dataNodeId, int maxBlockId) {
            this.dataNodeId = dataNodeId;
            this.maxBlockId = maxBlockId;
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
                        return null;
                    }
                }
            }
        }
        if (fileMap.containsKey(name)) {
            return fileMap.get(name);
        }
        return create(name);
    }


    @Override
    public String open(String filepath, int mode) {
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


    public FileInfo create(String fileName) {
//        find the dataNode with the least block
//        not write back yet
        int dataNodeId = 0;
        int maxBlockId = -1;
        int index = 0;
        for (int i = 0; i < dataNodeInfos.size(); i++) {
            DataNodeInfo dataNodeInfo = dataNodeInfos.get(i);
            if (maxBlockId == -1 || (dataNodeInfo.maxBlockId < maxBlockId)) {
                maxBlockId = dataNodeInfo.maxBlockId;
                dataNodeId = dataNodeInfo.dataNodeId;
                index = i;
            }
        }
        if (maxBlockId == -1) {
            return null;
        }
        dataNodeInfos.get(index).maxBlockId++;
        FileInfo fileInfo = new FileInfo(dataNodeId, maxBlockId + 1, fileName);
        fileMap.put(fileName, fileInfo);
        return fileInfo;
    }


    public boolean isWrite(int flag) {
        return (flag & 2) != 0;
    }

    @Override
    public void registerDataNode(int dataNodeId, int maxBlockId) {
        DataNodeInfo dataNodeInfo = new DataNodeInfo(dataNodeId, maxBlockId);
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
    public void registerBlock(int oldBlockId, int newBlockId) {
        for (String fileName : fileMap.keySet()) {
            FileInfo fileInfo = fileMap.get(fileName);
            if (fileInfo.blockIdToDataNodeId.containsKey(oldBlockId)) {
                fileInfo.blockIdToDataNodeId.put(newBlockId, fileInfo.blockIdToDataNodeId.get(oldBlockId));
                fileInfo.dirty = true;
                return;
            }
        }
    }


    @Override
    public String updateFile(String fileName) {
        FileInfo fileInfo = fileMap.get(fileName);
        if (!fileInfo.dirty)
            return "null";
        return fileInfo.toString();
    }
}
