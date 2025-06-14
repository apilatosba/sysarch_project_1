# Contributions

Briefly describe how each team member has contributed to the project. It goes without saying that every team members should have a good grasp of all parts of the submission.

There is only one team member.
Abdullah Oguz Topcuoglu: Tried to solve all the tasks until they pass all the tests :)

# Use of AI tools

Did you use any AI tools for the project? If so, which tools did you use?

Yes, I used chatgpt to ask questions to better understand what's going on with the tasks.

## Problem 2.6: Bonus

Please specify here how you approached this task, i.e. how do you ensure correctness and how did you optimize the generated code?

I first wanted to implement a function that just works without worrying about the performance. I thought that if I had a "swap" function
then I thought I could get any permutation I want. Just keep swapping until I get to the permutation I need. But it turned out that
even implementing such "swap" function is not easy. swap function would look like this: List<string> Swap(int rd, int rs1, int i, int j);
and swap function just returns the list of instructions which ultimately results in swapping the ith and jth bits of rs1 and storing it to rd.
I couldn't figure out how to implement such thing so I went with checking if permutation is pure rotation and if it is then generate a rori instruction.
I hope it will pass at least one eval test :)