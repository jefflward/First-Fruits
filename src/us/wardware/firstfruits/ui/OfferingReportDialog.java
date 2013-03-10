package us.wardware.firstfruits.ui;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.TextAction;

import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.XlsxFileFilter;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;


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
        setModalityType(ModalityType.MODELESS);

        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(TallyDialog.class.getResource("/icons/offering.png")).getImage());
        setIconImages(icons);
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
                        setVisible(false);
                        saveReport();
                        dispose();
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
        final StyleBuilder boldStyle         = stl.style().bold();  
        final StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalAlignment(HorizontalAlignment.CENTER);  
        final StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)  
                                            .setBorder(stl.pen1Point())  
                                            .setBackgroundColor(Color.LIGHT_GRAY);    
                  
        final TextColumnBuilder<String> categoryColumn  = col.column("Category", "category", type.stringType());  
        final TextColumnBuilder<BigDecimal> currencyColumn  = col.column("Currency", "currency", type.bigDecimalType());  
        final TextColumnBuilder<BigDecimal> checkColumn     = col.column("Checks", "checks", type.bigDecimalType());  
        final TextColumnBuilder<BigDecimal> totalsColumn     = col.column("Total", "total", type.bigDecimalType());
        
        final FileOutputStream os = new FileOutputStream(outputFile);
        try {             
            report()
              .setTemplate(Templates.reportTemplate)  
              .setColumnTitleStyle(columnTitleStyle)  
              .highlightDetailEvenRows()  
              .columns(categoryColumn, currencyColumn, checkColumn, totalsColumn)  
              .title(cmp.horizontalList()
                        .add(cmp.text(Settings.getInstance().getStringValue(Settings.ORGANIZATION_NAME_KEY)).setStyle(boldStyle))
                        .add(cmp.text("Offering Date: " + RecordManager.getInstance().getSelectedDate()).setStyle(boldStyle).setHorizontalAlignment(HorizontalAlignment.RIGHT)))
              .pageFooter(cmp.pageXofY().setStyle(boldCenteredStyle))
              .setDataSource(offeringPanel.createDataSource())
              .subtotalsAtSummary(sbt.sum(currencyColumn), sbt.sum(checkColumn), sbt.sum(totalsColumn))
              .toXlsx(os);
        } catch (DRException e) {  
            e.printStackTrace();  
        } finally {
            os.close();
        }
    }
}
