package org.example;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.time.Duration;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        connect();
    }

    public static DockerClient connect() {
        // 配置docker CLI的一些选项
        DefaultDockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
//                .withDockerTlsVerify(DOCKER_TLS_VERIFY)
                .withDockerHost("tcp://127.0.0.1:2375")
                // 与docker版本对应，参考https://docs.docker.com/engine/api/#api-version-matrix
                // 或者通过docker version指令查看api version
//                .withApiVersion(API_VERSION)
//                .withRegistryUrl(REGISTRY_URL)
                .build();

        // 创建DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient
                .Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        Info info = dockerClient.infoCmd().exec();
//        String infoStr = JSONObject.toJSONString(info);
        System.out.println("docker环境信息");
        System.out.println(info);
        return dockerClient;
    }


}
