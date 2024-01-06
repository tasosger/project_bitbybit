package bitbybit.docker;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContainerActionFrame extends JFrame {

    public ContainerActionFrame(String containerName) {
        setTitle("Container Actions");
        setSize(300, 270);

        // Create JList for actions
        DefaultListModel<String> actionListModel = new DefaultListModel<>();
        JList<String> actionList = new JList<>(actionListModel);

        // Create a JScrollPane to contain the JList
        JScrollPane actionScrollPane = new JScrollPane(actionList);

        // Add the JScrollPane to the frame
        add(actionScrollPane, BorderLayout.CENTER);

        // Create buttons for container actions
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            try {
                ActFrame af = new ActFrame("Start",containerName);
                af.setVisible(true);

                dispose();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "StopContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    ErrorFrame er =new ErrorFrame();
                    er.setVisible(true);
                }
                dispose();
            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
            JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            try {
                ActFrame af = new ActFrame("Pause",containerName);
                af.setVisible(true);

                dispose();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JButton unpauseButton = new JButton("Unpause");
        unpauseButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "UnpauseContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    ErrorFrame er =new ErrorFrame();
                    er.setVisible(true);
                }
                dispose();
            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> {
             try {
                ActFrame af = new ActFrame("Restart",containerName);
                af.setVisible(true);

                dispose();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "RemoveContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    ErrorFrame er =new ErrorFrame();
                    er.setVisible(true);
                }
                dispose();
            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JButton commandButton = new JButton("Execute Command");
        commandButton.addActionListener(e -> {
            CommandFrame com = new CommandFrame(containerName);
            com.setVisible(true);
        });
        JButton logsButton = new JButton("Logs");
        logsButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "ContainerLogs" + "?containerName=" + containerName.substring(containerName.indexOf("/") + 1, containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                StringBuilder res = new StringBuilder();
                if (con.getResponseCode()==HttpURLConnection.HTTP_OK){
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    while ((line = r.readLine())!=null){
                        res.append(line+"\n");
                    }
                }
                ResultFrame rs = new ResultFrame(res.toString());
                rs.setVisible(true);
                dispose();
            } catch (MalformedURLException e1){

            } catch (IOException e1){

            }
        });
        JButton volumeButton = new JButton("Volume Mounts");
        volumeButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "GetVolumeMounts" + "?containerName=" + containerName.substring(containerName.indexOf("/") + 1, containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                List<String> res = new ArrayList<>();
                if (con.getResponseCode()==HttpURLConnection.HTTP_OK){
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    while ((line = r.readLine())!=null){
                        res.add(line);
                    }
                }
                ResultFrame rs = new ResultFrame(res.toString());
                rs.setVisible(true);
                dispose();
            } catch (MalformedURLException e1){

            } catch (IOException e1){

            }
        });
        JPanel buttonPanel = new JPanel(new GridLayout(10, 1));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(unpauseButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(commandButton);
        buttonPanel.add(logsButton);
        buttonPanel.add(volumeButton);
        add(buttonPanel, BorderLayout.NORTH);

        setLocationRelativeTo(null);  // Center the frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Dispose the frame on close
    }


}
class ErrorFrame extends JFrame{
    public ErrorFrame() {
        setTitle("Message");
        setSize(100, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JTextArea to display text
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false); // Make it non-editable
        textArea.setLineWrap(true);  // Enable line wrapping
        textArea.setWrapStyleWord(true);
        textArea.setText("Error during API Request");

        // Create a JScrollPane to contain the JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Add the JScrollPane to the frame's content pane
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
class CommandFrame extends JFrame{
    public CommandFrame(String containerName){
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));
        JLabel commandLabel = new JLabel("Command:");
        JTextField commandField = new JTextField();
        JButton execbutton = new JButton("Execute");
        execbutton.addActionListener(e -> {
            try {
                String command = commandField.getText();
                URL url = new URL("http://localhost:8080/" + "ExecCommandContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(","))+"&command="+command);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                StringBuilder res = new StringBuilder();
                if (con.getResponseCode()==HttpURLConnection.HTTP_OK) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    while ((line = r.readLine()) != null) {
                        res.append(line+"\n");
                    }

                    ResultFrame rs = new ResultFrame(res.toString());
                    rs.setVisible(true);
                } else {
                    ErrorFrame er = new ErrorFrame();
                    er.setVisible(true);
                }
                dispose();

            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        add(commandLabel);
        add(commandField);
        add(execbutton);
    }

}

 class ActFrame extends JFrame{
    int millis;
     public ActFrame(String action, String containerName) {
         setTitle(action);
         setSize(400, 150);
         setLayout(new BorderLayout());

         JTextArea instructionTextArea = new JTextArea("Enter milliseconds or enter -1 for undefined");
         instructionTextArea.setWrapStyleWord(true);
         instructionTextArea.setLineWrap(true);
         instructionTextArea.setEditable(false);

         JPanel inputPanel = new JPanel(new GridBagLayout()); // Using GridBagLayout for more control
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5, 5, 5, 5); // Adding insets for spacing

         JLabel commandLabel = new JLabel("Milliseconds:");
         gbc.gridx = 0;
         gbc.gridy = 0;
         inputPanel.add(commandLabel, gbc);

         JTextField milField = new JTextField();
         milField.setPreferredSize(new Dimension(80, 25)); // Adjusted size of the text field
         gbc.gridx = 1;
         gbc.gridy = 0;
         inputPanel.add(milField, gbc);

         JButton actionButton = new JButton(action);
         actionButton.addActionListener(e -> {
             millis = Integer.parseInt(milField.getText());
             try {
                 URL url = new URL("http://localhost:8080/" + action + "Container" + "?containerName=" + containerName.substring(containerName.indexOf("/") + 1, containerName.indexOf(",")) + "&millis=" + millis);
                 HttpURLConnection con = (HttpURLConnection) url.openConnection();
                 con.setRequestMethod("GET");
                 if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                     ErrorFrame er = new ErrorFrame();
                     er.setVisible(true);
                 }
             } catch (IOException e1){
                 e1.printStackTrace();
             }
             dispose();
         });

         add(instructionTextArea, BorderLayout.NORTH);
         add(inputPanel, BorderLayout.CENTER);
         add(actionButton, BorderLayout.SOUTH);

         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     }

     public int getmillis(){
         return millis;
     }
 }

