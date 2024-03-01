### Task Allocation:  
Router class - Mason  
DistanceVector function (inside router class) - Cyarina   
Parser - Adrianna  
Main - Leah  

# The Assignment    

Routers can see their direct neighbors at startup, but not beyond, which is why they need to run the distanceVector protocol to learn about the rest of the network.  

### Each router:    
- Runs the distanceVector (DV) protocol.  
- Has a list of all the routers in the network, and it sends its- DV to all of its neighbors.   
- Receives DVs from its neighbors and updates its own DV accordingly.   
- Has a list of all the subnets in the network, and uses the DV to determine the best path to each subnet.   
- Has a list of all the hosts in the network, and uses the DV to determine the best path to each host.  

### To implement, you need:       
- A Router class with Distance Vector protocol in it.  
- A map with key: subnet, value: overall distance (total of costs).  

- For routing purposes, in the context of distance vector routing protocols, the cost of directly connected subnets is typically considered the cost to reach that subnet from the router. Don't count the destination subnet in the cost.  

For example, R1 to N4 has a cost of 1, and R1 to N1, N2, and N3 has a cost of 0.  

 - Enable multiple router (thread) instances in IntelliJ.  

# Results   

## Initial Setup  

### Directly Connected Subnets  
- The cost to reach a subnet that is directly connected to a router is considered `1`. This demonstrates the concept of cost through hops.  

### Router-to-Router Links  
- Since R1 is connected to R2, and R2 is connected to R3, the cost between each directly connected router pair is `1`.  

## Distance Vectors (Initial State)

### R1's Distance Vector  
- To N1, N2, N3: Cost `1` (directly connected).  
- R1 does not initially know about N4, N5, or N6.  

### R2's Distance Vector  
- To N3, N4: Cost `1` (directly connected).  
- R2 does not initially know about N1, N2, N5, or N6.  

### R3's Distance Vector  
- To N4, N5, N6: Cost `1` (directly connected).  
- R3 does not initially know about N1, N2, or N3.  

## After DV Exchange  

### R1's Updated DV  
- To N1, N2, N3: Cost `1`.  
- To N4: Cost `2` (1 to R2 + 1 from R2 to N4 directly).  
- To N5, N6: Cost `3` (1 to R2 + 1 to R3 from R2 + 1 from R3 to N5/N6 directly).  

### R2's Updated DV  
- To N1, N2: Cost `2` (1 to R1 + 1 from R1 to N1/N2 directly).  
- To N3, N4: Cost `1`.  
- To N5, N6: Cost `2` (1 to R3 + 1 from R3 to N5/N6 directly).  

### R3's Updated DV  
- To N1, N2: Cost `3` (1 to R2 from R3 + 1 to R1 from R2 + 1 from R1 to N1/N2 directly).  
- To N3: Cost `2` (1 to R2 + 1 from R2 to N3 directly).
- To N4, N5, N6: Cost `1`.  

## Notes  

### Direct Connections  
- Costs to directly connected subnets are set to `1` to indicate direct accessibility, reflecting a minimal but nonzero cost for routing purposes.  

### Routing Through Intermediaries  
- The costs increase by `1` for each router hop needed to reach a subnet not directly connected to the originating router.  

### Dynamic Nature  
- These DVs will adjust as routers exchange information. The outlined initial and updated states assume a simplified immediate convergence for illustration purposes.  

- Each router learns the best path to each subnet through periodic exchanges of distance vectors with its neighbors.  
