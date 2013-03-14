package us.wardware.firstfruits.fileio;

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

import javax.swing.JOptionPane;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;

public class GivingStatementWriter
{
    public static JasperReportBuilder createGivingStatement(String lastName, String firstName )
    {
        final StyleBuilder boldStyle = stl.style().bold();
        final StyleBuilder boldStyleSmall = stl.style().bold().setFontSize(8);
        final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        final StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle)
                        .setBorder(stl.pen1Point())
                        .setBackgroundColor(Color.LIGHT_GRAY);

        final TextColumnBuilder<String> dateColumn = col.column("Date", "date", type.stringType());
        final TextColumnBuilder<String> fundTypeColumn = col.column("Fund Type", "fundType", type.stringType());
        final TextColumnBuilder<Short> checkNumberColumn = col.column("Check #", "checkNumber", type.shortType());

        final List<TextColumnBuilder<?>> columnList = new ArrayList<TextColumnBuilder<?>>();
        columnList.add(dateColumn);
        columnList.add(fundTypeColumn);
        columnList.add(checkNumberColumn);

        final List<AggregationSubtotalBuilder<BigDecimal>> subtotalBuilders = new ArrayList<AggregationSubtotalBuilder<BigDecimal>>();
        final List<String> categories = Settings.getInstance().getCategories();
        for (String category : categories) {
            final TextColumnBuilder<BigDecimal> categoryColumn = col.column(category, category, type.bigDecimalType());
            columnList.add(categoryColumn);
            subtotalBuilders.add(sbt.sum(categoryColumn));
        }
        final TextColumnBuilder<BigDecimal> totalsColumn = col.column("Total", "total", type.bigDecimalType());
        columnList.add(totalsColumn);
        subtotalBuilders.add(sbt.sum(totalsColumn));

        final AggregationSubtotalBuilder<?>[] sbtBuilders = new AggregationSubtotalBuilder<?>[subtotalBuilders.size()];
        subtotalBuilders.toArray(sbtBuilders);

        final TextColumnBuilder<?>[] columns = new TextColumnBuilder<?>[columnList.size()];
        columnList.toArray(columns);
        
        final VerticalListBuilder summary = cmp.verticalList();
        summary.add(cmp.verticalGap(5));
        summary.add(cmp.text("Donor did not receive any goods or services in connection with the donations listed.").setStyle(boldStyleSmall));
        summary.add(cmp.verticalGap(15));
        summary.add(cmp.text("Authorized Signature: ").setStyle(boldStyle));
        summary.add(cmp.verticalGap(10));
        summary.add(cmp.text("                                            ").setStyle(stl.style().underline()));

