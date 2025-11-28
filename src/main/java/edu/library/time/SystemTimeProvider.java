package edu.library.time;

import java.time.LocalDate;


public class SystemTimeProvider implements TimeProvider {
    @Override
    public LocalDate today() {
        return LocalDate.now();
    }
}
