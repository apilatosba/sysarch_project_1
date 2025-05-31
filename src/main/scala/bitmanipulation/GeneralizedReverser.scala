package bitmanipulation

import chisel3._
import chisel3.util._

abstract class AbstractGeneralizedReverser(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val pattern = Input(UInt(log2Ceil(bitWidth).W))
    val result = Output(UInt(bitWidth.W))
  })
}

class GeneralizedReverserSubCircuit(bitWidth: Int, shiftAmount: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val enable = Input(Bool())
    val output = Output(UInt(bitWidth.W))
  })

  def GenerateLeftShiftMask(bitWidth: Int, shiftAmount: Int) : UInt = {
    val result = Wire(Vec(bitWidth, Bool())) // UInt(bitWidth.W)

    for (i <- 0 until bitWidth) {
      if ((i / shiftAmount) % 2 == 0) {
        result(i) := 0.B
      } else {
        result(i) := 1.B
      }
    }

    return result.asUInt
  }

  when (io.enable) {
    val leftShiftMask = GenerateLeftShiftMask(bitWidth, shiftAmount)
    val rightShiftMask = ~leftShiftMask
    io.output := ((io.input << shiftAmount) & leftShiftMask) | ((io.input >> shiftAmount) & rightShiftMask)
  } .otherwise {
    io.output := io.input
  }
}

class GeneralizedReverser(bitWidth: Int)
    extends AbstractGeneralizedReverser(bitWidth) {

  // ??? // TODO: implement Task 1.2 here

  val patternBitCount = log2Ceil(bitWidth)

  var current = io.input
  for (i <- 0 until patternBitCount) {
    val shiftAmount = 1 << i

    val subCircuit = Module(new GeneralizedReverserSubCircuit(bitWidth, shiftAmount))
    subCircuit.io.enable := io.pattern(i)
    subCircuit.io.input := current
    current = subCircuit.io.output
  }

  io.result := current
}
