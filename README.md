

https://github.com/user-attachments/assets/16e36675-3153-4472-88fd-12e85d931e70

# Lease Rental System

A full-stack apartment rental management platform that supports apartment operations, room management, viewing appointments, lease lifecycle management, and mobile tenant services.

---

## Project Overview

Lease Rental System is a full-stack rental management platform designed for property operators and tenants.

The system consists of:

- Admin Management Console (Vue 3 + Element Plus)
- Mobile Tenant Application (Vue 3 + Vant)
- Spring Boot Backend Services
- MySQL Database
- Redis Cache
- MinIO Object Storage
- Aliyun SMS Service

The platform supports:

- Apartment Management
- Room Management
- Lease Management
- Viewing Appointments
- User Management
- SMS Authentication
- Browsing History
- Contract Renewal
- Property Search

---

## Architecture

```text
Admin Console (Vue3)
        │
        ▼
   web-admin
(Spring Boot)

Mobile H5 (Vue3)
        │
        ▼
    web-app
(Spring Boot)

        │
        ▼
 ┌─────────────┐
 │   MySQL     │
 ├─────────────┤
 │   Redis     │
 ├─────────────┤
 │   MinIO     │
 ├─────────────┤
 │ Aliyun SMS  │
 └─────────────┘
```

---

## Technology Stack

### Backend

- Java 21
- Spring Boot 3.2
- MyBatis Plus
- MySQL
- Redis
- JWT Authentication
- MinIO
- Aliyun SMS
- Knife4j / OpenAPI
- Maven Multi-Module

### Frontend

#### Admin Console

- Vue 3
- TypeScript
- Pinia
- Element Plus
- Axios

#### Mobile Client

- Vue 3
- TypeScript
- Pinia
- Vant
- Axios

---

## Core Features

### Admin Side

- Administrator Login
- Captcha Verification
- Apartment Management
- Room Management
- Attribute Management
- Label Management
- Facility Management
- Payment Method Management
- Lease Term Management
- Viewing Appointment Management
- Lease Agreement Management
- User Management
- Staff & Role Management
- File Upload (MinIO)

### Tenant Side

- SMS Login
- Property Search
- Apartment Detail
- Room Detail
- Viewing Appointment Booking
- My Appointments
- Lease Management
- Lease Renewal
- Browsing History

---

## Project Structure

```text
lease
│
├── model
│   ├── Entity
│   ├── Enum
│   └── VO
│
├── common
│   ├── JWT
│   ├── Redis
│   ├── Exception
│   ├── MinIO
│   └── MyBatis Config
│
├── web
│   ├── web-admin
│   └── web-app
│
├── frontend
│   ├── rentHouseAdmin
│   └── rentHouseH5
│
└── docs
    └── Design-Document.md
```

---

## Key Technical Highlights

### JWT Authentication

- Admin Authentication
- Tenant Authentication
- Stateless Session Management

### Redis Integration

- Captcha Storage
- SMS Verification Codes
- Login Retry Protection
- Spring Cache

### Multi-Module Maven Design

```text
Parent Project
│
├── model
├── common
└── web
    ├── web-admin
    └── web-app
```

### Transaction Management

Complex apartment and room creation processes are protected using Spring Transaction Management to ensure consistency across multiple relationship tables.

### Scheduled Tasks

Daily lease expiration checks automatically update lease statuses.

---

## Database Design

Main domain entities:

- Apartment
- Room
- LeaseAgreement
- UserInfo
- ViewAppointment
- FacilityInfo
- LabelInfo
- PaymentType
- LeaseTerm

See detailed design:

```text
docs/Design-Document.md
```

---

## API Documentation

After startup:

```text
Admin API:
http://localhost:8080/doc.html

App API:
http://localhost:8081/doc.html
```

---

## Local Development

### Backend

```bash
mvn clean install

mvn -pl web/web-admin spring-boot:run

mvn -pl web/web-app spring-boot:run
```

### Frontend

```bash
cd frontend/rentHouseAdmin
npm install
npm run dev

cd ../rentHouseH5
npm install
npm run dev
```

---

## Environment Requirements

| Component | Version |
|------------|----------|
| Java | 21 |
| Maven | 3.9+ |
| MySQL | 8.x |
| Redis | 7.x / 8.x |
| Node.js | 18+ |

---

## Demo

### Screenshots

- Login Page
- Apartment Management
- Room Management
- Appointment Management
- Lease Management

### Video Demonstration

Coming Soon

---

## Documentation

- Design Document
- Database Design
- API Design
- Deployment Guide

See:

```text
docs/
```

---

## Author

This project was developed as a full-stack rental management system for learning modern Java backend development, system design, and enterprise application architecture.

Tech Focus:

- Spring Boot
- MyBatis Plus
- Redis
- JWT
- Vue 3
- Enterprise System Design
