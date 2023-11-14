package utils;

import com.google.gson.Gson;

public class File {
    public FileInfo fi;
    public FileDesc fd;

    public File(FileInfo ff, FileDesc fd) {
        this.fi = ff;
        this.fd = fd;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static File fromString(String str) {
        if (str.isEmpty() || str.equals("null")) {
            return null;
        }
        return new Gson().fromJson(str, File.class);
    }
}
