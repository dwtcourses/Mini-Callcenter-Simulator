# Mini Callcenter Simulator

In many cases, personnel planning is carried out in call centers based on the Erlang-C formula. However, the Erlang-C formula does not take into account many of the characteristics of real call center systems that are important for the performance variables. In the classic formulation, the Erlang C formula does not take into account either wait cancellers or repeaters. There is an extended variant of the Erlang-C formula that includes at least the wait cancellers. In addition, when using the Erlang-C formula, it is assumed that both the service times and the waiting time tolerances of the clients are exponentially distributed.

The Mini Call Center Simulator simulates a call center and takes into account exactly the above mentioned characteristics. When displaying the results, the simulation results are also compared to the Erlang-C results and it is indicated which properties of the system lead to deviations between simulation and formula results.

The Mini Call Center Simulator is mainly used for teaching purposes. In the Mini Callcenter Simulator different distributions for inter-arrival times, service times, etc. can be entered and the relevant properties "customer impatience" and "repeated tries" are also modeled, but neither different customer groups and skills of the agents nor an arrival rate that changes during the day are modeled. These and many other properties are available in the large call center simulator.

![Screenshot](https://raw.githubusercontent.com/A-Herzog/Mini-Callcenter-Simulator/master/screenshot_en.png)