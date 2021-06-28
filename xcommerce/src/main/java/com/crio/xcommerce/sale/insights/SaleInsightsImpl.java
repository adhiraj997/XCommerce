package com.crio.xcommerce.sale.insights;

//import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.crio.xcommerce.contract.exceptions.AnalyticsException;
import com.crio.xcommerce.contract.insights.SaleAggregate;
import com.crio.xcommerce.contract.insights.SaleAggregateByMonth;
import com.crio.xcommerce.contract.insights.SaleInsights;
import com.crio.xcommerce.contract.resolver.DataProvider;

import com.opencsv.CSVReader;  

public class SaleInsightsImpl implements SaleInsights { 
    
    public SaleAggregate getSaleInsights(DataProvider dataProvider, int year)
            throws IOException, AnalyticsException { 
        
        File CsvFile = dataProvider.resolveFile();
        String vendorName = dataProvider.getProvider();

        // BufferedReader br = new 
        //         BufferedReader(new FileReader(CsvFile, StandardCharsets.UTF_8));
        CSVReader reader = new CSVReader(new FileReader(CsvFile, StandardCharsets.UTF_8));
        // String line = "";
        // String splitBy = ",";
        String[] nextLine;
        int lineNumber = 0;

        Double[] amountByMonth = new Double[12];
        Arrays.fill(amountByMonth, 0, 12, 0.0);

        Double totalAmount = 0.0;
        List<SaleAggregateByMonth> aggregateByMonths = new ArrayList<>();

        if(vendorName.equalsIgnoreCase("Amazon")) {
            while((nextLine = reader.readNext()) != null) {
                //nextLine = line.split(splitBy);
                lineNumber++;
                //skip the first line
                if(lineNumber == 1) continue;

                // System.out.println(lineNumber);
                // System.out.println(nextLine.length);

                //Throw analytics error if date or amount is absent 
                if(nextLine[4].equals("")  || nextLine[5].equals(""))
                    throw new AnalyticsException("Date or amount is absent");

                // if(nextLine[4].equals("")  || nextLine.length == 5)
                //     throw new AnalyticsException("Date or amount is absent");
                
                //extract the year
                String date = nextLine[4];
                int yearInLine = Integer.parseInt(date.substring(0, 4));

                //continue if the year is not the required year
                if(yearInLine != year) continue;

                //extract the month 
                int month = Integer.parseInt(date.substring(5, 7));

                //if the status is shipped, extract the amount  
                Double amount;
                if(nextLine[3].equals("shipped"))
                    amount = Double.parseDouble(nextLine[5]);

                else continue;
                
                //increase the amount in array for the month, and also the total amount 
                amountByMonth[month - 1] += amount;
                totalAmount += amount;

            }

        }

        else if(vendorName.equalsIgnoreCase("Ebay")) {
            while((nextLine = reader.readNext()) != null) {
                //nextLine = line.split(splitBy);
                lineNumber++;
                //skip the first line
                if(lineNumber == 1) continue;

                //Throw analytics error if date or amount is absent 
                if(nextLine[3].equals("") || nextLine[4].equals(""))
                    throw new AnalyticsException("Date or amount is absent");

                // if(nextLine[3].equals("") || nextLine.length == 4)
                //     throw new AnalyticsException("Date or amount is absent");
                
                //extract the year
                String date = nextLine[3];
                int yearInLine = Integer.parseInt(date.substring(6));

                //continue if the year is not the required year
                if(yearInLine != year) continue;

                //extract the month 
                int month = Integer.parseInt(date.substring(0, 2));

                //if the status is complete or delivered, extract the amount  
                Double amount;
                if(nextLine[2].equals("complete") || nextLine[2].equals("Delivered"))
                    amount = Double.parseDouble(nextLine[4]);

                else continue;
                
                //increase the amount in array for the month, and also the total amount 
                amountByMonth[month - 1] += amount;
                totalAmount += amount;

            }

        }

        else if(vendorName.equalsIgnoreCase("Flipkart")) {
            while((nextLine = reader.readNext()) != null) {
                //nextLine = line.split(splitBy);
                lineNumber++;
                //skip the first line
                if(lineNumber == 1) continue;

                //Throw analytics error if date or amount is absent 
                if(nextLine[3].equals("") || nextLine[5].equals(""))
                    throw new AnalyticsException("Date or amount is absent");

                // if(nextLine[3].equals("") || nextLine.length == 5)
                //     throw new AnalyticsException("Date or amount is absent");
                
                //extract the year
                String date = nextLine[3];
                int yearInLine = Integer.parseInt(date.substring(0, 4));

                //continue if the year is not the required year
                if(yearInLine != year) continue;

                //extract the month 
                int month = Integer.parseInt(date.substring(5, 7));

                //if the status is complete, paid or shipped, extract the amount  
                Double amount;
                if(nextLine[4].equals("complete") || nextLine[4].equals("paid")
                        || nextLine[4].equals("shipped"))
                    amount = Double.parseDouble(nextLine[5]);

                else continue;
                
                //increase the amount in array for the month, and also the total amount 
                amountByMonth[month - 1] += amount;
                totalAmount += amount;

            }

        }

        //close BufferedReader object 
        //br.close();

        //Create SaleAggregateByMonth objects for each month
        for(int i = 0; i < 12; i++) {
            int month = i + 1;
            Double amount = amountByMonth[i];

            //Create the object 
            SaleAggregateByMonth saleAggregateByMonth = 
                    new SaleAggregateByMonth(month, amount);

            //add the object to the list 
            aggregateByMonths.add(saleAggregateByMonth);

        }

        //create a SaleAggregate object 
        SaleAggregate saleAggregate = new SaleAggregate();
        saleAggregate.setTotalSales(totalAmount);
        saleAggregate.setAggregateByMonths(aggregateByMonths);

        //return the object 
        return saleAggregate;

        
    }
}