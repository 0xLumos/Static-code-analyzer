package com.sta;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class App 
{
    public static void main( String[] args )
    {
        String repoUrl = "https://github.com/0xlumos/HexMatrix";
        String localPath = "C:\\Downloads\\";

        try {
            System.out.println("Cloning repository from " + repoUrl + " to " + localPath);
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(new File(localPath))
                    .call();
            System.out.println("Cloned");
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
       
    }

