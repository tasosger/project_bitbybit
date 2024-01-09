package bitbybit.docker;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageActionFrame extends JFrame {
    private final String im;
    public ImageActionFrame(String imageName){
        im = imageName.substring(imageName.indexOf(":") + 2, imageName.indexOf(","));
        setTitle("Image Actions");
        setSize(100, 150);

        // Create JList for actions
        DefaultListModel<String> actionListModel = new DefaultListModel<>();
        JList<String> actionList = new JList<>(actionListModel);

        // Create a JScrollPane to contain the JList
        JScrollPane actionScrollPane = new JScrollPane(actionList);

        // Add the JScrollPane to the frame
        add(actionScrollPane, BorderLayout.CENTER);

        // Create buttons for container actions
        JButton infoButton = new JButton("Info");
        infoButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/ImageResp?imageName=" + im);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    ErrorFrame er =new ErrorFrame();
                    er.setVisible(true);
                }
                List<String> res = new ArrayList<>();
                if (con.getResponseCode()==HttpURLConnection.HTTP_OK) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    while ((line = r.readLine()) != null) {
                        res.add(line);
                    }

                    ResultFrame rs = new ResultFrame(res.toString());
                    rs.setVisible(true);
                }
                dispose();
            } catch (IOException e1){
                e1.printStackTrace();
            }
        });
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            try {
                URL url = new URL("http://localhost:8080/RemoveImage?imageName=" + im);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                if (con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                    ErrorFrame er =new ErrorFrame();
                    er.setVisible(true);
                }

                dispose();
            } catch (IOException e1){
                e1.printStackTrace();
            }
        });
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        buttonPanel.add(infoButton);
        buttonPanel.add(removeButton);
        add(buttonPanel, BorderLayout.NORTH);
    }
}
