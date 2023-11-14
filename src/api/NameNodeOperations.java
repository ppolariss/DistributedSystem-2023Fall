package api;


/**
* api/NameNodeOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从api.idl
* 2023年11月14日 星期二 下午07时23分16秒 CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fd);
  void registerDataNode (int dataNodeId, String s);
  void registerBlock (String fileName, String fileInfo);
  String updateFile (String filepath);
} // interface NameNodeOperations
