/**
 * Created by jo on 24/07/17.
 * The purpose of this class is t0 handle all the jobs that ClientApp must do
 */
import java.io.Serializable;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import akka.actor.Props;
import akka.actor.Cancellable;
import scala.concurrent.duration.Duration;

public class ClientApp {
    static private String remotePath = null; // Akka path of the bootstrapping peer
    static private String usrinput = null;  // the request by client user on terminal
    static private String myId;
    static private String ip = null;
    static private String port = null;
    static private String value = null;
    static private String key_value = null;
    //static private key = null;
    public static abstract class ClientUser extends UntypedActor {
        public void preStart() {
            if (remotePath !=null){
                switch (usrinput){
                    case "leave":
                }
            }
        }

    }


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
        //To validate port number // <summary>
        //Port number is an unsigned short from 1-65535,
        // ports under 1024 are reserved for system services http, ftp, etc.
        /// Checks if the given string is a valid port number.

      //  public static boolean ValidportNum(String PortNum) {
         //   String[] input = PortNum;
       //     if (input.length != 1) {
         //       return false;
            //}
           //for (String str : input) {
              //  int i = Integer.parseInt(str);
                //if ((i < 0) || (i > 65536)) {
                  //  return false;
                //}
            //}
            //return true;
        //}

public static void main (String[] args) {
    //usrinput = args[2]
    if (args.length < 3 && args.length > 5) {
        System.out.println("Invalid user input length : Please,use the the input with length 3-5 ");
        return;
    }
    //System.out.println("Welcome to user screen");
    if (!validateIPAddress(args[0])) {
        System.out.println("The Ip address is invalid");
        return;
    }
    //if (!ValidportNum(args[1])) {
      //  System.out.println("The port number is invalid");
      //  return;
   // }
    Config config = ConfigFactory.load("application");
    System.out.println("start loading configuration");
    ip = args[0];
    port = args[1];
    usrinput = args[2];
    if (args.length == 3 && args[2].equals("leave")) {              //leave request
        //ValidportNum(args[1]);
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
    } else if (args.length == 4 && args[2].equals("read")) {           //read
        key_value = args[3];
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
        // The Akka path to the bootstrapping peer
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
    } else if (args.length == 5 && args[2].equals("write")) {          //write
        // System.out.println("write ");
        key_value = args[3];
        value = args[4];
        remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";

        // Create the actor system
        final ActorSystem system = ActorSystem.create("mysystem", config);
        // Creating single user to communicate with NodeApp
        final ActorRef ClientUser = system.actorOf(
                Props.create(ClientUser.class),    // actor class
                "client");                        // actor name);
    }
    else
        System.out.println("Wrong number of arguments: [remote_ip remote_port 'read' or 'write' or 'leave' key value]");

}
}
