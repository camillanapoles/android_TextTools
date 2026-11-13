package com.corphish.quicktools.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefinitionRepositoryImplTest {
    private lateinit var repository: DefinitionRepositoryImpl

    @Before
    fun setUp() {
        repository = DefinitionRepositoryImpl()
    }

    @Test
    fun testSlangLookup() = runTest {
        val result = repository.getDefinition("LOL")
        assertNotNull(result)
        assertEquals(1, result!!.size)
        assertEquals("LOL", result[0].word)
        assertEquals(DefinitionSource.LOCAL_SLANG, result[0].source)
        assertEquals("slang", result[0].meanings[0].partOfSpeech)
        assertEquals("Laughing Out Loud -or- Lots Of Love -or- Living On Lipitor", result[0].meanings[0].definitions[0].definition)
    }

    @Test
    fun testSlangLookup_Expanded() = runTest {
        val result = repository.getDefinition("AFAIK")
        assertNotNull(result)
        assertEquals("As Far As I Know", result!![0].meanings[0].definitions[0].definition)
        
        val fomo = repository.getDefinition("FOMO")
        assertNotNull(fomo)
        assertEquals("Fear Of Missing Out", fomo!![0].meanings[0].definitions[0].definition)
        
        val goat = repository.getDefinition("GOAT")
        assertNotNull(goat)
        assertEquals("Greatest Of All Time", goat!![0].meanings[0].definitions[0].definition)
        
        val day2 = repository.getDefinition("2day")
        assertNotNull(day2)
        assertEquals("Today", day2!![0].meanings[0].definitions[0].definition)
    }

    @Test
    fun testSlangLookup_CaseInsensitive() = runTest {
        val result = repository.getDefinition("brb")
        assertNotNull(result)
        assertEquals("Be Right Back", result!![0].meanings[0].definitions[0].definition)
    }
}
