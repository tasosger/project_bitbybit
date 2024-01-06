package bitbybit.docker;

import javax.swing.*;
import java.awt.*;

public class ResultFrame extends JFrame {
    public ResultFrame(String text){
        setSize(300,300);
        setTitle("Command Result");
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false); // Make it non-editable
        textArea.setLineWrap(true);  // Enable line wrapping
        textArea.setWrapStyleWord(true);
        textArea.setText(text);

        // Create a JScrollPane to contain the JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Add the JScrollPane to the frame's content pane
        getContentPane().add(scrollPane, BorderLayout.CENTER);

    }
}
