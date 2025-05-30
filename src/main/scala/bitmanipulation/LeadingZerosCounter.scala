package bitmanipulation

import chisel3._
import chisel3.util._

abstract class AbstractLeadingZerosCounter(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val result = Output(UInt(log2Ceil(bitWidth + 1).W))
  })
}

// You may expect bitWidth to be a power of two.
class LeadingZerosCounter(bitWidth: Int)
    extends AbstractLeadingZerosCounter(bitWidth) {

  // ??? // TODO: implement Task 1.1 here

  // implement n by n/2 n/2.
  // one n/2 for upper half one n/2 for lower half.
  // if upper half returns "10000.." which means all zeroes then add the result of lower half.
  // but we dont have addition. exploit the fact that we have all zeroes in the upper half then the addition is just an or operation
  // except one the lower half is also "10000..." then the result is "10000..." with one more zero
  // and the base is just when n is 1 and the result is just the negation of the input
  //
  // actually above thing is wrong now i noticed that the input is not in the form 2^n but is in the form of 2*n

  if (bitWidth == 1) {
    io.result := ~io.input
  } else {
    val resultBitWidth = log2Ceil(bitWidth + 1)
    val halfWidth = bitWidth / 2
    val upperHalf = io.input(bitWidth - 1, halfWidth)
    val lowerHalf = io.input(halfWidth - 1, 0)

    val upperSubCircuit = Module(new LeadingZerosCounter(halfWidth))
    upperSubCircuit.io.input := upperHalf

    val lowerSubCircuit = Module(new LeadingZerosCounter(halfWidth))
    lowerSubCircuit.io.input := lowerHalf

    when (upperSubCircuit.io.result === halfWidth.U) {
      // when (lowerSubCircuit.io.result(resultBitWidth - 2) === 1.U) {
      //   io.result := Cat(1.B, Fill(resultBitWidth - 1, 0.B))
      // } .otherwise {
      //   io.result := Cat(0.B, upperSubCircuit.io.result | lowerSubCircuit.io.result)
      // }
      io.result := upperSubCircuit.io.result +& lowerSubCircuit.io.result
    } .otherwise {
      io.result := upperSubCircuit.io.result
    }
  }
}
