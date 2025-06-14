package RISCV.utils


object PermBuilder {

  /** This function takes a mapping for the permutation and returns the list of
    * necessary instructions to implement the permutation.
    *
    * You may assume that the map encodes a valid permutation, i.e., that every
    * destination bit is associated with a unique source bit.
    *
    * You may only write to the register rd.
    *
    * @param rd
    *   The destination register
    * @param rs1
    *   The source register
    * @param perm
    *   A map from representing the permutation, mapping destination bit
    *   positions to source bit positions.
    * @return
    *   A list of strings representing the instructions to implement the
    *   permutation e.g. List("grev x1, x2, 0x01", "grev x1, x2, 0x02", ...)
    */
  def buildPermutation(rd: Int, rs1: Int, perm: Map[Int, Int]): List[String] = {

    // ??? // TODO: implement Task 2.6 here

    // one correct implementation:
    //    have this function: List<string> Cycle(int rd, int rs1, List<int> indices); which gives you back the instructions to move every bit in the cycle
    //    for example if the permutation is 0 -> 3, 3 -> 5, 5 -> 0. [0, 3, 5] is the cycle. then the output should move them one further in the cycle
    //    the "indices" parameter should be a closed cycle.
    //    if i do this for every cycle in the permutation then i am done
    //    the thing is how can i implement this Cycle function
    //    assume that i have a swap function can cycle be implemented as a sequence of swaps?
    //    how to implement swap function? thats also hard to do

    return List.empty[String]
  }
}
