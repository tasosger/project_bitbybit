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
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class DockerMain extends JFrame {
    private JList<String> jList ;
    private JPanel additionalButtonsPanel;
    private final JPanel contentPanel = new JPanel();
        public DockerMain() {
        setTitle("Docker App");
        setSize(900, 750);
            JPanel sideMenu = new JPanel();
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            sideMenu.setBackground(Color.DARK_GRAY);
            sideMenu.setLayout(new GridLayout(0, 1));
            String[] buttonLabels = {"Instructions","Containers", "Images", "Volumes", "Subnets"};
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
                        if("Instructions".equals(label)){

                            show_instructions();
                        }
                    }
                });

                sideMenu.add(menuButton);
            }
            additionalButtonsPanel = new JPanel();
            additionalButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            additionalButtonsPanel.setBackground(Color.DARK_GRAY);
            additionalButtonsPanel.setPreferredSize(new Dimension(getWidth(), 40));



            // Main content panel

            contentPanel.setBackground(Color.WHITE);
            jList = new JList<>();
            JScrollPane listScrollPane = new JScrollPane(jList);
            contentPanel.add(listScrollPane,BorderLayout.CENTER);
            // Set layout for the main content panel
            contentPanel.setLayout(new BorderLayout());

            setLayout(new BorderLayout());
            add(sideMenu, BorderLayout.WEST);
            add(contentPanel, BorderLayout.CENTER);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            show_instructions();
            show_instructions();
    }
    public void show_instructions(){
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));


        JLabel label1 = new JLabel("Welcome to Docker Application");
        JLabel label2 = new JLabel("By Bit By Bit");

        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(label1);
        headerPanel.add(label2);
        JTextArea instructionsTextArea = new JTextArea();
        instructionsTextArea.setText("Instructions:\n1.Containers Tab\n\t-Select from the list the container you want to use" +
                "\n\t-Select the desired action\n\t-Some actions (eg. start) will require the number of milliseconds you want the action to be executed for" +
                "\n\t-If you do not want to specify the milliseconds enter -1\n\t-You can view container measurements by pressing the measurements button\n\t" +
                "-You can query the database on a specific date, a specific container, or on both\n\t-If you do not wish to specify a field leave it empty\n\t" +
                "-To create a container press the create container button and insert container name and image name\n\t" +
                "-By pressing the ALL button you will view all containers\n\t-By pressing the RUNNING button you will view the running containers\n\t" +
                "-By pressing the PAUSED button you will view paused containers\n\t" +
                "-To use the command execution select the container press the EXECUTE COMMAND option, enter the command (eg ls) and press execute\n" +
                "1.Images Tab\n\t-By clicking on an image you can remove it or see information about it\n" +
                "3.Volumes Tab\n\t-Here you can see Docker Volumes\n4.Subnets Tab\n\t-Here you can see Subnets");
        instructionsTextArea.setEditable(false);

        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(instructionsTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        contentPanel.add(scrollPane);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        revalidate();
        repaint();
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
    public void displayListInFrame(List<String> itemList, String type) {
        DefaultListModel<String> listModel = new DefaultListModel<>();

        if (itemList != null) {
            for (String item : itemList) {
                listModel.addElement(item);
            }
        }

        // Create JList and set its model
        JList<String> jList = new JList<>(listModel);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Optional: Set selection mode
        if(type .equals( "Cont")) {
            // Add ListSelectionListener to the JList
            jList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedContainer = jList.getSelectedValue();
                    if (selectedContainer != null) {
                        ContainerActionFrame ac = new ContainerActionFrame(selectedContainer);
                        ac.setVisible(true);
                    }
                }
            });
        }
        if(type .equals( "Im")) {
            // Add ListSelectionListener to the JList
            jList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    String selectedImage = jList.getSelectedValue();
                    if (selectedImage != null) {
                        ImageActionFrame imf = new ImageActionFrame(selectedImage);
                        imf.setVisible(true);
                    }
                }
            });
        }
        // Create a JScrollPane to contain the JList
        JScrollPane scrollPane = new JScrollPane(jList);

        // Clear existing components in the main content panel
        contentPanel.removeAll();

        // Set layout for the main content panel
        contentPanel.setLayout(new BorderLayout());

        // If the type is "Cont" (Containers), add additional buttons
        if (type.equals("Cont")) {
            JPanel additionalButtonsPanel = createAdditionalButtonsPanel(this);
            contentPanel.add(additionalButtonsPanel, BorderLayout.NORTH);
        }
        if (type.equals("Im")){
            JPanel imPanel = pull_image_Panel();
            contentPanel.add(imPanel,BorderLayout.NORTH);
        }

        // Add JScrollPane to the main content panel
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Repaint and revalidate the frame to reflect changes
        revalidate();
        repaint();
    }
    protected static List<String> api_request(String u){
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
    private JPanel pull_image_Panel(){
        JPanel pullPanel = new JPanel();
        pullPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        pullPanel.setBackground(Color.DARK_GRAY);
        pullPanel.setPreferredSize(new Dimension(getWidth(), 40));
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.DARK_GRAY);
        JButton pullButton = new JButton("Pull Image");
        pullButton.addActionListener(e -> {
            PullFrame pf = new PullFrame();
            pf.setVisible(true);
        });
        pullButton.setForeground(Color.WHITE);
        pullButton.setBackground(Color.DARK_GRAY);
        pullButton.setPreferredSize(new Dimension(100, 30));
        pullPanel.add(pullButton);
        return pullPanel;
    }
    private JPanel createAdditionalButtonsPanel(DockerMain d) {
        JPanel additionalButtonsPanel = new JPanel();
        additionalButtonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        additionalButtonsPanel.setBackground(Color.DARK_GRAY);
        additionalButtonsPanel.setPreferredSize(new Dimension(getWidth(), 40));

        String[] additionalButtonLabels = {"All", "Running", "Paused","Measurements"};
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.DARK_GRAY);
        for (String label : additionalButtonLabels) {
            JButton additionalButton = new JButton(label);
            additionalButton.setForeground(Color.WHITE);
            additionalButton.setBackground(Color.DARK_GRAY);
            additionalButton.setPreferredSize(new Dimension(130, 30));
            additionalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (label.equals("Measurements")) {
                        DateIdFrame df = new DateIdFrame(d);
                        df.setVisible(true);
                    } else {
                        String apiEndpoint = "";
                        List<String> list = new ArrayList<>();
                        if (label.equals("All")) {
                            list = api_request("ListContainers");
                        }
                        if (label.equals("Running")) {
                            list = api_request("ListRunningContainers");
                        }
                        if (label.equals("Paused")) {
                            list = api_request("ListPausedContainers");
                        }

                        displayListInFrame(list, "Cont");
                    }
                }
            });
            additionalButtonsPanel.add(additionalButton);
        }
        JButton createContainerButton = new JButton("Create Container");
        createContainerButton.setForeground(Color.WHITE);
        createContainerButton.setBackground(Color.DARK_GRAY);
        createContainerButton.setPreferredSize(new Dimension(140, 30));
        createContainerButton.addActionListener(e -> {
            CreateContainerFrame createContainerFrame = new CreateContainerFrame();
            createContainerFrame.setVisible(true);
        });

        additionalButtonsPanel.add(leftPanel, BorderLayout.WEST);
        additionalButtonsPanel.add(createContainerButton, BorderLayout.EAST);

        return additionalButtonsPanel;
    }
    }
 class CreateContainerFrame extends JFrame {
    private JTextField containerNameField;
    private JTextField imageNameField;
     private JLabel statusLabel;

    public CreateContainerFrame() {
        setTitle("Create Container");
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));

        JLabel containerNameLabel = new JLabel("Container Name:");
        containerNameField = new JTextField();
        JLabel imageNameLabel = new JLabel("Image Name:");
        imageNameField = new JTextField();
        statusLabel = new JLabel("");

        JButton createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String containerName = containerNameField.getText();
                    String imageName = imageNameField.getText();
                    URL url = new URL("http://localhost:8080/CreateContainer?containerName=" + containerName + "&imageName=" + imageName);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    if(con.getResponseCode()!=HttpURLConnection.HTTP_OK) {
                        ErrorFrame er = new ErrorFrame();
                        er.setVisible(true);
                    }
                    dispose();
                } catch (IOException e2){
                    e2.printStackTrace();
                }
            }
        });

        add(containerNameLabel);
        add(containerNameField);
        add(imageNameLabel);
        add(imageNameField);
        add(createButton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

}
class DateIdFrame extends JFrame{
    private JTextField dateField;
    private JTextField idField;
    private JButton callApiButton;

