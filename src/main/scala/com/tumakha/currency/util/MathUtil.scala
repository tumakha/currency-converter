package com.tumakha.currency.util

import scala.math.BigDecimal.RoundingMode.HALF_UP

/**
 * @author Yuriy Tumakha
 */
object MathUtil {

  private val defaultPrecision = 4

  def round(num: Double, precision: Int = defaultPrecision): Double =
    BigDecimal(num).setScale(precision, HALF_UP).toDouble

}
