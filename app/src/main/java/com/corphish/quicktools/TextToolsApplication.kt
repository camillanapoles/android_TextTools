package com.corphish.quicktools

import android.app.Application
import com.corphish.quicktools.repository.AppMode
import com.corphish.quicktools.repository.ContextMenuOptionsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TextToolsApplication : Application() {

    @Inject
    lateinit var contextMenuOptionsRepository: ContextMenuOptionsRepository

    override fun onCreate() {
        super.onCreate()
        // TextTools X usa uma única entrada de menu -> launcher dinâmico.
        // Força SINGLE em todo início de processo para que um pref MULTI legado
        // (restaurado da nuvem por uma instalação anterior do app original, que
        // compartilha o package name) não reabilite os aliases antigos e esconda
        // o launcher novo.
        if (contextMenuOptionsRepository.getCurrentAppMode() != AppMode.SINGLE) {
            contextMenuOptionsRepository.setCurrentAppMode(AppMode.SINGLE)
        }
    }
}
