package bitbybit.docker_application;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

class ExecutionThread {
	private final String dockerApiUrl;
	
	public ExecutionThread(String dockerApi) {
		this.dockerApiUrl = dockerApi;
	}

    public void startContainer(String containerId) throws IOException {
        sendDockerCommand(containerId, "start");
    }

    public void stopContainer(String containerId) throws IOException {
        sendDockerCommand(containerId, "stop");
    }

    public void pauseContainer(String containerId) throws IOException {
        sendDockerCommand(containerId, "pause");
    }

    public void restartContainer(String containerId) throws IOException {
        sendDockerCommand(containerId, "restart");
    }
    private void sendDockerCommand(String containerId, String command) throws IOException {
    	String apiUrl = dockerApiUrl + "/containers/" + containerId + "/" + command;

        HttpClient httpClient = HttpClients.createDefault();
        HttpUriRequest request = new HttpPost(apiUrl);
        HttpResponse response = httpClient.execute(request);
    }
}
