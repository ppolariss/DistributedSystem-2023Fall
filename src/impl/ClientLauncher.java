package impl;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientLauncher {
    public static void main(String[] args) {
        ClientImpl client = new ClientImpl();
        try {
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.print(">> ");
                String s;
                try {
                    s = in.nextLine();
                    client.parse(s);
                } catch (NoSuchElementException nsee) {
                    s = "exit";
                    client.parse(s);
                    break;
                }
//            if (string.isEmpty()) {
//                continue;
//            }
//            System.out.println(string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //    public static class Output implements Runnable {
//
//        public void run() {
//            while (true) {
//                String message = connImpl.receiveMessage(token);
//                if (!message.isEmpty()) {
//                    System.out.println(message);
//                } else {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//    }
//
//    public static void main(String args[]) {
//        try {
//            // create and initialize the ORB
//            ORB orb = ORB.init(args, null);
//
//            // get the root naming context
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//
//            // Use NamingContextExt instead of NamingContext. This is
//            // part of the Interoperable naming Service.
//            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
//
//            // resolve the Object Reference in Naming
//            String name = "Conn";
//            connImpl = ConnHelper.narrow(ncRef.resolve_str(name));
//
//            System.out.println("Obtained a handle on server object: " + connImpl);
//            token = connImpl.connect();
//
//            new Thread(new Input()).start();
//            new Thread(new Output()).start();
//
//        } catch (Exception e) {
//            System.out.println("ERROR : " + e);
//            e.printStackTrace(System.out);
//        }
//    }
}

//public class ClientStart() {
//    public static void main(String[] args) {
//        ClientImpl client = new ClientImpl();
//        Scanner in = new Scanner(System.in);
//        while (true) {
//            String s = in.nextLine();
//            String string = client.parse(s);
//            System.out.println(string);
//        }
//    }
//
//    //, impl.Input
//    public static class Input implements Runnable {
//        public void run() {
//            ClientImpl client = new ClientImpl();
//            Scanner in = new Scanner(System.in);
//            while (true) {
//                String s = in.nextLine();
//                client.parse(s);
//            }
//        }
//    }
//
//    public static class Output implements Runnable {
//        public void run() {
//            while (true) {
//                String message = connImpl.receiveMessage();
//                if (!message.isEmpty()) {
//                    System.out.println(message);
//                } else {
//                    try {
//                        Thread.sleep(200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//
//    public static void main(String[] args) {
//        ClientImpl client = new ClientImpl();
//        new Thread(() -> {
//            Scanner in = new Scanner(System.in);
//            while (true) {
//                String s = in.nextLine();
//                client.parse(s);
//            }
//        }).start();
//        new Thread(() -> {
//            String message = client.receiveMessage();
//            if (!message.isEmpty()) {
//                System.out.println(message);
//            } else {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//}
