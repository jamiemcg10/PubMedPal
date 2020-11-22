PubMed Pal  
CS 622 Final Project  
Jamie Smart  
December 2020  

# ![Logo](./src/main/resources/static/favicon/favicon.ico) PubMed Pal

This program will allow a user to search PubMed xml files by interacting with a chatbot. Execution begins with the user selecting which files they want to search. After those files are pulled together (this will take up to 20 seconds), the user can interact with the chatbot and tell it a keyword that they want to search for. The user can also tell the chatbot a range of years to limit the search to. The user can specify if it wants the bot to use a brute force search, a Lucene search, or a database search of MongoDB or MySQL. 

An example of a message that the chatbot will search for is, "Search for articles about cancer from 2018 to 2020."

<br/>
In addition, the user can also ask to see their search history from that session. The chatbot will respond with how many times each searcg method was used, and a chronological list of what the searches were.
<br/>  
<br/>
    
The front-end of this program is built using Javascript/JQuery and interacts with the Java back-end using the Spring Boot framework.
<br/>  
<br/>
___
### To run on your computer: 
1) Create a MySQL database named 'pubmed'
2) Clone this repository
3) Create a logs subfolder in the main project directory
4) Edit the ConstantsTemplate.java file with the correct values for your system, rename the class to Constants, and save as Constants.java.
5) Install Apache Maven (if not already installed)
6) Open a terminal window in the root directory and run mvn spring-boot:run
7) Navigate to localhost:8080 in a Chrome browser