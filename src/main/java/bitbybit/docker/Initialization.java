package bitbybit.docker;

import com.sun.jdi.request.DuplicateRequestException;

public class Initialization {
    public static void initializeInst_im(){
        DockerInstance.initializeThreads();
        DockerInstance.containers = DockerInstance.listContainers();
        DockerImage.images= DockerImage.listImages();
    }
    public static void initialize_db(){
        DatabaseHandler.form_connection();
    }
    public static void initialize_all(){
        initialize_db();
        initializeInst_im();
    }
}
