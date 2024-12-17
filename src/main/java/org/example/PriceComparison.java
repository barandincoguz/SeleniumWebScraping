package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class PriceComparison {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String SEARCH_PRODUCT = "iphone 13 128GB";
    private static final int MAX_SEARCH_RESULTS = 10;


    // Method to initialize WebDriver
    private void initializeDriver() {

        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Method to close WebDriver
    private void closeDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Helper method to parse price string
    private Double parsePrice(String priceText) {
        try {
            return Double.parseDouble(
                    priceText.replaceAll("[^0-9,.]", "")
                            .replace(".", "")
                            .replace(",", ".")
                            .trim()
            );
        } catch (Exception e) {
            return null;
        }
    }

    // Method to scrape prices from Pazarama
    private List<Double> scrapePazaramaPrices() {
        initializeDriver();
        List<Double> prices = new ArrayList<>();

        try {
            driver.get("https://www.pazarama.com/");
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            // Find search input and submit search
            WebElement searchInput = driver.findElement(By.id("label-input"));
           
            searchInput.clear();
            searchInput.sendKeys(SEARCH_PRODUCT);
            searchInput.sendKeys(Keys.ENTER);

            // Wait and extract prices
            Thread.sleep(3000);
            List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector(".leading-tight")
            ));

            prices = priceElements.stream()
                    .map(element -> parsePrice(element.getText()))
                    .filter(Objects::nonNull).distinct().limit(MAX_SEARCH_RESULTS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error scraping N11: " + e.getMessage());
        } finally {
            closeDriver();
        }

        return prices;
    }

    // Method to scrape prices from Trendyol
    //**TRENDYOLU BAŞARILI BİR ŞEKİLDE ÇEKİYOR**********
    private List<Double> scrapeTrendyolPrices() {
        initializeDriver();
        List<Double> prices = new ArrayList<>();

        try {
            driver.get("https://www.trendyol.com/");
            driver.manage().window().maximize();
            // Find search input and submit search
            WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[data-testid='suggestion']")
            ));
            searchInput.clear();
            searchInput.sendKeys(SEARCH_PRODUCT);
            searchInput.sendKeys(Keys.ENTER);

            // Wait and extract prices
            Thread.sleep(3000);
            List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("prc-box-dscntd")
            ));

            prices = priceElements.stream()
                    .map(element -> parsePrice(element.getText()))
                    .filter(Objects::nonNull).distinct().limit(MAX_SEARCH_RESULTS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error scraping Trendyol: " + e.getMessage());
        } finally {
            closeDriver();
        }

        return prices;
    }

    // Method to scrape prices from Akakce
    private List<Double> scrapeAkakcePrices() {
        initializeDriver();
        List<Double> prices = new ArrayList<>();

        try {
            driver.get("https://www.akakce.com/");
            driver.manage().window().maximize();
            // Find search input and submit search
            WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input[name='q']")
            ));
            searchInput.clear();
            searchInput.sendKeys(SEARCH_PRODUCT);
            searchInput.sendKeys(Keys.ENTER);

            // Wait and extract prices
            Thread.sleep(3000);
            List<WebElement> priceElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("pt_v8")
            ));

            prices = priceElements.stream()
                    .map(element -> parsePrice(element.getText()))
                    .filter(Objects::nonNull).distinct().limit(MAX_SEARCH_RESULTS)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error scraping Akakce: " + e.getMessage());
        } finally {
            closeDriver();
        }

        return prices;
    }

    // Method to generate price comparison report
    public void generatePriceComparisonReport() {
        // Scrape prices from each website

        List<Double> PazaramaPrices = scrapePazaramaPrices();
        List<Double> trendyolPrices = scrapeTrendyolPrices();
        List<Double> akakcePrices = scrapeAkakcePrices();

        // Combine all prices
        List<Double> allPrices = new ArrayList<>();
        allPrices.addAll(PazaramaPrices);
        allPrices.addAll(trendyolPrices);
        allPrices.addAll(akakcePrices);

        // Remove any null or zero prices
        allPrices = allPrices.stream()
                .filter(price -> price != null && price > 0)
                .collect(Collectors.toList());

        // Calculate statistics
        if (!allPrices.isEmpty()) {
            double cheapestPrice = Collections.min(allPrices);
            double mostExpensivePrice = Collections.max(allPrices);
            double averagePrice = allPrices.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // Generate report
            System.out.println("Price Comparison Report for ---> " + SEARCH_PRODUCT);
            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            System.out.println("Pazarama Prices: " + PazaramaPrices);
            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            System.out.println("Trendyol Prices: " + trendyolPrices);
            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            System.out.println("Akakce Prices: " + akakcePrices);
            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
            System.out.println("Price Statistics:");
            System.out.printf("Cheapest Price: %.2f TL%n", cheapestPrice);
            System.out.printf("Most Expensive Price: %.2f TL%n", mostExpensivePrice);
            System.out.printf("Average Price: %.2f TL%n", averagePrice);
        } else {
            System.out.println("No prices could be retrieved.");
            closeDriver();
        }
    }

    public static void main(String[] args) {
        PriceComparison comparison = new PriceComparison();
        comparison.generatePriceComparisonReport();
    }
}