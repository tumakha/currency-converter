package com.tumakha.currency.util

import com.tumakha.currency.util.MathUtil.round
import org.scalatest.{ FlatSpec, Matchers }

/**
 * @author Yuriy Tumakha
 */
class MathUtilSpec extends FlatSpec with Matchers {

  "MathUtil.round" should "round Double to specified precision" in {
    round(11.123456, 3) shouldBe 11.123
    round(11.123456, 4) shouldBe 11.1235
    round(11.120009, 4) shouldBe 11.12
  }

}
