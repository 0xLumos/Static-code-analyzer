package com.sat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.api.Git;

public class SourceAnalyzer 
{
    private String repoUrl;

    private String localPath;
    private String outFile;



    public void getSourceCode(String repoUrl){
        this.repoUrl = repoUrl;

        try {
            Instant timestamp = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
            ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());
            String formattedTimestamp = zdt.format(formatter);
        
            String localPath = "C:\\SAT-TESTS\\"+ formattedTimestamp + "\\"  ; // Specify a folder for the cloned repository
            String outFile = localPath + "SAT-RESULTS.csv";
            System.out.println("Cloning repository from " + repoUrl + " to " + localPath);

            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(localPath))
                .call();
            this.localPath = localPath;
            this.outFile = outFile;
            System.out.println("Cloned successfully!");
        } catch (Exception e) {
            System.err.println("Cloning failed: " + e.getMessage());
        }
    }

    public void analyzeSourceCode(){
        String command = "pmd check -R pmd-rulesets.xml -d " + localPath + " -r " + outFile;
        System.out.println("Analyzing...");
        System.out.println(command);
        try {
            // Create the process builder
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.redirectErrorStream(true); // Redirect error stream to output stream
            processBuilder.command("cmd.exe", "/c", command);
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
    
    // public static void main( String[] args )
    // {   
    //     SourceAnalyzer app = new SourceAnalyzer();
    //     app.analyzeSourceCode();
    //     app.getSourceCode("https://github.com/0xlumos/HexMatrix.git");
        
    // }
}
