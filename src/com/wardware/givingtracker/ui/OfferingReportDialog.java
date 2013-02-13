package com.wardware.givingtracker.ui;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.TextAction;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;

import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.fileio.FileUtils;
import com.wardware.givingtracker.fileio.XlsxFileFilter;

public class OfferingReportDialog extends JDialog
{
    private JButton saveButton;
    private OfferingPanel offeringPanel;
    private File outputFile;
    
    public OfferingReportDialog()
    {
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);

        setTitle("Offering Report");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        offeringPanel = new OfferingPanel();
        add(offeringPanel, BorderLayout.CENTER);
        
        final JPanel buttonPanel = new JPanel();
        saveButton = new JButton(new TextAction("Save"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    chooseOutputFile();
                    if (outputFile != null) {
                        saveReport();
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
    
    private void saveReport() throws IOException
    {
        StyleBuilder boldStyle         = stl.style().bold();  
        StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);  
        StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)  
                                            .setBorder(stl.pen1Point())  
                                            .setBackgroundColor(Color.LIGHT_GRAY);    
                  
        //                                                           title,     field name     data type  
        TextColumnBuilder<String> categoryColumn  = col.column("Category", "category", type.stringType());  
        TextColumnBuilder<BigDecimal> currencyColumn  = col.column("Currency", "currency", type.bigDecimalType());  
        TextColumnBuilder<BigDecimal> checkColumn     = col.column("Checks", "checks", type.bigDecimalType());  
        TextColumnBuilder<BigDecimal> totalsColumn     = col.column("Total", "total", type.bigDecimalType());
        
        //StyleBuilder offeringTotalStyle = stl.style(Templates.boldStyle).setHorizontalAlignment(HorizontalAlignment.RIGHT);  
        
        final FileOutputStream os = new FileOutputStream(outputFile);
        try {             
            report()//create new report design  
              .setTemplate(Templates.reportTemplate)  
              .setColumnTitleStyle(columnTitleStyle)  
              .highlightDetailEvenRows()  
              .columns(//add columns  
                categoryColumn, currencyColumn, checkColumn, totalsColumn)  
              .title(cmp.text("Offering Date: " + RecordManager.getInstance().getSelectedDate()).setStyle(boldCenteredStyle))//shows report title  
              .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))//shows number of page at page footer  
              .setDataSource(offeringPanel.createDataSource())//set datasource  
              .subtotalsAtSummary(sbt.sum(totalsColumn))
              //.summary(  
                  //cmp.text(offeringPanel.getOfferingTotal()).setValueFormatter(Templates.createCurrencyValueFormatter("Offering Total:")).setStyle(offeringTotalStyle)) 
              .toXlsx(os);
        } catch (DRException e) {  
            e.printStackTrace();  
        } finally {
            os.close();
        }
    }
}