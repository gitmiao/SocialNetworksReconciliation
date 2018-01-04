For all three tasks, the command to run the program is
cd taks1(2,3)
javac *.java
java -Xms512M -Xmx1024M Main

I think my algorithm in task1 and task2 both outperforms the one in the paper. 
Please run all my three tasks on the large data set, if you think it runs for too long, you could kill the program, 
and the output.txt will contain (almost) all matches it have found when it is killed. However, killing my program is 
discouraged, because generally speaking, the time spent to find a new match will keep decreasing as the program finds 
more and more matches, so just wait for a few more minutes could give the program a chance to match much more nodes.

Task1
YOU CAN TEST MY ALGORITHM WITH LARGE DATASET
My algorithm is based on the one in Korula Nitish and Silvio Lattanzi's paper, with one step forward:
They didn't provide a "tie break" rule in their algorithm, i.e. how to choose the one to match if there are multiple pairs with 
the same maximum similarity score. I solve it by first group pairs by the set of similarity witnesses, and if there is a group that 
have only one pair, then return it. Otherwise (all groups have multiple pairs), I pick the pair that is most likely to be the real match. 
When computing the likelihood, I am using the degree information for each pair of nodes, and derive a probability that these two nodes have the 
same degree in the origin graph (i.e. can be the same user). The probability calculation depends on the ratio of s1 and s2, and in real networks, 
we can estimate the ratio by computing the average number of friend a user have in the social network and make assumption that the population 
that use the two social networks are similar.
Using this algorithm, I can almost match all pairs of users. I simulated a few test cases using the same parameters as in the specification, 
and the result are:
Test1: number of matches reported: 49952, number of correct matches: 49840, precision rate: 99.78%
Test1: number of matches reported: 49946, number of correct matches: 49832, precision rate: 99.77%
Test1: number of matches reported: 49984, number of correct matches: 49964, precision rate: 99.96%
Test1: number of matches reported: 49982, number of correct matches: 49954, precision rate: 99.94%
The simulator program is at task1/simulator/GenerateNetwork.java

Task2 
YOU CAN TEST MY ALGORITHM WITH LARGE DATASET
My algorithm is based on the "Time-dependent Multinomial Model" in Luca Rossi and Micro Musolesi's paper, with a few changes:
1 I also separate the data points by weekday and weekend as people tends to go to different places on weekdays and weekends. 
2 I use 3 time units instead of 4 as proposed in the paper: midnight - 8am, 8am to 7pm and 7pm to midnight. 
3 I also checked the date range of the training data and testing data for each pair, and penalize the score computed by 
"Time-dependent Multinomial Model" if (generally speaking )dates in the testing data are not in the date range of the training data.
4 Because checking all possible pairs at each iteration will take too much time. I updated the algorithm to: 
	Order all users in N2 by number of check in records
	For threshold = 0.9, 0.09, 0.009, ... 9e-20, -Inf
		For each user in N2
			For each user in N1
				compute score of pair N1 N2, 
				if the score is greater than threshold, match N1 and N2, and delete them from unmatched user set
				
My algorithm can output all pairs of users, with precision rate around 83% (n=10) on the sample data. I also simulated a few test cases, 
and the results are similar. 
The simulator program is at task2/simulator/DataGenerator.java

Taks3 
YOU CAN TEST MY ALGORITHM WITH LARGE DATASET
My algorithm is similar to the "Time-dependent Multinomial Model" I used in task2, with a few changes:
1 Check in data has weight, the more it is closer to Jan 1 2010, the biger the weight is.
2 For users that doesn't have overlap in location at all, I check the similarity in frequency of check-ins at weekdays and 
weekend as well as different session of a day. Then match the pair with highest similarity. This approach is not very successful though.

My algorithm can output all pairs of users, with precision rate 62%   
  
