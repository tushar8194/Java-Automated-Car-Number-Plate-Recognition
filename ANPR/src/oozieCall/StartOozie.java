package oozieCall;

import java.util.Properties;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClient.*;
import org.apache.oozie.client.*;

public class StartOozie {
	
	
/*	// get a OozieClient for local Oozie
    OozieClient wc = new OozieClient("http://172.20.141:11000/oozie");

    // create a workflow job configuration and set the workflow application path
    Properties conf = wc.createConfiguration();
   //test// Properties con= new Properties();
    //Properties conf = wc.createConfiguration();
    conf.setProperty(OozieClient.APP_PATH,"hdfs://foo:9000/usr/tucu/my-wf-app");

    // setting workflow parameters
    conf.setProperty("jobTracker", "foo:9001");
    conf.setProperty("inputDir", "/usr/tucu/inputdir");
    conf.setProperty("outputDir", "/usr/tucu/outputdir");
    

    // submit and start the workflow job
    String jobId = wc.run(conf);
    //System.out.println("Workflow job submitted");

    // wait until the workflow job finishes printing the status every 10 seconds
    while (wc.getJobInfo(jobId).getStatus() == Workflow.Status.RUNNING) {
        System.out.println("Workflow job running ...");
        //Thread.sleep(10 * 1000);
    }

    // print the final status o the workflow job
    System.out.println("Workflow job completed ...");
    System.out.println(wf.getJobInfo(jobId));*/
}	






