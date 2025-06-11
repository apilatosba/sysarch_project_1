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

class Shuffler(bitWidth: Int) extends AbstractShuffler(bitWidth) {

  // ??? // TODO: implement Task 1.3 here

  // uint32_t shuffle32_stage(uint32_t src, uint32_t maskL, uint32_t maskR, int N)
  // {
  //    uint32_t x = src & ~(maskL | maskR);
  //    x |= ((src << N) & maskL) | ((src >> N) & maskR);
  //    return x;
  // }

  // uint32_t shfl32(uint32_t rs1, uint32_t rs2)
  // {
  //    uint32_t x = rs1;
  //    int shamt = rs2 & 15;
  //    if (shamt & 8) x = shuffle32_stage(x, 0x00ff0000, 0x0000ff00, 8);
  //    if (shamt & 4) x = shuffle32_stage(x, 0x0f000f00, 0x00f000f0, 4);
  //    if (shamt & 2) x = shuffle32_stage(x, 0x30303030, 0x0c0c0c0c, 2);
  //    if (shamt & 1) x = shuffle32_stage(x, 0x44444444, 0x22222222, 1);
  //    return x;
  // }

  // uint32_t unshfl32(uint32_t rs1, uint32_t rs2)
  // {
  //    uint32_t x = rs1;
  //    int shamt = rs2 & 15;
  //    if (shamt & 1) x = shuffle32_stage(x, 0x44444444, 0x22222222, 1);
  //    if (shamt & 2) x = shuffle32_stage(x, 0x30303030, 0x0c0c0c0c, 2);
  //    if (shamt & 4) x = shuffle32_stage(x, 0x0f000f00, 0x00f000f0, 4);
  //    if (shamt & 8) x = shuffle32_stage(x, 0x00ff0000, 0x0000ff00, 8);
  //    return x;
  // }

  def ShuffleStage(src: UInt, N: Int, maskL: UInt, maskR: UInt): UInt = {
    val x = src & ~(maskL | maskR)
    val shiftedBits = ((src << N) & maskL) | ((src >> N) & maskR)
    x | shiftedBits
  }

  def GenerateStageMasks(bitWidth: Int): Seq[(Int, BigInt, BigInt)] = {
    val numStages = log2Ceil(bitWidth) - 1
    (0 until numStages).map { i =>
      val N = 1 << i
      val groupSize = 4 * N
      var maskL = BigInt(0)
      var maskR = BigInt(0)

      for (j <- 0 until bitWidth) {
        val j_in_group = j % groupSize
        if (j_in_group >= N && j_in_group < 2 * N) {
          maskR = maskR.setBit(j)
        }
        if (j_in_group >= 2 * N && j_in_group < 3 * N) {
          maskL = maskL.setBit(j)
        }
      }
      (N, maskL, maskR)
    }
  }

  val stageMasks = GenerateStageMasks(bitWidth)

  var currentShfl = io.input
  for (i <- (stageMasks.length - 1) to 0 by -1) {
    val (n, maskL, maskR) = stageMasks(i)
    val nextShfl = Wire(UInt(bitWidth.W))

    when(io.pattern(i)) {
      nextShfl := ShuffleStage(currentShfl, n, maskL.U(bitWidth.W), maskR.U(bitWidth.W))
    }.otherwise {
      nextShfl := currentShfl
    }
    currentShfl = nextShfl
  }
  val shflResult = currentShfl

  var currentUnshfl = io.input
  for (i <- 0 until stageMasks.length) {
    val (n, maskL, maskR) = stageMasks(i)
    val nextUnshfl = Wire(UInt(bitWidth.W))

    when(io.pattern(i)) {
      nextUnshfl := ShuffleStage(currentUnshfl, n, maskL.U(bitWidth.W), maskR.U(bitWidth.W))
    }.otherwise {
      nextUnshfl := currentUnshfl
    }
    currentUnshfl = nextUnshfl
  }
  val unshflResult = currentUnshfl

  when(io.unshuffle.asBool) {
    io.result := unshflResult
  } .otherwise {
    io.result := shflResult
  }
}
