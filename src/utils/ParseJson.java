package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

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
            Type listType = new TypeToken<ArrayList<FileInfo>>() {
            }.getType();
            ArrayList<FileInfo> list = new Gson().fromJson(jsonData, listType);
            if (list == null) {
                return fileInfoHashMap;
            }
            for (FileInfo fileInfo : list) {
                fileInfoHashMap.put(fileInfo.fileName, fileInfo);
            }
//            JsonArray jsonArray = new Gson().fromJson(jsonData, JsonArray.class);
//            if (jsonArray == null) {
//                return fileInfoHashMap;
//            }
//            for (JsonElement jsonElement : jsonArray) {
////                String filename = jsonElement.getAsJsonObject().get("fileName").getAsString();
////                FileInfo fileInfo = new Gson().fromJson(jsonElement.getAsJsonObject().get("fileInfo"), FileInfo.class);
////                fileInfoHashMap.put(filename, fileInfo);
//                FileInfo fileInfo = new Gson().fromJson(jsonElement, FileInfo.class);
//                fileInfoHashMap.put(fileInfo.fileName, fileInfo);
//            }
////            for (String filename : fileInfoHashMap.keySet()) {
////
////                System.out.println(filename);
////                System.out.println(fileInfoHashMap.get(filename).toString());
////            }
            return fileInfoHashMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeBackFileInfo(String filePath, HashMap<String, FileInfo> fileInfoHashMap) {
//        it's magic to find some element of hashMap is null
//        so I remove null directly
        ArrayList<FileInfo> fileInfos = new ArrayList<>();
        for (String filename : fileInfoHashMap.keySet()) {
            if (fileInfoHashMap.get(filename) != null)
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
            if (Files.notExists(path)) {
                Files.createFile(path);
                return blockIds;
            }
            String jsonData = new String(Files.readAllBytes(path));
//            create type for json
            Type listType = new TypeToken<ArrayList<Block>>() {
            }.getType();
            ArrayList<Block> list = new Gson().fromJson(jsonData, listType);
            if (list == null) {
                return blockIds;
            }
            for (Block block : list) {
                blockIds.put(block.id, block.size);
            }
            return blockIds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockIds;
    }

    public static void writeBackDataInfo(String filePath, HashMap<Integer, Integer> blockIds) {
        ArrayList<Block> blockIdList = new ArrayList<>();
        for (Integer blockId : blockIds.keySet()) {
            blockIdList.add(new Block(blockId, blockIds.get(blockId)));
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


