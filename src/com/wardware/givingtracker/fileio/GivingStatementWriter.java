package com.wardware.givingtracker.fileio;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.Settings;

public class GivingStatementWriter
{
    public static void writeGivingStatement(String name, File outputFile) throws IOException
    {
        final StyleBuilder boldStyle         = stl.style().bold();  
        final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);  
        final StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)  
                                            .setBorder(stl.pen1Point())  
                                            .setBackgroundColor(Color.LIGHT_GRAY);    
                  
        final TextColumnBuilder<String> dateColumn  = col.column("Date", "date", type.stringType());  
        
        final List<TextColumnBuilder<?>> columnList = new ArrayList<TextColumnBuilder<?>>();
        columnList.add(dateColumn);
        
        final List<AggregationSubtotalBuilder<BigDecimal>> subtotalBuilders = new ArrayList<AggregationSubtotalBuilder<BigDecimal>>();
        final List<String> categories = Settings.getInstance().getCategories();
        for (String category : categories) {
            final TextColumnBuilder<BigDecimal> categoryColumn = col.column(category, category, type.bigDecimalType());
            columnList.add(categoryColumn);
            subtotalBuilders.add(sbt.sum(categoryColumn));
        }
        final TextColumnBuilder<BigDecimal> totalsColumn     = col.column("Total", "total", type.bigDecimalType());
        columnList.add(totalsColumn);
        subtotalBuilders.add(sbt.sum(totalsColumn));
        
        final AggregationSubtotalBuilder<?>[] sbtBuilders = new AggregationSubtotalBuilder<?>[subtotalBuilders.size()];
        subtotalBuilders.toArray(sbtBuilders);
        
        final TextColumnBuilder<?>[] columns = new TextColumnBuilder<?>[columnList.size()];
        columnList.toArray(columns);
        
        final FileOutputStream os = new FileOutputStream(outputFile);
        try {             
            report()//create new report design  
              .setTemplate(Templates.reportTemplate)  
              .setColumnTitleStyle(columnTitleStyle)  
              .highlightDetailEvenRows()  
              .columns(columns)
              .title(cmp.text("STATEMENT OF GIVING").setStyle(boldCenteredStyle))//shows report title  
              .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))//shows number of page at page footer  
              .setDataSource(createDataSource(name))//set datasource  
              .subtotalsAtSummary(sbtBuilders)
              .summary(cmp.text("Authorized signature: ______________________________").setStyle(boldStyle))
              .toXlsx(os);
        } catch (DRException e) {  
            e.printStackTrace();  
        } finally {
            os.flush();
            os.close();
        }
   }
    
    private static JRDataSource createDataSource(String name) 
    {  
        final List<GivingRecord> records = RecordManager.getInstance().getRecordsForName(name);
        
        final List<String> columnList = new ArrayList<String>();
        columnList.add("date");
        final List<String> categories = Settings.getInstance().getCategories();
        columnList.addAll(categories);
        columnList.add("total");
        
        final String[] columns = new String[columnList.size()];
        columnList.toArray(columns);
        
        final DRDataSource dataSource = new DRDataSource(columns);
        
        for (GivingRecord record : records) {
            final List<Object> data = new ArrayList<Object>();
            data.add(record.getDate());
            for (String category : categories) {
                final BigDecimal amount = new BigDecimal(record.getAmountForCategory(category));
                data.add(amount);
            }
            data.add(new BigDecimal(record.getTotal()));
            dataSource.add(data.toArray());
        }
        
        return dataSource;  
    }
}
