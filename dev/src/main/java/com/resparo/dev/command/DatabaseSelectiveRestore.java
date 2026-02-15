package com.resparo.dev.command;

import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

@Component
@Command(group = "Restore" , description = "Selective Restoration of Databases")
public class DatabaseSelectiveRestore {
  
    @Command(description = "Selective Restoration of Databases")
    public String selectiveRestoreDb(){
        return  " ";
    }
}
