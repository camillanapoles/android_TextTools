package com.corphish.quicktools.activities

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import com.corphish.quicktools.data.Constants

/**
 * Ponto de entrada único do menu de contexto (activity-alias habilitado).
 *
 * No modelo novo, é um delegador fino: encaminha o texto selecionado para
 * [LauncherActivity] (for-result) e repassa o resultado ao sistema para
 * substituição in-place. O conteúdo dinâmico vive no Launcher.
 */
class OptionsActivity : NoUIActivity() {

    private val router = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            setResult(RESULT_OK, it.data)
        }
        finish()
    }

    override fun handleIntent(intent: Intent): Boolean {
        if (intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)) {
            val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString()
            val readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
            val forceCopy = intent.getBooleanExtra(Constants.INTENT_FORCE_COPY, false)

            router.launch(
                Intent(this, LauncherActivity::class.java).apply {
                    putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                    putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, readonly)
                    putExtra(Constants.INTENT_FORCE_COPY, forceCopy)
                }
            )
            return false // aguarda o resultado do Launcher antes de finalizar
        }

        return true
    }
}