    public DateIdFrame(DockerMain p) {
        setTitle("Measurement Frame");
        setSize(400, 150);
        setLocationRelativeTo(null);

        initializeComponents();
        setLayout();
        callApiButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String date = dateField.getText();
                String id = idField.getText();
                if(id.equals("")) {
                    List<String> sl = DockerMain.api_request("DateMeasurements" + "?date=" + date);
                    p.displayListInFrame(sl, "Meas");
                }
                if(date.equals("")){
                    List<String> sl = DockerMain.api_request("IdMeasurements" + "?name=" + id);
                    p.displayListInFrame(sl, "Meas");
                }
                if(!date.equals("")&&!id.equals("")){
                    List<String> sl = DockerMain.api_request("DateIdMeasurements" + "?name=" + id+"&date="+date);
                    p.displayListInFrame(sl, "Meas");
                }
                dispose();
            }
        });
    }
    private void initializeComponents() {
        dateField = new JTextField(10);
        idField = new JTextField(10);
        callApiButton = new JButton("Get Measurements");
    }

    private void setLayout() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Date: "));

        panel.add(dateField);
        panel.add(new JLabel("Enter Name: "));
        panel.add(idField);
        panel.add(callApiButton);

        getContentPane().add(panel);
    }
}
class PullFrame extends JFrame{
    public PullFrame(){
        setSize(300, 200);
        setLayout(new GridLayout(3, 2));
        JLabel imageLabel = new JLabel("Image Name:");
        JTextField imageField = new JTextField();
        JButton execbutton = new JButton("Pull");
        execbutton.addActionListener(e -> {
            try {
                String imageName = imageField.getText();
                URL url = new URL("http://localhost:8080/" + "PullImage" + "?imageName=" + imageName);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                if(con.getResponseCode()!=HttpURLConnection.HTTP_OK){
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
        add(imageLabel);
        add(imageField);
        add(execbutton);
    }

}
