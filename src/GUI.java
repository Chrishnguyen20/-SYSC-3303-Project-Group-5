import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUI extends JFrame{
    private JFrame frame; 
    private JPanel panel;
    private JLabel elevatorLabels[];
//    private JLabel label1;
//    private JLabel label2;
//    private JLabel label3;
//    private JLabel label4;
    
    public GUI(int elevatorCount) {
        init(elevatorCount);
    }
    
    public void init(int elevatorCount) {    
        this.frame = new JFrame();
        
        this.panel = new JPanel();
        
        elevatorLabels = new JLabel[elevatorCount];
        
        for (int i = 0; i < elevatorCount; ++i) {
        	this.elevatorLabels[i] = new JLabel("Elevator "+i+" |1| Idle");
        	this.panel.add(elevatorLabels[i]);
        }
        
//        this.label1 = new JLabel("Elevator 0 |1| Idle");
//        this.label2 = new JLabel("Elevator 1 |1| Idle");
//        this.label3 = new JLabel("Elevator 2 |1| Idle");
//        this.label4 = new JLabel("Elevator 3 |1| Idle");
//        
//        this.panel.add(label1);
//        this.panel.add(label2);
//        this.panel.add(label3);
//        this.panel.add(label4);
        this.panel.setBorder(BorderFactory.createEmptyBorder(250, 250, 250, 250));
        this.panel.setLayout(new GridLayout(4, 2));
        
        
        this.frame.setTitle("SYSC 3303 Group 5");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        this.frame.setSize(750,750);
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);
        this.frame.add(panel, BorderLayout.CENTER);
    }
    
    public void setLabel(int index, String s) {
    	elevatorLabels[index].setText(s);
    }
    
//    public void setLabel1(String s) {
//        label1.setText(s);
//    }
//    
//    public void setLabel2(String s) {
//        label2.setText(s);
//    }
//    
//    public void setLabel3(String s) {
//        label3.setText(s);
//    }
//    
//    public void setLabel4(String s) {
//        label4.setText(s);
//    }
}