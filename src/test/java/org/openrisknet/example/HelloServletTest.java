package org.openrisknet.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class HelloServletTest {

    @Deployment
    public static WebArchive createDeployment() {
         return ShrinkWrap.create(WebArchive.class, "example-java-servlet.war")
                 .addClass(HelloServlet.class);
    }


    @Test
    @RunAsClient
    public void get(@ArquillianResource URL webappUrl) throws Exception {

        System.out.println("URL: " + webappUrl);

        // GET
        {
            InputStream is = webappUrl.openStream();
            String response = Utils.slurp(is);
            assertThat(response, startsWith("Hello"));
        }
    }

}