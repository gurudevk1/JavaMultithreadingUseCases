import java.io.*;
import java.net.*;

class ServerService {
	public static int TIME_TO_WAIT=20000;
	
	//static String startCommand = "C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\bin\\startup.bat";//for linux use
	//static String stopCommand = "C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\bin\\shutdown.bat";
	
//	static String stopCommand="C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -File C:\\Scripts\\Shutdown-Tomcat.ps1";
//	static String startCommand= "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -File C:\\Scripts\\Startup-Tomcat.ps1";
//	static String cdCMD="C:\\Scripts";
	
//	static String cdCMD="C:\\Program Files\\Apache Software Foundation\\tomcat\\apache-tomcat-9.0.14\\bin";
	static String cdCMD="C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\bin";
	static String stopCommand="shutdown.bat";
	static String startCommand= "startup.bat";
	
	//C:\Program Files\Apache Software Foundation\tomcat\apache-tomcat-9.0.14\bin
	//static String startCommand = "C:\\Program Files\\Apache Software Foundation\\tomcat\\apache-tomcat-9.0.14\\bin\\startup.bat";
	//static String stopCommand = "C:\\Program Files\\Apache Software Foundation\\tomcat\\apache-tomcat-9.0.14\\bin\\shutdown.bat";
	

	public static void main(String[] args) {
		try{ 
			System.out.println("Start API test");;
			PerformanceTest pTest=new PerformanceTest();
			Thread apiThread = new Thread(pTest);
			
			long stime = System.currentTimeMillis();
			
			apiThread.start();
			apiThread.join(TIME_TO_WAIT); 
			
			long endTime=System.currentTimeMillis();
			
			System.out.println("End API test");;

			if((endTime-stime)>=TIME_TO_WAIT || apiThread.isAlive()) {
				System.out.println(" Server Looks Unhealthy , received response in"+(endTime-stime));
				try {
					System.out.println("Thread for Stoping  Server");
					ServerUtils stopObj=new ServerUtils(stopCommand);
					Thread stopThread = new Thread(stopObj);
					stopThread.start();
					stopThread.join();
				} catch (Throwable ex) {
			        System.err.println("Uncaught exception - " + ex.getMessage());
			        ex.printStackTrace(System.err);
			    }
				try {
					System.out.println("Thread for Starting Server");
					ServerUtils startObj=new ServerUtils(startCommand);
					Thread startThread = new Thread(startObj);
					startThread.start();
					startThread.join();
					System.out.println("Executed restart command");
				} catch (Throwable ex) {
			        System.err.println("Uncaught exception - " + ex.getMessage());
			        ex.printStackTrace(System.err);
			    }
			}else {
				System.out.println(" Server Looks Healthy , received response in"+(endTime-stime));
			}
		}catch (Throwable ex) {
	        System.err.println("Uncaught exception - " + ex.getMessage());
	        ex.printStackTrace(System.err);
	    }
	}

}
class PerformanceTest implements Runnable{

	public void run() {
		HttpURLConnection conn=null;
		try {
			
			System.out.println("Calling the API .");
			
			URL url = new URL("http://localhost:8080/myWebApp/rest/performanceTestGETAPI/1");
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty ("Authorization", "Basic _ADD_YOUR_CREDENTAILS_HERE_");
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			if (conn.getResponseCode() != 200) {
				System.out.println("Error while calling the API .");
				Thread.sleep(ServerService.TIME_TO_WAIT);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			

		} catch (MalformedURLException e) {
			try {
				Thread.sleep(ServerService.TIME_TO_WAIT);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();

		} catch (IOException e) {
			
			System.out.println("API hosting System is down...Restarting the server now");
			e.printStackTrace();
			try {
				Thread.sleep(ServerService.TIME_TO_WAIT);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(ServerService.TIME_TO_WAIT);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			if(conn!=null)
				conn.disconnect();
		}
	}

}
/*
 * Handling Server related tasks
 * */
class ServerUtils implements Runnable{
	private String command;
	
	public ServerUtils(String command) {
		this.command=command;
	}
	public void run() {
		if(!this.command.toLowerCase().contains("start")) {
			stopServer();
		}else {
			startServer();
		}
	}
	public boolean stopServer() {
		try {
			System.out.println("Stoping the Server");;
			//StopServer.main(new String[1]);
//			executeProcess("C:\\Scripts","javac StopServer.java");
//			executeProcess("C:\\Scripts","java StopServer");
			executeProcess(ServerService.cdCMD,command);
			System.out.println("Stopped the Server");;
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean startServer() {
		System.out.println("Starting the Server start logic");
		try {
			System.out.println("Sleeping for 20Sec");;
			Thread.sleep(20000);
			System.out.println("Starting the Server");;
			executeProcess(ServerService.cdCMD,command);
			System.out.println("Started the Server");;
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	public  void executeProcess(String cmd,String command) throws Exception {
		Process process;
		System.out.println("Executing the command"+command);
		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
		File dir = new File(cmd);
		pb.directory(dir);
		process = pb.start();
		StringBuilder output = new StringBuilder();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line + "\n");
        }

        int exitVal = process.waitFor();
        if (exitVal == 0) {
            System.out.println(output);
            System.exit(0);
        } else {
        	 System.out.println("We have issue");
        }
		System.out.println(process.toString());
	} 
}

