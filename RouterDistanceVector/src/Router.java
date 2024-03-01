import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Router {

    //----------------------------------Main---------------------------------------
    public static void main(String[] args) throws UnknownHostException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Input Router Name:\n");
        String name = scanner.nextLine();

        System.out.println("Input Router IP Address:\n");
        String IP = scanner.nextLine();

        System.out.println("Input Router Port Number:\n");
        int portnum = Integer.parseInt(scanner.nextLine());

        GenerateRouter(name,IP,portnum);

        System.out.println();
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

    //--------------Start---------------
    public void start(){
        CalcDistanceVector();
        PrintData();
    }
    //-----------------------------------

    //-----------------------------Distance Vector----------------------------------
    public void CalcDistanceVector(){

    }
    //------------------------------Display Data-------------------------------------
    public void PrintData(){
        //For each subnet in config file
            //Count the number of subnets between this router and that subnet ++

        //For each subnet in config file
            //Find the first (next hop) subnet to the
                //destination subnet from this router

        String format = "Distance Cost From " + this.name + " to " +
                         "Router in List: " + "CalcDistance Vector(name, otherName)";
    }
    //----------------------------Router Generation---------------------------------
    public static void GenerateRouter(String name, String IpAddress, int Port){
        try{
            Router router = new Router(name, IpAddress, Port);
            router.start();
        }catch (UnknownHostException e){
            System.err.println("Router setup error: " + e.getMessage());
        }
    }
    //--------------------------------Next Hop--------------------------------------
    public String NextHop(){
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
