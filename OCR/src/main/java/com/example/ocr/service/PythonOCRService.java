package com.example.ocr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class PythonOCRService {

    @Value("${ocr.python-script-path}")
    private String pythonScriptPath;

    public String runOCRScript(String filePath) {
        StringBuilder outputText = new StringBuilder();

        try {
            // Command to run the Python script with the given file path
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    pythonScriptPath,
                    filePath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read the output from the script
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                outputText.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            System.out.println("✅ Python script exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
            outputText.append("❌ Error while executing Python OCR script: ").append(e.getMessage());
        }

        // Log the result
        System.out.println("📄 OCR Text Output:\n" + outputText);

        return outputText.toString();
    }
}
