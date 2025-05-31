package project1.public.bitmanipulation.generalized_reverse

import bitmanipulation.GeneralizedReverser
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GREVComprehensiveTest
    extends AnyFlatSpec
    with ChiselScalatestTester
    with Matchers {
  behavior of "GeneralizedReverser (GREV)"

  "xxd" should "xxd" in {
    test(new GeneralizedReverser(32)) { c =>
      val input    = BigInt("01", 2).U(32.W)
      val pattern  = 1.U(5.W)
      val expected = BigInt("10", 2).U(32.W)

      c.io.input.poke(input)
      c.io.pattern.poke(pattern)
      c.clock.step()
      c.io.result.expect(expected)
    }
  }

  "xxd2" should "xxd2" in {
    test(new GeneralizedReverser(32)) { c =>
      val input    = BigInt("0111", 2).U(32.W)
      val pattern  = 2.U(5.W)
      val expected = BigInt("1101", 2).U(32.W)

      c.io.input.poke(input)
      c.io.pattern.poke(pattern)
      c.clock.step()
      c.io.result.expect(expected)
    }
  }


}
