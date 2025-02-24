package com.stevekung.jsonpathtest;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

public class JsonPathTest
{
    public static void main(String[] args) throws IOException
    {
        var jsonStr = new String(Files.readAllBytes(Paths.get("src/main/resources/json/universe.json")));
        var jsonPath = JsonPath.parse(Configuration.defaultConfiguration().jsonProvider().parse(jsonStr));

        var universeId = jsonPath.read("universe.id");
        var celestialName = jsonPath.read("universe.planets[0].name");
        var celestialNames = jsonPath.<List<String>>read("universe.planets[*].name");
        var atmospheres = jsonPath.<List<String>>read("universe.planets[*].atmosphere..type").stream().distinct().toList();

        System.out.println("Welcome to universe: " + universeId);
        System.out.println("Celestial Name: " + celestialName);
        System.out.println("All Celestial Name: " + celestialNames);
        System.out.println("All Atmospheres: " + atmospheres);

        var massLessThan100 = filter(where("mass").lt(100));
        List<String> planetLessMass = jsonPath.read("$.universe.planets[?].name", massLessThan100);
        System.out.println("Mass less than 100: " + planetLessMass);

        var massMoreThan50 = jsonPath.<List<String>>read("$.universe.planets[?(@.mass > 50)].name");
        System.out.println("Mass more than 50: " + massMoreThan50);

        List<String> lastGalaxyName = jsonPath.read("universe.galaxies[-1:].name");
        List<String> lastGalaxyType = jsonPath.read("universe.galaxies[-1:].type");
        System.out.println("Last Galaxy: Name %s, Type %s".formatted(lastGalaxyName.getFirst(), lastGalaxyType.getFirst()));
    }
}