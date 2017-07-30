/**
 * Created by jo on 23/07/17.
 */
/************************************************************************
 *Distributed Systems1 Course Project                                   *
 * Yoseph M.M and Selamawit Y.G                                         *
 * Distributed Key-Value Store with Data Partitioning and Replication   *
 ************************************************************************
 */
import java.beans.EventHandler;
import java.io.Serializable;
import java.util.EventListener;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import akka.Version;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Cancellable;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;
//The Node class we do the tasks of "Join, Leave, Join write, Join read ,recovery...etc for node" and
//For client Read,write,join,leave,read version,locale storage,
public class NodeApp {
    static private String remotePath = null; // Akka path of the bootstrapping peer
    static private int myId; // ID of the local node
    static private String value = null;
    public static  String nodmsg = null;
    public static final int N_sysparametres = 3; //N is a system parameter that defines the degree of replication
    public static final int R_read = 2; //Number of Read Quorum
    public static final int W_write = 2; //Number of Write Quorum
    static private int T_timer = 10; //Timeout duration in second for internal nodes
    //static private int msTimeoutJoin = 1000;
    //static private int msDataRequest = 5000;
    //static private int msTimeoutRead = T;
    //static private int msTimeoutWrite = T;
    //static public String clientPath = null;
    public static class Join implements Serializable {
        int id;
        public Join(int id) {
            this.id = id;
        }
    }
    public static class RequestNodelist implements Serializable {

    }
    public static class Nodelist implements Serializable {
        Map<Integer, ActorRef> nodes;
        public Nodelist(Map<Integer, ActorRef> nodes) {
            this.nodes = Collections.unmodifiableMap(new HashMap<Integer, ActorRef>(nodes));
        }
    }
    //Read request for replicated nodes
    public static class Nodereadrequest implements Serializable{
        String ip;
        String  port;
        int nodeId;
        int version;

        public Nodereadrequest(String ip, String port, int nodeId, int version) {
            this.ip = ip;
            this.port = port;
            this.nodeId = nodeId;
            this.version = version;
        }
    }
    //Read answer for coordinator for the read request
    public static class NodereadAnswer implements Serializable{
        String ip;
        String port;
        int nodeId;
        int version;

        public NodereadAnswer(String ip, String port, int nodeId, int version) {
            this.ip = ip;
            this.port = port;
            this.nodeId = nodeId;
            this.version = version;
        }
    }
    public static class Nodewriterequest implements Serializable{
        String value;
        int key;
        int version;

        public Nodewriterequest(String value, int key, int version) {
            this.value = value;
            this.key = key;
            this.version = version;
        }
    }
    public static class Leave implements Serializable{
        int id;
        public Leave(int id) {  //in this point we can use also port number since it is identical
            this.id = id;
    }
    }

    // client read request  -> a random node
    public static class ClientReadRequest implements Serializable{
        private int key;

        public int getKey() {
            return key;
        }

        public ClientReadRequest(int key) {
            this.key = key;
        }
    }

    public static class ClientReadAnswer implements Serializable {
        private int key;
        private int value;

        public ClientReadAnswer(int key, int value) {
            this.key = key;
            this.value = value;
        }

        public int getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }
    }

    //client write request  -> a random node
public static class ClientWriteRequest implements Serializable{
    private int key;
    private String version;

    public int getKey() {
        return key;
    }

    public String getVersion() {
        return version;
    }

