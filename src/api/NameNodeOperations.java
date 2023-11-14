package api;


/**
* api/NameNodeOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��api.idl
* 2023��11��14�� ���ڶ� ����07ʱ23��16�� CST
*/

public interface NameNodeOperations 
{
  String open (String filepath, int mode);
  void close (String fd);
  void registerDataNode (int dataNodeId, String s);
  void registerBlock (String fileName, String fileInfo);
  String updateFile (String filepath);
} // interface NameNodeOperations
