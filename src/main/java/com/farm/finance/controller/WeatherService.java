package com.farm.finance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    // ===== YOUR API KEY FROM OPENWEATHERMAP =====
    // Step 1: Go to https://openweathermap.org
    // Step 2: Sign up for free account
    // Step 3: Get your API Key from dashboard
    private static final String API_KEY = "your_api_key_here";  // ← PASTE YOUR KEY HERE!

    // ===== DEFAULT CITY =====
    private static final String DEFAULT_CITY = "Bangalore";  // ← Change to your city!

    public String getWeather() {
        return getWeather(DEFAULT_CITY);
    }

    public String getWeather(String city) {
        try {
            // Step 1: Build the URL
            String url = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city + "&appid=" + API_KEY + "&units=metric";

            // Step 2: Call the API
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            // Step 3: Parse the response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Step 4: Get weather details
            String weather = root.path("weather").get(0).path("description").asText();
            double temp = root.path("main").path("temp").asDouble();
            double humidity = root.path("main").path("humidity").asDouble();
            double windSpeed = root.path("wind").path("speed").asDouble();

            // Step 5: Get emoji for weather
            String emoji = getWeatherEmoji(weather);

            // Step 6: Format the result
            return emoji + " " + weather + ", " + Math.round(temp) + "°C, Humidity: " + Math.round(humidity) + "%";

        } catch (Exception e) {
            return "⚠️ Weather unavailable";
        }
    }

    // ===== GET EMOJI FOR WEATHER =====
    private String getWeatherEmoji(String weather) {
        String lower = weather.toLowerCase();
        if (lower.contains("clear") || lower.contains("sun")) return "☀️";
        if (lower.contains("cloud")) return "☁️";
        if (lower.contains("rain") || lower.contains("drizzle")) return "🌧️";
        if (lower.contains("thunder") || lower.contains("storm")) return "⛈️";
        if (lower.contains("snow") || lower.contains("ice")) return "❄️";
        if (lower.contains("fog") || lower.contains("mist")) return "🌫️";
        return "🌤️";
    }
}