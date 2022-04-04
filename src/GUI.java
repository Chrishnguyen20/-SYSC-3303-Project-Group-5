import java.awt.BorderLayout;
import java.awt.Font;
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
    
    public GUI(int elevatorCount) {
        init(elevatorCount);
    }
    
    public void init(int elevatorCount) {    
        this.frame = new JFrame();
        
        this.panel = new JPanel();
        
        elevatorLabels = new JLabel[elevatorCount];
        
        for (int i = 0; i < elevatorCount; ++i) {
        	JLabel label = new JLabel("Elevator "+i+" |1| Idle");
        	//label.setFont(new Font(Font.SANS_SERIF, Font.BOLD|Font.ITALIC, 20));
        	label.setFont(new Font(Font.MONOSPACED, Font.BOLD|Font.ITALIC, 20));
        	//label.setSize(400, 150);
        	this.elevatorLabels[i] = label;
        	this.panel.add(elevatorLabels[i]);
        }
        
        this.panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        this.panel.setLayout(new GridLayout(4, 2));
        
        
        this.frame.setTitle("SYSC 3303 Group 5");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setVisible(true);
        this.frame.setSize(750, 750);
        this.frame.setLocationRelativeTo(null);
        this.frame.setResizable(false);
        this.frame.add(panel, BorderLayout.CENTER);
    }
    
    public void setLabel(int index, String s) {
    	elevatorLabels[index].setText(s);
    }
}