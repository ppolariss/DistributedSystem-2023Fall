package utils;

import com.google.gson.Gson;

//TODO: According to your design, complete the FileDesc class, which wraps the information returned by NameNode open()
public class FileDesc {
    /* the id should be assigned uniquely during the lifetime of NameNode,
     * so that NameNode can know which client's open has over at close
     * e.g., on nameNode1
     * client1 opened file "Hello.txt" with mode 'w' , and retrieved a FileDesc with 0x889
     * client2 tries opening the same file "Hello.txt" with mode 'w' , and since the 0x889 is not closed yet, the return
     * value of open() is null.
     * after a while client1 call close() with the FileDesc of id 0x889.
     * client2 tries again and get a new FileDesc with a new id 0x88a
     */
    static int lastId = 0;
    final long id;
    public int mode;

    public FileDesc(long id) {
        this.id = id;
    }

    public FileDesc(long id, int mode) {
        this.id = id;
        this.mode = mode;
    }

    public FileDesc(int mode) {
        lastId++;
        this.id = lastId;
        this.mode = mode;
    }

    public long getId() {
        return id;
    }

    /* The following method is for conversion, so we can have interface that return string, which is easy to write in idl */
    @Override
    public String toString() {
        return new Gson().toJson(this);
//        return "0x" + Long.toHexString(id);
//        return null;
    }

    public static FileDesc fromString(String str) {
        if (str.isEmpty() || str.equals("null")){
            return null;
        }
        return new Gson().fromJson(str, FileDesc.class);
//        return null;
    }

}
