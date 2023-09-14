# SearchRepo: Search repositories on GitHub
This is the tool developed for searching repositories on Github.


## What is SearchRepo?
A tool that collects projects on GitHub based on the input keywords in names, Description, Topics and Readme. Then, it excludes some repositories based on defined symptomes in the code.

_1. Repository collection_

It identifies projects on GitHub using Github search API (https://docs.github.com/en/rest/search?apiVersion=2022-11-28). There are some limitations on the search API such as rate limitation that we make a delay between requests to overcome this limitation. Another limitation is the maximum number of retrieved repositories in the search API that is 1000. We overcome this limitation by defining short period of date to have less than 1000 repositories. In addition, each page includes 100 repositories and we use a loop to have repositories in all pages.
We excluded the forks of another repository to prevent duplicate projects.
We had to select GitHub projects as, practically, we cannot analyse all projects. Therefore, we set up several selection criteria such as REST API related keywords, minimum number of stars and forks, minimum size of the projects, date of the last pushed, and some symptoms in the config.json. It firstly collect projects based on the keywords, then filters the projects based on the symptoms in their code.

## How to configure SearchRepo?

In order to configure the SearchRepo, there is a config.json file in the project. It contains: 

* "keywords" --> It collects repositories based on the keywords. It means, if a repository has the keywords in the name, topics, description, and readme, it will be collected.

* "date" --> It collects repositories based on the date. It means, if repository had been pushed in the period, it will be collected.

* "stars" --> It collects repositories which their stars are more than this number of stars.

* "forks" --> It collects repositories which their forks are more than this number of forks.

* "size" It collects repositories which their size are more than this number (KB).

* "language" It collects repositories developed using this language.
  
* "token" --> It uses Github API in order to collect collect projects, and search code in the repositories. These APIs need a token that you should generate a Githab token on the Github website (https://docs.github.com/en/enterprise-server@3.4/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token). 

* "username" --> Authentication of APIs needs token and username of a github account. You should input your github username.
  
* "numOfSymptoms" --> It gets symptoms in order to filter the repositories. You can define the number of symptoms and the symptoms in this parameter and following parameters, respectively.

## How SearchRepo works?
1- It searches for repositories based on the keywords and save them in the AllRepositories.csv file.

2- It filters repositories that include the symptoms and save them in the FilteredRepositories.csv file.

## How to launch SearchRepo?

You can clone and run the SearchRepo:

1- Clone the project: Open a Command Prompt (cmd.exe), use this command: git clone https://github.com/arash-mazidi/SearchRepositories.git

2- Open the project in Eclipse IDE

3- There is a lib folder in the project that contains libraries that are needed to run this tool. You can import all libraries in the classpath of the project. We tested the SearchRepo on the Eclipse IDE.

4- Set up the config.json file (It is explained in the previous sections).

5- Run the SearchRepo by running the SearchRepositories/main/src/Search/searchRepo.java.
