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

  // val generalizedReverser = Module(genGeneralizedReverser())
  // val shuffler = Module(genShuffler())
  // val rotater = Module(genRotater())

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







  // decode & (re‐)use the RV32I control for stall=NO_STALL by default
  val control = Module(new ControlUnit)
  control.io_reset    <> io_reset
  control.io_ctrl.instr_type := instr_type
  control.io_ctrl.data_gnt    := true.B
  io.stall := control.io_ctrl.stall

  // decoder to extract rs1,rs2,rd,imm
  val dec = Module(new Decoder)
  dec.io_reset         <> io_reset
  dec.io_decoder.instr := io.instr

  // submodules
  val grev   = Module(genGeneralizedReverser())
  val shfl   = Module(genShuffler())
  val rot    = Module(genRotater())

  // default: pacify unused
  io_data <> DontCare
  io_pc   <> DontCare
  io_trap <> DontCare

  grev.io    <> DontCare
  shfl.io    <> DontCare
  rot.io     <> DontCare

  // PC always +4
  io_pc.pc_wdata := io_pc.pc + 4.U
  io_pc.pc_we    := control.io_ctrl.stall === STALL_REASON.NO_STALL

  // register‐file address ports from decoder
  io_reg.reg_rs1 := dec.io_decoder.rs1
  io_reg.reg_rs2 := dec.io_decoder.rs2
  io_reg.reg_rd  := dec.io_decoder.rd

  // default writeback off & data = 0
  io_reg.reg_write_en   := false.B
  io_reg.reg_write_data := 0.U

  // extract operands & immediate/pattern
  val a        = io_reg.reg_read_data1
  val b        = io_reg.reg_read_data2
  val imm5     = dec.io_decoder.instr(24,20)
  val patt5    = b(4,0)       // dynamic pattern from rs2 low bits
  val reverseA = Reverse(a)

  switch(instr_type) {
    // ---- Combinational GREV ----
    is(RISCV_TYPE.grev) {
      grev.io.input   := a
      grev.io.pattern := patt5
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := grev.io.result
    }

    // ---- Combinational SHFL / UNSHFL ----
    is(RISCV_TYPE.shfl) {
      shfl.io.input     := a
      shfl.io.pattern   := patt5
      shfl.io.unshuffle := 0.U
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := shfl.io.result
    }
    is(RISCV_TYPE.unshfl) {
      shfl.io.input     := a
      shfl.io.pattern   := patt5
      shfl.io.unshuffle := 1.U
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := shfl.io.result
    }

    // ---- Immediate GREV I / SHFL I / UNSHFL I ----
    is(RISCV_TYPE.grevi) {
      grev.io.input   := a
      grev.io.pattern := imm5
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := grev.io.result
    }
    is(RISCV_TYPE.shfli) {
      shfl.io.input     := a
      shfl.io.pattern   := imm5
      shfl.io.unshuffle := 0.U
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := shfl.io.result
    }
    is(RISCV_TYPE.unshfli) {
      shfl.io.input     := a
      shfl.io.pattern   := imm5
      shfl.io.unshuffle := 1.U
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := shfl.io.result
    }

    // ---- Sequential ROTATES: ROL / ROR / RORI ----
    is(RISCV_TYPE.rol) {
      rot.io.input := a
      rot.io.shamt := patt5
      rot.io.start := true.B
      io.stall     := STALL_REASON.EXECUTION_UNIT
    }
    is(RISCV_TYPE.ror) {
      rot.io.input := a
      rot.io.shamt := patt5
      rot.io.start := true.B
      io.stall     := STALL_REASON.EXECUTION_UNIT
    }
    is(RISCV_TYPE.rori) {
      rot.io.input := a
      rot.io.shamt := imm5
      rot.io.start := true.B
      io.stall     := STALL_REASON.EXECUTION_UNIT
    }
  }

  // feed sequential rotater and write back when done
  // these two lines apply regardless of which rotate variant
  when(instr_type.isOneOf(RISCV_TYPE.rol, RISCV_TYPE.ror, RISCV_TYPE.rori)) {
    // keep driving inputs
    // rot.io.input and shamt already set above
    rot.io.start := false.B     // only true in the cycle we matched above
    io.stall     := Mux(rot.io.done, STALL_REASON.NO_STALL, STALL_REASON.EXECUTION_UNIT)
    when(rot.io.done) {
      io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := rot.io.result
    }
  }

}
