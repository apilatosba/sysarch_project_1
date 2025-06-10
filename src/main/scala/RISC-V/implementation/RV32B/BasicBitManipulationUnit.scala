package RISCV.implementation.RV32B

import chisel3._
import chisel3.util._

import RISCV.interfaces.generic.AbstractExecutionUnit
import RISCV.model._
import bitmanipulation.AbstractLeadingZerosCounter
import RISCV.implementation.RV32I.Decoder
import RISCV.implementation.RV32I.ControlUnit

class BasicBitManipulationUnit(
    genLeadingZerosCounter: () => AbstractLeadingZerosCounter
) extends AbstractExecutionUnit(InstructionSets.BasicBit) {
  io.misa := "b01__0000__0_00000_00000_00000_00000_00010".U

  val leadingZerosCounter = Module(genLeadingZerosCounter())

  io.stall := STALL_REASON.NO_STALL

  io_data <> DontCare
  // io_reg <> DontCare
  // io_pc <> DontCare
  // io_reset <> DontCare
  io_trap <> DontCare

  // leadingZerosCounter.io <> DontCare

  // ??? // TODO: implement Task 2.4 here

  val control_unit = Module(new ControlUnit)
  control_unit.io_reset <> io_reset
  control_unit.io_ctrl.instr_type := instr_type
  control_unit.io_ctrl.data_gnt := true.B // io_data.data_gnt
  io.stall := control_unit.io_ctrl.stall

  val decoder = Module(new Decoder)
  decoder.io_reset <> io_reset
  decoder.io_decoder.instr := io.instr

  // // Assign the program counter interface
  // io_pc.pc_wdata := io_reset.boot_addr // Default value
  // switch(control_unit.io_ctrl.next_pc_select) {
  //   is(NEXT_PC_SELECT.PC_PLUS_4) {
  //     io_pc.pc_wdata := io_pc.pc + 4.U
  //   }
  //   is(NEXT_PC_SELECT.BRANCH) {
  //     // no branches. i am cluless Clueless
  //     io_pc.pc_wdata := DontCare
  //   }
  //   is (NEXT_PC_SELECT.IMM) {
  //     io_pc.pc_wdata := io_pc.pc + decoder.io_decoder.imm
  //   }
  //   is (NEXT_PC_SELECT.ALU_OUT_ALIGNED) {
  //     // no alu either. is it always PC_PLUS_4
  //     io_pc.pc_wdata := DontCare
  //   }
  // }
  io_pc.pc_we := control_unit.io_ctrl.stall === STALL_REASON.NO_STALL
  io_pc.pc_wdata := io_pc.pc + 4.U

  io_reg.reg_rs1 := decoder.io_decoder.rs1
  io_reg.reg_rs2 := decoder.io_decoder.rs2
  io_reg.reg_rd := decoder.io_decoder.rd
  io_reg.reg_write_en := control_unit.io_ctrl.reg_we
  io_reg.reg_write_data := 0.U

  io_reg.reg_write_en := control_unit.io_ctrl.reg_we

  // tie off counter input by default
  leadingZerosCounter.io.input := 0.U

  // extract operands and rd
  val a  = io_reg.reg_read_data1
  val b  = io_reg.reg_read_data2

  // prepare helpers
  val revA = Reverse(a)
  val popA = PopCount(a)
  val lz   = leadingZerosCounter.io.result

  // this is like alu. idk
  switch(io.instr_type) {
    is(RISCV_TYPE.clz) {
      leadingZerosCounter.io.input := a
      // io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := lz
    }
    is(RISCV_TYPE.ctz) {
      leadingZerosCounter.io.input := revA
      // io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := lz
    }
    is(RISCV_TYPE.cpop) {
      // io_reg.reg_write_en   := true.B
      io_reg.reg_write_data := popA
    }
    is(RISCV_TYPE.min) {
      // io_reg.reg_write_en   := true.B
      when (a.asSInt < b.asSInt) {
        io_reg.reg_write_data := a
      } .otherwise {
        io_reg.reg_write_data := b
      }
    }
    is(RISCV_TYPE.max) {
      // io_reg.reg_write_en   := true.B
      when (a.asSInt > b.asSInt) {
        io_reg.reg_write_data := a
      } .otherwise {
        io_reg.reg_write_data := b
      }
    }
    is(RISCV_TYPE.minu) {
      // io_reg.reg_write_en   := true.B
      when (a < b) {
        io_reg.reg_write_data := a
      } .otherwise {
        io_reg.reg_write_data := b
      }
    }
    is(RISCV_TYPE.maxu) {
      // io_reg.reg_write_en   := true.B
      when (a > b) {
        io_reg.reg_write_data := a
      } .otherwise {
        io_reg.reg_write_data := b
      }
    }
  }

  // // Assign the trap interface
  // io_trap.trap_valid := false.B
  // io_trap.trap_reason := TRAP_REASON.NONE


  // // Assign the program counter interface
  // when (io_reset.rst_n) {
  //   io_pc.pc_wdata := io_reset.boot_addr // Reset to boot address
  // } .otherwise {
  //   io_pc.pc_wdata := io_pc.pc + 4.U // Increment PC by 4 on each cycle
  // }
  // io_pc.pc_we := true.B


  // // Assign the data interface
  // io_data.data_req := false.B
  // io_data.data_addr := 0.U
  // io_data.data_be := 0.U
  // io_data.data_we := false.B
  // io_data.data_wdata := 0.U


}
