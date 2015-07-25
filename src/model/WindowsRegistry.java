package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class WindowsRegistry {
	private static final String REG_LOC = "\"HKCU\\Software\\SeaHawk\\Movie Library\"";
	
	private static class StreamReader extends Thread {
		private InputStream input;
		private StringWriter writer = new StringWriter();
		
		public StreamReader(InputStream in) {
			input = in;         
		}
		
		public void run() {
			try {
				int c;
				while ((c = input.read()) != -1)
					writer.write(c);
			}
			catch (IOException e) {}
		}
		
		public String getResult() {
			return writer.toString();
		}
	}
		
	public static void write(String key, String value) {
		try {
			Runtime.getRuntime().exec("REG ADD " + REG_LOC + " /v " + key + " /t REG_SZ /d " + value + " /f");
		} 
		catch (IOException ex) {}
	}
	
	public static String read(String key) {
		try {
			Process process = Runtime.getRuntime().exec("REG QUERY " + REG_LOC + " /v " + key);
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			String output = reader.getResult();			
			String[] parsed = output.split(" ");
			return parsed[parsed.length-1].trim();
		}
		catch (Exception ex) {
			return null;
		}
	}	
}
