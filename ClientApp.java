/**
 * Created by jo on 24/07/17.
 * The purpose of this class is t0 handle all the jobs that ClientApp must do
 */
import java.io.Serializable;

import akka.Version;
import akka.actor.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ClientApp {
    static private String remotePath = null; // Akka path of the bootstrapping peer
    static private String usrinput = null;  // the request by client user on terminal
    static private String myId;
    static private String ip = null;
    static private String port = null;
    static private String value = null;
    static private String key= null;
    static private String version = null;
    public static final int clientTimeout_seconds = 15;

    public static class client extends UntypedActor{
        public void onReceive(Object message){
            //if (remotePath !=null){
                //switch (usrinput){
                   // case "args[3]": //Leave request

                      //  break;
                    //case "args[4]": //read request


               // }
            //}


        }

    }




    //this class is to request read
    //to validate Ip address

        public static boolean validateIPAddress(String ipAddress) {
            String[] input = ipAddress.split("\\.");
            if (input.length != 4) {
                return false;
            }
            for (String str : input) {
                int i = Integer.parseInt(str);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            return true;
        }

        //According to RFC 793, the port is a 16 bit unsigned int.
           //This means the range is 0 - 65535( 2^16 - 1)
          // ports under 1024 are reserved for system services http, ftp, etc.

    public static boolean ValidportNum (String PortNum) {
        try {
            //Port number validation (16bit data)
            if ( (Integer.parseInt(PortNum) < 0) || (Integer.parseInt(PortNum) > 65535)) {
                return false;
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

public static void main (String[] args) throws Exception {
    //usrinput = args[2]
    if (args.length < 3 || args.length > 5) {
        System.out.println("Invalid user input length : Please,use the the input with length 3-5 ");
        return;
    }
    System.out.println("Welcome to user screen");
    if (!validateIPAddress(args[0])) {
        System.out.println("The Ip address is invalid");
        return;
    }
    if (!ValidportNum(args[1])) {
       System.out.println("The port number is invalid");
       return;
    }
    Config config = ConfigFactory.load("application");
    System.out.println("start loading configuration");
    ip = args[0];
    port = args[1];
    //usrinput = args[2];   // argument 2 is the user input either read,write or leave
    if (args.length == 3 && args[2].equals("leave")) {              //leave request
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";


        System.out.println("The node is which left the ring is" + ip + ":" + port);
    }





    else if (args.length == 4 && args[2].equals("read")) {           //read request
        key = args[3];
       // value = args[4];
        // The Akka path to the bootstrapping peer
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";

        // convert the key from string to int
        int key1 = Integer.parseInt(key);

        // ask to the client
        ActorSystem system = ActorSystem.create("mysystem", config);
        ActorSelection myTargetNode = system.actorSelection(remotePath);

        // send the real request
        Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
        Future<Object> request = Patterns.ask(myTargetNode, new NodeApp.ClientReadRequest(key1), timeout);

        // we wait for a reply
        Object reply = Await.result(request, timeout.duration());
        NodeApp.ClientReadAnswer answer = (NodeApp.ClientReadAnswer) reply;

        // read the answer
        int value = answer.getValue();
        System.out.println("The Read value is " + value);
            //This one is to eliminator the waiting for forever
        system.shutdown();

        // random string
        String randomID = UUID.randomUUID().toString();

    }
        else if (args.length == 5 && args[2].equals("write")) {          //write request
        // System.out.println("write ");
        key = args[3];
        value  = args[4];
        //version= args[5];
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
   //Converting from string to int
        int key2 = Integer.parseInt(key);
        //int version = Integer.parseInt(version);

        // Create the actor system
        ActorSystem system = ActorSystem.create("mysystem", config);
        // Creating single user to communicate with NodeAp
        ActorSelection myTargetNode = system.actorSelection(remotePath);
        Timeout timeout = new Timeout(15, TimeUnit.SECONDS);
        //Using Future’ will send a message to the receiving Actor asynchronously and will immediately return a ‘Future’.
        Future<Object> request = Patterns.ask(myTargetNode, new NodeApp.ClientWriteRequest(key2,version), timeout);
        // we wait for a write reply
        Object reply = Await.result(request, timeout.duration());
        NodeApp.ClientWriteAnswer answer = (NodeApp.ClientWriteAnswer) reply;
        // Write the answer
        int value = answer.getValue();
        System.out.println("The write value and version is  " + value + version);
        //This one is to eliminator the waiting for forever
        system.shutdown();
    }
    else
        System.out.println("Wrong number of arguments: [remote_ip remote_port 'read' or 'write' or 'leave' or key value]");

}
}
