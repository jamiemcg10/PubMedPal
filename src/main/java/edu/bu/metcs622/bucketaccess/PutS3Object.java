package edu.bu.metcs622.bucketaccess;


/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import edu.bu.metcs622.main.Constants;

public class PutS3Object {
	static BufferedWriter searchLogWriter;

//    public static void main(String[] args) throws IOException {
//    	System.setProperty("aws.accessKeyId", Constants.ACCESS_KEY_ID); 
//    	System.setProperty("aws.secretAccessKey", Constants.SECRET_ACCESS_KEY);
//    	
//    	String[] tempArgs = {"pubmedpal", "testlog.csv", ".//testlog.csv"};
//    	args = tempArgs;
//    	
//    	File searchLog = new File(".//testlog.csv");
//		if (searchLog.createNewFile()) { // create file
//			System.out.println("Search log doesn't exist");
//			searchLogWriter = new BufferedWriter(new FileWriter(searchLog));
//			searchLogWriter.write("Date,File size (KB),Type,Method,Term,Time (ms.)\n");
//			searchLogWriter.flush();
//		} else { // get file
//			searchLogWriter = new BufferedWriter(new FileWriter(searchLog, true));
//		}
//    	
//        final String USAGE = "\n" +
//                "Usage:\n" +
//                "  PutObject <bucketName> <objectKey> <objectPath> \n\n" +
//                "Where:\n" +
//                "  bucketName - the Amazon S3 bucket to upload an object into.\n" +
//                "  objectKey - the object to upload (for example, book.pdf).\n" +
//                "  objectPath - the path where the file is located (for example, C:/AWS/book2.pdf). \n\n" ;
//
//        if (args.length != 3) {
//            System.out.println(USAGE);
//            System.exit(1);
//       }
//
//        String bucketName = args[0];
//        String objectKey = args[1];
//        String objectPath = args[2];
//
//        System.out.println("Putting object " + objectKey +" into bucket "+bucketName);
//        System.out.println("  in bucket: " + bucketName);
//
//        Region region = Region.US_EAST_2;
//        S3Client s3 = S3Client.builder()
//                .region(region)
//                .build();
//
//        String result = putS3Object(s3, bucketName, objectKey, objectPath);
//        System.out.println("Tag information: "+result);
//        s3.close();
//    }
	
    public static boolean writeFile(File file, String fileName) throws IOException {
    	boolean successful = true;
    	
    	try {
			File log = file;
			searchLogWriter = new BufferedWriter(new FileWriter(log, true));
			

			String bucketName = "pubmedpal";
			String objectKey = fileName;
			String objectPath = ".//"+fileName;

			System.out.println("Putting object " + objectKey +" into bucket "+bucketName);
			System.out.println("  in bucket: " + bucketName);

			Region region = Region.US_EAST_2;
			S3Client s3 = S3Client.builder()
			        .region(region)
			        .build();

			String result = putS3Object(s3, bucketName, objectKey, objectPath);
			System.out.println("Tag information: "+result);
			s3.close();
		} catch (Exception e) {
			successful = false;
			e.printStackTrace();
		}
        
        return successful;
    }

    public static String putS3Object(S3Client s3,
                                     String bucketName,
                                     String objectKey,
                                     String objectPath) {

        try {
           PutObjectResponse response = s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build(),
                    RequestBody.fromBytes(getObjectFile(objectPath)));

           return response.eTag();

        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    // Return a byte array
    private static byte[] getObjectFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
}

