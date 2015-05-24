package com.lessmarkup.engine.cache

import java.util.OptionalLong
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}
import com.google.inject.Inject
import com.lessmarkup.dataobjects.Currency
import com.lessmarkup.interfaces.cache.AbstractCacheHandler
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}

class CurrencyCacheItem(val currencyId: Long, val name: String, val code: String, val rate: Double, val isBase: Boolean)

object CurrencyCache {
  private val CookieCurrencyName: String = "currency"
}

class CurrencyCache @Inject() (domainModelProvider: DomainModelProvider) extends AbstractCacheHandler(Array[Class[_]](classOf[Currency])) {
  private val currencies: Map[Long, CurrencyCacheItem] = readCurrencies
  private val baseCurrencyId: Option[Long] = currencies.filter(_._2.isBase).keys.headOption

  def readCurrencies = {
    val domainModel: DomainModel = this.domainModelProvider.create
    try {
      domainModel.query.from(classOf[Currency]).where("Enabled = $", new java.lang.Boolean(true)).toList(classOf[Currency]).map(c => {
        val item: CurrencyCacheItem = new CurrencyCacheItem(c.getId, c.getName, c.getCode, c.getRate, c.getIsBase)
        (c.getId, item)
      }).toMap
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  def getCurrentCurrencyId(request: HttpServletRequest): OptionalLong = {
    val cookie = request.getCookies.toList.find(_.getName == CurrencyCache.CookieCurrencyName)
    if (cookie.isDefined) {
      val currencyId: Long = cookie.get.getValue.toLong
      return OptionalLong.of(currencyId)
    }
    OptionalLong.empty
  }

  def setCurrentCurrencyId(response: HttpServletResponse, currencyId: OptionalLong) {
    val cookie = if (!currencyId.isPresent) {
      new Cookie(CurrencyCache.CookieCurrencyName, null)
    } else {
      new Cookie(CurrencyCache.CookieCurrencyName, currencyId.toString)
    }
    response.addCookie(cookie)
  }

  def getCurrentCurrency(request: HttpServletRequest): CurrencyCacheItem = {
    val currencyId: OptionalLong = getCurrentCurrencyId(request)
    if (!currencyId.isPresent) {
      return null
    }
    currencies.get(currencyId.getAsLong).get
  }

  def toUserCurrency(request: HttpServletRequest, value: Double): Double = {
    val currency: CurrencyCacheItem = getCurrentCurrency(request)
    if (currency == null || baseCurrencyId.isEmpty || currency.currencyId == baseCurrencyId.get) {
      return value
    }
    val baseCurrencyRate: Double = currencies.get(baseCurrencyId.get).get.rate
    val userCurrencyRate: Double = currency.rate
    if (baseCurrencyRate == userCurrencyRate) {
      return value
    }
    (value / baseCurrencyRate) / userCurrencyRate
  }
}
