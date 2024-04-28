
package com.crio.warmup.stock.portfolio;

import netscape.javascript.JSException;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


RestTemplate restTemplate;
private StockQuotesService stockQuotesService;
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  //@Deprecated
  PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  
   PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService = stockQuotesService;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException,StockQuoteServiceException
       {

       return stockQuotesService.getStockQuote(symbol, from, to);
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
    // TODO Auto-generated method stub

    List<AnnualizedReturn> annualizedReturnsList = new ArrayList<AnnualizedReturn>();
    
    for(PortfolioTrade trade : portfolioTrades)
    {
  
      
    try {
      List<Candle> candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(),endDate);
   
      //Prepare the data
      double open = candles.get(0).getOpen();
      double close = candles.get(candles.size()-1).getClose();
      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, open, close);

      annualizedReturnsList.add(annualizedReturn);
    }
     
  catch (Exception e) {
    //TODO: handle exception
  }
    }

   /*  Collections.sort(annualizedReturnsList, new Comparator<AnnualizedReturn>() {

      @Override
      public int compare(AnnualizedReturn arg0, AnnualizedReturn arg1) {
        // TODO Auto-generated method stub
        int val = 0;
        if(arg0.getAnnualizedReturn() > arg1.getAnnualizedReturn())
        {
          val = -1;
        }
        else
        {
          val = 1;
        }
        return val;
      }
      
    });*/

    Collections.sort(annualizedReturnsList,getComparator());


     return annualizedReturnsList;

  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {

        double total_returns = (sellPrice - buyPrice) / buyPrice;

        double total_num_years = (ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate) / 365.24);

        double annualized_returns = Math.pow((1+total_returns),(1/total_num_years)) - 1;

        System.out.println("total_returns : "+ total_returns);
        System.out.println("total_num_years : "+ total_num_years);
        System.out.println("annualized_returns : "+ annualized_returns);
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, total_returns);
  }



  


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
    LocalDate endDate, int numThreads) throws InterruptedException,StockQuoteServiceException
    {
      List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();

      List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();

      final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

      for(int i = 0 ; i < portfolioTrades.size() ; i++)
      {
        PortfolioTrade trade = portfolioTrades.get(i);

        Callable<AnnualizedReturn> callableTask = () -> {return calculateAnnualizedReturns(endDate, trade);};
        Future<AnnualizedReturn> futureReturn = pool.submit(callableTask);
        futureReturnsList.add(futureReturn);
        //AnnualizedReturn annreturn = calculateAnnualizedReturns(endDate, trade);
        //annualizedReturns.add(annreturn);
      }


      for(int i = 0 ; i < portfolioTrades.size() ; i++)
      {
        Future<AnnualizedReturn> futureReturn = futureReturnsList.get(i);
        try {
          AnnualizedReturn returns = futureReturn.get();
          annualizedReturns.add(returns);
        } catch (ExecutionException e) {
          //TODO: handle exception
          throw new StockQuoteServiceException("Error when calling API");
        }
      }
      Collections.sort(annualizedReturns,getComparator(
        
      ));
      return annualizedReturns;
    }

    public AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade) throws StockQuoteServiceException
      {
        Double buyPrice = 0.0;
        double sellPrice = 0.0;
        try {
          LocalDate startDate = trade.getPurchaseDate();
          String symbol = trade.getSymbol();

          List<Candle> candles = getStockQuote(symbol,startDate,endDate);

          buyPrice = candles.get(0).getOpen();
          sellPrice = candles.get(candles.size()-1).getClose();
        } catch (JsonProcessingException e) {
          //TODO: handle exception
          throw new RuntimeException();
        }
        return  calculateAnnualizedReturns(endDate,trade,buyPrice,sellPrice);
      }

}
