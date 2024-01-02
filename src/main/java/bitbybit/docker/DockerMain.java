package bitbybit.docker;

import com.github.dockerjava.api.model.Container;
import org.springframework.boot.SpringApplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DockerMain extends JFrame {
    private JList<String> jList ;
    private JPanel additionalButtonsPanel;
    private JPanel contentPanel;
        public DockerMain() {
        setTitle("Docker App");
        setSize(900, 750);
            JPanel sideMenu = new JPanel();
            sideMenu.setBackground(Color.DARK_GRAY);
            sideMenu.setLayout(new GridLayout(0, 1));
            String[] buttonLabels = {"Containers", "Images", "Volumes", "Subnets"};
            for (String label : buttonLabels) {
                JButton menuButton = new JButton(label);
                menuButton.setForeground(Color.WHITE);
                menuButton.setBackground(Color.DARK_GRAY);
                menuButton.setPreferredSize(new Dimension(100, 40));
                menuButton.setBorderPainted(false);
                menuButton.setBorder(BorderFactory.createEmptyBorder(-5, -5, -5, -5));

                menuButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if ("Containers".equals(label)) {
                            List<String> list = api_request("ListContainers");
                            displayListInFrame(list,"Cont");
                        }
                        if ("Images".equals(label)){
                            List<String> list = api_request("ListImages");
                            displayListInFrame(list,"Im");
                        }
                        if("Subnets".equals(label)){
                            List<String> list = api_request("ListSubnets");
                            displayListInFrame(list,"Sub");
                        }
                        if("Volumes".equals(label)){
                            List<String> list = api_request("ListVolumes");
                            displayListInFrame(list,"Vol");
                        }
                    }
                });

                sideMenu.add(menuButton);
            }
            additionalButtonsPanel = new JPanel();
            additionalButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            additionalButtonsPanel.setBackground(Color.DARK_GRAY);
            additionalButtonsPanel.setPreferredSize(new Dimension(getWidth(), 40));
            String[] additionalButtonLabels = {"All", "Running", "Paused"};
            for (String label : additionalButtonLabels) {
                JButton additionalButton = new JButton(label);
                additionalButton.setForeground(Color.WHITE);
                additionalButton.setBackground(Color.DARK_GRAY);
                additionalButton.setPreferredSize(new Dimension(100, 30));
                additionalButtonsPanel.add(additionalButton);
            }


            // Main content panel
             contentPanel = new JPanel();
            contentPanel.setBackground(Color.WHITE);
            jList = new JList<>();
            JScrollPane listScrollPane = new JScrollPane(jList);
            contentPanel.add(listScrollPane,BorderLayout.CENTER);
            // Set layout for the main content panel
            contentPanel.setLayout(new BorderLayout());

            // Add components to the main content panel
            contentPanel.add(new JLabel("Main Content"), BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(sideMenu, BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);

    }
    public static void main(String[] args) {
             startSpringBootApp();
             Initialization.initialize_db();
             DatabaseThread dbt = new DatabaseThread();
             dbt.start();
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               new DockerMain().setVisible(true);

            }
        });
    }
    private static void startSpringBootApp() {
        Class<?> springBootAppClass = SpringBootApp.class;
        String[] springBootAppArgs = new String[0];
        SpringApplication.run(springBootAppClass, springBootAppArgs);
    }
    private void displayListInFrame(List<String> itemList, String type) {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (itemList != null) {
            for (String item : itemList) {
                listModel.addElement(item);
            }
        }

        // Create JList and set its model
        JList<String> jList = new JList<>(listModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Optional: Set selection mode

        // Add ListSelectionListener to the JList
        jList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedContainer = jList.getSelectedValue();
                if (selectedContainer != null) {
                    System.out.println("Selected container: " + selectedContainer);
                    ContainerActionFrame ac =new ContainerActionFrame(selectedContainer);
                    ac.setVisible(true);
                }
            }
        });

        // Create a JScrollPane to contain the JList
        JScrollPane scrollPane = new JScrollPane(jList);

        // Clear existing components in the main content panel
        contentPanel.removeAll();

        // Set layout for the main content panel
        contentPanel.setLayout(new BorderLayout());

        // If the type is "Cont" (Containers), add additional buttons
        if (type.equals("Cont")) {
            JPanel additionalButtonsPanel = createAdditionalButtonsPanel();
            contentPanel.add(additionalButtonsPanel, BorderLayout.NORTH);
        }

        // Add JScrollPane to the main content panel
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Repaint and revalidate the frame to reflect changes
        revalidate();
        repaint();
    }
    private List<String> api_request(String u){
            try{
                URL url=new URL("http://localhost:8080/"+u);
                HttpURLConnection con =(HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int respc = con.getResponseCode();
                if (respc == HttpURLConnection.HTTP_OK){
                    BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    List<String> res = new ArrayList<>();
                    while ((line = r.readLine())!=null){
                        res.add(line);
                    }
                    System.out.println(res.size());
                    return res;
                } else {
                    System.out.println("err"+con.getResponseCode());
                }
            } catch (IOException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return List.of("error");
    }
    private JPanel createAdditionalButtonsPanel() {
        JPanel additionalButtonsPanel = new JPanel();
        additionalButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        additionalButtonsPanel.setBackground(Color.DARK_GRAY);
        additionalButtonsPanel.setPreferredSize(new Dimension(getWidth(), 40));

        String[] additionalButtonLabels = {"All", "Running", "Paused"};
        for (String label : additionalButtonLabels) {
            JButton additionalButton = new JButton(label);
            additionalButton.setForeground(Color.WHITE);
            additionalButton.setBackground(Color.DARK_GRAY);
            additionalButton.setPreferredSize(new Dimension(100, 30));
            additionalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String apiEndpoint= "";
                    List<String> list = new ArrayList<>();
                   if(label.equals("All")) {
                       list = api_request("ListContainers");
                   }
                   if(label.equals("Running")) {
                       list = api_request("ListRunningContainers");
                   }
                   if(label.equals("Paused")){
                       list = api_request("ListPausedContainers");
                   }

                    displayListInFrame(list,"Cont");
                }
            });
            additionalButtonsPanel.add(additionalButton);
        }

        return additionalButtonsPanel;
    }
    }
