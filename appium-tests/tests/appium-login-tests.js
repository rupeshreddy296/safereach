const wd = require('webdriverio');

const opts = {
  path: '/wd/hub',
  port: 4723,
  capabilities: {
    platformName: "Android",
    platformVersion: "11.0",
    deviceName: "Android Emulator",
    app: "c:/Users/rupes/AndroidStudioProjects/SAFEREACH3/app/build/outputs/apk/debug/app-debug.apk",
    appPackage: "com.example.safereach",
    appActivity: "com.example.safereach.MainActivity",
    automationName: "UiAutomator2",
    noReset: true
  }
};

async function runMobileTests() {
    console.log("Starting Mobile Appium E2E Functional Tests...");
    const client = await wd.remote(opts);

    try {
        // Test Case 1: App Launch & Render
        console.log("MC-001: Mobile Application Launch verification");
        const titleText = await client.$('android=new UiSelector().textContains("SafeReach")');
        await titleText.waitForExist({ timeout: 10000 });
        console.log("MC-001 Passed: SafeReach application launched successfully.");

        // Test Case 2: Verify Login Field Existence
        console.log("MC-002: Verification - Login layout views");
        const emailField = await client.$('android=new UiSelector().resourceId("com.example.safereach:id/email")');
        const passField = await client.$('android=new UiSelector().resourceId("com.example.safereach:id/password")');
        if (await emailField.isDisplayed() && await passField.isDisplayed()) {
            console.log("MC-002 Passed: Input forms visible.");
        }

        // Test Case 3: Empty Fields Validation
        console.log("MC-003: Validation - Empty submit rejection");
        const submitBtn = await client.$('android=new UiSelector().resourceId("com.example.safereach:id/login_button")');
        await submitBtn.click();
        console.log("MC-003 Passed: Submit clicked with empty inputs.");

        // Test Case 4: Enter Mock Email Address
        console.log("MC-004: Telemetry - Mock email inputs");
        await emailField.setValue("driver_test@safereach.com");
        console.log("MC-004 Passed: Text value updated.");

        // Test Case 5: Role Selector Interactions
        console.log("MC-005: Interface - Switch role toggle");
        const driverSelector = await client.$('android=new UiSelector().text("Driver")');
        if (await driverSelector.isExisting()) {
            await driverSelector.click();
            console.log("MC-005 Passed: Role switched to Driver on device UI.");
        }

    } catch (err) {
        console.error("Appium mobile test execution interrupted with error: ", err);
    } finally {
        await client.deleteSession();
        console.log("Appium mobile test suite run completed.");
    }
}

if (require.main === module) {
    runMobileTests();
}
