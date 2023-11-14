package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParseJson {
//    public static String toJson(Object obj) {
//        Gson gson = new Gson();
//        return gson.toJson(obj);
//    }
//
//    public static <T> T fromJson(String json, Class<T> classOfT) {
//        Gson gson = new Gson();
//        return gson.fromJson(json, classOfT);
//    }

    public static HashMap<String, FileInfo> getFileInfo(String filePath) {
        HashMap<String, FileInfo> fileInfoHashMap = new HashMap<>();
        Path path = Paths.get(filePath);
        try {
            if (Files.notExists(path)) {
//                String currentDirectory = System.getProperty("user.dir");
                Files.createFile(path);
            }
            String jsonData = new String(Files.readAllBytes(path));
            JsonArray jsonArray = new Gson().fromJson(jsonData, JsonArray.class);
            if (jsonArray == null) {
                return fileInfoHashMap;
            }
            for (JsonElement jsonElement : jsonArray) {
                String filename = jsonElement.getAsJsonObject().get("fileName").getAsString();
                FileInfo fileInfo = new Gson().fromJson(jsonElement.getAsJsonObject().get("fileInfo"), FileInfo.class);
                fileInfoHashMap.put(filename, fileInfo);
            }
            return fileInfoHashMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeBackFileInfo(String filePath, HashMap<String, FileInfo> fileInfoHashMap) {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (String filename : fileInfoHashMap.keySet()) {
            fileInfos.add(fileInfoHashMap.get(filename));
        }
        String jsonData = new Gson().toJson(fileInfos);
        Path path = Paths.get(filePath);
        try {
            Files.write(path, jsonData.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Integer, Integer> getDataInfo(String filePath) {
        HashMap<Integer, Integer> blockIds = new HashMap<>();
        Path path = Paths.get(filePath);
        try {
            JsonArray jsonArray = new Gson().fromJson(new String(Files.readAllBytes(path)), JsonArray.class);
            if (jsonArray == null) {
                return blockIds;
            }
            for (JsonElement jsonElement : jsonArray) {
                blockIds.add(jsonElement.getAsInt());
            }
            return blockIds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeBackDataInfo(String filePath, HashSet<Integer> blockIds) {
        ArrayList<Integer> blockIdList = new ArrayList<>();
        for (Integer blockId : blockIds) {
            blockIdList.add(blockId);
        }
        String jsonData = new Gson().toJson(blockIdList);
        Path path = Paths.get(filePath);
        try {
            Files.write(path, jsonData.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


