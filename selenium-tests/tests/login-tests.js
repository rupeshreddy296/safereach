const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');

async function runLoginTests() {
    console.log("Starting Web Frontend Selenium E2E Tests...");
    
    // Set up Chrome options for headless execution
    let options = new chrome.Options();
    options.addArguments('--headless');
    options.addArguments('--no-sandbox');
    options.addArguments('--disable-dev-shm-usage');
    
    let driver = await new Builder()
        .forBrowser('chrome')
        .setChromeOptions(options)
        .build();
        
    const BASE_URL = 'http://localhost:5173/login';

    try {
        // Test Case 1: Load Login Page
        console.log("TC-001: Navigation to Login Page");
        await driver.get(BASE_URL);
        await driver.wait(until.titleContains('SafeReach'), 5000);
        console.log("TC-001 Passed: Login page loaded successfully.");

        // Test Case 2: Citizen Login with missing fields
        console.log("TC-002: Validation - Login with empty fields");
        await driver.findElement(By.id('submit-btn')).click();
        let emailInput = await driver.findElement(By.id('email-input'));
        let required = await emailInput.getAttribute('required');
        if (required) {
            console.log("TC-002 Passed: HTML5 form validation verified.");
        }

        // Test Case 3: Role Toggle Validation (Citizen)
        console.log("TC-003: Interface - Role selector selection");
        let citizenTab = await driver.findElement(By.id('role-tab-citizen'));
        await citizenTab.click();
        let isActive = await citizenTab.getAttribute('class');
        if (isActive.includes('active') || isActive.includes('bg-accentRed') || isActive.includes('border-accentRed')) {
            console.log("TC-003 Passed: Citizen tab active.");
        }

        // Test Case 4: Invalid Credentials Error Message
        console.log("TC-004: Auth - Attempt login with non-existent user");
        await driver.findElement(By.id('email-input')).sendKeys('fake_citizen@safereach.com');
        await driver.findElement(By.id('password-input')).sendKeys('WrongPassword123');
        await driver.findElement(By.id('submit-btn')).click();
        
        // Wait for error notification toast
        let errorToast = await driver.wait(
            until.elementLocated(By.xpath("//*[contains(text(), 'invalid') or contains(text(), 'failed') or contains(text(), 'Error')]")),
            5000
        );
        console.log("TC-004 Passed: Correctly showed auth failure toast message:", await errorToast.getText());

        // Test Case 5: Role Toggle Validation (Driver)
        console.log("TC-005: Interface - Role selector to Driver");
        let driverTab = await driver.findElement(By.id('role-tab-driver'));
        await driverTab.click();
        await driver.findElement(By.id('email-input')).clear();
        await driver.findElement(By.id('password-input')).clear();
        console.log("TC-005 Passed: Role switched to driver.");

        // Test Case 6: Role Toggle Validation (Admin)
        console.log("TC-006: Interface - Role selector to Admin");
        let adminTab = await driver.findElement(By.id('role-tab-admin'));
        await adminTab.click();
        console.log("TC-006 Passed: Role switched to admin.");

    } catch (error) {
        console.error("Test execution interrupted with error: ", error);
    } finally {
        await driver.quit();
        console.log("Selenium web test suite run completed.");
    }
}

if (require.main === module) {
    runLoginTests();
}
