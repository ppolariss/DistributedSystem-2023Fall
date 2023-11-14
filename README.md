# DistributedSystem-2023Fall
Distributed System

Distributed System lab1 in 2023Fall
Follow Hadoop and simulate Client, NameNode and DataNode
Written in java with corba
# Usage
jdk1.8
Gson2.8.0

```
orbd -ORBInitialPort 1050 -ORBInitialHost localhost

javac -cp "lib/gson-2.8.0.jar;src" src/*.java src/api/*.java src/impl/*.java src/utils/*.java -d bin

java -cp "bin/;lib/gson-2.8.0.jar" impl/NameNodeLauncher -ORBInitialPort 1050 -ORBInitialHost localhost

java -cp "bin/;lib/gson-2.8.0.jar" 1 impl/DataNodeLauncher -ORBInitialPort 1050 -ORBInitialHost localhost
%% 此处可以修改数字表示启动第几个dataNode 初始最多可启动4个，可以在clientImpl中修改最大个数 %%

java -cp "bin/;lib/gson-2.8.0.jar" impl/ClientLauncher -ORBInitialPort 1050 -ORBInitialHost localhost
```

# Test
单元测试
根据代码做了部分微调
进行Client单元测试时，务必保证NameNode和至少一个DataNode可用
