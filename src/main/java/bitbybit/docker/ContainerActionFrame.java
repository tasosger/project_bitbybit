package bitbybit.docker;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ContainerActionFrame extends JFrame {

    public ContainerActionFrame(String containerName) {
        setTitle("Container Actions");
        setSize(300, 200);

        // Create JList for actions
        DefaultListModel<String> actionListModel = new DefaultListModel<>();
        JList<String> actionList = new JList<>(actionListModel);

        // Create a JScrollPane to contain the JList
        JScrollPane actionScrollPane = new JScrollPane(actionList);

        // Add the JScrollPane to the frame
        add(actionScrollPane, BorderLayout.CENTER);

        // Create buttons for container actions
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Start");
                try {
                    URL url = new URL("http://localhost:8080/" + "StartContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")) + "&millis=-1");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    System.out.println(con.getResponseCode());
                    System.out.println(url);
                    dispose();
                }catch (IOException e1){
                    e1.printStackTrace();
                } catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });

        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/" + "StopContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                System.out.println(con.getResponseCode());
                System.out.println(url);
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
                URL url = new URL("http://localhost:8080/" + "PauseContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(","))+"&millis=-1");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                System.out.println(url+" "+ con.getResponseCode());
                dispose();
            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });
        JButton unpauseButton = new JButton("Unpause");
        unpauseButton.addActionListener(e -> {
            try {
                System.out.println("Unpause");
                URL url = new URL("http://localhost:8080/" + "UnpauseContainer" + "?containerName=" + containerName.substring(containerName.indexOf("/")+1,containerName.indexOf(",")));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                System.out.println(con.getResponseCode());
                System.out.println(url);
                dispose();
            }catch (IOException e1){
                e1.printStackTrace();
            } catch (Exception e1){
                e1.printStackTrace();
            }
        });

        // Add buttons to the frame
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1));
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(unpauseButton);
        add(buttonPanel, BorderLayout.NORTH);

        setLocationRelativeTo(null);  // Center the frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Dispose the frame on close
    }


}