package com.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Main {
    private static WebDriver driver = null;
    public static final String WEBDRIVER_LOCATION = "./chromedriver";

    public static void main(String args[]) throws Exception {
        Main main = new Main();
        main.setUp(args[0], args[1]);
    }

    public void setUp(String inputFilePath, String outputFolder) throws IOException, CsvException {
        Map<String, String> problemToLink = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(inputFilePath))) {
            List<String[]> allRows = reader.readAll();
            allRows.forEach(row -> problemToLink.put(row[1], row[0]));
        }

        if (driver == null) {
            System.setProperty("webdriver.chrome.driver", WEBDRIVER_LOCATION);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.manage().window().setSize(new Dimension(1000, 1278));
        }

        // iterate through all the link
        for (Map.Entry<String, String> problem : problemToLink.entrySet()) {
            String problemNumber = problem.getKey().replace(" ", "");
            System.out.println("Downloading data for problem " + problemNumber);
            String problemFolder = outputFolder + File.separator + problemNumber + File.separator;
            Files.createDirectories(Paths.get(problemFolder));

            File solutionLinkFile = new File(problemFolder + problemNumber+"-link.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionLinkFile));
            writer.write(problem.getValue());
            writer.close();

            driver.get(problem.getValue());
            //wait until page loads
            new WebDriverWait(driver, 10).until(
                    webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            //get problem definition
            Screenshot problemScreenshot = new AShot().shootingStrategy(
                    ShootingStrategies.viewportRetina(2500, 0, 0, 2))
                    .takeScreenshot(driver);
            ImageIO.write(problemScreenshot.getImage(), "PNG", new File(problemFolder + problemNumber + "-problem.PNG"));

            // click on show solution
            WebElement showSolutionWebElement = driver.findElement(By.xpath("//button[@class='cta mobile yellow']"));
            showSolutionWebElement.click();
            try {
                Thread.sleep(2000);
                // capture screenshot - Ashot has various strategies which differ for one display to another
                // below is configured for display which has retina display
                Screenshot solutionScreenshot = new AShot().shootingStrategy(
                        ShootingStrategies.viewportRetina(2500, 0, 0, 2))
                        .takeScreenshot(driver);
                ImageIO.write(solutionScreenshot.getImage(), "PNG", new File(problemFolder + problemNumber + "-solution.PNG"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        driver.quit();
    }
}
