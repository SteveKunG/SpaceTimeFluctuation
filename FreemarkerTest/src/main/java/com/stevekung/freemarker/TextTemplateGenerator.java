package com.stevekung.freemarker;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import freemarker.template.Configuration;

public class TextTemplateGenerator
{
    public static void main(String[] args) throws Exception
    {
        // Configure Freemarker
        var cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());

        // Load the template
        var template = cfg.getTemplate("text_template.txt");

        // Prepare the data model
        Map<String, Object> data = new HashMap<>();

        data.put("old_blackhole", Boolean.TRUE);

        data.put("blackhole_desc", "This blackhole has only one million-hour lifespan.");

        data.put("evaporated_at", LocalDate.now());

        var spaghettifiedObjects = List.of(
                new SpaghettifiedObject("star", "Alpha Centauri"),
                new SpaghettifiedObject("planet", "Klen Darth II"),
                new SpaghettifiedObject("furry", "Simonz")
        );

        data.put("spaghettified_objects", spaghettifiedObjects);

        var virtualParticles = Map.of("0", "1", "1", "0");

        data.put("virtual_particles", virtualParticles);

        data.put("accepted_list", spaghettifiedObjects.stream().map(SpaghettifiedObject::type).collect(Collectors.joining(",")));

        // Generate a text file
        var file = new File("output/generated_text.txt");
        file.getParentFile().mkdirs();
        var fileWriter = new FileWriter(file);
        template.process(data, fileWriter);
        fileWriter.close();

        System.out.println("Text generated successfully!");
    }

    public record SpaghettifiedObject(String type, String name)
    {

    }
}