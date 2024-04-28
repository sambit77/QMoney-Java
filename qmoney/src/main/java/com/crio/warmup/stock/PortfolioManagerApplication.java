
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.*;
import java.io.IOException; 
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
 









  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  // private static File resolveFileFromResources(String filename) throws URISyntaxException {
  //   Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  //   ThreadContext.put("runId", UUID.randomUUID().toString());
  //   return Paths.get(
  //       Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  // }


  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
}


  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    //return null
    return objectMapper;
  }

  public static List<String> mainReadFile(String[] fileName) throws Exception
  {
    ObjectMapper om = getObjectMapper();
    om.registerModule(new JavaTimeModule()); 
    File file = resolveFileFromResources(fileName[0]);

    PortfolioTrade[] trades = om.readValue(file, PortfolioTrade[].class);

    List<String> tradeSymbols = new ArrayList<String>();
    for(PortfolioTrade pt: trades)
    {
      tradeSymbols.add(pt.getSymbol());
    }
    return tradeSymbols;
  }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/rahulbal99-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }



  

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {

    List<PortfolioTrade>  trades = readTradesFromJson(args[0]);

    List<TotalReturnsDto> tradeAPIResult = new ArrayList<TotalReturnsDto>();
    RestTemplate restTemplate = new RestTemplate();

    //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    //LocalDate localDate = LocalDate.parse(args[1],formatter);

    for(PortfolioTrade t : trades)
    {
      String url = prepareUrl(t, LocalDate.parse(args[1]), "51ede3d5d4ac95f0fcf90b815ea923ece2e4c27c");
      TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);

      if(results != null)
      {
        tradeAPIResult.add(new TotalReturnsDto(t.getSymbol(), results[results.length-1].getClose()));
      }
    }

    Collections.sort(tradeAPIResult,TotalReturnsDto.closingComparator);

    List<String> sortedSymbolsBasedOnClosingPrice = new ArrayList<String>();
    for(TotalReturnsDto t : tradeAPIResult)
    {
      sortedSymbolsBasedOnClosingPrice.add(t.getSymbol());
    }
     return sortedSymbolsBasedOnClosingPrice;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  // public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
  //   ObjectMapper om = new ObjectMapper();
  //   om.registerModule(new JavaTimeModule()); 
  //   //String base = "/home/crio-user/workspace/rahulbal99-ME_QMONEY_V2/qmoney/src/test/resources/assessments/";
  //   //File file =resolveFileFromResources(filename);
  //   String path = "/home/crio-user/workspace/rahulbal99-ME_QMONEY_V2/qmoney/src/test/resources/assessments/";
  //   PortfolioTrade[] trades = om.readValue(new File(path+filename), PortfolioTrade[].class);

  //   List<PortfolioTrade> tradesList = new ArrayList<PortfolioTrade>();

  //   for(PortfolioTrade pt: trades)
  //   {
  //     tradesList.add(pt);
  //   }
  //    return tradesList;
  // }
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    System.out.println("Working Directory: " + System.getProperty("user.dir"));
    File file = resolveFileFromResources(filename);
    ObjectMapper objectmapper = getObjectMapper();
    PortfolioTrade[] portfolioTrade = objectmapper.readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> listPortfolioTrade = Arrays.asList(portfolioTrade);
    return listPortfolioTrade;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String getToken()
  {
    return "51ede3d5d4ac95f0fcf90b815ea923ece2e4c27c";
  }
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    //https://api.tiingo.com/tiingo/daily/googl/prices?startDate=2019-01-02&token=51ede3d5d4ac95f0fcf90b815ea923ece2e4c27c
    //String apiendpoint = "" 
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate().toString() + "&endDate=" + endDate + "&token=" + token;
     return url;
  }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.




  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String url = prepareUrl(trade,endDate,token);

    TiingoCandle[] tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
    List<Candle> candles = new ArrayList<Candle>();
    for(TiingoCandle t : tiingoCandles)
    {
      candles.add(t);
    }

     return candles;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {

    List<PortfolioTrade>  portfolioTrades = readTradesFromJson(args[0]);
    LocalDate endDate =  LocalDate.parse(args[1]);

    List<AnnualizedReturn> annualizedReturnsList = new ArrayList<AnnualizedReturn>();
    
    for(PortfolioTrade trade : portfolioTrades)
    {
      List<Candle> candles = fetchCandles(trade, endDate, "51ede3d5d4ac95f0fcf90b815ea923ece2e4c27c");
      //Prepare the data
      double open = getOpeningPriceOnStartDate(candles);
      double close = getClosingPriceOnEndDate(candles);
      AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, open, close);

      annualizedReturnsList.add(annualizedReturn);
    }

    Collections.sort(annualizedReturnsList, new Comparator<AnnualizedReturn>() {

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
      
    });


     return annualizedReturnsList;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

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























  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       //String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();

        RestTemplate restTemplate = new RestTemplate();
       List<PortfolioTrade> portfolioTrades = readTradesFromJson(args[0]);
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
       return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }






















  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    //printJsonObject(mainReadFile(args));


    //printJsonObject(mainReadQuotes(args));



    //printJsonObject(mainCalculateSingleReturn(args));




    printJsonObject(mainCalculateReturnsAfterRefactor(args));



  }
}

