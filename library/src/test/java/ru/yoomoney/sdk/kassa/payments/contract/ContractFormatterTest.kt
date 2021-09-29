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
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod
import ru.yoomoney.sdk.kassa.payments.model.ExternalConfirmation
import ru.yoomoney.sdk.kassa.payments.savePaymentMethodOptionTexts

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
            customerId = customerId,
            savePaymentMethodOptionTexts = savePaymentMethodOptionTexts,
            userAgreementUrl = "userAgreementUrl"
        )

        // when
        val expectedOption: SavePaymentMethodOption = when (expectedOptionCase) {
            OptionCase.CARD_SWITCH_RECURRENT_NO_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = "Разрешить автосписания",
                subtitle = "После оплаты привяжем карту: магазин сможет <a href=''>списывать деньги без вашего участия</>",
                screenTitle = "Как работают автоматические списания",
                screenText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n" +
                        " \n" +
                        "Автосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )
            OptionCase.CARD_SWITCH_NO_RECURRENT_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = "Сохранить платёжные данные",
                subtitle = "Магазин <a href=''>сохранит данные вашей карты</> — \n" +
                        "в следующий раз можно будет их не вводить",
                screenTitle = "Сохранение платёжных данных",
                screenText = "Если вы это разрешили, магазин сохранит данные вашей банковской карты — номер, имя владельца и срок действия (всё, кроме кода CVC). В следующий раз не нужно будет вводить их, чтобы заплатить в этом магазине. \n \nУдалить данные можно в процессе оплаты (нажмите «Редактировать мои карты») или через службу поддержки."
            )
            OptionCase.CARD_SWITCH_RECURRENT_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = "Разрешить автосписания \n" +
                        "и сохранить платёжные данные",
                subtitle = "После оплаты магазин <a href=''>сохранит данные карты и сможет списывать деньги без вашего участия</>",
                screenTitle = "Автосписания \nи сохранение платёжных данных",
                screenText = "Если вы это разрешили, магазин сохранит данные банковской карты — номер, имя владельца, срок действия (всё, кроме кода CVC). В следующий раз не нужно будет их вводить, чтобы заплатить в этом магазине. \n \nКроме того, мы привяжем карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n \nАвтосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )

            OptionCase.CARD_MESSAGE_RECURRENT_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = "Разрешим автосписания и сохраним платёжные данные",
                subtitle = "Заплатив здесь, вы соглашаетесь <a href=''>сохранить данные карты и списывать деньги без вашего участия</>",
                screenTitle = "Автосписания \nи сохранение платёжных данных",
                screenText = "Если вы это разрешили, магазин сохранит данные банковской карты — номер, имя владельца, срок действия (всё, кроме кода CVC). В следующий раз не нужно будет их вводить, чтобы заплатить в этом магазине. \n \nКроме того, мы привяжем карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n \nАвтосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )
            OptionCase.CARD_MESSAGE_NO_RECURRENT_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = "Сохраним платёжные данные",
                subtitle = "Заплатив здесь, вы разрешаете магазину <a href=''>сохранить данные вашей карты</> — в следующий раз можно их не вводить\n",
                screenTitle = "Сохранение платёжных данных",
                screenText = "Если вы это разрешили, магазин сохранит данные вашей банковской карты — номер, имя владельца и срок действия (всё, кроме кода CVC). В следующий раз не нужно будет вводить их, чтобы заплатить в этом магазине. \n \nУдалить данные можно в процессе оплаты (нажмите «Редактировать мои карты») или через службу поддержки."
            )
            OptionCase.CARD_MESSAGE_RECURRENT_NO_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = "Разрешим автосписания",
                subtitle = "Заплатив здесь, вы разрешаете привязать карту и <a href=''>списывать деньги без вашего участия</>",
                screenTitle = "Как работают автоматические списания",
                screenText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n \nАвтосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )
            OptionCase.GPAY_SWITCH_RECURRENT_NO_SAVE -> SavePaymentMethodOption.SwitchSavePaymentMethodOption(
                title = "Разрешить автосписания",
                subtitle = "После оплаты привяжем карту: магазин сможет <a href=''>списывать деньги без вашего участия</>",
                screenTitle = "Как работают автоматические списания",
                screenText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n \nАвтосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )
            OptionCase.GPAY_MESSAGE_RECURRENT_NO_SAVE -> SavePaymentMethodOption.MessageSavePaymentMethodOption(
                title = "Разрешим автосписания",
                subtitle = "Заплатив здесь, вы разрешаете привязать карту и <a href=''>списывать деньги без вашего участия</>",
                screenTitle = "Как работают автоматические списания",
                screenText = "Если вы согласитесь на автосписания, мы привяжем банковскую карту (в том числе использованную через Google Pay) к магазину. После этого магазин сможет присылать запросы на автоматические списания денег — тогда платёж выполняется без дополнительного подтверждения с вашей стороны. \n \nАвтосписания продолжатся даже при перевыпуске карты, если ваш банк умеет автоматически обновлять данные. Отменить их и отвязать карту можно в любой момент — через службу поддержки магазина."
            )
            OptionCase.NONE -> SavePaymentMethodOption.None
        }
        val actual = content.getSavePaymentMethodOption()

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