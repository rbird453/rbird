import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;

public class RbirdTest {
	String[] format;
	WebDriver driver;
	static String sep = File.separator;
	static String logDirectory;
	static String BASE_URL = "https://portal.risebird.io/";

	private String createDir() {
		
		try {
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("E-dd-MMM-yyyy_HH-mm-ss");
			format = formatter.format(now).split("_");	
			logDirectory = String.join(sep, "D:", "Crew_Comp", "chromelogs", format[0], format[1], "");			
			File logDir = new File(logDirectory);
			boolean dirExists = logDir.exists();
			if (!dirExists) {
				logDir.mkdirs();
			}			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return String.join("_", format[0], format[1], "");
	}
	
	

	@Test
	public void rbird_test() {
//		String dir = createDir();
//		dir = logDirectory+dir;
//		
//		System.setProperty("webdriver.chrome.logfile", dir+"webdriver.log");
//		PrintStream logFile = null;
//		try {
//			logFile = new PrintStream(new File(dir+"console.txt"));
//		} catch (FileNotFoundException e) {
//			System.out.println(e.getMessage());
//		}
//
//		System.setOut(logFile);
		
		WebDriverManager.chromedriver().setup();
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");		
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

		try {
			if (driver != null) {
				driver.quit();
			}
			Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
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