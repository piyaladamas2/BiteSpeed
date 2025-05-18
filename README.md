# BiteSpeed

Bitespeed needs a way to identify and keep track of a customer's identity across multiple purchases.

## 🌐 Live Deployment

Access the deployed API here:  
👉 [https://bitespeed-env-1.eba-hszaiu2u.eu-north-1.elasticbeanstalk.com/identify](https://bitespeed-env-1.eba-hszaiu2u.eu-north-1.elasticbeanstalk.com/identify)

# Contact Identifier API

This is a Spring Boot RESTful service that identifies users based on their contact details (email and/or phone number). If a matching contact exists, it returns the identified user information; otherwise, it creates a new contact record.

## Features

- Identify users by email or phone number
- Returns consolidated contact information
- Automatically handles creation or linking of new contacts

## Technologies Used

- Java 17
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- MySQL
- Lombok

## API Endpoints

### Identify Contact

**POST** `/identify`

#### Request Body

```json
{
  "email": "john.doe@example.com",
  "phoneNumber": "9748873365"
},
{
    "email":"piyal.adamas2@gmail.com",
    "phoneNumber":"974887320"
}
```

```
{
    "contact": {
        "primaryContactId": 1,
        "emails": [
            "john.doe@example.com",
            "piyal.adamas2@gmail.com"
        ],
        "phoneNumbers": [
            "974887320"
        ],
        "secondaryContactIds": [
            2
        ]
    }
}
```
