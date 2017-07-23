package com.practice.springboot.undertow.reverseproxy_practice;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RequestDumpingHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class App {
	
	@Autowired
	private Environment env;
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	public LoadBalancingProxyClient wso2demo1LoadBalancingProxyClient() throws URISyntaxException {
		LoadBalancingProxyClient loadBalancer = new LoadBalancingProxyClient().addHost(new URI(env.getProperty("servicelocator.wso2demo1.url")))
				.setConnectionsPerThread(5);
		return loadBalancer;
	}
	
	@Bean
	public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
		UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
		
		// Customizing the deployment info and adding handlers.
		factory.addDeploymentInfoCustomizers(deploymentInfo -> {
			deploymentInfo.addInnerHandlerChainWrapper(new HandlerWrapper() {
				@Override
				public HttpHandler wrap(HttpHandler handler) {
					return new RequestDumpingHandler(handler);
				}
			});
			
			deploymentInfo.addInnerHandlerChainWrapper(new HandlerWrapper() {
				
				@Override
				public HttpHandler wrap(HttpHandler handler) {
					HttpHandler proxyHandler = null;
					try {
						proxyHandler = new ProxyHandler(wso2demo1LoadBalancingProxyClient(), handler);
					} catch (URISyntaxException e) {
						System.out.println("Caught a URI Syntax Exception. Check the URL format for WSO2DEMO1.");
					}
					return proxyHandler;
				}
			});
		});
		return factory;
	}
}
