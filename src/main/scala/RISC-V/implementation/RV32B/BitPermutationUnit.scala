package RISCV.implementation.RV32B

import chisel3._
import chisel3.util._

import RISCV.interfaces.generic.AbstractExecutionUnit
import RISCV.model._
import bitmanipulation.AbstractGeneralizedReverser
import bitmanipulation.AbstractShuffler
import bitmanipulation.AbstractSequentialRotater
import RISCV.implementation.RV32I.ControlUnit
import RISCV.implementation.RV32I.Decoder

class BitPermutationUnit(
    genGeneralizedReverser: () => AbstractGeneralizedReverser,
    genShuffler: () => AbstractShuffler,
    genRotater: () => AbstractSequentialRotater
) extends AbstractExecutionUnit(InstructionSets.BitPerm) {

  io.misa := "b01__0000__0_00000_00000_00000_00000_00010".U

  val generalizedReverser = Module(genGeneralizedReverser())
  val shuffler = Module(genShuffler())
  val rotater = Module(genRotater())

  // io.stall := STALL_REASON.NO_STALL

  // io_data <> DontCare
  // io_reg <> DontCare
  // io_pc <> DontCare
  // io_reset <> DontCare
  // io_trap <> DontCare

  // generalizedReverser.io <> DontCare
  // shuffler.io <> DontCare
  // rotater.io <> DontCare

  // ??? // TODO: implement Task 2.5 here


  val decoder = Module(new Decoder)
  decoder.io_reset <> io_reset
  decoder.io_decoder.instr := io.instr

  object State extends ChiselEnum {
    val Normal = Value("b0".U)
    val Rotating = Value("b1".U)
  }
  val state = RegInit(State.Normal)

  io.stall := STALL_REASON.NO_STALL
  io_reg.reg_write_en := false.B
  io_reg.reg_write_data := 0.U
  rotater.io.start := false.B

  io_reg.reg_rs1 := decoder.io_decoder.rs1
  io_reg.reg_rs2 := decoder.io_decoder.rs2
  io_reg.reg_rd  := decoder.io_decoder.rd

  val rs1_val = io_reg.reg_read_data1
  val rs2_val = io_reg.reg_read_data2
  val imm_val = decoder.io_decoder.imm

  val imm_pattern = imm_val(4,0)
  val rs2_pattern = rs2_val(4,0)

  generalizedReverser.io.input := rs1_val
  generalizedReverser.io.pattern := rs2_pattern // default

  shuffler.io.input := rs1_val
  shuffler.io.pattern := rs2_pattern // default
  shuffler.io.unshuffle := false.B // default

  rotater.io.input := rs1_val
  rotater.io.shamt := 0.U // default

  switch(state) {
    is(State.Normal) {
      io.stall := STALL_REASON.NO_STALL

      switch(instr_type) {
        is(RISCV_TYPE.grev) {
          generalizedReverser.io.pattern := rs2_pattern
          io_reg.reg_write_data := generalizedReverser.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.grevi) {
          generalizedReverser.io.pattern := imm_pattern
          io_reg.reg_write_data := generalizedReverser.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.shfl) {
          shuffler.io.pattern := rs2_pattern
          shuffler.io.unshuffle := false.B
          io_reg.reg_write_data := shuffler.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.shfli) {
          shuffler.io.pattern := imm_pattern
          shuffler.io.unshuffle := false.B
          io_reg.reg_write_data := shuffler.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.unshfl) {
          shuffler.io.pattern := rs2_pattern
          shuffler.io.unshuffle := true.B
          io_reg.reg_write_data := shuffler.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.unshfli) {
          shuffler.io.pattern := imm_pattern
          shuffler.io.unshuffle := true.B
          io_reg.reg_write_data := shuffler.io.result
          io_reg.reg_write_en := true.B
        }
        is(RISCV_TYPE.rol) {
          rotater.io.start := true.B
          rotater.io.shamt := rs2_pattern
          rotater.io.input := Reverse(rs1_val)
          io.stall := STALL_REASON.EXECUTION_UNIT
          state := State.Rotating
          io_reg.reg_write_en := false.B
        }
        is(RISCV_TYPE.ror) {
          rotater.io.start := true.B
          rotater.io.shamt := rs2_pattern
          io.stall := STALL_REASON.EXECUTION_UNIT
          state := State.Rotating
          io_reg.reg_write_en := false.B
        }
        is(RISCV_TYPE.rori) {
          rotater.io.start := true.B
          rotater.io.shamt := imm_pattern
          io.stall := STALL_REASON.EXECUTION_UNIT
          state := State.Rotating
          io_reg.reg_write_en := false.B
        }
      }
    }
    is(State.Rotating) {
      io.stall := STALL_REASON.EXECUTION_UNIT
      rotater.io.start := false.B

      when(rotater.io.done) {
        io_reg.reg_write_en := true.B

        when (instr_type === RISCV_TYPE.rol) {
          io_reg.reg_write_data := Reverse(rotater.io.result)
        } .otherwise {
          io_reg.reg_write_data := rotater.io.result
        }

        io.stall := STALL_REASON.NO_STALL
        state := State.Normal
      }
    }
  }

  io_data.data_req := false.B
  io_data.data_addr := 0.U
  io_data.data_be := 0.U
  io_data.data_we := false.B
  io_data.data_wdata := 0.U

  io_pc.pc_wdata := io_pc.pc + 4.U
  io_pc.pc_we := io.stall === STALL_REASON.NO_STALL

  io_trap.trap_valid := false.B
  io_trap.trap_reason := TRAP_REASON.NONE
}
