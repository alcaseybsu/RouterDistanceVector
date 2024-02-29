Routers can see their direct neighbors at startup, but not beyond, which is why they need to run the distanceVector protocol to learn about the rest of the network.  
Each router runs the distanceVector protocol.   
It has a list of all the routers in the network, and it sends its distance vector to all of its neighbors.   
It also receives distance vectors from its neighbors and updates its own distance vector accordingly.   
It also has a list of all the subnets in the network, and it uses the distance vector to determine the best path to each subnet.   
It also has a list of all the hosts in the network, and it uses the distance vector to determine the best path to each host.  
Need Router class with Distance Veector protocol in it.  
map: key: subnet, value: overall distance (total of costs)  
 
Count only subnets, not routers in the "cost" (don't count the destination subnet).  
For example, R1 to N4 = (cost of) 1, R1 to N1, N2, and N3 = (cost of ) 0.  
Enable multiple router instances.  
