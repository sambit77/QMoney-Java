
package com.crio.warmup.stock.dto;

import java.util.Comparator;

public class TotalReturnsDto {

  private String symbol;
  private Double closingPrice;

  public static Comparator closingComparator = new Comparator<TotalReturnsDto>() {

    @Override
    public int compare(TotalReturnsDto arg0,TotalReturnsDto arg1) {
      // TODO Auto-generated method stub
      int val = 0;
      if(arg0.getClosingPrice() > arg1.getClosingPrice())
      {
        val = 1;
      }
      else
      {
        val = -1;
      }
      return val;
    }
    
  };

  public TotalReturnsDto(String symbol, Double closingPrice) {
    this.symbol = symbol;
    this.closingPrice = closingPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public Double getClosingPrice() {
    return closingPrice;
  }

  public void setClosingPrice(Double closingPrice) {
    this.closingPrice = closingPrice;
  }
}
