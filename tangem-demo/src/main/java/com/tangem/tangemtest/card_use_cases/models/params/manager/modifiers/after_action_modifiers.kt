package com.tangem.tangemtest.card_use_cases.models.params.manager.modifiers

import com.tangem.common.tlv.TlvTag
import com.tangem.tangemtest.card_use_cases.models.params.manager.IncomingParameter
import com.tangem.tangemtest.card_use_cases.models.params.manager.ParamsManager
import com.tangem.tasks.ScanEvent
import com.tangem.tasks.TaskEvent

/**
 * Created by Anton Zhilenkov on 13.03.2020.
 * Семейство классов AfterActionModification предназначены для модификации параметров (если это необходимо)
 * после вызова CardManager.anyAction
 *
 * Возвращает список параметров подвергшихся модификации
 */
interface AfterActionModification {
    fun modify(taskEvent: TaskEvent<*>, paramsList: List<IncomingParameter>): List<IncomingParameter>
}

class AfterScanModifier : AfterActionModification {
    override fun modify(taskEvent: TaskEvent<*>, paramsList: List<IncomingParameter>): List<IncomingParameter> {
        val parameter = ParamsManager.findParameter(TlvTag.CardId, paramsList) ?: return listOf()

        return if (taskEvent is TaskEvent.Event && taskEvent.data is ScanEvent.OnReadEvent) {
            parameter.data = (taskEvent.data as ScanEvent.OnReadEvent).card.cardId
            listOf(parameter)
        } else listOf()
    }
}