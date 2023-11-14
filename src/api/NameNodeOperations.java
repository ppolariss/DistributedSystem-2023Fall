package api;


/**
* api/NameNodeOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��api.idl
* 2023��11��13�� ����һ ����09ʱ42��08�� CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fd);
  void registerDataNode (int dataNodeId, int maxBlockId);
  void registerBlock (int oldBlockId, int newBlockId);
  String updateFile (String filepath);
} // interface NameNodeOperations
