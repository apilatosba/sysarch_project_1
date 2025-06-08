package bitmanipulation

import chisel3._
import chisel3.util._

abstract class AbstractFixedRotater(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val result = Output(UInt(bitWidth.W))
  })
}

class FixedRotater(bitWidth: Int, shamt: Int)
    extends AbstractFixedRotater(bitWidth) {

  // ??? // TODO: implement Task 1.4 here

/*
  uint_xlen_t ror(uint_xlen_t rs1, uint_xlen_t rs2)
  {
    int shamt = rs2 & (XLEN - 1);
    return (rs1 >> shamt) | (rs1 << ((XLEN - shamt) & (XLEN - 1)));
  }
*/

  val s = shamt & (bitWidth - 1)
  io.result := (io.input >> s) | (io.input << (bitWidth - s))
}

abstract class AbstractSequentialRotater(bitWidth: Int) extends Module {
  val io = IO(new Bundle {
    val input = Input(UInt(bitWidth.W))
    val shamt = Input(UInt(log2Ceil(bitWidth).W))
    val start = Input(Bool())
    val done = Output(Bool())
    val result = Output(UInt(bitWidth.W))
  })
}

class SequentialRotater(bitWidth: Int, generator: () => AbstractFixedRotater)
    extends AbstractSequentialRotater(bitWidth) {

  val rotater = Module(generator())

  // Rotater.io <> DontCare

  // ??? // TODO: implement Task 1.4 here

  val intermediateResult = RegInit(0.U(bitWidth.W))
  val remainingShamt = RegInit(0.U(log2Ceil(bitWidth).W))

  // when (io.shamt <= 0.U) {
  //   io.done := true.B
  //   io.result := io.input
  // } .otherwise {
  //   when (io.start) {
  //     intermediateResult := io.input
  //     // rotater.io.input := io.input
  //     remainingShamt := io.shamt - 1.U
  //     io.done := false.B
  //   } .elsewhen (remainingShamt > 0.U) {
  //     remainingShamt := remainingShamt - 1.U
  //   } .otherwise {
  //     io.done := true.B
  //   }
  // }

  when (io.start) {
    intermediateResult := io.input
    remainingShamt := io.shamt
  } .otherwise {
    intermediateResult := rotater.io.result
    remainingShamt := remainingShamt - 1.U
  }

  // .elsewhen (remainingShamt > 0.U) {
  //   remainingShamt := remainingShamt - 1.U
  // } .otherwise {
  //   io.done := true.B
  // }

  // one because of the one cycle latency. this feels wrong but it works
  when (remainingShamt <= 1.U) {
    io.done := true.B
  } .otherwise {
    io.done := false.B
  }

  rotater.io.input := intermediateResult

  when (io.start) {
    io.result := io.input
  } .otherwise {
    io.result := rotater.io.result
  }
}
