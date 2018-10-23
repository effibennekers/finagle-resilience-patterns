package com.ing.example.jfall.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherReport {

    public enum Condition {
        PARTLY_SUNNY,
        SCATTERED_THUNDERSTORMS,
        SHOWERS,
        SCATTERED_SHOWERS,
        RAIN_AND_SNOW,
        OVERCAST,
        LIGHT_SNOW,
        FREEZING_DRIZZLE,
        CHANCE_OF_RAIN,
        SUNNY,
        CLEAR,
        MOSTLY_SUNNY,
        PARTLY_CLOUDY,
        MOSTLY_CLOUDY,
        CHANCE_OF_STORM,
        RAIN,
        CHANCE_OF_SNOW,
        CLOUDY,
        MIST,
        STORM,
        THUNDERSTORM,
        CHANCE_OF_TSTORM,
        SLEET,
        SNOW,
        ICY,
        DUST,
        FOG,
        SMOKE,
        HAZE,
        FLURRIES,
        LIGHT_RAIN,
        SNOW_SHOWERS,
        HAIL;

    }

    public enum WindDirection {
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST;
    }

    private Condition condition;
    private Integer temperature;
    private Integer windForce;
    private WindDirection windDirection;

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public Integer getWindForce() {
        return windForce;
    }

    public void setWindForce(Integer windForce) {
        this.windForce = windForce;
    }

    public WindDirection getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(WindDirection windDirection) {
        this.windDirection = windDirection;
    }

}
