
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  RestTemplate restTemplate;
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonMappingException, JsonProcessingException , StockQuoteServiceException
       {

        try{
        String url = buildUri(symbol, from, to);

        ObjectMapper objectMapper = getObjectMapper();
        String stocks = restTemplate.getForObject(url, String.class);

    TiingoCandle[] tiingoCandles = objectMapper.readValue(stocks, TiingoCandle[].class);
    List<Candle> candles = new ArrayList<Candle>();
    for(TiingoCandle t : tiingoCandles)
    {
      candles.add(t);
    }

     return candles;
  }
  catch(Exception e)
  {
    throw new StockQuoteServiceException(e.getMessage());
  }
  }

  

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
         + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    String url = String.format("https:api.tiingo.com/tiingo/daily/%s/prices?"
    + "startDate=%s&endDate=%s&token=%s", symbol, startDate,endDate,"51ede3d5d4ac95f0fcf90b815ea923ece2e4c27c");

         return url;
}

private static ObjectMapper getObjectMapper() {
  ObjectMapper objectMapper = new ObjectMapper();
  objectMapper.registerModule(new JavaTimeModule());
  //return null
  return objectMapper;
}



}
