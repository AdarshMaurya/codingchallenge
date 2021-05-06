package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Main {
    private static WebDriver driver = null;
    public static final String WEBDRIVER_LOCATION = "./chromedriver";

    public static void main(String args[]) throws IOException {
        Main main = new Main();
        main.setUp();
    }

    public void setUp() throws IOException {
        ArrayList<String> problemListLink = new ArrayList<>();
        problemListLink.add("url");
        problemListLink.add("url");

        // create directory to store files
        Files.createDirectories(Paths.get("./DailyCodingProblem"));

        if (driver == null) {
            System.setProperty("webdriver.chrome.driver", WEBDRIVER_LOCATION);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.manage().window().setSize(new Dimension(1024, 768));

            // iterate through all the link
            for (String link : problemListLink) {
                driver.get(link);
                //wait until page loads
                new WebDriverWait(driver, 10).until(
                        webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

                // click on show solution
                WebElement showSolutionWebElement = driver.findElement(By.xpath("//button[@class='cta mobile yellow']"));
                showSolutionWebElement.click();
                try {
                    Thread.sleep(2000);
                    // capture screenshot - Ashot has various strategies which differ for one display to another
                    // below is configured for display which has retina display
                    Screenshot screenshot = new AShot().shootingStrategy(
                            ShootingStrategies.viewportRetina(2500, 0, 0, 2))
                            .takeScreenshot(driver);

                    // get problem number to make the new file
                    String file_number = "Daily_Coding_Problem_" + link.split("/")[4].split("\\?")[0];
                    ImageIO.write(screenshot.getImage(), "PNG", new File("./DailyCodingProblem/" + file_number + ".PNG"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            driver.quit();
        }
    }
}
