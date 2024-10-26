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
/*This class fetches a github repository and saves it locally 
It uses the Git API to clone the given Link to the repo
It also analyzes the cloned repository using PMD tool*/
{
    private String repoUrl;        // Remote Repository URL
    private String localPath;        // Local path to the repo
    private String outFile;        // Path to the results file


    // This method creates a local path, named by the date of creation, then pulls a public github repository to the folder
    public void getSourceCode(String repoUrl){
        this.repoUrl = repoUrl;

        try {
            Instant timestamp = Instant.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ssSS");
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

    // This method analyses the locally-stored git repository, using pmd analysis tool, then save the results to a file
    public void analyzeSourceCode(){
    
        String command = "pmd check -R pmd-rulesets.xml -d " + localPath + " -r " + outFile;        // The command that's going to be based to the process builder
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
