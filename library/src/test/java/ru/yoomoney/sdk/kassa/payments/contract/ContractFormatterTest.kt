/*
 * The MIT License (MIT)
 * Copyright © 2021 NBCO YooMoney LLC
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

package ru.yoomoney.sdk.kassa.payments.contract

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import ru.yoomoney.sdk.kassa.payments.R
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.utils.getMessageWithLink

internal enum class OptionCase {
    CARD_SWITCH_RECURRENT_NO_SAVE,
    CARD_SWITCH_NO_RECURRENT_SAVE,
    CARD_SWITCH_RECURRENT_SAVE,
    CARD_MESSAGE_RECURRENT_SAVE,
    CARD_MESSAGE_NO_RECURRENT_SAVE,
    CARD_MESSAGE_RECURRENT_NO_SAVE,

    GPAY_SWITCH_RECURRENT_NO_SAVE,
    GPAY_MESSAGE_RECURRENT_NO_SAVE,

    NONE
}

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class GetSavePaymentMethodOptionTest(
    private val savePaymentMethod: SavePaymentMethod,
    private val customerId: String?,
    private val contractInfo: ContractInfo,
    private val expectedOptionCase: OptionCase
) {

    @Test
    fun test() {
        // given
        val context = RuntimeEnvironment.application

        val content = Contract.State.Content(
            shopTitle = "shopTitle",
            shopSubtitle = "shopSubtitle",
            shouldSavePaymentMethod = false,
            savePaymentMethod = savePaymentMethod,
            shouldSavePaymentInstrument = false,
            isSinglePaymentMethod = false,
            contractInfo = contractInfo,
            confirmation = ExternalConfirmation,
            isSplitPayment = false,
            customerId = customerId
        )

        // when
        val expectedOption: SavePaymentMethodOption = when (expectedOptionCase) {
            OptionCase.CARD_SWITCH_RECURRENT_NO_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_approve_with_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_card_message_text_with_switch_part_1,
                    R.string.ym_save_payment_method_linked_card_message_text_with_switch_part_2
                ) {}
            )
            OptionCase.CARD_SWITCH_NO_RECURRENT_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_save_payment_details_with_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_part_1,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_part_2,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_part_3
                ) {}
            )
            OptionCase.CARD_SWITCH_RECURRENT_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_save_payments_with_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_with_switch_part_1,
                    R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_with_switch_part_2
                ) {}
            )

            OptionCase.CARD_MESSAGE_RECURRENT_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_save_payments_without_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_part_1,
                    R.string.ym_save_payment_method_linked_card_auto_write_off_message_text_part_2
                ) {}
            )
            OptionCase.CARD_MESSAGE_NO_RECURRENT_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_save_payment_details_without_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_1,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_2,
                    R.string.ym_save_payment_method_linked_saved_card_message_text_with_switch_part_3
                ) {}
            )
            OptionCase.CARD_MESSAGE_RECURRENT_NO_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_auto_write_off_approve_without_switch),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_linked_card_message_text_part_1,
                    R.string.ym_save_payment_method_linked_card_message_text_part_2
                ) {}
            )
            OptionCase.GPAY_SWITCH_RECURRENT_NO_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = context.getString(R.string.ym_contract_save_payment_method_gpay_switch_title),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_gpay_switch_text_part_1,
                    R.string.ym_save_payment_method_gpay_switch_text_part_2
                ) {}
            )
            OptionCase.GPAY_MESSAGE_RECURRENT_NO_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = context.getString(R.string.ym_contract_save_payment_method_gpay_switch_title),
                subtitle = getMessageWithLink(
                    context,
                    R.string.ym_save_payment_method_gpay_switch_text_part_1,
                    R.string.ym_save_payment_method_gpay_switch_text_part_2
                ) {}
            )
            OptionCase.NONE -> SavePaymentMethodOption.None
        }
        val actual = content.getSavePaymentMethodOption(context)

        // then

        assertEquals(expectedOption::class, actual::class)

        when (expectedOption) {
            is SavePaymentMethodOption.SwitchSavePaymentMethodOption -> {
                actual as SavePaymentMethodOption.SwitchSavePaymentMethodOption
                assertEquals(expectedOption.title, actual.title)
                assertEquals(expectedOption.subtitle.toString(), actual.subtitle.toString())
            }
            is SavePaymentMethodOption.MessageSavePaymentMethodOption -> {
                actual as SavePaymentMethodOption.MessageSavePaymentMethodOption
                assertEquals(expectedOption.title, actual.title)
                assertEquals(expectedOption.subtitle.toString(), actual.subtitle.toString())
            }
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{index}: isValid({0}, {1}, {2})={3}")
        fun data(): Iterable<Array<out Any?>> {
            return arrayListOf(
                arrayOf(SavePaymentMethod.OFF, null, createWalletContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createWalletContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createWalletLinkedCardContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createWalletLinkedCardContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createAbstractWalletContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createAbstractWalletContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createSberBankContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createSberBankContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createNewBankCardContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createNewBankCardContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createLinkedBankCardContractInfo(), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "null", createLinkedBankCardContractInfo(), OptionCase.NONE),


                // new bank card test cases
                arrayOf(
                    SavePaymentMethod.OFF,
                    "customerId",
                    createNewBankCardContractInfo(true, true),
                    OptionCase.CARD_SWITCH_NO_RECURRENT_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.OFF,
                    "customerId",
                    createNewBankCardContractInfo(true, false),
                    OptionCase.NONE
                ),
                arrayOf(
                    SavePaymentMethod.OFF,
                    "customerId",
                    createNewBankCardContractInfo(false, true),
                    OptionCase.CARD_SWITCH_NO_RECURRENT_SAVE
                ),

                arrayOf(SavePaymentMethod.OFF, null, createNewBankCardContractInfo(true, true), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createNewBankCardContractInfo(true, false), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createNewBankCardContractInfo(false, true), OptionCase.NONE),


                arrayOf(
                    SavePaymentMethod.ON,
                    "customerId",
                    createNewBankCardContractInfo(true, true),
                    OptionCase.CARD_MESSAGE_RECURRENT_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.ON,
                    "customerId",
                    createNewBankCardContractInfo(true, false),
                    OptionCase.CARD_MESSAGE_RECURRENT_NO_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.ON,
                    "customerId",
                    createNewBankCardContractInfo(false, true),
                    OptionCase.CARD_MESSAGE_NO_RECURRENT_SAVE
                ),

                arrayOf(
                    SavePaymentMethod.ON,
                    null,
                    createNewBankCardContractInfo(true, true),
                    OptionCase.CARD_MESSAGE_RECURRENT_NO_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.ON,
                    null,
                    createNewBankCardContractInfo(true, false),
                    OptionCase.CARD_MESSAGE_RECURRENT_NO_SAVE
                ),
                arrayOf(SavePaymentMethod.ON, null, createNewBankCardContractInfo(false, true), OptionCase.NONE),

                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    "customerId",
                    createNewBankCardContractInfo(true, true),
                    OptionCase.CARD_SWITCH_RECURRENT_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    "customerId",
                    createNewBankCardContractInfo(true, false),
                    OptionCase.CARD_SWITCH_RECURRENT_NO_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    "customerId",
                    createNewBankCardContractInfo(false, true),
                    OptionCase.CARD_SWITCH_NO_RECURRENT_SAVE
                ),

                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    null,
                    createNewBankCardContractInfo(true, true),
                    OptionCase.CARD_SWITCH_RECURRENT_NO_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    null,
                    createNewBankCardContractInfo(true, false),
                    OptionCase.CARD_SWITCH_RECURRENT_NO_SAVE
                ),
                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    null,
                    createNewBankCardContractInfo(false, true),
                    OptionCase.NONE
                ),


                // gpay test cases
                arrayOf(SavePaymentMethod.OFF, "customerId", createGPayContractInfo(true), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, "customerId", createGPayContractInfo(false), OptionCase.NONE),

                arrayOf(SavePaymentMethod.OFF, null, createGPayContractInfo(true), OptionCase.NONE),
                arrayOf(SavePaymentMethod.OFF, null, createGPayContractInfo(false), OptionCase.NONE),

                arrayOf(
                    SavePaymentMethod.ON,
                    "customerId",
                    createGPayContractInfo(true),
                    OptionCase.GPAY_MESSAGE_RECURRENT_NO_SAVE
                ),
                arrayOf(SavePaymentMethod.ON, "customerId", createGPayContractInfo(false), OptionCase.NONE),

                arrayOf(
                    SavePaymentMethod.ON,
                    null,
                    createGPayContractInfo(true),
                    OptionCase.GPAY_MESSAGE_RECURRENT_NO_SAVE
                ),
                arrayOf(SavePaymentMethod.ON, null, createGPayContractInfo(false), OptionCase.NONE),

                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    "customerId",
                    createGPayContractInfo(true),
                    OptionCase.GPAY_SWITCH_RECURRENT_NO_SAVE
                ),
                arrayOf(SavePaymentMethod.USER_SELECTS, "customerId", createGPayContractInfo(false), OptionCase.NONE),

                arrayOf(
                    SavePaymentMethod.USER_SELECTS,
                    null,
                    createGPayContractInfo(true),
                    OptionCase.GPAY_SWITCH_RECURRENT_NO_SAVE
                ),
                arrayOf(SavePaymentMethod.USER_SELECTS, null, createGPayContractInfo(false), OptionCase.NONE)

            ).toList()
        }
    }
}