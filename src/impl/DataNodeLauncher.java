package impl;

import api.DataNode;
import api.DataNodeHelper;
import api.NameNode;
import api.NameNodeHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

public class DataNodeLauncher {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1"); // 0RB IP
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");
            // 0RB port
            // init ORB object
            ORB orb = ORB.init(args, properties);
            // get RootPOA activate POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

//            // new a object
//            int dataNodeId = DataNodeImpl.numberOfDataNode;
//            DataNodeImpl dataNodeServant = new DataNodeImpl();
//
//            // / export
//            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(dataNodeServant);
//            DataNode href = DataNodeHelper.narrow(ref);
//            // Naming context
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references("DataService");
//            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//
//            // obtain a remote object
//            NameNode nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));
//            dataNodeServant.setNameNode(nameNode);

            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // obtain a remote object
            NameNode nameNode = NameNodeHelper.narrow(ncRef.resolve_str("NameNode"));

            // new a object
            DataNodeImpl dataNodeServant = new DataNodeImpl(nameNode);
            int dataNodeId = DataNodeImpl.numberOfDataNode;

            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(dataNodeServant);
            DataNode href = DataNodeHelper.narrow(ref);

            // bind to Naming
            NameComponent[] path = ncRef.to_name("DataNode" + dataNodeId);
            ncRef.rebind(path, href);
            System.out.println("DataNode" + dataNodeId + " is ready and waiting...");
            // waiting
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
