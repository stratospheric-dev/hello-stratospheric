package com.myorg;

import dev.stratospheric.cdk.ApplicationEnvironment;
import dev.stratospheric.cdk.Network;
import dev.stratospheric.cdk.Service;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.HashMap;

public class ServiceApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = (String) app.getNode().tryGetContext("accountId");
        requireNonEmpty(accountId, "context variable 'accountId' must not be null");

        String region = (String) app.getNode().tryGetContext("region");
        requireNonEmpty(region, "context variable 'region' must not be null");

        String applicationName = (String) app.getNode().tryGetContext("applicationName");
        requireNonEmpty(applicationName, "context variable 'applicationName' must not be null");

        String environmentName = (String) app.getNode().tryGetContext("environmentName");
        requireNonEmpty(environmentName, "context variable 'environmentName' must not be null");

        String dockerRepositoryName = (String) app.getNode().tryGetContext("dockerRepositoryName");
        requireNonEmpty(dockerRepositoryName, "context variable 'dockerRepositoryName' must not be null");

        String dockerImageTag = (String) app.getNode().tryGetContext("dockerImageTag");
        requireNonEmpty(dockerImageTag, "context variable 'dockerImageTag' must not be null");

        Environment env = makeEnv(accountId, region);

        ApplicationEnvironment applicationEnvironment = new ApplicationEnvironment(
                applicationName,
                environmentName
        );

        Stack serviceStack = new Stack(app, "ServiceStack", StackProps.builder()
                .stackName(environmentName + "-Service")
                .env(env)
                .build());

        Network.NetworkOutputParameters networkOutputParameters =
                Network.getOutputParametersFromParameterStore(serviceStack, applicationEnvironment.getEnvironmentName());

        Service service = new Service(
                serviceStack,
                "Service",
                env,
                applicationEnvironment,
                new Service.ServiceInputParameters(
                        new Service.DockerImageSource(dockerRepositoryName, dockerImageTag),
                        new HashMap<>()),
                networkOutputParameters
        );

        app.synth();
    }

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }

    public static void requireNonEmpty(String string, String message) {
        if (string == null || "".equals(string)) {
            throw new IllegalArgumentException(message);
        }
    }

}