        public ClientWriteRequest(int key, String version) {
            this.key = key;
            this.version = version;
        }
    }
    public static class ClientWriteAnswer implements Serializable{
        private int key;
        private int value;
        private String version;

        public int getKey() {
            return key;
        }

        public int getValue() {
            return value;
        }

        public String getVersion() {
            return version;
        }

        public ClientWriteAnswer(int key, int value, String version) {
            this.key = key;
            this.value = value;
            this.version = version;
        }
    }


    //The Joined guest node requesting information from exiting nodes
    public static class nodesInfo implements Serializable{
        int id;
        ActorRef guest;
        Map<Integer, ActorRef> newNode;
        public nodesInfo(int id ,ActorRef guest,Map<Integer,ActorRef> newNode){
            this.id = id;
            this.guest = guest;
            this.newNode = newNode;
        }
    }

    public static class Node extends UntypedActor {
        // The table of all nodes in the system id->ref
        private Map<Integer, ActorRef> nodes = new HashMap<>();

        public void preStart() {
            if (remotePath != null) {
                getContext().actorSelection(remotePath).tell(new RequestNodelist(), getSelf());
            }
            nodes.put(myId, getSelf());
        }

        public void onReceive(Object message) {
            if (message instanceof RequestNodelist) {
                getSender().tell(new Nodelist(nodes), getSelf());
            }
            else if (message instanceof Nodelist) {
                nodes.putAll(((Nodelist)message).nodes);
                for (ActorRef n: nodes.values()) {
                    n.tell(new Join(myId), getSelf());
                }
            }
            else if (message instanceof Join) {
                int id = ((Join)message).id;
                System.out.println("Node " + id + " joined");
                nodes.put(id, getSender());
            }

            // read operation from the client
            else if(message instanceof ClientReadRequest) {
                ClientReadRequest request =(ClientReadRequest) message;
                // TODO> implement protocol
                //int value1 = Integer.parseInt(value);
                getSender().tell(new ClientReadAnswer(request.getKey(), 12), getSelf());
            }
            //write operation from client
            else if (message instanceof ClientWriteRequest) {
                ClientWriteRequest request = (ClientWriteRequest) message;
                getSender().tell(new ClientWriteAnswer(request.getKey(), 13,request.getVersion()), getSelf());
            }
            else if (message instanceof Leave){
                System.out.println("The Node have to share data before leaving");

            }
            else
                unhandled(message);		// this actor does not handle any incoming messages
        }
    }

    public static void main(String[] args) {
                    //the will check node IP and Port
        if (args.length == 1|| args.length > 3) {
            System.out.println("Please use the argument: [remote_ip remote_port]");
            return;
        }

        // Load the "application.conf"
        Config config = ConfigFactory.load("application");
        myId = config.getInt("nodeapp.id");
        if (args.length == 2) {
            // Starting with a bootstrapping node
            String ip = args[0];
            String port = args[1];
            // The Akka path to the bootstrapping peer
            remotePath = "akka.tcp://mysystem@"+ip+":"+port+"/user/node";
            System.out.println("Starting node " + myId + "; bootstrapping node: " + ip + ":"+ port);
        }
        else if (args.length == 3) {
            if (args[0].equals("join")) {
                nodmsg = "join";
                String ip = args[1];
                String port = args[2];
                // The Akka path to the bootstrapping peer
                remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
                System.out.println("The Joining Node is  " + ip + ":" + port);
            } else if (args[0].equals("recover")) { //here the recovery will be occurred
                nodmsg = "recover";
                String ip = args[1];
                String port = args[2];
                // The Akka path to the bootstrapping peer
                remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
                System.out.println("Successful Recovery for Node  " + ip + ":" + port);

            }
            else System.out.println("Invalid data: Please check your input data [spelling,ip or port]");
        }

        else
            System.out.println("Starting disconnected node " + myId);

       // if(myId<0||myId>10000){				//10000 is Random number to add this feature
           // System.out.println("Invalid node id. Please, use in between 0 and 10000");
           // return;




       // N = 5;
       // R = 3;
       /// W = 3;
       // T = 600;
      //  msTimeoutRead = T;
      //  msTimeoutWrite = T;
       // if (R + W <= N) {
          //  System.out.println("Please, replication parameters are not satisfied the condition R+N > N for node " + myId);
         //   return;

       // if (args.length == 3) {
            // Starting with a bootstrapping node
           // String ip = args[1];
          //  String port = args[2];
            // The Akka path to the bootstrapping peer
          //  remotePath = "akka.tcp://mysystem@" + ip + ":" + port + "/user/node";
        //    System.out.println("Starting node " + myId + "; bootstrapping node: " + ip + ":" + port);
     //   } else
        //    System.out.println("Starting disconnected node " + myId + " with replication paramenters equal to N=" + N
           //         + " writing quorum W=" + W + ", reading quorum equal to R=" + R + " and timeout set to T=" + T
            //        + " [ms].");


        // Create the actor system
        final ActorSystem system = ActorSystem.create("mysystem", config);


        // Create a single node actor
        final ActorRef receiver = system.actorOf(
                Props.create(Node.class),	// actor class
                "node"						// actor name
        );
    }

}
