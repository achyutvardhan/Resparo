package com.resparo.dev.command;

import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
@Command(group = "My Commands")
public class Mycommand {
    
    @Command(description = "Prints Hello, World!")
    public String hello(@Option(description = "Name to greet", defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }
}
