package bitmanipulation

import chisel3._
import chisel3.util._

abstract class AbstractShuffler(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val pattern = Input(UInt((log2Ceil(bitWidth) - 1).W))
    val unshuffle = Input(UInt(1.W))
    val result = Output(UInt(bitWidth.W))
  })
}

class ShufflerSubCircuit(bitWidth: Int, shiftAmount: Int) extends Module {
    val io = IO(new Bundle {
    val input  = Input(UInt(bitWidth.W))
    val enable = Input(Bool())
    val output = Output(UInt(bitWidth.W))
  })

  def GenerateLeftMask(bitWidth: Int, shiftAmount: Int): UInt = {
    var result = BigInt(0)
    for (j <- 0 until bitWidth) {
      if (((j / shiftAmount) & 1) == 0) {
        result = result.setBit(j)
      }
    }
    return result.U(bitWidth.W)
  }

  val maskL = GenerateLeftMask(bitWidth, shiftAmount)
  val maskR = (~maskL).asUInt

  when (io.enable) {
    val leftPortion  = (io.input & maskL) << shiftAmount
    val rightPortion = (io.input & maskR) >> shiftAmount
    io.output := leftPortion | rightPortion
  } .otherwise {
    io.output := io.input
  }
}

class Shuffler(bitWidth: Int) extends AbstractShuffler(bitWidth) {

  // ??? // TODO: implement Task 1.3 here

  val stages = log2Ceil(bitWidth) - 1

  val shflResult = {
    var current: UInt = io.input

    for (i <- (stages - 1) to 0 by -1) {
      val shiftAmt = 1 << i
      val stageSub = Module(new ShufflerSubCircuit(bitWidth, shiftAmt))
      stageSub.io.enable := io.pattern(i)
      stageSub.io.input := current
      current = stageSub.io.output
    }
    current
  }

  val ushflResult = {
    var current: UInt = io.input

    for (i <- 0 until stages) {
      val shiftAmt = 1 << i
      val stageSub = Module(new ShufflerSubCircuit(bitWidth, shiftAmt))
      stageSub.io.enable := io.pattern(i)
      stageSub.io.input := current
      current = stageSub.io.output
    }
    current
  }

  when (io.unshuffle.asBool) {
    io.result := ushflResult
  } .otherwise {
    io.result := shflResult
  }
}
