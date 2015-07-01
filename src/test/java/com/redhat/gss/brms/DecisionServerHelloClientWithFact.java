package com.redhat.gss.brms;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.example.project1.Person;
import org.kie.internal.runtime.helper.BatchExecutionHelper;

public class DecisionServerHelloClientWithFact {

	private static final String KIE_SERVER_USR = "jesuino";
	private static final String KIE_SERVER_PSW = "redhat2014!";
	private static final String HELLO_RULE_ENDPOINT = "http://localhost:8080/kie-server/services/rest/server/containers/approve";

	
	/**
	 * Builds the necessary Command and send it to the server
	 * 
	 * @throws Exception
	 */
	@Test
	public void executeHelloCommand() throws Exception {
		Person p = new Person();
		p.setName("William");
		p.setProcessed(false);
		p.setAge(21);
		
		Person p2 = new Person();
		p2.setName("Levi");
		p2.setProcessed(false);
		p2.setAge(5);
		
		List<GenericCommand<?>> commands = new ArrayList<>();
		// You can first add the facts in the memory
		commands.add(new InsertObjectCommand(p));
		commands.add(new InsertObjectCommand(p2));
		// then execute this to only call fire all rules and comment the insert fact commands
		commands.add(new FireAllRulesCommand());
		BatchExecutionCommand cmd = new BatchExecutionCommandImpl(commands);
		String cmdXML = BatchExecutionHelper.newXStreamMarshaller().toXML(cmd);
		System.out.println("== SENDING COMMANDS ==");
		System.out.println(cmdXML);
		String response = sendCommand(cmdXML);
		System.out.println("== RESPONSE FROM SERVER ==");
		System.out.println(response);
	}

	/**
	 * 
	 * Creates the actual HTTP request and execute it, then save the response
	 * from the server in a String
	 * 
	 * @param commands
	 *            Kie Commands in XML format
	 * @return The response from the server
	 * @throws Exception
	 */
	private String sendCommand(String commands) throws Exception {
		String auth = getAuthHeader(KIE_SERVER_USR, KIE_SERVER_PSW);
		URL url = new URL(HELLO_RULE_ENDPOINT);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/xml");
		con.setRequestProperty("Authorization", auth);
		con.setDoOutput(true);
		con.getOutputStream().write(commands.getBytes());
		return IOUtils.toString(con.getInputStream());
	}

	/**
	 * Generate the BASIC auth header to get authenticated in the server
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	private String getAuthHeader(String username, String password) {
		String auth = username + ":" + password;
		byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset
				.forName("US-ASCII")));
		return "Basic " + new String(encodedAuth);
	}
}