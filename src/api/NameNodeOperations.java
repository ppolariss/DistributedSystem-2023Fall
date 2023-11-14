package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月14日 星期二 下午04时08分20秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fd);
  void registerDataNode (int dataNodeId, String s);
  void registerBlock (int oldBlockId, int newBlockId);
  String updateFile (String filepath);
} // interface NameNodeOperations
