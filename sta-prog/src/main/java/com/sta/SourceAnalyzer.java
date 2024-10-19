package com.sta;

import java.io.*;


import java.lang.Runtime;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.api.Git;

public class SourceAnalyzer 
{
    private String repoUrl;

    private Instant timestamp = Instant.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());
    String formattedTimestamp = zdt.format(formatter);

    private String localPath = "C:\\STA-TESTS\\"+ formattedTimestamp + "\\"  ; // Specify a folder for the cloned repository
    private String outFile = localPath + "STA-RESULTS.txt";

    public SourceAnalyzer(){

    }

    public void getSourceCode(String repoUrl){
        this.repoUrl = repoUrl;

        try {
            System.out.println("Cloning repository from " + repoUrl + " to " + localPath);
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(localPath))
                .call();
            System.out.println("Cloned successfully!");
        } catch (Exception e) {
            System.err.println("Cloning failed: " + e.getMessage());
        }
    }

    public void analyzeSourceCode(){
        String command = "C:\\Users\\NALHOUSE\\Desktop\\pmd-bin-7.6.0\\bin\\pmd.bat check -R pmd-rulesets.xml -d " + localPath + " -r " + outFile;
        System.out.println("Analyzing...");
        try {
            // Create the process builder
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true); // Redirect error stream to output stream

            // Start the process
            Process process = processBuilder.start();
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Wait for the process to finish and get the exit value
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getLocalPath(){
        return localPath;
    }

    public String getOutFile(){
        return outFile;
    }
    
    public static void main( String[] args )
    {   
        SourceAnalyzer app = new SourceAnalyzer();
        app.analyzeSourceCode();
        app.getSourceCode("https://github.com/0xlumos/HexMatrix.git");
        
    }
}
