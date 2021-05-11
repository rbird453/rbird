import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class WebDriverManagerTest2 {
	WebDriver driver;
	static String headless;
	static String logDirectory;
	static String BASE_URL = "https://portal.risebird.io/";
	static SessionId session_id;
	
	public static RemoteWebDriver createDriverFromSession(final SessionId sessionId, URL command_executor){
	    CommandExecutor executor = new HttpCommandExecutor(command_executor) {

	    @Override
	    public Response execute(Command command) throws IOException {
	        Response response = null;
	        if (command.getName() == "newSession") {
	            response = new Response();
	            response.setSessionId(sessionId.toString());
	            response.setStatus(0);
	            response.setValue(Collections.<String, String>emptyMap());

	            try {
	                Field commandCodec = null;
	                commandCodec = this.getClass().getSuperclass().getDeclaredField("commandCodec");
	                commandCodec.setAccessible(true);
	                commandCodec.set(this, new W3CHttpCommandCodec());

	                Field responseCodec = null;
	                responseCodec = this.getClass().getSuperclass().getDeclaredField("responseCodec");
	                responseCodec.setAccessible(true);
	                responseCodec.set(this, new W3CHttpResponseCodec());
	            } catch (NoSuchFieldException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            }

	        } else {
	            response = super.execute(command);
	        }
	        return response;
	    }
	    };

	    return new RemoteWebDriver(executor, new DesiredCapabilities());
	}
	
	public static ChromeDriver getDriver() {
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		//options.addArguments("--"+headless);		
		WebDriver driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		driver.get(BASE_URL);
		return new ChromeDriver();
	}

	public static void main(String [] args) {

		ChromeDriver driver = getDriver();
	    HttpCommandExecutor executor = (HttpCommandExecutor) driver.getCommandExecutor();
	    URL url = executor.getAddressOfRemoteServer();
	   
	    if(session_id == null) {
	    	session_id = driver.getSessionId();
	    }

	    driver = (ChromeDriver) createDriverFromSession(session_id, url);
	    driver.get("http://tarunlalwani.com");
	}

//	public static void main(String... args) {
//		if(args.length > 0) {
//			headless = args[0];
//		}
//		new WebDriverManagerTest2().testDriverManagerChrome();
//	}

	private static void createDir() {
		try {
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("E-dd-MMM-yyyy_HH-mm-ss");
			String strDate = formatter.format(now);
			String[] dir = strDate.split("_");
			//String curDir = System.getProperty("user.dir");
			//String log = String.join(File.separator, curDir, dir[0], dir[1]);
			logDirectory = "D:\\Crew_Comp\\chromelogs\\"+dir[0]+"\\"+dir[1]+"\\";
			//logDirectory += dir[0]+"\\"+dir[1]+"\\";
			File logDir = new File(logDirectory);
			boolean dirExists = logDir.exists();
			if (!dirExists) {
				logDir.mkdirs();
			}			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void testDriverManagerChrome() {
		createDir();
		System.setProperty("webdriver.chrome.logfile", logDirectory + "webdriver.log");
		PrintStream logFile = null;
		try {
			logFile = new PrintStream(new File(logDirectory + "console.txt"));
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}

		System.setOut(logFile);
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--"+headless);		
		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
		driver.get(BASE_URL);
		WebElement login = getElement(By.id("logonIdentifier"), 30);
		if (login != null) {
			login.clear();
			login.sendKeys("mukaram.mohammad@gmail.com");
		}
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("MOHA@065");
		driver.findElement(By.xpath("//button[contains(text(),'Sign in')]")).click();
		checkOpenInterview();
		assignInterviews();

//		try {
//			if (driver != null) {
//				driver.quit();
//			}
//			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
//		} catch (IOException e) {
//			System.out.println(e.getMessage());
//		}
	}	
	
	By assign = By.xpath("//span[contains(@title,'Assign Yourself') or contains(text(),'Assign Yourself')]");
	
	private int getInterviews() {
		int size = getElements(assign, 30);
		System.out.println("No. of Interviews is "+size);
		return size;
	}

	private void assignInterviews() {		
		
		while(true) {
			if(getInterviews() == 0) {
				return;
			}
			WebElement element = getElement(assign, 30);
			if(element == null) {
				return;
			}
			element.click();
			By ok = By.xpath("//button[text()='OK']");
			WebElement okButton = getElement(ok, 5);
			if (okButton != null) {
				okButton.click();
			}
			openInterview();
		}		
	}

	private WebElement getElement(By by, int timeOutInSeconds) {
		try {
			return new WebDriverWait(driver, timeOutInSeconds)
					.until(visibilityOfElementLocated(by));
		} catch (TimeoutException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	private int getElements(By by, int timeOutInSeconds) {
		try {
			return new WebDriverWait(driver, timeOutInSeconds)
					.until(visibilityOfAllElementsLocatedBy(by)).size();
		} catch (TimeoutException e) {
			System.out.println(e.getMessage());
		}
		return 0;
	}
	
	private boolean checkOpenInterview() {
		try {
			new WebDriverWait(driver, 30).until(titleContains("Interviews"));
			if(!driver.getTitle().contains("Open Interviews")) {
				driver.get(BASE_URL+"Interview/OpenInterview");	
			}			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;		
	}

	private void openInterview() {
		boolean openInterview = false;
		try {
			System.out.println("Current url before -> " + driver.getCurrentUrl());
			openInterview = new WebDriverWait(driver, 30).until(urlContains("OpenInterview"));
			System.out.println("openInterview -> " + openInterview);
			System.out.println("Current url after -> " + driver.getCurrentUrl());
			if (!openInterview) {
				driver.get(BASE_URL+"Interview/OpenInterview");
			}
		} catch (TimeoutException e) {
			System.out.println(e.getMessage());
		}
		finally {
			if (!openInterview) {
				driver.get(BASE_URL+"Interview/OpenInterview");
			}
		}
	}
}