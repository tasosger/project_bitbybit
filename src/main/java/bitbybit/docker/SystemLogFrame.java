package bitbybit.docker;

import javax.swing.*;
import java.awt.*;

public class SystemLogFrame extends JFrame {
    public SystemLogFrame(String message){
        setTitle("Message");
        setSize(600, 100);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false); // Make it non-editable
        textArea.setLineWrap(true);  // Enable line wrapping
        textArea.setWrapStyleWord(true);
        textArea.setText(message);

        // Create a JScrollPane to contain the JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Add the JScrollPane to the frame's content pane
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
