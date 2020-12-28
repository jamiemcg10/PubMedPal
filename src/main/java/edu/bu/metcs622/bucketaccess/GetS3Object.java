package edu.bu.metcs622.bucketaccess;


/*
 Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 This file is licensed under the Apache License, Version 2.0 (the "License").
 You may not use this file except in compliance with the License. A copy of
 the License is located at
  http://aws.amazon.com/apache2.0/
 This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 CONDITIONS OF ANY KIND, either express or implied. See the License for the
 specific language governing permissions and limitations under the License.
*/

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
* Get an object within an Amazon S3 bucket.
* 
* This code expects that you have AWS credentials set up per:
* http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
*/
public class GetS3Object {
//  public static void main(String[] args) {
//  	System.setProperty("aws.accessKeyId", Constants.ACCESS_KEY_ID); 
//  	System.setProperty("aws.secretAccessKey", Constants.SECRET_ACCESS_KEY);
//	  
//	  String[] tempArgs = {"pubmedpal", "search_log.csv"};
//	  args = tempArgs;
//  	
//      final String USAGE = "\n" +
//              "To run this example, supply the name of an S3 bucket and object to\n" +
//              "download from it.\n" +
//              "\n" +
//              "Ex: GetObject <bucketname> <filename>\n";
//
//      if (args.length < 2) {
//          System.out.println(USAGE);
//          System.exit(1);
//      }
//
//      String bucket_name = args[0];
//      String key_name = args[1];
//      
//      
//      File tempFile = new File("testlog2.csv");
//
//      System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
//      final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
//      try {
//          S3Object o = s3.getObject(bucket_name, key_name);
//          S3ObjectInputStream s3is = o.getObjectContent();
//          FileOutputStream fos = new FileOutputStream(tempFile);
//          byte[] read_buf = new byte[1024];
//          int read_len = 0;
//          while ((read_len = s3is.read(read_buf)) > 0) {
//              fos.write(read_buf, 0, read_len);
//          }
//          s3is.close();
//          fos.close();
//      } catch (AmazonServiceException e) {
//    	  System.err.println(tempFile.length());
//          System.err.println("4" + e.getErrorMessage());
//          System.exit(1);
//      } catch (FileNotFoundException e) {
//          System.err.println("5" + e.getMessage());
//          System.exit(1);
//      } catch (IOException e) {
//          System.err.println("6" + e.getMessage());
//          System.exit(1);
//      }
//      System.out.println(tempFile.length());
//      System.out.println("Done!");
//  }
  
  public static File getFile(String fileName) {
  	
      String bucket_name = "pubmedpal";
      String key_name = fileName;
      
      File tempFile = new File(fileName);
      
      System.out.format("Downloading %s from S3 bucket %s...\n", key_name, bucket_name);
      final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
      try {
          S3Object o = s3.getObject(bucket_name, key_name);
          S3ObjectInputStream s3is = o.getObjectContent();
          FileOutputStream fos = new FileOutputStream(tempFile);
          byte[] read_buf = new byte[1024];
          int read_len = 0;
          while ((read_len = s3is.read(read_buf)) > 0) {
              fos.write(read_buf, 0, read_len);
          }
          s3is.close();
          fos.close();
      } catch (AmazonServiceException e) {
          System.err.println(e.getErrorMessage());
//          System.exit(1);
      } catch (FileNotFoundException e) {
          System.err.println(e.getMessage());
          System.exit(1);
      } catch (IOException e) {
          System.err.println(e.getMessage());
          System.exit(1);
      }
      

      System.out.println("Done!");
      return tempFile;
	  
  }
}