package com.lessmarkup.engine.currencies;

import com.google.inject.Inject;
import com.lessmarkup.dataobjects.Currency;
import com.lessmarkup.interfaces.cache.AbstractCacheHandler;
import com.lessmarkup.interfaces.data.DomainModel;
import com.lessmarkup.interfaces.data.DomainModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CurrencyCache extends AbstractCacheHandler {

    private final Map<Long, CurrencyCacheItem> currencies = new HashMap<>();
    private final List<CurrencyCacheItem> currencyList = new ArrayList<>();
    private final DomainModelProvider domainModelProvider;
    private OptionalLong baseCurrencyId = OptionalLong.empty();
    
    private static final String CookieCurrencyName = "currency";

    @Inject
    public CurrencyCache(DomainModelProvider domainModelProvider) {
        super(new Class<?>[] { Currency.class });
        this.domainModelProvider = domainModelProvider;
    }
    
    @Override
    public void initialize(OptionalLong objectId) {
        if (objectId.isPresent()) {
            throw new IllegalArgumentException();
        }
        
        try (DomainModel domainModel = this.domainModelProvider.create()) {
            domainModel.query().from(Currency.class).where("Enabled = $", true).toList(Currency.class).forEach(c -> {
                CurrencyCacheItem item = new CurrencyCacheItem(c.getId(), c.getName(), c.getCode(), c.getRate(), c.getIsBase());
                currencies.put(c.getId(), item);
                currencyList.add(item);
                if (c.getIsBase()) {
                    baseCurrencyId = OptionalLong.of(c.getId());
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(CurrencyCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public OptionalLong getCurrentCurrencyId(HttpServletRequest request) {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(CookieCurrencyName)).findFirst();
        if (cookie.isPresent()) {
            long currencyId = Long.parseLong(cookie.get().getValue());
            return OptionalLong.of(currencyId);
        }
        
        return OptionalLong.empty();
    }
    
    public void setCurrentCurrencyId(HttpServletResponse response, OptionalLong currencyId) {
        Cookie cookie;
        if (!currencyId.isPresent()) {
            cookie = new Cookie(CookieCurrencyName, null);
        } else {
            cookie = new Cookie(CookieCurrencyName, currencyId.toString());
            
        }
        response.addCookie(cookie);
    }
    
    public CurrencyCacheItem getCurrentCurrency(HttpServletRequest request) {
        OptionalLong currencyId = getCurrentCurrencyId(request);
        if (!currencyId.isPresent()) {
            return null;
        }
        
        return currencies.get(currencyId.getAsLong());
    }
    
    public double toUserCurrency(HttpServletRequest request, double value) {
        CurrencyCacheItem currency = getCurrentCurrency(request);
        
        if (currency == null || !baseCurrencyId.isPresent() || currency.getCurrencyId() == baseCurrencyId.getAsLong()) {
            return value;
        }
        
        double baseCurrencyRate = currencies.get(baseCurrencyId.getAsLong()).getRate();
        double userCurrencyRate = currency.getRate();
        
        if (baseCurrencyRate == userCurrencyRate) {
            return value;
        }
        
        return (value / baseCurrencyRate) / userCurrencyRate;
    }
}
