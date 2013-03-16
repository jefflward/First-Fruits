package us.wardware.firstfruits.ui;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.TextAction;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.XlsxFileFilter;
import us.wardware.firstfruits.ui.OfferingPanel.OfferingReportSettings;


public class OfferingReportDialog extends JDialog
{
    private JButton printButton;
    private JButton saveButton;
    private OfferingPanel offeringPanel;
    private File outputFile;
    
    public OfferingReportDialog(JFrame owner)
    {
        super(owner);
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());

        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(TallyDialog.class.getResource("/icons/offering.png")).getImage());
        setIconImages(icons);
        setTitle("Offering Report");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        offeringPanel = new OfferingPanel();
        add(offeringPanel, BorderLayout.CENTER);
        
        final JPanel buttonPanel = new JPanel();
        
        printButton = new JButton(new TextAction("Print"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                printReport();
            }
        });
        buttonPanel.add(printButton);
        
        saveButton = new JButton(new TextAction("Save"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    chooseOutputFile();
                    if (outputFile != null) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        saveReport();
                        setCursor(Cursor.getDefaultCursor());
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(OfferingReportDialog.this, "Error occurred while running report: " + e.getMessage(), "Run Report Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        saveButton.setDefaultCapable(true);
        
        buttonPanel.add(saveButton);
        
        final JButton closeButton = new JButton(new TextAction("Close"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        setResizable(false);
        invalidate();
        pack();
    }
    
    private void chooseOutputFile()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new XlsxFileFilter());                    
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            if (FileUtils.getExtension(outputFile) == null) {
                outputFile = new File(outputFile.getAbsolutePath().concat("." + FileUtils.XLSX));
            }
        }
    }
    
    private JasperReportBuilder createReport()
    {
        final StyleBuilder boldStyle         = stl.style().bold();  
        final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);  
        final StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)  
                                            .setBorder(stl.pen1Point())  
                                            .setBackgroundColor(Color.LIGHT_GRAY);    
                  
        final TextColumnBuilder<String> categoryColumn  = col.column("Category", "category", type.stringType());  
        final TextColumnBuilder<BigDecimal> currencyColumn  = col.column("Currency", "currency", type.bigDecimalType());  
        final TextColumnBuilder<BigDecimal> checkColumn     = col.column("Checks", "checks", type.bigDecimalType());  
        final TextColumnBuilder<BigDecimal> totalsColumn     = col.column("Total", "total", type.bigDecimalType());
        
        final JasperReportBuilder report = report()
          .setTemplate(Templates.reportTemplate)  
          .setColumnTitleStyle(columnTitleStyle)  
          .highlightDetailEvenRows()  
          .columns(categoryColumn, currencyColumn, checkColumn, totalsColumn)  
          .title(cmp.verticalList()
                    .add(cmp.horizontalList()
                            .add(cmp.text(Settings.getInstance().getStringValue(Settings.ORGANIZATION_NAME_KEY)).setStyle(boldStyle))
                            .add(cmp.text("Offering Date: " + RecordManager.getInstance().getSelectedDate()).setStyle(boldStyle).setHorizontalAlignment(HorizontalAlignment.RIGHT)))
                    .add(cmp.verticalGap(10)))
          .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))
          .setDataSource(offeringPanel.createDataSource())
          .subtotalsAtSummary(sbt.sum(currencyColumn), sbt.sum(checkColumn), sbt.sum(totalsColumn))
          .setPageFormat(PageType.LETTER)
          .setPageMargin(DynamicReports.margin(20));
        
        final VerticalListBuilder summary = cmp.verticalList();
        final OfferingReportSettings offeringReportSettings = offeringPanel.getOfferingReportSettings();
        if (offeringReportSettings.includeContributions) {
            summary.add(cmp.verticalGap(30));
            summary.add(cmp.subreport(createRecordsSubReport(offeringReportSettings.includeDonorNames)));
        }
        
        if (offeringReportSettings.includeSignatures) {
            summary.add(cmp.verticalGap(30));
            
            final HorizontalListBuilder signatures = cmp.horizontalList();
            final VerticalListBuilder signature1 = cmp.verticalList();
            signature1.add(cmp.filler().setFixedDimension(200, 10).setStyle(stl.style().setTopBorder(stl.pen1Point())));
            signature1.add(cmp.text(offeringReportSettings.signature1).setStyle(stl.style()));
            signatures.add(signature1);
            
            final VerticalListBuilder signature2 = cmp.verticalList();
            signature2.add(cmp.filler().setFixedDimension(200, 10).setStyle(stl.style().setTopBorder(stl.pen1Point())));
            signature2.add(cmp.text(offeringReportSettings.signature2).setStyle(stl.style()));
            signatures.add(signature2);
            
            summary.add(signatures);
        }
        report.summary(summary);
        
        return report;
    }
    
    public JasperReportBuilder createRecordsSubReport(boolean includeDonorNames)
    {
        final StyleBuilder boldStyle = stl.style().bold();
        final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);
        final StyleBuilder columnTitleStyle = stl.style(boldCenteredStyle)
                        .setBorder(stl.pen1Point())
                        .setBackgroundColor(Color.LIGHT_GRAY);

        final TextColumnBuilder<String> lastNameColumn = col.column("Last Name", "lastName", type.stringType());
        final TextColumnBuilder<String> firstNameColumn = col.column("First Name", "firstName", type.stringType());
        final TextColumnBuilder<String> fundTypeColumn = col.column("Fund Type", "fundType", type.stringType());
        final TextColumnBuilder<Short> checkNumberColumn = col.column("Check #", "checkNumber", type.shortType());

        final List<TextColumnBuilder<?>> columnList = new ArrayList<TextColumnBuilder<?>>();
        if (includeDonorNames) {
            columnList.add(lastNameColumn);
            columnList.add(firstNameColumn);
        }
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

        return report()// create new report design
        .setTemplate(Templates.reportTemplate)
        .setColumnTitleStyle(columnTitleStyle)
        .title(cmp.verticalList()
                  .add(cmp.text("Offering Contributions").setStyle(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER))
                  .add(cmp.verticalGap(10)))
        .highlightDetailEvenRows()
        .columns(columns)
        .setDataSource(createRecordsDataSource(includeDonorNames))
        .subtotalsAtSummary(sbtBuilders);
    }
    
    private JRDataSource createRecordsDataSource(boolean includeDonorNames)
    {
        final String selectedDate = offeringPanel.getSelectedDate();
        final List<GivingRecord> records = RecordManager.getInstance().getRecordsForDate(selectedDate);

        final List<String> columnList = new ArrayList<String>();
        if (includeDonorNames) {
            columnList.add("lastName");
            columnList.add("firstName");
        }
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
            if (includeDonorNames) {
                data.add(record.getLastName());
                data.add(record.getFirstName());
            }
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
    
    private void printReport()
    {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            final JasperReportBuilder report = createReport();
            setCursor(Cursor.getDefaultCursor());
            report.print(true);
        } catch (DRException e) {
            e.printStackTrace();
        }
    }

    private void saveReport() throws IOException
    {
        final FileOutputStream os = new FileOutputStream(outputFile);
        try {             
            createReport().toXlsx(os);
        } catch (DRException e) {  
            e.printStackTrace();  
        } finally {
            os.close();
        }
    }
}
