package com.main.stepper.logger.api;

import com.main.stepper.logger.implementation.data.Log;

import java.io.Serializable;
import java.util.List;

public interface ILogger extends Serializable {
    void log(String uuid, String message);
    void log(String message);
    List<Log> getLog(String uuid);
    List<Log> getLog();
    ILogger getSubLogger(String uuid);
    void clear();
}
