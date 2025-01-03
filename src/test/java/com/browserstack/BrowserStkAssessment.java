package com.browserstack;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BrowserStkAssessment {
    public static final String USERNAME = "albinsebastian_iN3HsC";
    public static final String AUTOMATE_KEY = "yzxUKpmZqLksTGJqSvBU";
    public static final String BROWSERSTACK_URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";
    private static final String RAPIDAPI_URL = "https://rapid-translate-multi-traduction.p.rapidapi.com/t";
    private static final String RAPIDAPI_HOST = "rapid-translate-multi-traduction.p.rapidapi.com";
    private static final String RAPIDAPI_KEY = "8fdcc5e613mshb3504ccd2782677p143a5fjsn264ec74c85ae";

    public static void main(String[] args) throws Exception {
        
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("os", "Windows");
        caps.setCapability("os_version", "10");
        caps.setCapability("browser", "Chrome");

        //RemoteWebDriver Init.
        WebDriver driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), caps);        
        // Storing translated titles
        List<String> translatedTitles = new ArrayList<>();
        
        try {
    
            driver.get("https://elpais.com/");
            driver.manage().window().maximize();
            Thread.sleep(Duration.ofSeconds(8).toMillis());
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            driver.findElement(By.xpath("//*[@id=\"didomi-notice-agree-button\"]")).click();

            // fetching the first 5 articles
            for (int i = 1; i <= 5; i++) {
                try {
                    WebElement titleElement = driver.findElement(By.xpath("/html/body/main/div[2]/section[1]/div/div/article[" + i + "]/header/h2/a"));
                    String title = titleElement.getText();

                    
                    
                    // Translate title to english
                    String translatedTitle = translateText(title, "es", "en");

                    System.out.println("Original Title: " + title);
                    System.out.println("Translated Title: " + translatedTitle);
                    
                 // storing translated title for later analysis
                    translatedTitles.add(translatedTitle);

                 
                    
                    // content preview
                    WebElement contentElement = driver.findElement(By.xpath("/html/body/main/div[2]/section[1]/div/div/article[" + i + "]/p"));
                    String content = contentElement.getText();
                    System.out.println("Content: " + content);
                    
                    
                    // Check for cover image and download
                    try {
                        WebElement imageElement = driver.findElement(By.xpath("/html/body/main/div[2]/section[1]/div/div/article[" + i + "]/figure/img"));
                        String imageUrl = imageElement.getAttribute("src");
                        saveImage(imageUrl, "article_" + i + ".jpg");
                    } catch (NoSuchElementException e) {
                        System.out.println("No cover image found for article " + i);
                    }
                } catch (Exception e) {
                    System.out.println("Error processing article " + i + ": " + e.getMessage());
                }
            }
  
         // Analyse and print repeated words
            analyzeRepeatedWords(translatedTitles);
         
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
    
    
    
    private static void saveImage(String imageUrl, String fileName) {
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image saved: " + fileName);
        } catch (IOException e) {
            System.out.println("Failed to save image: " + e.getMessage());
        }
    }

    private static String translateText(String text, String fromLang, String toLang) {
        try {
            // Setting up the API connection
            URL url = new URL(RAPIDAPI_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("x-rapidapi-host", RAPIDAPI_HOST);
            connection.setRequestProperty("x-rapidapi-key", RAPIDAPI_KEY);
            connection.setDoOutput(true);

            // request payload
            JSONObject requestBody = new JSONObject();
            requestBody.put("from", fromLang);
            requestBody.put("to", toLang);
            requestBody.put("q", text);

            // Sending the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                   
                    return response.toString();
             
                }
            } else {
                System.out.println("API Error: Response Code " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    System.out.println("Error Response: " + errorResponse.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Translation failed: " + e.getMessage());
        }
        return "[Translation Error]";
    }
    
    private static void analyzeRepeatedWords(List<String> translatedTitles) {
        // Convert all translated titles to lowercase and split into words
        Map<String, Integer> wordCount = new HashMap<>();

        for (String title : translatedTitles) {
            String[] words = title.toLowerCase().split("\\W+"); // Split by non-word characters
            for (String word : words) {
                if (word.length() > 1) { // Skip single characters (if needed)
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Print words that appear more than twice
        System.out.println("\nRepeated Words (appeared more than twice):");
        wordCount.forEach((word, count) -> {
            if (count > 2) {
                System.out.println(word + ": " + count);
            }
        });
    }
    
    
}
