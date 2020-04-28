## ODOT

* Project Introduction : [One Day One Tip](https://github.com/goodGid/ODOT/wiki/%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-%EC%86%8C%EA%B0%9C)

---

## OS

* Ubuntu 18.04.3

---

## Install

### Git

```
sudo add-apt-repository ppa:git-core/ppa
sudo apt-get update && sudo apt-get dist-upgrade
sudo apt-get install git-core
git version
```

### Clone Project

* Repo : https://github.com/goodGid/ODOT

```
git clone https://github.com/goodGid/ODOT.git
```


### Maven
    
```
sudo apt-get install maven
```


---


## Server Environment

* Spring Boot (v2.2.6.RELEASE)

* Maven


---


## How to deployment

### Step 1.

* Use `mvn package` command to create jar file

```
ubuntu@node1:~/ODOT$ ls
README.md  pom.xml  src
ubuntu@node1:~/ODOT$ mvn package
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------------------< goodgid:odot >----------------------------
[INFO] Building odot 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.254 s
[INFO] Finished at: 2020-04-24T12:59:59Z
[INFO] ------------------------------------------------------------------------
```

* Check **jar** file

    - The jar file is created by combining the names of artifactId and version.
    
    ```
    ## pom.xml
    <artifactId>odot</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    ```
    
    - format : {{artifactId}}-{{version}}.jar
    
    - ex) odot-0.0.1-SNAPSHOT.jar 
    
    - The jar file is located under the target directory.
    
    - ex) target/odot-0.0.1-SNAPSHOT.jar 
    
    
* If the `maven test` goal fails, run it with the following command.

```
mvn package -DskipTests
```
  
    
### Step 2. 

* Use the `java -jar` command to start the server

```
java -jar odot-0.0.1-SNAPSHOT.jar
```


### Step 3.

* Request to check if server is running normally

    - Use Static IP of AWS EC2 

    - hostname : 13.124.47.92
    
    - port : 8080
    
    - ex) http://13.124.47.92:8080/health/check


### Step 4.

* Set it to run in the background using the `nohup` and `&` command.

```
nohup java -jar target/odot-0.0.1-SNAPSHOT.jar&
```

* If you want to terminate the server, Find and kill

```
# Find Server Process
## Usage 1. 
ps -ef | grep '{project_name}'
ex) ps -ef | grep 'odot'

## Usage 2. 
ps -ef | grep 'java -jar'

# Kill Process
kill -9 pid
```

---

## Tip

* [Alias Setting for server management](https://gist.github.com/goodGid/1710d1542368ebf637b315882bc419d2)

* [Setting Nginx](https://goodgid.github.io/Nginx/)

* [If you want to use Domain Name](https://goodgid.github.io/Free-Domain/)
