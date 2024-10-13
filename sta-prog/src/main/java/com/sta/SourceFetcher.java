package com.sta;

import java.io.File;

import org.eclipse.jgit.api.Git;

public class SourceFetcher 
{
    private String repoUrl;
    public SourceFetcher(){

    }

    public void getSourceCode(String repoUrl){
        
        String localPath = "C:\\STA-TESTS\\HexMatrix"; // Specify a folder for the cloned repository
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
    public static void main( String[] args )
    {   
        SourceFetcher app = new SourceFetcher();
        String repoUrl = "https://github.com/0xlumos/HexMatrix.git";
        app.getSourceCode(repoUrl);
    }
}