package portal.controllers;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerController {
    public static String makeCheck(String pathout, String cName) throws DockerException, InterruptedException, DockerCertificateException, IOException {
        System.out.println("pathout: " + pathout);
        System.out.println("cName: " + cName);
        // pathout - Путь до программы
        // cName - Имя исполняемого файла программы
        final DockerClient docker = DefaultDockerClient.fromEnv().build();
        System.out.println( docker.info().kernelVersion() );
        String name = "vorpal/borealis-standalone";
        System.out.println( docker.listImages(DockerClient.ListImagesParam.byName("vorpal/borealis-standalone")));

        //String dockid = "70cfbbcb5048"; //for check image
        String pathIn = "/home/borealis/borealis/build/project";

        //final ImageInfo infoim = docker.inspectImage(dockid);
        //System.out.println( infoim.toString() );
        //
        //docker.startContainer(dockid);

        // Bind container ports to host ports
        final String[] ports = {"80", "24"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        List<PortBinding> randomPort = new ArrayList<>();
        randomPort.add(PortBinding.randomPort("0.0.0.0"));
        portBindings.put("443", randomPort);

        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(name).exposedPorts(ports)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        final ContainerCreation creation = docker.createContainer(containerConfig);
        final String id = creation.id();


        final ContainerInfo info = docker.inspectContainer(id);

        // Start container
        docker.startContainer(id);




        Path path = Paths.get(pathout);
        final String[] command000 = {"bash", "-c", "mkdir /home/borealis/borealis/build/project"};
        final ExecCreation execCreation000 = docker.execCreate(
                id, command000, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output000 = docker.execStart(execCreation000.id());
        final String execOutput000 = output000.readFully();
        System.out.println("output: " + execOutput000);
        docker.copyToContainer(path, id, pathIn); // add project to docker image

        docker.copyToContainer(path, id, pathIn); // add project to docker image
        final String[] command00 = {"bash", "-c", "chmod -R a+rX /home/borealis/borealis/build/project"};
        final ExecCreation execCreation00 = docker.execCreate(
                id, command00, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output00 = docker.execStart(execCreation00.id());
        final String execOutput00 = output00.readFully();
        System.out.println("Chmod output: " + execOutput00);

        final String[] command0 = {"bash", "-c", "ls -l /home/borealis/borealis/build/project/1"};
        final ExecCreation execCreation0 = docker.execCreate(
                id, command0, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output0 = docker.execStart(execCreation0.id());
        final String execOutput0 = output0.readFully();
        System.out.println("Ls inner: " + execOutput0);

        final String[] command1 = {"bash", "-c", "ls -l -R /home/borealis/borealis/build/project/ | grep test*.c"};
        final ExecCreation execCreation1 = docker.execCreate(
                id, command1, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        final LogStream output1 = docker.execStart(execCreation1.id());
        final String execOutput1 = output1.readFully();
        System.out.println("Ls inner: " + execOutput1);

        String execOutput = "";
        List<String> files = new ArrayList<String>(Arrays.asList(cName.split(" ")));
        for (String file: files) {
            final String[] command = {"bash", "-c", "../wrapper  ---dump-output:json ---dump-output-file:json.t /home/borealis/borealis/build/project/" + file};
            final ExecCreation execCreation = docker.execCreate(
                    id, command, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());
            final LogStream output = docker.execStart(execCreation.id());
            execOutput += output.readFully();
        }
        System.out.println(execOutput);

        // Kill container
        docker.killContainer(id);

        // Remove container
        docker.removeContainer(id);

        // Close the docker client
        docker.close();


        //docker.stopContainer(dockid, 10);
        return execOutput;
    }
}