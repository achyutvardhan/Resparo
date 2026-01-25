package com.resparo.dev.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class StringToPathConverter implements Converter<String, Path> {

    @Override
    @NotNull
    public Path convert(String source) {
        return Paths.get(source).toAbsolutePath().normalize();
    }
}