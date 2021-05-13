/*
 * The MIT License (MIT)
 * Copyright © 2020 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ru.yoomoney.sdk.kassa.payments.paymentOptionInfo

private const val MIN_DIGIT = 0
private const val DIGITS_MIDDLE = 5
private const val LAST_DIGIT_MASK = 10
private const val MAX_DIGIT = 9
private const val CARD_NUMBER_MIN_LENGTH = 13
private const val CARD_NUMBER_MAX_LENGTH = 19

private val DIGIT_RANGE = MIN_DIGIT..MAX_DIGIT
private val LENGTH_RANGE = CARD_NUMBER_MIN_LENGTH..CARD_NUMBER_MAX_LENGTH


internal fun String.isCorrectPan() = map(Character::getNumericValue).isCorrectPan()

private fun List<Int>.isCorrectPan() =
    size in LENGTH_RANGE && all(DIGIT_RANGE::contains) && checkCardLuhnAlgorithm(this)

// Simplified version of Luhn algorithm.
// Implementation is partially taken from
// https://github.com/ErikSchierboom/exercism/blob/master/kotlin/luhn/src/main/kotlin/Luhn.kt
private fun checkCardLuhnAlgorithm(digits: List<Int>) = digits.asReversed().asSequence()
    .mapIndexed { index, digit ->
        when {
            index % 2 == 0 -> digit
            digit < DIGITS_MIDDLE -> digit * 2
            else -> digit * 2 - MAX_DIGIT
        }
    }
    .sum() % LAST_DIGIT_MASK == 0
