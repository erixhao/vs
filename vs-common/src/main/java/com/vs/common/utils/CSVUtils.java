package com.vs.common.utils;


import com.vs.common.domain.TradingBook;
import com.vs.common.domain.vo.CSVSupport;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by erix-mac on 15/8/6.
 */
public final class CSVUtils {
    private static CSVFormat CSV_FORMAT = CSVFormat.EXCEL;


    @SneakyThrows(IOException.class)
    public static void writeCSV(String fileName, List<? extends CSVSupport> domains, boolean append) {

        try {
            @Cleanup
            FileWriter fileWriter = new FileWriter(fileName,append);
            @Cleanup
            CSVPrinter writer = new CSVPrinter(fileWriter, CSV_FORMAT);

            if ( domains == null || domains.size() == 0)
                return;

            String[] header = domains.get(0).getCSVHeader();
            for (int i=0;i<domains.size();i++) {

                if ( i ==0 ){
                    writer.printRecord(header);
                }

                writer.printRecord(domains.get(i).getCSVRecord());
            }
        } finally {
        }
    }

    public static void writeTradeBackTest(String fileName, List<TradingBook> tradingBooks){
    }
}
