package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
public class LoginTest {
    private WebDriver driver;
    private String browser;

    public LoginTest(String browser) {
        this.browser = browser;
    }

    @BeforeMethod
    public void setUp() {
        // Initialize WebDriver based on browser
        if (browser.equalsIgnoreCase("chrome")) {
            driver = new ChromeDriver();
        } else if (browser.equalsIgnoreCase("edge")) {
            driver = new EdgeDriver();
        } else {
            throw new IllegalArgumentException("Invalid browser specified");
        }

        // Configure browser settings
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password, char expectedResult) {
        try {
            // Navigate to Pandora login page
            driver.get("https://tr.pandora.net/tr/login/");

            // Find login elements
            WebElement usernameField = driver.findElement(By.id("login-form-email"));
            WebElement passwordField = driver.findElement(By.id("login-form-password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[data-auto='btnSubmitLogin']"));

            // Clear and enter credentials
            usernameField.clear();
            usernameField.sendKeys(username);
            passwordField.clear();
            passwordField.sendKeys(password);

            // Click login button using JavaScript
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", loginButton);

            // Create WebDriverWait
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            switch (expectedResult) {
                case 'T': // Successful Login
                    try {
                        // Look for successful login indicators
                        WebElement loginSuccessElement = wait.until((
                                ExpectedConditions.presenceOfElementLocated(By.className("card-detail-info"))
                        ));
                        Assert.assertTrue(loginSuccessElement.isDisplayed(), "Login was expected to be successful");
                        System.out.println("Login successful for user: " + username);
                    } catch (Exception e) {
                        Assert.fail("Expected successful login, but no success indicator found");
                    }
                    break;

                case 'F': // Failed Login (Incorrect Credentials)
                    try {
                        // Look for error messages
                        WebElement errorMessage = wait.until((
                                ExpectedConditions.presenceOfElementLocated(By.className("alert-danger"))
                        ));
                        Assert.assertTrue(errorMessage.isDisplayed(), "Login was expected to fail");
                        System.out.println("Login failed as expected for user: " + username);
                    } catch (Exception e) {
                        Assert.fail("Expected login to fail, but no error message found");
                    }
                    break;

                case 'E': // Empty Credentials
                    try {
                        // Check for validation messages
                        WebElement validationMessage = wait.until((
                                ExpectedConditions.presenceOfElementLocated(By.className("invalid-feedback"))

                        ));
                        Assert.assertTrue(validationMessage.isDisplayed(), "Empty credentials should show validation error");
                        System.out.println("Validation error displayed for empty credentials");
                    } catch (Exception e) {
                        Assert.fail("Expected validation error for empty credentials");
                    }
                    break;

                default:
                    Assert.fail("Invalid test scenario specified");
            }
        } catch (Exception e) {
            Assert.fail("Login test failed: " + e.getMessage());
        }
    }

    @DataProvider(name = "loginData")
    public Object[][] loginDataProvider() {
        return new Object[][] {
                // username, password, result
                // 'T' - True (Successful Login)
                // 'F' - False (Failed Login)
                // 'E' - Empty Credentials
                {"barandincoguz@gmail.com", "05444971797Baran?", 'T'},  // Valid credentials
                {"invalid@example.com", "wrongpassword", 'F'},         // Invalid credentials
                {"", "", 'E'}                                          // Empty credentials
        };
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Factory
    public Object[] createTestInstances() {
        return new Object[] {
                new LoginTest("chrome"),
                new LoginTest("edge")
        };
    }
}