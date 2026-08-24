package com.corphish.quicktools.repository

import com.corphish.quicktools.functions.ContextFunctions
import com.corphish.quicktools.functions.IntentAction
import com.corphish.quicktools.functions.ParsedTextAction
import com.corphish.quicktools.functions.TextActionDeterminationFunctions
import com.corphish.quicktools.functions.UnitConverter
import javax.inject.Inject

class TextActionRepositoryImpl @Inject constructor(
    private val contextFunctions: ContextFunctions,
    private val textActionDeterminationFunctions: TextActionDeterminationFunctions,
    private val unitConverter: UnitConverter
): TextActionRepository {
    private suspend fun determineTextSpecificActions(text: String) =
        textActionDeterminationFunctions.determineTextActions(text, contextFunctions.textClassifier())

    override suspend fun buildTextActions(
        text: String,
        canBeApplied: Boolean
    ): List<ParsedTextAction> {
        val list = mutableListOf<ParsedTextAction>()

        if (canBeApplied) {
            list += textActionDeterminationFunctions.applyAction(text)
        }

        list += textActionDeterminationFunctions.copyToClipboardAction(text)
        list += ParsedTextAction(IntentAction.EDIT_AND_COPY, text)
        list += ParsedTextAction(IntentAction.DEFINITION_LOOKUP, text)
        list += determineTextSpecificActions(text)
        list += textActionDeterminationFunctions.unitConversionActions(text, unitConverter)

        return list
    }

    override fun performTextAction(action: ParsedTextAction): Boolean {
        return when (action.type) {
            IntentAction.EMAIL -> contextFunctions.sendEmail(action.parsedText)
            IntentAction.PHONE -> contextFunctions.dialPhone(action.parsedText)
            IntentAction.URL -> contextFunctions.openInWeb(action.parsedText)
            IntentAction.MAP -> contextFunctions.openMap(action.parsedText)
            IntentAction.FLIGHT -> contextFunctions.trackFlight(action.parsedText)
            IntentAction.CALENDAR -> contextFunctions.addToCalendar(action.parsedText)
            IntentAction.APPLY, IntentAction.UNIT_CONVERSION, IntentAction.CURRENCY_CONVERSION, IntentAction.EDIT_AND_COPY, IntentAction.DEFINITION_LOOKUP -> {
                throw IllegalArgumentException("Apply/Conversion/Edit/Lookup function must be performed within activity")
            }
            IntentAction.COPY_TO_CLIPBOARD -> {
                contextFunctions.copyToClipboard(action.parsedText)
                true
            }
        }
    }
}