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


    private static final int MAX_DATA_NODE = 4;
    private NameNode nameNode;
    private DataNode[] dataNodes = new DataNode[MAX_DATA_NODE];


    //    fdMap: fd -> file
    private HashMap<Integer, File> fdMap = new HashMap<>();


    @Override
    public int open(String filepath, int mode) {
        String fileInfo = nameNode.open(filepath, mode);
        if (Objects.equals(fileInfo, "null")) {
            return -1;
        }
        File file = File.fromString(fileInfo);
        int fd = Math.toIntExact(file.fd.getId());
        fdMap.put(fd, file);
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
        Set<Integer> blockIds = file.fi.blockIdToDataNodeId.keySet();
        int maxBlockId = blockIds.stream().max(Integer::compareTo).orElse(-1);
        int dataNodeId = file.fi.blockIdToDataNodeId.get(maxBlockId);
        DataNode dataNode = dataNodes[dataNodeId];

//        TODO
        byte[] send = new byte[4 * 1024];
        System.arraycopy(bytes, 0, send, 0, bytes.length);
        dataNode.append(maxBlockId, send);

        updateFile(file.fi.fileName);
        System.out.println("INFO: write done");
    }

    //    access nameNode after append to update if necessary
    private void updateFile(String fileName) {
        String info = nameNode.updateFile(fileName);
        if (info.equals("null")) {
            return;
        }

        fdMap.forEach((k, v) -> {
            if (v.fi.fileName.equals(fileName)) {
                fdMap.get(k).fi = FileInfo.fromString(info);
            }
        });
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
            DataNode dataNode = dataNodes[dataNodeId];
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
        return res;
    }

    @Override
    public void close(int fd) {
        nameNode.close(fdMap.get(fd).fd.toString());
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
            System.out.println("INFO: bye");
            System.exit(0);
        } else {
            System.out.println("INFO: unknown command");
        }
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
            for (int dataNodeId = 1; dataNodeId < 2; dataNodeId++) {
                dataNodes[dataNodeId] = DataNodeHelper.narrow(ncRef.resolve_str("DataNode" + dataNodeId));
                System.out.println("DataNode" + dataNodeId + " is obtained.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


