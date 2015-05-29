/*
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package com.lessmarkup.engine.cache

import javax.servlet.http.Cookie

import com.google.inject.Inject
import com.lessmarkup.dataobjects.Currency
import com.lessmarkup.framework.system.RequestContextHolder
import com.lessmarkup.interfaces.cache.AbstractCacheHandler
import com.lessmarkup.interfaces.data.{DomainModel, DomainModelProvider}

class CurrencyCacheItem(val currencyId: Long, val name: String, val code: String, val rate: Double, val isBase: Boolean)

object CurrencyCache {
  private val CookieCurrencyName: String = "currency"
}

class CurrencyCache @Inject() (domainModelProvider: DomainModelProvider) extends AbstractCacheHandler(Seq(classOf[Currency])) {

  private val currencies: Map[Long, CurrencyCacheItem] = {
    val domainModel: DomainModel = this.domainModelProvider.create
    try {
      domainModel.query.from(classOf[Currency]).where("Enabled = $", new java.lang.Boolean(true)).toList(classOf[Currency]).map(c => {
        val item: CurrencyCacheItem = new CurrencyCacheItem(c.id, c.name, c.code, c.rate, c.isBase)
        (c.id, item)
      }).toMap
    } finally {
      if (domainModel != null) domainModel.close()
    }
  }

  private val baseCurrencyId: Option[Long] = currencies.filter(_._2.isBase).keys.headOption

  def getCurrentCurrencyId: Option[Long] = {

    val requestContext = RequestContextHolder.getContext

    val cookie = requestContext.getCookie(CurrencyCache.CookieCurrencyName)
    if (cookie.isDefined) {
      val currencyId: Long = cookie.get.getValue.toLong
      Option(currencyId)
    } else {
      None
    }
  }

  def setCurrentCurrencyId(currencyId: Option[Long]) {

    val requestContext = RequestContextHolder.getContext

    val cookie = if (currencyId.isEmpty) {
      new Cookie(CurrencyCache.CookieCurrencyName, null)
    } else {
      new Cookie(CurrencyCache.CookieCurrencyName, currencyId.toString)
    }

    requestContext.setCookie(cookie)
  }

  def getCurrentCurrency: Option[CurrencyCacheItem] = {
    val currencyId = getCurrentCurrencyId
    if (currencyId.isEmpty) {
      None
    } else {
      currencies.get(currencyId.get)
    }
  }

  def toUserCurrency(value: Double): Double = {
    val currency = getCurrentCurrency
    if (currency.isEmpty || baseCurrencyId.isEmpty || currency.get.currencyId == baseCurrencyId.get) {
      return value
    }
    val baseCurrencyRate: Double = currencies.get(baseCurrencyId.get).get.rate
    val userCurrencyRate: Double = currency.get.rate
    if (baseCurrencyRate == userCurrencyRate) {
      return value
    }
    (value / baseCurrencyRate) / userCurrencyRate
  }
}
