package com.sat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.api.Git;

public class SourceAnalyzer{
        /*This class fetches a github repository and saves it locally 
        It uses the Git API to clone the given Link to the repo
        It also analyzes the cloned repository using PMD tool*/

        private String repoUrl;        // Github Remote Repository URL
        private String localPath;        // Local path to the repo
        private String outFile;        // Path to the results file


        /* This method creates a local path, named by the date of creation, 
        then pulls a public github repository to a local folder, the local path is stored and passed to the analyser
        The name of the folder is a timestamp of when a repo gets cloned*/ 
        public void getSourceCode(String repoUrl){
            this.repoUrl = repoUrl;    

            // This try-catch statement will attempt to get an instance of time, clone the desired repository, and save the file locally
            try {
                Instant timestamp = Instant.now();      // Get current time
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ssSS");
                ZonedDateTime zdt = timestamp.atZone(ZoneId.systemDefault());
                String formattedTimestamp = zdt.format(formatter);
            
                String localPath = "C:\\SAT-TESTS\\"+ formattedTimestamp + "\\"  ;      // Specify a folder for the cloned repository
                String outFile = localPath + "SAT-RESULTS.csv";     // The file that's going to contain the results
                System.out.println("Cloning repository from " + repoUrl + " to " + localPath);

                Git.cloneRepository()  // Calling the org.eclipse.jgit.api.Git class, and passing the required parameters
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

        /* This method  */
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
