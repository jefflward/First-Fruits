package us.wardware.firstfruits;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import us.wardware.firstfruits.ui.FirstFruitsFrame;


public class FirstFruits
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final FirstFruitsFrame firstFruitsFrame = new FirstFruitsFrame();
                firstFruitsFrame.setVisible(true);
            }
        });
    }
}
