# Imgur Project

## Overview

This project is a Spring Boot app that integrates with Imgur API's to view, delete, and upload local images. Securely register a user in order to receive authentication and authorization to get access to the API's. Upon uploading a new image (with Kafka running), a message will be output, including the username and image name that was just created in the event.

## Functionality

- **Upload Images**: Users can upload images with titles and descriptions.
- **View Images**: Retrieve image details or links by database ID.
- **Delete Images**: Delete an image securely by database ID.
- **List User Images**: View all images associated with the authenticated user.
- **Event Messaging**: Publishes image upload events to a Kafka topic.

## Setup

### 1. Clone the Repository

```bash
git clone https://github.com/llama95/synch.git
cd synch
```

### 2. Start Kafka Locally

Navigate to your Kafka installation directory.

- **Start Zookeeper:**

  ```bash
  bin/zookeeper-server-start.sh config/zookeeper.properties
  ```

- **Start Kafka:**

  ```bash
  bin/kafka-server-start.sh config/server.properties
  ```

- **Create the `user-events` topic:**

  ```bash
  bin/kafka-topics.sh --create --topic user-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
  ```

### 3. Build and Run the Application

Navigate to the project directory:

```bash
cd /path/to/project
```

- **Build the project:**

  ```bash
  mvn clean package
  ```

- **Run the application:**

  ```bash
  mvn spring-boot:run
  ```

The application will start on [http://localhost:8080](http://localhost:8080).

When a new image is uploaded, Kafka will publish a message that includes username and image info, e.g.,

```json
{"username":"john_doe", "imageName":"Example Image #1"}
```

## Endpoints

**Open `Synch.postman_collection.json` in the project root for a Postman collection to carry out the below API requests! All of these routes can use basic auth and a username/password combo.**

### 1. Register User

Register a user to access the protected routes that interact with Imgur.

**POST** `/api/users/register`

**Example cURL Request:**

```bash
curl --location --request POST 'http://localhost:8080/api/users/register' \
--header 'Content-Type: application/json' \
--data-raw '{
    "username": "john_doe",
    "password": "password123"
}'
```

### 2. Upload Image

Upload an image to Imgur and the H2 database. The application has 5 test images stored locally. The file path must be `1.jpg`, `2.jpg`, up to `5.jpg`. **You must use these files/paths to successfully upload an image.**

**POST** `/api/images/upload`

**Parameters:**

- `filePath`: Path to the local image file.
- `title`: Title of the image.
- `description`: Description of the image.

**Example cURL Request:**

```bash
curl -u john_doe:password123 -X POST "http://localhost:8080/api/images/upload" \
  -d "filePath=1.jpg" \
  -d "title=1" \
  -d "description=The number 1"
```

### 3. View Image by ID

Retrieve image details or link by database ID.

**GET** `/api/images/{id}`

**Response:** Redirects to the image URL hosted on Imgur.

**Example cURL Request:**

```bash
curl -u john_doe:password123 -X GET "http://localhost:8080/api/images/1"
```

### 4. Delete Image by ID

Delete an image securely by database ID.

**DELETE** `/api/images/{id}`

**Response:** Confirms successful deletion of the image.

**Example cURL Request:**

```bash
curl -u john_doe:password123 -X DELETE "http://localhost:8080/api/images/1"
```

### 5. List User Images

Retrieve all images uploaded by the authenticated user.

**GET** `/api/images/my-images`

**Response:** Returns a list of all images uploaded by the user.

**Example cURL Request:**

```bash
curl -u john_doe:password123 -X GET "http://localhost:8080/api/images/my-images"
```

## Testing

Running 'mvn test' will run the application tests.


