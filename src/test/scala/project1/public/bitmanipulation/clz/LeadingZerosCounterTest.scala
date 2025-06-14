package project1.public.bitmanipulation.clz

import bitmanipulation.LeadingZerosCounter
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LeadingZerosCounterTest
    extends AnyFlatSpec
    with ChiselScalatestTester
    with Matchers {
  behavior of "LeadingZerosCounter"

  "LeadingZerosCounter(32)" should "count number of leading bits correctly" in {
    test(new LeadingZerosCounter(32)).withAnnotations(Seq(WriteVcdAnnotation)) {
      c =>
        c.io.input.poke(0.U)
        c.io.result.expect(32.U)
    }
  }

  "LeadingZerosCounter" should "count leading zeros" in {
    test(new LeadingZerosCounter(8)) { c =>
      c.io.input.poke("b00000000".U)
      c.io.result.expect(8.U)
      c.io.input.poke("b00000001".U)
      c.io.result.expect(7.U)
      c.io.input.poke("b10000000".U)
      c.io.result.expect(0.U)
      c.io.input.poke("b00100000".U)
      c.io.result.expect(2.U)
    }
  }
}