        return report()// create new report design
        .setTemplate(Templates.reportTemplate)
        .setColumnTitleStyle(columnTitleStyle)
        .highlightDetailEvenRows()
        .columns(columns)
        .title(createTitle(lastName, firstName))
        .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))
        .setDataSource(createDataSource(lastName, firstName))
        .subtotalsAtSummary(sbtBuilders)
        .summary(summary);
    }
    
    public static void writeGivingStatement(String lastName, String firstName, File outputFile) throws IOException
    {
        final FileOutputStream os = new FileOutputStream(outputFile);
        try {
            final JasperReportBuilder statement = createGivingStatement(lastName, firstName);
            statement.toXlsx(os);
        } catch (DRException e) {
            JOptionPane.showMessageDialog(null, "Exception occurred writing report: " + e.getMessage());
            e.printStackTrace();
        } finally {
            os.flush();
            os.close();
        }
    }
    
    private static ComponentBuilder<?, ?> createTitle(String lastName, String firstName)
    {
        final VerticalListBuilder list = cmp.verticalList();
        list.add(cmp.text("Statement of Giving").setStyle(stl.style().bold()).setHorizontalAlignment(HorizontalAlignment.CENTER));
        list.add(cmp.horizontalList()
                      .setStyle(stl.style(10).setHorizontalAlignment(HorizontalAlignment.LEFT))
                      .setGap(50)
                      .add(cmp.hListCell(createNameComponent(lastName, firstName)).heightFixedOnTop())
                      .add(cmp.hListCell(createChurchAddressComponent()).heightFixedOnTop()), 
                 cmp.verticalGap(10));
       
        return list;
    }
    
    private static ComponentBuilder<?, ?> createNameComponent(String lastName, String firstName)
    {
        final HorizontalListBuilder list = cmp.horizontalList().setBaseStyle(stl.style().setTopBorder(stl.pen1Point()).setLeftPadding(10));
        addAddressAttribute(list, "Address", "", true);
        addAddressAttribute(list, "", "", true);
        addAddressAttribute(list, "", "", true);
        addAddressAttribute(list, "Phone", "", true);
        final HorizontalListBuilder title = cmp.horizontalFlowList();
        title.add(cmp.text(String.format("Donor: %s %s", firstName, lastName)).setStyle(Templates.boldStyle)).newRow();
        return cmp.verticalList(title, list);
    }
    
    private static ComponentBuilder<?, ?> createChurchAddressComponent()
    {
        final HorizontalListBuilder list = cmp.horizontalList().setBaseStyle(stl.style().setTopBorder(stl.pen1Point()).setLeftPadding(10));
        addAddressAttribute(list, "Address", Settings.getInstance().getStringValue(Settings.ADDRESS1), false);
        addAddressAttribute(list, "", Settings.getInstance().getStringValue(Settings.ADDRESS2), false);
        final String city = Settings.getInstance().getStringValue(Settings.CITY);
        final String state = Settings.getInstance().getStringValue(Settings.STATE);
        final String zip = Settings.getInstance().getStringValue(Settings.ZIP);
        final String cityStateZip = String.format("%s, %s %s", city, state, zip);
        addAddressAttribute(list, "", cityStateZip, false);
        addAddressAttribute(list, "Phone", Settings.getInstance().getStringValue(Settings.PHONE), false);
        final HorizontalListBuilder title = cmp.horizontalFlowList();
        title.add(cmp.text(Settings.getInstance().getStringValue(Settings.ORGANIZATION_NAME_KEY)).setStyle(Templates.boldStyle)).newRow();
        return cmp.verticalList(title, list);
    }
    
    private static void addAddressAttribute(HorizontalListBuilder list, String label, String value, boolean allowEmptyValue) {
        if (allowEmptyValue || (value != null && !value.isEmpty())) {
            if (label != null && !label.isEmpty()) {
                list.add(cmp.text(label + ":").setFixedColumns(8).setStyle(Templates.boldStyle), cmp.text(value)).newRow();
            } else {
                list.add(cmp.text("").setFixedColumns(8).setStyle(Templates.boldStyle), cmp.text(value)).newRow();
            }
        }
    }

    private static JRDataSource createDataSource(String lastName, String firstName)
    {
        final List<GivingRecord> records = RecordManager.getInstance().getRecordsForName(lastName, firstName);

        final List<String> columnList = new ArrayList<String>();
        columnList.add("date");
        columnList.add("fundType");
        columnList.add("checkNumber");
        final List<String> categories = Settings.getInstance().getCategories();
        columnList.addAll(categories);
        columnList.add("total");

        final String[] columns = new String[columnList.size()];
        columnList.toArray(columns);

        final DRDataSource dataSource = new DRDataSource(columns);

        for (GivingRecord record : records) {
            final List<Object> data = new ArrayList<Object>();
            data.add(record.getDateString());
            data.add(record.getFundType());
            if (record.getCheckNumber().isEmpty()) {
                data.add(null);
            } else {
                data.add(Short.parseShort(record.getCheckNumber()));
            }
            for (String category : categories) {
                final BigDecimal amount = new BigDecimal(record.getAmountForCategory(category));
                data.add(amount);
            }
            data.add(new BigDecimal(record.getTotal()));
            dataSource.add(data.toArray());
        }

        return dataSource;
    }

    public static void printGivingStatement(String lastName, String firstName)
    {
        final JasperReportBuilder statement = createGivingStatement(lastName, firstName);
        try {
            statement.print(true);
        } catch (DRException e) {
            e.printStackTrace();
        }
    }
}
