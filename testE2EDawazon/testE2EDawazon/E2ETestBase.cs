using Microsoft.Playwright;
using Microsoft.Playwright.NUnit;

namespace testE2EDawazon;

public abstract class E2ETestBase : PageTest
{
    private const string BaseUrl = "http://localhost:5000";
    protected string BaseTestUrl => BaseUrl;

    public override BrowserNewContextOptions ContextOptions()
    {
        return new BrowserNewContextOptions
        {
            Locale = "es-ES",
            TimezoneId = "Europe/Madrid",
            RecordVideoDir = "TestVideos",
            IgnoreHTTPSErrors = true
        };
    }
    [SetUp]
    public async Task BrowserSetup()
    {
        // Esta configuración hace que el navegador sea visible
    }
    protected async Task CaptureScreenshotAsync(string stepName)
    {
        var screenshotPath = Path.Combine("TestScreenshots", TestContext.CurrentContext.Test.Name);
        Directory.CreateDirectory(screenshotPath);
        await Page.ScreenshotAsync(new PageScreenshotOptions
        {
            Path = Path.Combine(screenshotPath, $"{stepName}.png"),
            FullPage = true
        });
    }
}