/**
 * Created by jo on 23/07/17.
 */
/************************************************************************
 *Distributed Systems1 Course Project                                   *
 * Yoseph M.M and Selamawit Y.G                                         *
 * Distributed Key-Value Store with Data Partitioning and Replication   *
 ************************************************************************
 */
import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Cancellable;
import scala.concurrent.duration.Duration;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class NodeApp {
    static private String remotePath = null; // Akka path of the bootstrapping peer
    static private int myId; // ID of the local node
    //static private String command = null;
    static private int N_sysparametres = 5; //N is a system parameter that defines the degree of replication
    static private int R_read = 3; //Number of Read Quorum
    static private int W_write = 3; //Number of Write Quorum
    static private int T_timer = 600; //Timeout duration
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
    public static class RequestNodelist implements Serializable {}
    public static class Nodelist implements Serializable {
        Map<Integer, ActorRef> nodes;
        public Nodelist(Map<Integer, ActorRef> nodes) {
            this.nodes = Collections.unmodifiableMap(new HashMap<Integer, ActorRef>(nodes));
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
            else
                unhandled(message);		// this actor does not handle any incoming messages
        }
    }

    public static void main(String[] args) {

        if (args.length != 0 && args.length !=2 ) {
            System.out.println("Wrong number of arguments: [remote_ip remote_port]");
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
        else
            System.out.println("Starting disconnected node " + myId);

       // if(myId<0||myId>10000){																	//10000 is Random number to add this feature
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
        //final ActorSystem system = ActorSystem.create("mysystem", config);
        final ActorSystem system = ActorSystem.create("mysystem", config);


        // Create a single node actor
        final ActorRef receiver = system.actorOf(
                Props.create(Node.class),	// actor class
                "node"						// actor name
        );
    }

}
