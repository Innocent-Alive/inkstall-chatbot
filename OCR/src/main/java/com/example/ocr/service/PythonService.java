package com.example.ocr.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class PythonService {

    public String splitPdf(String filePath, String range) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("python", "python/split_pdf.py", filePath, range);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script failed with exit code: " + exitCode + "\nOutput:\n" + output);
        }

        return output.toString();
    }
}
