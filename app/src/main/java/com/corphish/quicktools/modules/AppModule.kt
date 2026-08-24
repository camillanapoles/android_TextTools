package com.corphish.quicktools.modules

import android.content.Context
import com.corphish.quicktools.functions.ContextFunctions
import com.corphish.quicktools.functions.FileFunctions
import com.corphish.quicktools.functions.NumberFunctions
import com.corphish.quicktools.functions.TextActionDeterminationFunctions
import com.corphish.quicktools.functions.TextClassifierFunctions
import com.corphish.quicktools.functions.TextFunctions
import com.corphish.quicktools.functions.TextTemplateFunctions
import com.corphish.quicktools.functions.UnitConverter
import com.corphish.quicktools.repository.ContextMenuOptionsRepository
import com.corphish.quicktools.repository.ContextMenuOptionsRepositoryImpl
import com.corphish.quicktools.repository.CurrencyRepository
import com.corphish.quicktools.repository.CurrencyRepositoryImpl
import com.corphish.quicktools.repository.DefinitionRepository
import com.corphish.quicktools.repository.DefinitionRepositoryImpl
import com.corphish.quicktools.repository.NumberAnalysisRepository
import com.corphish.quicktools.repository.NumberAnalysisRepositoryImpl
import com.corphish.quicktools.repository.SettingsRepository
import com.corphish.quicktools.repository.TextActionRepository
import com.corphish.quicktools.repository.TextActionRepositoryImpl
import com.corphish.quicktools.repository.TextAnalysisRepository
import com.corphish.quicktools.repository.TextAnalysisRepositoryImpl
import com.corphish.quicktools.repository.TextReplacementRepository
import com.corphish.quicktools.repository.TextReplacementRepositoryImpl
import com.corphish.quicktools.repository.TextRepository
import com.corphish.quicktools.repository.TextRepositoryImpl
import com.corphish.quicktools.repository.TextTemplateRepository
import com.corphish.quicktools.repository.TextTemplateRepositoryImpl
import com.corphish.quicktools.repository.TextTransformRepository
import com.corphish.quicktools.repository.TextTransformRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) =
        SettingsRepository(context)

    @Provides
    @Singleton
    fun provideTextRepository(fileFunctions: FileFunctions): TextRepository =
        TextRepositoryImpl(fileFunctions)

    @Provides
    @Singleton
    fun provideTextReplacementRepository(): TextReplacementRepository =
        TextReplacementRepositoryImpl()

    @Provides
    @Singleton
    fun provideTextTransformRepository(textFunctions: TextFunctions): TextTransformRepository =
        TextTransformRepositoryImpl(textFunctions)

    @Provides
    @Singleton
    fun provideNumberAnalysisRepository(numberFunctions: NumberFunctions): NumberAnalysisRepository =
        NumberAnalysisRepositoryImpl(numberFunctions)

    @Provides
    @Singleton
    fun provideContextMenuOptionsRepository(@ApplicationContext context: Context): ContextMenuOptionsRepository =
        ContextMenuOptionsRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideTextActionRepository(contextFunctions: ContextFunctions, textActionDeterminationFunctions: TextActionDeterminationFunctions, unitConverter: UnitConverter): TextActionRepository =
        TextActionRepositoryImpl(contextFunctions, textActionDeterminationFunctions, unitConverter)

    @Provides
    @Singleton
    fun provideTextAnalysisRepository(textFunctions: TextFunctions, textClassifierFunctions: TextClassifierFunctions, contextFunctions: ContextFunctions): TextAnalysisRepository =
        TextAnalysisRepositoryImpl(textFunctions, textClassifierFunctions, contextFunctions)

    @Provides
    @Singleton
    fun provideTextTemplateRepository(@ApplicationContext context: Context, textTemplateFunctions: TextTemplateFunctions): TextTemplateRepository =
        TextTemplateRepositoryImpl(context, textTemplateFunctions)

    @Provides
    @Singleton
    fun provideDefinitionRepository(): DefinitionRepository =
        DefinitionRepositoryImpl()

    @Provides
    @Singleton
    fun provideCurrencyRepository(): CurrencyRepository =
        CurrencyRepositoryImpl()
}
