package utils;

import com.google.gson.Gson;

public class Block {
    public int id;
    public int size;

    public Block(Integer blockId, Integer integer) {
        this.id = blockId;
        this.size = integer;
    }

    public static Block fromString(String str) {
        if (str.isEmpty() || str.equals("null")) {
            return null;
        }
        return new Gson().fromJson(str, Block.class);
    }
}
