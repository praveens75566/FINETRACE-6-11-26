package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PricePrecisionConfig
import com.example.data.model.SymbolInfo
import com.example.data.model.formatPriceDynamic
import com.example.data.model.getDisplayDecimals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Before
  fun setUp() {
    PricePrecisionConfig.clearAll()
    PricePrecisionConfig.maxPrecision = null
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FinTrace", appName)
  }

  @Test
  fun `default display decimals matching SymbolInfo specifications`() {
    val xau = SymbolInfo.find("XAU/USD")
    val eur = SymbolInfo.find("EUR/USD")
    assertEquals(2, xau.getDisplayDecimals())
    assertEquals(5, eur.getDisplayDecimals())
  }

  @Test
  fun `global max precision overrides default ones`() {
    val xau = SymbolInfo.find("XAU/USD")
    val eur = SymbolInfo.find("EUR/USD")
    PricePrecisionConfig.maxPrecision = 3
    assertEquals(3, xau.getDisplayDecimals())
    assertEquals(3, eur.getDisplayDecimals())
  }

  @Test
  fun `per-asset decimals override takes highest precedence`() {
    val xau = SymbolInfo.find("XAU/USD")
    val eur = SymbolInfo.find("EUR/USD")
    PricePrecisionConfig.maxPrecision = 3
    PricePrecisionConfig.setOverride("XAU/USD", 4)
    assertEquals(4, xau.getDisplayDecimals())
    assertEquals(3, eur.getDisplayDecimals())
  }

  @Test
  fun `formatting prices dynamically formats correct decimal counts`() {
    val price = 1234.56789
    assertEquals("1,234.57", price.formatPriceDynamic(2))
    assertEquals("1,234.568", price.formatPriceDynamic(3))
    assertEquals("1,234.5679", price.formatPriceDynamic(4))
  }
}
