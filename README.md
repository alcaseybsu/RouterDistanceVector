Task Allocation:  
Router class - Mason  
DistanceVector function (inside router class) - Cyarina   
Parser - Adrianna  
Main - Leah  

Routers can see their direct neighbors at startup, but not beyond, which is why they need to run the distanceVector protocol to learn about the rest of the network.  

Each router:  
Runs the distanceVector (DV) protocol.  
Has a list of all the routers in the network, and it sends its DV to all of its neighbors.   
Receives DVs from its neighbors and updates its own DV accordingly.   
Has a list of all the subnets in the network, and uses the DV to determine the best path to each subnet.   
Has a list of all the hosts in the network, and uses the DV to determine the best path to each host. 

To implement:  
Need Router class with Distance Vector protocol in it.  
Need a map with key: subnet, value: overall distance (total of costs).  
 
Count only subnets, not routers in the "cost", and don't count the destination subnet.  
For example, R1 to N4 has a cost of 1, and R1 to N1, N2, and N3 has a cost of 0.  
Enable multiple router (thread) instances in IntelliJ.  
