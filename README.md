# SpectaPro - Event Booking Mobile App

![Android](https://img.shields.io/badge/Android-API_26%2B-3DDC84?logo=android)
![Java](https://img.shields.io/badge/Java-1.8%2B-007396?logo=java)
![Spring](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=spring)
![Oracle](https://img.shields.io/badge/Oracle-11g-F80000?logo=oracle)

## 🎭 Introduction
SpectaPro revolutionizes event booking with real-time reservations, personalized recommendations, and seamless ticketing system integration.

## ✨ Key Features
### 🔍 Advanced Search
- Multi-criteria filters (date, venue, category, price)
- Semantic search engine
- Saved search history

### 🎟️ Smart Booking
- 3-step reservation process
- Real-time seat availability
- Multiple payment options

### 👤 User Profile
- Favorite events & history
- Personalized preferences
- Rating & review system

## 📱 Technical Architecture
### Mobile Frontend
| Component       | Specification              |
|-----------------|----------------------------|
| Platform        | Android (min API 26)       |
| Language        | Java 1.8+                  |
| Architecture    | MVVM Pattern               |
| Testing         | Integration Tests          |

### Backend Services
| Component       | Technology                 |
|-----------------|----------------------------|
| Database        | Oracle 11g Express         |
| Framework       | Spring Boot 3.2 (Java 17)  |
| Security        | Spring Security 6.1        |
| Notifications   | Mailjet API Integration    |

## 🛠️ Core Use Cases
### 1. Event Search
```mermaid
graph TD
    A[User enters keywords] --> B[Apply filters]
    B --> C[Display matching events]
    C --> D[Save frequent searches]

### 2. Event Details
Complete event information

Venue details with Google Maps

Pricing and availability

### 3. Ticket Selection
Choose date/time

Select ticket quantity and category

Real-time availability check

### 4. Secure Payment
Multiple payment methods

Instant confirmation

E-ticket delivery

## 🚀 Getting Started
### Prerequisites
Android Studio Flamingo (2022.2.1)

Java JDK 1.8+

Oracle Database 11g Express

### Installation
Clone the repository:

    ```bash
git clone https://github.com/RanaRomdhane/spectapro-mobile-app.git

Import into Android Studio

Configure backend connection in config.properties

Build and run on emulator/device