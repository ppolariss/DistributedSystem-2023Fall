package impl;
//TODO: your implementation

import api.*;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import utils.File;
import utils.FileInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientImpl implements Client {

    static final String CMD_OPEN = "open";
    static final String CMD_READ = "read";
    static final String CMD_APPEND = "append";
    static final String CMD_CLOSE = "close";
    static final String CMD_EXIT = "exit";


    //    private static final int MAX_DATA_NODE = 4;
    private static final int maxBlockSize = 4 * 1024;
    private NameNode nameNode;
    //    private DataNode[] dataNodes = new DataNode[MAX_DATA_NODE + 1];
    ArrayList<DataNode> dataNodeList = new ArrayList<>();


    //    fdMap: fd -> file
    private HashMap<Integer, File> fdMap = new HashMap<>();


    @Override
    public int open(String filepath, int mode) {
        String fileInfo = nameNode.open(filepath, mode);
        if (Objects.equals(fileInfo, "null")) {
//            System.out.println("server");
            return -1;
        }
        File file = File.fromString(fileInfo);
        int fd;
        if (file != null) {
            fd = Math.toIntExact(file.fd.getId());
        } else {
            System.out.println("INFO: open failed");
            return -1;
        }
        fdMap.put(fd, file);
        boolean isCreateNewFile = true;
        for (int size : file.fi.blockIdToSize.values()) {
            if (size != 0) {
                isCreateNewFile = false;
                break;
            }
        }
        if (isCreateNewFile && !file.fi.blockIdToDataNodeId.isEmpty()) {
            file.fi.blockIdToDataNodeId.forEach((k, v) -> appendContent(file, k, v, new byte[0]));
        }
        return fd;
    }

    @Override
    public void append(int fd, byte[] bytes) {
        File file = fdMap.get(fd);
        if (file == null) {
            System.out.println("INFO: fd not found");
            return;
        }
        if ((file.fd.mode & 2) == 0) {
            System.out.println("INFO: append not allowed");
            return;
        }
//        nextBlockId means the last block
        Set<Integer> blockIds = file.fi.blockIdToDataNodeId.keySet();
        int nextBlockId = blockIds.stream().max(Integer::compareTo).orElse(-1);
        int dataNodeId = file.fi.blockIdToDataNodeId.get(nextBlockId);

        if (appendContent(file, nextBlockId, dataNodeId, bytes))
            System.out.println("INFO: write done");
    }

    private boolean appendContent(File file, int nextBlockId, int dataNodeId, byte[] append) {
        boolean isCreateNewFile = append.length == 0;
//        DataNode dataNode = dataNodes[dataNodeId];
        DataNode dataNode = dataNodeList.get(dataNodeId - 1);
        //      update blockIdToSize blockIdToDataNodeId and NameNode
//        for loop
        int lastSize = file.fi.blockIdToSize.get(nextBlockId);
        boolean quit = true;
//        for update
        ArrayList<Integer> newBlockIdList = new ArrayList<>();

        while (true) {
            if (lastSize + append.length > maxBlockSize) {
                quit = false;
            }
            int sendLength = Math.min(maxBlockSize - lastSize, append.length);

            byte[] send = new byte[maxBlockSize];
            System.arraycopy(append, 0, send, 0, sendLength);
            dataNode.append(nextBlockId, send);
            append = Arrays.copyOfRange(append, sendLength, append.length);

            file.fi.blockIdToSize.put(nextBlockId, lastSize + sendLength);
            if (quit)
                break;
            else {
                quit = true;
                nextBlockId = dataNode.randomBlockId();
                newBlockIdList.add(nextBlockId);
                lastSize = 0;
            }
        }
        if (!isCreateNewFile) {
//            File file = fdMap.get(fd);
//            int dataNodeId = file.fi.blockIdToDataNodeId.get(oldBlockId);
            for (Integer blockId : newBlockIdList) {
                file.fi.blockIdToDataNodeId.put(blockId, dataNodeId);
            }
            file.fi.lastModified = System.currentTimeMillis();

            updateFileInfo(file.fi.fileName, file.fi);
        }
        return true;
    }

    private void updateFileInfo(String fileName, FileInfo fileInfo) {
        fdMap.forEach((k, v) -> {
            if (v.fi.fileName.equals(fileName)) {
                fdMap.get(k).fi = fileInfo;
            }
        });
//        for (Integer blockId : newBlockIdList) {
//            nameNode.registerBlock(oldBlockId, blockId);
//        }
//        System.out.println(fileInfo.toString());
        nameNode.registerBlock(fileName, fileInfo.toString());
    }


    @Override
    public byte[] read(int fd) {
        byte[] res = new byte[0];
        File file = fdMap.get(fd);
        if (file == null) {
            System.out.println("INFO: fd not found");
            return null;
        }
//        System.out.println(file.fd.toString());
        if ((file.fd.mode & 1) == 0) {
            System.out.println("INFO: READ not allowed");
            return null;
        }
        Set<Integer> blockIds = file.fi.blockIdToDataNodeId.keySet();
        for (Integer blockId : blockIds) {
            int dataNodeId = file.fi.blockIdToDataNodeId.get(blockId);
//            DataNode dataNode = dataNodes[dataNodeId];
            DataNode dataNode = dataNodeList.get(dataNodeId - 1);
            byte[] bytes = dataNode.read(blockId);
            if (bytes == null) {
                System.out.println("INFO: read failed");
                return null;
            }
            byte[] tmp = new byte[res.length + bytes.length];
            System.arraycopy(res, 0, tmp, 0, res.length);
            System.arraycopy(bytes, 0, tmp, res.length, bytes.length);
            res = tmp;
        }

        file.fi.lastAccess = System.currentTimeMillis();
        updateFileInfo(file.fi.fileName, file.fi);
        return res;
    }

    @Override
    public void close(int fd) {
        if (fdMap.containsKey(fd)) {
            nameNode.close(fdMap.get(fd).fd.toString());
            fdMap.remove(fd);
        } else System.out.println("INFO: fd not found");
    }

    void parse(String str) {
        String[] args = str.split("\\s+");
//        drop empty string
        ArrayList<String> list = new ArrayList<>();
        for (String s : args) {
            if (!s.isEmpty()) {
                list.add(s);
            }
        }
        args = list.toArray(new String[0]);

        // 01 read 10 write 11 r+w
        if (args[0].equals(CMD_OPEN)) {
            if (args.length != 3) {
                System.out.println("Usage: open <filepath> <mode>");
                return;
            }
            int mode = 0;
            if (args[2].contains("r")) {
                mode |= 1;
            }
            if (args[2].contains("w")) {
                mode |= 2;
            }
            int fd = open(args[1], mode);
            if (fd == -1) {
                System.out.println("INFO: open failed");
                return;
            }
            System.out.println("INFO: fd=" + fd);
        } else if (args[0].equals(CMD_READ)) {
            if (args.length != 2) {
                System.out.println("Usage: read <fd>");
                return;
            }
            int fd;
            try {
                fd = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Usage: read <fd>");
                return;
            }
            byte[] read = read(fd);
            if (read != null) {
                System.out.println(new String(read));
            }
        } else if (args[0].equals(CMD_APPEND)) {
            if (args.length < 3) {
                System.out.println("Usage: append <fd> <content>");
                return;
            }
            int fd;
            try {
                fd = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Usage: append <fd> <content>");
                return;
            }
            Pattern pattern = Pattern.compile(args[1]);
            Matcher matcher = pattern.matcher(str);
            if (!matcher.find()) {
                System.out.println("Usage: append <fd> <content>");
                return;
            }
            String content = str.substring(matcher.end() + 1);
            append(fd, content.getBytes());
        } else if (args[0].equals(CMD_CLOSE)) {
            if (args.length != 2) {
                System.out.println("Usage: close <fd>");
                return;
            }
            int fd;
            try {
                fd = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Usage: close <fd>");
                return;
            }
            close(fd);
            System.out.println("INFO: fd " + fd + " closed");
        } else if (args[0].equals(CMD_EXIT)) {
            if (args.length != 1) {
                System.out.println("Usage: exit");
                return;
            }
            exit();
            System.out.println("INFO: bye");
            System.exit(0);
        } else {
            System.out.println("INFO: unknown command");
        }
    }

    private void exit() {
        if (fdMap.isEmpty()) {
            return;
        }
        while (!fdMap.keySet().isEmpty()) {
            close(fdMap.keySet().iterator().next());
        }
//        fdMap.keySet().forEach(this::close);
    }

    public ClientImpl() {
//        Arrays.fill(ids, -1);
        try {
            String[] args = {};
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");//ORB IP
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");
            //0RB port
            // new ORB object
            ORB orb = ORB.init(args, properties);
            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // obtain a remote object
            nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
            System.out.println("NameNode is obtained.");
            for (int dataNodeId = 1; true; dataNodeId++) {
                try {
                    dataNodeList.add(DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId)));
//                    dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
                    System.out.println("DataNode" + dataNodeId + " is obtained.");
                } catch (Exception ignored) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


