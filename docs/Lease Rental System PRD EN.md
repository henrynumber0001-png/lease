# **Lease Rental Platform**

## **Product Requirements Document (PRD)**

### **Version 1.6**

------

# **1. Project Overview**

## **1.1 Project Background**

The Lease Rental Platform is a comprehensive apartment rental management system designed to streamline the rental lifecycle for both tenants and property administrators.

The platform consists of two major components:

### **Mobile Application**

Designed for tenants and prospective renters.

### **Administrative Management System**

Designed for property managers and system administrators.

The system aims to digitize the entire rental process, including:

- Property discovery
- Viewing appointments
- Lease creation
- Lease renewal
- Lease termination
- Tenant management
- Property management

------

## **1.2 Project Objectives**

### **Business Objectives**

- Improve rental management efficiency
- Reduce manual administrative work
- Provide a self-service platform for tenants
- Centralize apartment and lease information
- Improve occupancy and tenant experience

### **Technical Objectives**

- Build a scalable web application
- Support future expansion and integration
- Provide secure authentication and authorization
- Ensure high system availability and maintainability

------

# **2. User Roles**

## **Tenant**

A registered user who can:

- Search available properties
- View apartment and room details
- Schedule property visits
- Manage leases
- Track browsing history

------

## **Property Administrator**

Responsible for:

- Apartment management
- Room management
- Lease management
- Viewing appointment management
- Tenant account management

------

## **System Administrator**

Responsible for:

- Internal user management
- Role management
- System configuration
- Security and access control

------

# **3. Core Business Processes**

## **3.1 Property Rental Workflow**

Property Published
       ↓
Tenant Browses Property
       ↓
Viewing Appointment
       ↓
Lease Agreement Created
       ↓
Tenant Confirmation
       ↓
Lease Activated

## **3.2 Lease Renewal Workflow**

Active Lease
       ↓
Renewal Request
       ↓
Administrator Approval
       ↓
Lease Updated

## **3.3 Lease Termination Workflow**

Active Lease
       ↓
Termination Request
       ↓
Administrator Review
       ↓
Property Released

# **4. Lease Status Lifecycle**

The platform supports the following lease states:

| **Status**           | **Description**                                |
| -------------------- | ---------------------------------------------- |
| Pending Confirmation | Lease created and awaiting tenant confirmation |
| Active               | Lease successfully signed                      |
| Cancelled            | Lease cancelled before activation              |
| Expired              | Lease reached its end date                     |
| Pending Termination  | Tenant requested termination                   |
| Terminated           | Lease officially terminated                    |
| Pending Renewal      | Renewal request submitted                      |

# **5. Functional Requirements**

## **5.1 Mobile Application**

### **Property Search**

Users can search available rooms based on:

- Location
- Apartment
- Rental price
- Lease duration
- Payment method

### **Property Details**

Users can view:

- Apartment information
- Room information
- Photos
- Facilities
- Rental terms

### **Viewing Appointment**

Users can:

- Schedule appointments
- Modify appointments
- Review appointment status

### **Lease Management**

Users can:

- View lease agreements
- Request lease renewal
- Request lease termination

### **Browsing History**

Users can review previously viewed rooms and apartments.

------

## **5.2 Administrative Portal**

### **Apartment Management**

Administrators can:

- Create apartments
- Update apartment information
- Delete apartments
- Publish or unpublish apartments

Managed information includes:

- Address
- Description
- Facilities
- Contact information
- Images

------

### **Room Management**

Administrators can:

- Create rooms
- Edit room information
- Delete rooms
- Publish or unpublish rooms

Managed information includes:

- Room number
- Rental price
- Floor plan
- Lease options
- Payment methods
- Facilities

------

### **Attribute Management**

The system supports centralized management of:

- Apartment facilities
- Room facilities
- Lease durations
- Payment methods
- Utility fees
- Property tags

------

### **Appointment Management**

Administrators can:

- Review viewing requests
- Approve appointments
- Reject appointments
- Update appointment status

------

### **Lease Management**

Administrators can:

- Create leases
- Modify leases
- Terminate leases
- Renew leases
- Track lease status

------

### **User Management**

Administrators can manage:

#### **Tenant Accounts**

- View tenant information
- Activate accounts
- Suspend accounts

#### **Administrative Accounts**

- Create users
- Assign roles
- Disable accounts

------

# **6. Non-Functional Requirements**

## **Performance**

- Average API response time < 2 seconds
- Support 1,000 concurrent users
- Database query response time < 500 ms

------

## **Security**

The platform must implement:

- JWT Authentication
- Role-Based Access Control (RBAC)
- Password encryption
- HTTPS communication
- Input validation

------

## **Availability**

- Target uptime: 99.9%
- Daily automated backup
- Disaster recovery capability

------

## **Scalability**

The system should support:

- Future microservice migration
- Horizontal scaling
- Cloud deployment

------

# **7. System Architecture**

## **Frontend**

### **Mobile Application**

- Vue 3

### **Admin Portal**

- Vue 3

------

## **Backend**

- Spring Boot
- Spring MVC
- MyBatis
- MyBatis Plus

------

## **Data Storage**

### **Relational Database**

- MySQL

### **Cache Layer**

- Redis

### **Object Storage**

- MinIO

------

## **Deployment**

- Nginx
- Linux Server Environment

------

# **8. Database Design Overview**

The system contains the following core entities:

| **Entity**          | **Description**         |
| ------------------- | ----------------------- |
| Apartment           | Property information    |
| Room                | Individual rental unit  |
| Tenant              | Mobile application user |
| Lease Agreement     | Rental contract         |
| Viewing Appointment | Property visit request  |
| Browsing History    | User browsing records   |
| Administrative User | Back-office user        |

## **Entity Relationships**

Apartment
    ↓
Room
    ↓
Lease Agreement
    ↓
Tenant

Tenant
    ↓
Viewing Appointment

Tenant
    ↓
Browsing History

# **9. API Requirements**

The platform exposes RESTful APIs for:

## **Authentication**

- Login
- Logout
- User Profile

------

## **Apartment Services**

- Apartment CRUD
- Apartment Search
- Apartment Details

------

## **Room Services**

- Room CRUD
- Room Search
- Room Details

------

## **Appointment Services**

- Create Appointment
- Update Appointment
- Appointment History

------

## **Lease Services**

- Create Lease
- Update Lease
- Renew Lease
- Terminate Lease

------

## **User Services**

- Tenant Management
- Administrator Management
- Role Management

------

# **10. Future Enhancements**

Potential future releases may include:

- Online payment integration
- Digital contract signing
- AI-powered property recommendations
- Chat support
- Mobile push notifications
- Multi-language support
- Analytics dashboard
- Cloud-native deployment architecture

------

# **11. Success Metrics**

The project will be considered successful if:

- Property search response time remains below 2 seconds.
- More than 95% of lease operations are completed online.
- System uptime exceeds 99.9%.
- User satisfaction score exceeds 4.5/5.
- Administrative workload is reduced by at least 50%.