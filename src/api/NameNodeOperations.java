package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月13日 星期一 下午09时42分08秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fd);
  void registerDataNode (int dataNodeId, int maxBlockId);
  void registerBlock (int oldBlockId, int newBlockId);
  String updateFile (String filepath);
} // interface NameNodeOperations
