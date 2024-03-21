//Mason

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Router {
    //----------------------------------Main---------------------------------------
    public static void main(String[] args) throws UnknownHostException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Input Router Name:\n");
        String name = scanner.nextLine();

        System.out.println("Input Router IP Address:\n");
        String address = scanner.nextLine();

        System.out.println("Input Router Port Number:\n");
        int port = Integer.parseInt(scanner.nextLine());

        GenerateRouter(name,address,port);

        System.out.println();
    }
    //-----------------------------------------------------------------------------
    public static void LaunchRouter(String name, String IpAddress, int port){
        //Generate Router
        //GenerateRouter(name,IpAddress,port);

            //Find Subnet neighbors
        String startHeaderSubnet = "#Routers to subnets";
        String endHeaderSubnet = "#End Routers to Subnets List";
        List<String> subnetNeighbors = RouterParser.NeighborFinder(startHeaderSubnet, endHeaderSubnet,name);

        String startHeaderRouter = "#Routers to Routers";
        String endHeaderRouter = "#End Router to Router List";
        List<String> routerNeighbors = RouterParser.NeighborFinder(startHeaderRouter, endHeaderRouter,name);

        //Generate Initial distance Vector
        //TODO
                //distance to connections is always 0
                //next hop is always itself for directly connected subnets
        PrintData(subnetNeighbors, routerNeighbors, name);
        //Subnet,distance vector,nextHop
        //N1,0,R1
        //N2,0,R1

        //Find Router neighbors
        //Send innitial distance vector to neighbors



    }
    //-----------------------------------------------------------------------------

    //-----Define Public Variables-----
    private final String name;
    private final InetAddress Address;
    private final int Port;
    //----------------------------------

    //-----------------------------------Router------------------------------------
    public Router(String name, String IpAddress, int Port) throws UnknownHostException {
        this.name = name;
        this.Address = InetAddress.getByName(IpAddress);
        this.Port = Port;
    }
    //-----------------------------------------------------------------------------

    //-----------------------------Distance Vector----------------------------------
    public void CalcDistanceVector(){
        //Should take 2 arguments, return distance vector
    }
    //------------------------------Display Data-------------------------------------
    public static void PrintData(List<String> subnetNeighbors, List<String> routerNeighbors, String name){
        //ArrayList<Object> displayData = new ArrayList<>();

//Subnet,distance vector,nextHop
        //N1,0,R1
        //N2,0,R1
        //---------------------------------------
        System.out.println("Distance vector data of router: " + name +".\n\n");
        System.out.println("The format to display the distance vector from this router to each subnet is as follows:\n");
        System.out.println("Current Router , Subnet , the distance vector between them , the next hop to reach that subnet.\n");
        //---------------------------------------
        //For each subnet in config file

            //Count the number of subnets between this router and that subnet ++
//
        //For each subnet in config file
            //Find the first (next hop) subnet to the
                //destination subnet from this router

        String format = "Distance Cost From " + name + " to " +
                         "Router in List: " + "CalcDistance Vector(name, otherName)";
    }
    //--------------Start---------------
    public void start(String name, String IpAddress, int Port){
        LaunchRouter(name,IpAddress,Port);
    }
    //-----------------------------------

    //----------------------------Router Generation---------------------------------
    public static void GenerateRouter(String name, String IpAddress, int Port){
        try{
            Router router = new Router(name, IpAddress, Port);
            router.start(name,IpAddress,Port);
        }catch (UnknownHostException e){
            System.err.println("Router setup error: " + e.getMessage());
        }
    }
    //--------------------------------Next Hop--------------------------------------
    public String NextHop(String router){
        String routerName = this.name;
        //call parser
        String nextHop = "";
        return nextHop;
    }
    //-----------------------------------------------------------------------------
}
// Grade is on:
//Ability to print out its distance vector to each subnet
//  AND the next hop from that router to each subnet

// git add .
