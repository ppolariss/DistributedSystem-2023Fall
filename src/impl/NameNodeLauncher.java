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

public class NameNodeLauncher {
    public static void main(String[] args) {
//        final int MAX_DATA_NODE = 2;
//        DataNode[] dataNodes = new DataNode[MAX_DATA_NODE];
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

            // new a object
            NameNodeImpl nameNodeServant = new NameNodeImpl();
            // / export
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(nameNodeServant);
            NameNode href = NameNodeHelper.narrow(ref);
            // Naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);


            // bind to Naming
            NameComponent[] path = ncRef.to_name("NameNode");

//            nameNodeServant.setNcRef(ncRef);

            ncRef.rebind(path, href);
            System.out.println("NameNode is ready and waiting...");
            // waiting
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
