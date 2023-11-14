package utils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class FileInfo {

    public Integer fileSize;

    public HashMap<Integer, Integer> blockIdToDataNodeId;
    public long createTime;
    public long lastModified;
    public long lastAccess;
    //    public int lastSize;
    public String fileName;

    public boolean dirty;

    public String toString() {
//        return null;
        return new Gson().toJson(this);
    }


    public static FileInfo fromString(String str) {
        if (str.isEmpty()||str.equals("null")){
            return null;
        }
        return new Gson().fromJson(str, FileInfo.class);
    }

    public FileInfo(Integer dataNodeId, Integer blockId, String fileName) {
        this.fileSize = 0;
        this.blockIdToDataNodeId = new HashMap<>();
        this.blockIdToDataNodeId.put(blockId, dataNodeId);
        this.createTime = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
        this.lastAccess = System.currentTimeMillis();
        this.fileName = fileName;
        dirty = false;
//        this.lastSize = lastSize;
    }
}
